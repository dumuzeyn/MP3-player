package com.dumuzeyn.mp3player;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class TrackStore {
    private static final String PREFS = "mp3_player_store";
    private static final String TRACKS = "tracks";

    private TrackStore() {}

    public static ArrayList<Track> load(Context context) {
        ArrayList<Track> tracks = new ArrayList<>();
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(TRACKS, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int index = 0; index < array.length(); index++) {
                JSONObject item = array.getJSONObject(index);
                tracks.add(new Track(
                        item.getString("uri"),
                        item.optString("title", "РџРµСЃРЅСЏ"),
                        item.optString("artist", "РќРµРёР·РІРµСЃС‚РЅС‹Р№ РёСЃРїРѕР»РЅРёС‚РµР»СЊ"),
                        item.optString("album", "РќРµРёР·РІРµСЃС‚РЅС‹Р№ Р°Р»СЊР±РѕРј"),
                        item.optString("genre", "РќРµРёР·РІРµСЃС‚РЅС‹Р№ Р¶Р°РЅСЂ")
                ));
            }
        } catch (Exception ignored) {
            tracks.clear();
        }
        sort(tracks);
        return tracks;
    }

    public static void save(Context context, List<Track> tracks) {
        JSONArray array = new JSONArray();
        for (Track track : tracks) {
            JSONObject item = new JSONObject();
            try {
                item.put("uri", track.uri);
                item.put("title", track.title);
                item.put("artist", track.artist);
                item.put("album", track.album);
                item.put("genre", track.genre);
                array.put(item);
            } catch (Exception ignored) {}
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(TRACKS, array.toString()).apply();
    }

    public static Track fromUri(Context context, Uri uri) {
        String title = null;
        String artist = null;
        String album = null;
        String genre = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
        } catch (Exception ignored) {
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {}
        }

        if (title == null || title.trim().isEmpty()) {
            String path = uri.getLastPathSegment();
            title = path == null ? "РџРµСЃРЅСЏ" : path.substring(path.lastIndexOf('/') + 1);
        }
        if (artist == null || artist.trim().isEmpty()) {
            artist = "РќРµРёР·РІРµСЃС‚РЅС‹Р№ РёСЃРїРѕР»РЅРёС‚РµР»СЊ";
        }
        if (album == null || album.trim().isEmpty()) {
            album = "РќРµРёР·РІРµСЃС‚РЅС‹Р№ Р°Р»СЊР±РѕРј";
        }
        if (genre == null || genre.trim().isEmpty()) {
            genre = "РќРµРёР·РІРµСЃС‚РЅС‹Р№ Р¶Р°РЅСЂ";
        }
        return new Track(uri.toString(), title.trim(), artist.trim(), album.trim(), genre.trim());
    }

    public static Track refreshMetadata(Context context, Track oldTrack) {
        Track fresh = fromUri(context, Uri.parse(oldTrack.uri));
        String title = fresh.title == null || fresh.title.trim().isEmpty() ? oldTrack.title : fresh.title;
        String artist = fresh.artist == null || fresh.artist.trim().isEmpty() ? oldTrack.artist : fresh.artist;
        String album = fresh.album == null || fresh.album.trim().isEmpty() ? oldTrack.album : fresh.album;
        String genre = fresh.genre == null || fresh.genre.trim().isEmpty() ? oldTrack.genre : fresh.genre;
        return new Track(oldTrack.uri, title, artist, album, genre);
    }

    public static void sort(List<Track> tracks) {
        // RU: РѕР±С‹С‡РЅС‹Р№ РєРѕРјРїР°СЂР°С‚РѕСЂ Р±РµР· Java 8 default-РјРµС‚РѕРґРѕРІ, С‡С‚РѕР±С‹ APK Р·Р°РїСѓСЃРєР°Р»СЃСЏ Р±РµР· desugar.
        // EN: plain comparator avoids Java 8 default methods, so the APK runs without desugar.
        Collections.sort(tracks, new Comparator<Track>() {
            @Override
            public int compare(Track left, Track right) {
                return left.title.toLowerCase(Locale.ROOT).compareTo(right.title.toLowerCase(Locale.ROOT));
            }
        });
    }
}

