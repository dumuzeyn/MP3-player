package com.dumuzeyn.mp3player;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int PICK_AUDIO = 2001;
    private static final String PREFS = "mp3_player_ui";
    private static final String FAVORITES = "favorites";
    private static final String PLAYLISTS = "playlists";
    private static final String THEME = "theme";
    private static final String CUSTOM_TIMER = "customTimer";

    private final ArrayList<Track> tracks = new ArrayList<>();
    private final HashSet<String> favorites = new HashSet<>();
    private final ArrayList<Playlist> playlists = new ArrayList<>();
    private final ArrayList<Track> playbackQueue = new ArrayList<>();
    private final HashMap<String, Bitmap> coverCache = new HashMap<>();
    private final String[] tabs = {"РџРµСЃРЅРё", "РР·Р±СЂР°РЅРЅРѕРµ", "РџР»РµР№Р»РёСЃС‚С‹", "Р–Р°РЅСЂС‹", "РСЃРїРѕР»РЅРёС‚РµР»Рё", "РђР»СЊР±РѕРјС‹"};

    private FrameLayout root;
    private LinearLayout page;
    private LinearLayout tabRow;
    private LinearLayout list;
    private LinearLayout miniPlayer;
    private TextView miniTitle;
    private TextView miniSub;
    private Button miniButton;
    private FrameLayout overlayHost;
    private HorizontalScrollView tabsScroll;
    private SharedPreferences prefs;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Handler playbackHandler = new Handler(Looper.getMainLooper());
    private final Handler sleepHandler = new Handler(Looper.getMainLooper());

    private int tabIndex = 0;
    private int currentIndex = -1;
    private boolean playing = false;
    private int loopMode = 0;
    private int customTimerMinutes = 10;
    private long sleepTimerEndsAt = 0L;
    private boolean dark = false;
    private String search = "";

    private int bg;
    private int fg;
    private int muted;
    private int line;
    private int panel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        dark = "dark".equals(prefs.getString(THEME, "light"));
        customTimerMinutes = prefs.getInt(CUSTOM_TIMER, 10);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 33);
        }
        loadState();
        buildUi();
        refreshMissingMetadataAsync();
        preloadCoverCacheAsync();
    }

    private void loadState() {
        tracks.clear();
        tracks.addAll(TrackStore.load(this));
        favorites.clear();
        favorites.addAll(prefs.getStringSet(FAVORITES, new HashSet<String>()));
        playlists.clear();
        String raw = prefs.getString(PLAYLISTS, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                Playlist playlist = new Playlist(item.optString("name", "РџР»РµР№Р»РёСЃС‚"));
                JSONArray songs = item.optJSONArray("songs");
                if (songs != null) {
                    for (int s = 0; s < songs.length(); s++) playlist.uris.add(songs.getString(s));
                }
                playlists.add(playlist);
            }
        } catch (Exception ignored) {}
    }

    private void refreshMissingMetadataAsync() {
        new Thread(new Runnable() {
            @Override public void run() {
                final ArrayList<Track> freshTracks = new ArrayList<>(tracks);
                boolean changed = false;
                for (int i = 0; i < freshTracks.size(); i++) {
                    Track track = freshTracks.get(i);
                    if ("РќРµРёР·РІРµСЃС‚РЅС‹Р№ Р°Р»СЊР±РѕРј".equals(track.album) || "РќРµРёР·РІРµСЃС‚РЅС‹Р№ Р¶Р°РЅСЂ".equals(track.genre)) {
                        Track fresh = TrackStore.refreshMetadata(MainActivity.this, track);
                        if (!fresh.album.equals(track.album) || !fresh.genre.equals(track.genre) || !fresh.artist.equals(track.artist)) {
                            freshTracks.set(i, fresh);
                            changed = true;
                        }
                    }
                }
                if (!changed) return;
                TrackStore.save(MainActivity.this, freshTracks);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        tracks.clear();
                        tracks.addAll(freshTracks);
                        render();
                    }
                });
            }
        }).start();
    }

    private void saveState() {
        prefs.edit()
                .putStringSet(FAVORITES, new HashSet<>(favorites))
                .putString(PLAYLISTS, playlistsJson())
                .putString(THEME, dark ? "dark" : "light")
                .putInt(CUSTOM_TIMER, customTimerMinutes)
                .apply();
    }

    private String playlistsJson() {
        JSONArray array = new JSONArray();
        for (Playlist playlist : playlists) {
            try {
                JSONObject item = new JSONObject();
                item.put("name", playlist.name);
                JSONArray songs = new JSONArray();
                for (String uri : playlist.uris) songs.put(uri);
                item.put("songs", songs);
                array.put(item);
            } catch (Exception ignored) {}
        }
        return array.toString();
    }

    private void colors() {
        bg = dark ? Color.BLACK : Color.WHITE;
        fg = dark ? Color.WHITE : Color.BLACK;
        muted = dark ? Color.rgb(190, 190, 190) : Color.rgb(80, 80, 80);
        line = dark ? Color.rgb(90, 90, 90) : Color.rgb(190, 190, 190);
        panel = dark ? Color.rgb(12, 12, 12) : Color.WHITE;
    }

    private void buildUi() {
        colors();
        root = new FrameLayout(this);
        root.setBackgroundColor(bg);

        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(8), dp(14), dp(8), dp(8));
        root.addView(page, new FrameLayout.LayoutParams(-1, -1));

        buildHeader();
        buildTabs();

        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list, new ScrollView.LayoutParams(-1, -2));
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        overlayHost = new FrameLayout(this);
        root.addView(overlayHost, new FrameLayout.LayoutParams(-1, -1));

        buildMiniPlayer();
        setContentView(root);
        render();
    }

    private void buildHeader() {
        LinearLayout header = row();
        ImageView appIcon = new ImageView(this);
        appIcon.setImageResource(getResources().getIdentifier("ic_music_vector", "drawable", getPackageName()));
        appIcon.setColorFilter(fg);
        header.addView(appIcon, square(42));
        TextView title = text("MP3 Player", 20, true);
        header.addView(title, new LinearLayout.LayoutParams(0, dp(60), 1));
        Button theme = icon(dark ? "вј" : "в—ђ");
        theme.setTextSize(28);
        theme.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dark = !dark;
                saveState();
                buildUi();
            }
        });
        header.addView(theme, square(58));
        page.addView(header, new LinearLayout.LayoutParams(-1, dp(66)));
    }

    private void buildTabs() {
        LinearLayout strip = new LinearLayout(this);
        strip.setOrientation(LinearLayout.VERTICAL);
        strip.addView(lineView(), new LinearLayout.LayoutParams(-1, 1));
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        tabsScroll = hsv;
        hsv.setHorizontalScrollBarEnabled(false);
        tabRow = new LinearLayout(this);
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        hsv.addView(tabRow);
        strip.addView(hsv, new LinearLayout.LayoutParams(-1, dp(48)));
        strip.addView(lineView(), new LinearLayout.LayoutParams(-1, 1));
        page.addView(strip, new LinearLayout.LayoutParams(-1, dp(50)));
        final int tabCycles = 21;
        final int middleCycle = tabCycles / 2;
        for (int cycle = 0; cycle < tabCycles; cycle++) {
            for (int i = 0; i < tabs.length; i++) {
                final int index = i;
                Button tab = button(tabs[i]);
                tab.setTag(index);
                styleTab(tab, index);
                tab.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        tabIndex = index;
                        search = "";
                        render();
                    }
                });
                tabRow.addView(tab, new LinearLayout.LayoutParams(dp(132), dp(48)));
            }
        }
        hsv.post(new Runnable() {
            @Override public void run() {
                final int cycleWidth = Math.max(1, tabRow.getWidth() / tabCycles);
                tabsScroll.scrollTo(cycleWidth * middleCycle, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    tabsScroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                        @Override public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                            int min = cycleWidth * (middleCycle - 2);
                            int max = cycleWidth * (middleCycle + 2);
                            if (scrollX < min) tabsScroll.scrollTo(scrollX + cycleWidth, 0);
                            else if (scrollX > max) tabsScroll.scrollTo(scrollX - cycleWidth, 0);
                        }
                    });
                }
            }
        });
    }

    private void styleTab(Button tab, int index) {
        tab.setTextSize(15);
        tab.setGravity(Gravity.CENTER);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(index == tabIndex ? fg : Color.TRANSPARENT);
        drawable.setCornerRadius(dp(10));
        tab.setBackground(drawable);
        tab.setTextColor(index == tabIndex ? bg : muted);
    }

    private void refreshTabs() {
        if (tabRow == null) return;
        for (int i = 0; i < tabRow.getChildCount(); i++) {
            View child = tabRow.getChildAt(i);
            if (child instanceof Button && child.getTag() instanceof Integer) {
                styleTab((Button) child, (Integer) child.getTag());
            }
        }
    }

    private void buildMiniPlayer() {
        miniPlayer = new LinearLayout(this);
        miniPlayer.setOrientation(LinearLayout.HORIZONTAL);
        miniPlayer.setGravity(Gravity.CENTER_VERTICAL);
        miniPlayer.setPadding(dp(14), 0, dp(10), 0);
        setSurface(miniPlayer, bg, true);
        miniPlayer.setVisibility(View.GONE);
        miniPlayer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openFullPlayer(); }
        });

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        miniTitle = text("РџРµСЃРЅСЏ", 16, true);
        miniSub = text("РќРµРёР·РІРµСЃС‚РЅС‹Р№ РёСЃРїРѕР»РЅРёС‚РµР»СЊ", 12, false);
        miniTitle.setSingleLine(true);
        miniTitle.setEllipsize(TextUtils.TruncateAt.END);
        miniSub.setSingleLine(true);
        miniSub.setEllipsize(TextUtils.TruncateAt.END);
        labels.addView(miniTitle);
        labels.addView(miniSub);
        miniPlayer.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));

        miniButton = icon("в–¶");
        miniButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                toggleCurrent();
            }
        });
        miniPlayer.addView(miniButton, square(52));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, dp(74), Gravity.BOTTOM);
        params.setMargins(0, 0, 0, 0);
        root.addView(miniPlayer, params);
    }

    private void render() {
        refreshTabs();
        list.removeAllViews();
        renderSectionHeader();
        if (tabIndex == 0) renderSongs(filter(tracks));
        else if (tabIndex == 1) renderSongs(filter(favoriteTracks()));
        else if (tabIndex == 2) renderPlaylists();
        else renderGroups(tabs[tabIndex]);
        updateMini();
    }

    private void renderSectionHeader() {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        String name = tabs[tabIndex];
        if (tabIndex == 0) name = "РџРµСЃРµРЅ " + tracks.size();
        TextView title = text(name, 22, true);
        title.setSingleLine(true);
        block.addView(title, new LinearLayout.LayoutParams(-1, dp(48)));

        if (tabIndex == 0 || tabIndex == 1) {
            LinearLayout header = row();
            Button playAll = icon("в–¶");
            playAll.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { playList(currentVisibleTracks(), false); }
            });
            header.addView(playAll, square(52));
            Button shuffle = shuffleButton();
            shuffle.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { playList(currentVisibleTracks(), true); }
            });
            header.addView(shuffle, square(52));
            if (tabIndex == 0) {
                Button add = icon("+");
                add.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { openPicker(); }
                });
                header.addView(add, square(52));
            } else {
                Button addFav = icon("+");
                addFav.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { openAddFavorites(); }
                });
                header.addView(addFav, square(52));
            }
            Button searchButton = searchButton();
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { openSearch(); }
            });
            header.addView(searchButton, square(52));
            block.addView(header, new LinearLayout.LayoutParams(-1, dp(62)));
        } else if (tabIndex == 2) {
            LinearLayout header = row();
            Button add = icon("+");
            add.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { createPlaylistDialog(); }
            });
            header.addView(add, square(52));
            Button searchButton = searchButton();
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { openSearch(); }
            });
            header.addView(searchButton, square(52));
            block.addView(header, new LinearLayout.LayoutParams(-1, dp(62)));
        }
        list.addView(block);
    }

    private ArrayList<Track> currentVisibleTracks() {
        if (tabIndex == 1) return filter(favoriteTracks());
        return filter(tracks);
    }

    private ArrayList<Track> favoriteTracks() {
        ArrayList<Track> result = new ArrayList<>();
        for (Track track : tracks) if (favorites.contains(track.uri)) result.add(track);
        return result;
    }

    private ArrayList<Track> filter(ArrayList<Track> source) {
        if (search.trim().isEmpty()) return source;
        ArrayList<Track> result = new ArrayList<>();
        String q = search.toLowerCase(Locale.ROOT);
        for (Track track : source) {
            if (track.title.toLowerCase(Locale.ROOT).contains(q) || track.artist.toLowerCase(Locale.ROOT).contains(q)) result.add(track);
        }
        return result;
    }

    private void renderSongs(ArrayList<Track> source) {
        if (source.isEmpty()) {
            TextView empty = text(tabIndex == 0 ? "Р”РѕР±Р°РІСЊС‚Рµ MP3 РёР»Рё РґСЂСѓРіРѕР№ Р°СѓРґРёРѕС„Р°Р№Р»" : "Р—РґРµСЃСЊ РїРѕРєР° РїСѓСЃС‚Рѕ", 18, true);
            empty.setPadding(dp(12), dp(24), dp(12), dp(24));
            list.addView(empty);
            return;
        }
        for (int i = 0; i < source.size(); i++) {
            Track track = source.get(i);
            list.addView(songRow(track, true, true));
        }
    }

    private View songRow(final Track track, boolean allowMenu, boolean allowPlay) {
        return songRow(track, allowMenu, allowPlay, null);
    }

    private View songRow(final Track track, boolean allowMenu, boolean allowPlay, final Runnable afterPlay) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(8), dp(10), dp(8));
        setSurface(row, isCurrent(track) ? fg : panel, false);

        ImageView cover = coverView();
        Bitmap bitmap = cachedCover(track);
        if (bitmap != null) cover.setImageBitmap(bitmap);
        else cover.setBackgroundColor(isCurrent(track) ? bg : (dark ? Color.rgb(28, 28, 28) : Color.rgb(235, 235, 235)));
        cover.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                currentIndex = tracks.indexOf(track);
                openFullPlayer();
            }
        });
        row.addView(cover, square(58));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(12), 0, dp(8), 0);
        TextView title = text(track.title, 17, true);
        title.setTextColor(isCurrent(track) ? bg : fg);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        main.addView(title);
        View waveform = wave(track, isCurrent(track));
        main.addView(waveform);
        row.addView(main, new LinearLayout.LayoutParams(0, dp(70), 1));

        if (tabIndex == 1) {
            Button heart = icon(favorites.contains(track.uri) ? "в™ҐпёЋ" : "в™ЎпёЋ");
            heart.setTextSize(14);
            applyButtonColors(heart, isCurrent(track) ? fg : bg, isCurrent(track) ? bg : fg);
            heart.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    toggleFavorite(track);
                    render();
                }
            });
            row.addView(heart, square(42));
        } else if (allowMenu) {
            Button menu = icon("в‹Ї");
            applyButtonColors(menu, isCurrent(track) ? fg : bg, isCurrent(track) ? bg : fg);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { openSongActions(track); }
            });
            row.addView(menu, square(48));
        }

        Button play = icon(isCurrent(track) && playing ? "в…Ў" : "в–¶");
        applyButtonColors(play, isCurrent(track) ? fg : bg, isCurrent(track) ? bg : fg);
        play.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isCurrent(track)) toggleCurrent();
                else playTrack(track);
                if (afterPlay != null) afterPlay.run();
            }
        });
        row.addView(play, square(48));
        return spaced(row);
    }

    private void renderPlaylists() {
        ArrayList<Playlist> visible = new ArrayList<>();
        for (Playlist playlist : playlists) {
            if (search.trim().isEmpty() || playlist.name.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) visible.add(playlist);
        }
        if (visible.isEmpty()) {
            TextView empty = text("РџР»РµР№Р»РёСЃС‚РѕРІ РїРѕРєР° РЅРµС‚", 18, true);
            empty.setPadding(dp(12), dp(24), dp(12), dp(24));
            list.addView(empty);
            return;
        }
        for (final Playlist playlist : visible) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            setSurface(card, panel, false);

            LinearLayout top = row();
            LinearLayout names = new LinearLayout(this);
            names.setOrientation(LinearLayout.VERTICAL);
            TextView name = text(playlist.name, 22, true);
            name.setSingleLine(true);
            name.setEllipsize(TextUtils.TruncateAt.END);
            TextView count = text(playlist.uris.size() + " РїРµСЃРµРЅ", 13, false);
            names.addView(name);
            names.addView(count);
            top.addView(names, new LinearLayout.LayoutParams(0, -2, 1));
            Button play = icon("в–¶");
            play.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { playList(playlistTracks(playlist), false); }
            });
            top.addView(play, square(48));
            Button shuffle = shuffleButton();
            shuffle.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { playList(playlistTracks(playlist), true); }
            });
            top.addView(shuffle, square(48));
            Button delete = icon("Г—");
            delete.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { confirmDeletePlaylist(playlist); }
            });
            top.addView(delete, square(48));
            card.addView(top);

            LinearLayout preview = row();
            ImageView cover = coverView();
            ArrayList<Track> pTracks = playlistTracks(playlist);
            Bitmap playlistCover = pTracks.isEmpty() ? null : cachedCover(pTracks.get(0));
            if (playlistCover != null) cover.setImageBitmap(playlistCover);
            else cover.setBackgroundColor(dark ? Color.rgb(28, 28, 28) : Color.rgb(235, 235, 235));
            preview.addView(cover, square(86));
            TextView songs = text(previewText(pTracks), 16, true);
            songs.setPadding(dp(12), 0, 0, 0);
            preview.addView(songs, new LinearLayout.LayoutParams(0, dp(96), 1));
            card.addView(preview);
            card.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { openPlaylist(playlist); }
            });
            list.addView(spaced(card));
        }
    }

    private String previewText(ArrayList<Track> source) {
        if (source.isEmpty()) return "Р’ РїР»РµР№Р»РёСЃС‚Рµ РїРѕРєР° РЅРµС‚ РїРµСЃРµРЅ.";
        StringBuilder builder = new StringBuilder();
        int count = Math.min(3, source.size());
        for (int i = 0; i < count; i++) {
            if (i > 0) builder.append("\n");
            builder.append(source.get(i).title);
        }
        return builder.toString();
    }

    private ArrayList<Track> playlistTracks(Playlist playlist) {
        ArrayList<Track> result = new ArrayList<>();
        for (String uri : playlist.uris) {
            Track track = findTrack(uri);
            if (track != null) result.add(track);
        }
        return result;
    }

    private void renderGroups(String kind) {
        Map<String, ArrayList<Track>> groups = groupedTracks();
        for (final Map.Entry<String, ArrayList<Track>> entry : groups.entrySet()) {
            LinearLayout card = row();
            card.setPadding(dp(12), dp(12), dp(12), dp(12));
            setSurface(card, panel, false);

            ImageView cover = coverView();
            Bitmap bitmap = entry.getValue().isEmpty() ? null : cachedCover(entry.getValue().get(0));
            if (bitmap != null) cover.setImageBitmap(bitmap);
            else cover.setBackgroundColor(dark ? Color.rgb(28, 28, 28) : Color.rgb(235, 235, 235));
            card.addView(cover, square(72));

            LinearLayout names = new LinearLayout(this);
            names.setOrientation(LinearLayout.VERTICAL);
            names.setPadding(dp(12), 0, dp(8), 0);
            TextView title = text(entry.getKey(), 20, true);
            title.setSingleLine(true);
            title.setEllipsize(TextUtils.TruncateAt.END);
            TextView count = text(entry.getValue().size() + " РїРµСЃРµРЅ", 13, false);
            names.addView(title);
            names.addView(count);
            card.addView(names, new LinearLayout.LayoutParams(0, dp(72), 1));

            Button play = icon("в–¶");
            play.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { playList(entry.getValue(), false); }
            });
            card.addView(play, square(52));
            Button shuffle = shuffleButton();
            shuffle.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { playList(entry.getValue(), true); }
            });
            card.addView(shuffle, square(52));
            card.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { openGroupSongs(entry.getKey(), entry.getValue()); }
            });
            list.addView(spaced(card));
        }
    }

    private Map<String, ArrayList<Track>> groupedTracks() {
        LinkedHashMap<String, ArrayList<Track>> groups = new LinkedHashMap<>();
        for (Track track : tracks) {
            String key;
            if (tabIndex == 4) key = cleanGroup(track.artist, "РќРµРёР·РІРµСЃС‚РЅС‹Р№ РёСЃРїРѕР»РЅРёС‚РµР»СЊ");
            else if (tabIndex == 5) key = cleanGroup(track.album, "РќРµРёР·РІРµСЃС‚РЅС‹Р№ Р°Р»СЊР±РѕРј");
            else key = cleanGroup(track.genre, "РќРµРёР·РІРµСЃС‚РЅС‹Р№ Р¶Р°РЅСЂ");
            if (!groups.containsKey(key)) groups.put(key, new ArrayList<Track>());
            groups.get(key).add(track);
        }
        return groups;
    }

    private String cleanGroup(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private void openGroupSongs(String title, ArrayList<Track> source) {
        showPanel(title, source, null);
    }

    private void openPlaylist(final Playlist playlist) {
        showPanel(playlist.name, playlistTracks(playlist), new PanelAction() {
            @Override public void add() { openAddToPlaylist(playlist); }
        });
    }

    private void showPanel(String title, ArrayList<Track> source, final PanelAction action) {
        final FrameLayout shade = shade();
        LinearLayout card = panelCard();
        LinearLayout header = row();
        header.addView(text(title, 20, true), new LinearLayout.LayoutParams(0, dp(58), 1));
        Button playAll = icon("в–¶");
        playAll.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                playList(source, false);
                if (shade.getParent() != null) overlayHost.removeView(shade);
                showPanel(title, source, action);
            }
        });
        header.addView(playAll, square(52));
        Button shuffle = shuffleButton();
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                playList(source, true);
                if (shade.getParent() != null) overlayHost.removeView(shade);
                showPanel(title, source, action);
            }
        });
        header.addView(shuffle, square(52));
        if (action != null) {
            Button add = icon("+");
            add.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { action.add(); }
            });
            header.addView(add, square(52));
        }
        Button close = icon("Г—");
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { overlayHost.removeView(shade); updateMini(); }
        });
        header.addView(close, square(52));
        card.addView(header);

        ScrollView scroll = new ScrollView(this);
        LinearLayout panelList = new LinearLayout(this);
        panelList.setOrientation(LinearLayout.VERTICAL);
        for (Track track : source) {
            panelList.addView(songRow(track, false, true, new Runnable() {
                @Override public void run() {
                    if (shade.getParent() != null) overlayHost.removeView(shade);
                    showPanel(title, source, action);
                }
            }));
        }
        scroll.addView(panelList);
        card.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        shade.addView(card, bottomParams());
        overlayHost.addView(shade);
        updateMini();
    }

    private void openQueuePanel() {
        if (playbackQueue.isEmpty() && currentIndex >= 0 && currentIndex < tracks.size()) {
            playbackQueue.add(tracks.get(currentIndex));
        }
        final FrameLayout shade = shade();
        LinearLayout card = panelCard();
        LinearLayout header = row();
        header.addView(text("РЎРїРёСЃРѕРє РїСЂРѕРёРіСЂС‹РІР°РЅРёСЏ", 20, true), new LinearLayout.LayoutParams(0, dp(58), 1));
        Button add = icon("+");
        add.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openAddToQueue(); }
        });
        header.addView(add, square(52));
        Button close = icon("Г—");
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { overlayHost.removeView(shade); updateMini(); }
        });
        header.addView(close, square(52));
        card.addView(header);

        ScrollView scroll = new ScrollView(this);
        LinearLayout panelList = new LinearLayout(this);
        panelList.setOrientation(LinearLayout.VERTICAL);
        for (final Track track : new ArrayList<>(activeQueue())) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(8), dp(10), dp(8));
            setSurface(row, isCurrent(track) ? fg : panel, false);
            ImageView cover = coverView();
            Bitmap bitmap = cachedCover(track);
            if (bitmap != null) cover.setImageBitmap(bitmap);
            row.addView(cover, square(58));
            TextView title = text(track.title, 17, true);
            title.setPadding(dp(12), 0, dp(8), 0);
            title.setTextColor(isCurrent(track) ? bg : fg);
            row.addView(title, new LinearLayout.LayoutParams(0, dp(70), 1));
            Button remove = icon("в€’");
            remove.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    removeFromQueue(track);
                    overlayHost.removeView(shade);
                    openQueuePanel();
                }
            });
            row.addView(remove, square(48));
            Button play = icon(isCurrent(track) && playing ? "в…Ў" : "в–¶");
            play.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    playQueueTrack(track);
                    overlayHost.removeView(shade);
                    openQueuePanel();
                }
            });
            row.addView(play, square(48));
            panelList.addView(spaced(row));
        }
        scroll.addView(panelList);
        card.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        shade.addView(card, bottomParams());
        overlayHost.addView(shade);
        updateMini();
    }

    private void openAddToQueue() {
        final HashSet<String> selected = new HashSet<>();
        showPickPanel("Р”РѕР±Р°РІРёС‚СЊ РІ СЃРїРёСЃРѕРє", selected, new PickDone() {
            @Override public void done(Set<String> uris) {
                if (playbackQueue.isEmpty() && currentIndex >= 0 && currentIndex < tracks.size()) {
                    playbackQueue.add(tracks.get(currentIndex));
                }
                for (String uri : uris) {
                    Track track = findTrack(uri);
                    if (track != null && !isInPlaybackQueue(track)) playbackQueue.add(track);
                }
                overlayHost.removeAllViews();
                openQueuePanel();
            }
        });
    }

    private void removeFromQueue(Track track) {
        for (int i = playbackQueue.size() - 1; i >= 0; i--) {
            if (playbackQueue.get(i).uri.equals(track.uri)) playbackQueue.remove(i);
        }
        if (isCurrent(track)) {
            Intent intent = new Intent(MainActivity.this, PlayerService.class);
            intent.setAction(PlayerService.ACTION_STOP);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
            else startService(intent);
            currentIndex = -1;
            playing = false;
        }
    }

    private void playQueueTrack(Track track) {
        int queueIndex = queueIndexOf(track);
        if (queueIndex < 0) return;
        currentIndex = tracks.indexOf(track);
        playing = true;
        startServiceAction(PlayerService.ACTION_PLAY_INDEX, queueIndex, false);
        startPlaybackWatcher();
        render();
    }

    private boolean isInPlaybackQueue(Track track) {
        for (Track item : playbackQueue) {
            if (item.uri.equals(track.uri)) return true;
        }
        return false;
    }

    private void openAddFavorites() {
        final HashSet<String> selected = new HashSet<>();
        showPickPanel("Р”РѕР±Р°РІРёС‚СЊ РІ РёР·Р±СЂР°РЅРЅРѕРµ", selected, new PickDone() {
            @Override public void done(Set<String> uris) {
                favorites.addAll(uris);
                saveState();
                render();
            }
        });
    }

    private void openAddToPlaylist(final Playlist playlist) {
        final HashSet<String> selected = new HashSet<>();
        showPickPanel("Р”РѕР±Р°РІРёС‚СЊ РІ " + playlist.name, selected, new PickDone() {
            @Override public void done(Set<String> uris) {
                for (String uri : uris) if (!playlist.uris.contains(uri)) playlist.uris.add(uri);
                saveState();
                render();
                overlayHost.removeAllViews();
                openPlaylist(playlist);
            }
        });
    }

    private void showPickPanel(String title, final HashSet<String> selected, final PickDone done) {
        final FrameLayout shade = shade();
        LinearLayout card = panelCard();
        LinearLayout header = row();
        header.addView(text(title, 20, true), new LinearLayout.LayoutParams(0, dp(58), 1));
        Button ok = icon("+");
        ok.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                overlayHost.removeView(shade);
                done.done(selected);
                updateMini();
            }
        });
        header.addView(ok, square(52));
        Button close = icon("Г—");
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { overlayHost.removeView(shade); updateMini(); }
        });
        header.addView(close, square(52));
        card.addView(header);

        ScrollView scroll = new ScrollView(this);
        final LinearLayout panelList = new LinearLayout(this);
        panelList.setOrientation(LinearLayout.VERTICAL);
        for (final Track track : tracks) {
            panelList.addView(pickSongRow(track, selected));
        }
        scroll.addView(panelList);
        card.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        shade.addView(card, bottomParams());
        overlayHost.addView(shade);
        updateMini();
    }

    private View pickSongRow(final Track track, final HashSet<String> selected) {
        final LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(8), dp(10), dp(8));
        setSurface(row, selected.contains(track.uri) ? fg : panel, false);

        ImageView cover = coverView();
        Bitmap bitmap = cachedCover(track);
        if (bitmap != null) cover.setImageBitmap(bitmap);
        else cover.setBackgroundColor(selected.contains(track.uri) ? bg : (dark ? Color.rgb(28, 28, 28) : Color.rgb(235, 235, 235)));
        row.addView(cover, square(58));

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setPadding(dp(12), 0, dp(8), 0);
        TextView title = text(track.title, 17, true);
        title.setTextColor(selected.contains(track.uri) ? bg : fg);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        main.addView(title);
        main.addView(wave(track, selected.contains(track.uri)));
        row.addView(main, new LinearLayout.LayoutParams(0, dp(70), 1));

        final Button mark = icon(selected.contains(track.uri) ? "вњ”" : "+");
        mark.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (selected.contains(track.uri)) selected.remove(track.uri);
                else selected.add(track.uri);
                boolean picked = selected.contains(track.uri);
                setSurface(row, picked ? fg : panel, false);
                title.setTextColor(picked ? bg : fg);
                mark.setText(picked ? "вњ”" : "+");
                applyButtonColors(mark, picked ? fg : bg, picked ? bg : fg);
            }
        });
        applyButtonColors(mark, selected.contains(track.uri) ? fg : bg, selected.contains(track.uri) ? bg : fg);
        row.addView(mark, square(48));

        Button play = icon(isCurrent(track) && playing ? "в…Ў" : "в–¶");
        play.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                playTrack(track, false);
                ((Button) v).setText("в…Ў");
            }
        });
        row.addView(play, square(48));
        return spaced(row);
    }

    private void openSongActions(final Track track) {
        final FrameLayout shade = shade();
        LinearLayout card = panelCard();
        card.addView(text(track.title, 20, true), new LinearLayout.LayoutParams(-1, dp(52)));
        Button fav = button(favorites.contains(track.uri) ? "в™Ґ РЈР±СЂР°С‚СЊ РёР· РёР·Р±СЂР°РЅРЅРѕРіРѕ" : "в™Ў Р”РѕР±Р°РІРёС‚СЊ РІ РёР·Р±СЂР°РЅРЅРѕРµ");
        fav.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                toggleFavorite(track);
                overlayHost.removeView(shade);
                render();
            }
        });
        card.addView(fav, new LinearLayout.LayoutParams(-1, dp(54)));
        Button playlist = button("+ Р”РѕР±Р°РІРёС‚СЊ РІ РїР»РµР№Р»РёСЃС‚");
        playlist.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                overlayHost.removeView(shade);
                choosePlaylistForTrack(track);
            }
        });
        card.addView(playlist, new LinearLayout.LayoutParams(-1, dp(54)));
        Button delete = button("Г— РЈРґР°Р»РёС‚СЊ РёР· РїСЂРёР»РѕР¶РµРЅРёСЏ");
        delete.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                overlayHost.removeView(shade);
                confirmDeleteTrack(track);
            }
        });
        card.addView(delete, new LinearLayout.LayoutParams(-1, dp(54)));
        Button close = button("Р—Р°РєСЂС‹С‚СЊ");
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { overlayHost.removeView(shade); updateMini(); }
        });
        card.addView(close, new LinearLayout.LayoutParams(-1, dp(54)));
        shade.addView(card, bottomParams());
        overlayHost.addView(shade);
        updateMini();
    }

    private void choosePlaylistForTrack(final Track track) {
        final String[] names = new String[playlists.size() + 1];
        for (int i = 0; i < playlists.size(); i++) names[i] = playlists.get(i).name;
        names[names.length - 1] = "РЎРѕР·РґР°С‚СЊ РЅРѕРІС‹Р№";
        new AlertDialog.Builder(this)
                .setTitle("Р”РѕР±Р°РІРёС‚СЊ РІ РїР»РµР№Р»РёСЃС‚")
                .setItems(names, (dialog, which) -> {
                    if (which == playlists.size()) createPlaylistAndAdd(track);
                    else {
                        Playlist playlist = playlists.get(which);
                        if (!playlist.uris.contains(track.uri)) playlist.uris.add(track.uri);
                        saveState();
                    }
                })
                .show();
    }

    private void createPlaylistAndAdd(final Track track) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("РќР°Р·РІР°РЅРёРµ РїР»РµР№Р»РёСЃС‚Р°");
        new AlertDialog.Builder(this)
                .setTitle("РќРѕРІС‹Р№ РїР»РµР№Р»РёСЃС‚")
                .setView(input)
                .setNegativeButton("РќРµС‚", null)
                .setPositiveButton("Р”Р°", (dialog, which) -> {
                    Playlist playlist = new Playlist(input.getText().toString().trim().isEmpty() ? "РџР»РµР№Р»РёСЃС‚" : input.getText().toString().trim());
                    playlist.uris.add(track.uri);
                    playlists.add(playlist);
                    saveState();
                    render();
                })
                .show();
    }

    private void confirmDeleteTrack(final Track track) {
        new AlertDialog.Builder(this)
                .setTitle("РЈРґР°Р»РёС‚СЊ РїРµСЃРЅСЋ?")
                .setMessage("РџРµСЃРЅСЏ РїСЂРѕРїР°РґРµС‚ РёР· РїСЂРёР»РѕР¶РµРЅРёСЏ, РЅРѕ С„Р°Р№Р» РѕСЃС‚Р°РЅРµС‚СЃСЏ РЅР° С‚РµР»РµС„РѕРЅРµ.")
                .setNegativeButton("РќРµС‚", null)
                .setPositiveButton("Р”Р°", (dialog, which) -> {
                    tracks.remove(track);
                    favorites.remove(track.uri);
                    for (Playlist playlist : playlists) playlist.uris.remove(track.uri);
                    TrackStore.save(this, tracks);
                    saveState();
                    render();
                })
                .show();
    }

    private void confirmDeletePlaylist(final Playlist playlist) {
        new AlertDialog.Builder(this)
                .setTitle("РЈРґР°Р»РёС‚СЊ РїР»РµР№Р»РёСЃС‚?")
                .setMessage("РџРµСЃРЅРё РѕСЃС‚Р°РЅСѓС‚СЃСЏ РІ РїСЂРёР»РѕР¶РµРЅРёРё.")
                .setNegativeButton("РќРµС‚", null)
                .setPositiveButton("Р”Р°", (dialog, which) -> {
                    playlists.remove(playlist);
                    saveState();
                    render();
                })
                .show();
    }

    private void createPlaylistDialog() {
        showInputPanel("РЎРѕР·РґР°С‚СЊ РїР»РµР№Р»РёСЃС‚", "РќР°Р·РІР°РЅРёРµ РїР»РµР№Р»РёСЃС‚Р°", "", false, new InputDone() {
            @Override public void done(String value) {
                String name = value.trim();
                playlists.add(new Playlist(name.isEmpty() ? "РџР»РµР№Р»РёСЃС‚" : name));
                saveState();
                render();
            }
        });
    }

    private void openSearch() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(search);
        input.setHint("РџРѕРёСЃРє");
        new AlertDialog.Builder(this)
                .setTitle("РџРѕРёСЃРє")
                .setView(input)
                .setNegativeButton("РЎР±СЂРѕСЃ", (dialog, which) -> {
                    search = "";
                    render();
                })
                .setPositiveButton("РќР°Р№С‚Рё", (dialog, which) -> {
                    search = input.getText().toString();
                    render();
                })
                .show();
    }

    private void showInputPanel(String title, String hint, String initialValue, boolean number, final InputDone done) {
        final FrameLayout shade = shade();
        LinearLayout card = panelCard();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout header = row();
        TextView titleView = text(title, 22, true);
        header.addView(titleView, new LinearLayout.LayoutParams(0, dp(58), 1));
        Button close = icon("Г—");
        close.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                overlayHost.removeView(shade);
                updateMini();
            }
        });
        header.addView(close, square(52));
        card.addView(header);

        final EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(initialValue);
        input.setHint(hint);
        input.setTextColor(fg);
        input.setHintTextColor(muted);
        input.setTextSize(18);
        input.setPadding(dp(14), 0, dp(14), 0);
        input.setInputType(number ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
        setSurface(input, panel, true);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(-1, dp(58));
        inputParams.setMargins(0, dp(10), 0, dp(16));
        card.addView(input, inputParams);

        LinearLayout actions = row();
        Button cancel = button("РћС‚РјРµРЅР°");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                overlayHost.removeView(shade);
                updateMini();
            }
        });
        actions.addView(cancel, new LinearLayout.LayoutParams(0, dp(54), 1));
        Button ok = button("Р“РѕС‚РѕРІРѕ");
        applyButtonColors(ok, fg, bg);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String value = input.getText().toString();
                overlayHost.removeView(shade);
                done.done(value);
                updateMini();
            }
        });
        actions.addView(ok, new LinearLayout.LayoutParams(0, dp(54), 1));
        card.addView(actions);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, dp(230), Gravity.BOTTOM);
        shade.addView(card, params);
        overlayHost.addView(shade);
        input.requestFocus();
    }

    private void openFullPlayer() {
        if (currentIndex < 0 && !tracks.isEmpty()) currentIndex = 0;
        if (currentIndex < 0) return;
        miniPlayer.setVisibility(View.GONE);
        final Track track = tracks.get(currentIndex);
        final FrameLayout sheet = new FrameLayout(this);
        sheet.setBackgroundColor(bg);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(18), dp(16), dp(20));
        sheet.addView(body, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout header = row();
        Button back = icon("в†ђ");
        back.setTextSize(34);
        back.setTypeface(Typeface.DEFAULT_BOLD);
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                overlayHost.removeView(sheet);
                updateMini();
            }
        });
        header.addView(back, square(58));
        header.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
        Button queue = icon("в°");
        queue.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openQueuePanel(); }
        });
        header.addView(queue, square(58));
        body.addView(header, new LinearLayout.LayoutParams(-1, dp(72)));

        ImageView cover = coverView();
        Bitmap bitmap = cover(track);
        if (bitmap != null) cover.setImageBitmap(bitmap);
        else cover.setBackgroundColor(dark ? Color.rgb(28, 28, 28) : Color.rgb(235, 235, 235));
        LinearLayout.LayoutParams coverParams = new LinearLayout.LayoutParams(dp(280), dp(280));
        coverParams.gravity = Gravity.CENTER_HORIZONTAL;
        body.addView(cover, coverParams);

        TextView title = text(track.title, 24, true);
        title.setGravity(Gravity.CENTER);
        body.addView(title, new LinearLayout.LayoutParams(-1, dp(54)));
        int queueIndex = queueIndexOf(track);
        int queueSize = playbackQueue.isEmpty() ? tracks.size() : playbackQueue.size();
        TextView sub = text(track.artist + " вЂў " + (queueIndex + 1) + " РёР· " + queueSize, 15, false);
        sub.setGravity(Gravity.CENTER);
        body.addView(sub, new LinearLayout.LayoutParams(-1, dp(34)));

        LinearLayout secondary = row();
        final Button timer = button(timerButtonText());
        timer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { timerDialog(); }
        });
        secondary.addView(timer, new LinearLayout.LayoutParams(0, dp(58), 1));
        Button like = button(favorites.contains(track.uri) ? "в™ҐпёЋ Р›Р°Р№Рє" : "в™ЎпёЋ Р›Р°Р№Рє");
        like.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                toggleFavorite(track);
                overlayHost.removeView(sheet);
                openFullPlayer();
            }
        });
        secondary.addView(like, new LinearLayout.LayoutParams(0, dp(58), 1));
        Button loop = button(loopLabel());
        loop.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                loopMode = (loopMode + 1) % 3;
                Intent intent = new Intent(MainActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_LOOP);
                intent.putExtra(PlayerService.EXTRA_LOOP_MODE, loopMode);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
                else startService(intent);
                overlayHost.removeView(sheet);
                openFullPlayer();
            }
        });
        secondary.addView(loop, new LinearLayout.LayoutParams(0, dp(58), 1));
        body.addView(secondary);

        final SeekBar seek = new SeekBar(this);
        seek.setMax(Math.max(1, PlayerService.lastDuration));
        seek.setProgress(Math.max(0, PlayerService.lastPosition));
        body.addView(seek, new LinearLayout.LayoutParams(-1, dp(42)));
        LinearLayout time = row();
        final TextView elapsed = text(formatMs(PlayerService.lastPosition), 13, false);
        final TextView remain = text("-" + formatMs(Math.max(0, PlayerService.lastDuration - PlayerService.lastPosition)), 13, false);
        remain.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        time.addView(elapsed, new LinearLayout.LayoutParams(0, dp(28), 1));
        time.addView(remain, new LinearLayout.LayoutParams(0, dp(28), 1));
        body.addView(time);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (!fromUser) return;
                elapsed.setText(formatMs(progress));
                remain.setText("-" + formatMs(Math.max(0, PlayerService.lastDuration - progress)));
            }

            @Override public void onStartTrackingTouch(SeekBar bar) {}

            @Override public void onStopTrackingTouch(SeekBar bar) {
                Intent intent = new Intent(MainActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_SEEK);
                intent.putExtra(PlayerService.EXTRA_POSITION, bar.getProgress());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
                else startService(intent);
            }
        });

        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable tick = new Runnable() {
            @Override public void run() {
                if (sheet.getParent() == null) return;
                PlayerService.refreshSnapshot();
                if (PlayerService.lastIndex < 0) {
                    currentIndex = -1;
                    playing = false;
                    overlayHost.removeView(sheet);
                    updateMini();
                    render();
                    return;
                }
                if (PlayerService.lastUri != null && !PlayerService.lastUri.isEmpty() && !PlayerService.lastUri.equals(track.uri)) {
                    Track serviceTrack = findTrack(PlayerService.lastUri);
                    if (serviceTrack != null) {
                        currentIndex = tracks.indexOf(serviceTrack);
                        overlayHost.removeView(sheet);
                        openFullPlayer();
                        render();
                        return;
                    }
                }
                seek.setMax(Math.max(1, PlayerService.lastDuration));
                seek.setProgress(Math.max(0, PlayerService.lastPosition));
                elapsed.setText(formatMs(PlayerService.lastPosition));
                remain.setText("-" + formatMs(Math.max(0, PlayerService.lastDuration - PlayerService.lastPosition)));
                timer.setText(timerButtonText());
                handler.postDelayed(this, 250);
            }
        };
        handler.postDelayed(tick, 700);

        body.addView(new View(this), new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout controls = row();
        controls.setGravity(Gravity.CENTER);
        Button prev = icon("вЏ®");
        prev.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { previous(); overlayHost.removeView(sheet); openFullPlayer(); }
        });
        controls.addView(prev, square(68));
        Button play = icon(playing ? "в…Ў" : "в–¶");
        play.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { toggleCurrent(); overlayHost.removeView(sheet); openFullPlayer(); }
        });
        controls.addView(play, square(84));
        Button next = icon("вЏ­");
        next.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { next(); overlayHost.removeView(sheet); openFullPlayer(); }
        });
        controls.addView(next, square(68));
        body.addView(controls, new LinearLayout.LayoutParams(-1, dp(112)));
        overlayHost.addView(sheet, new FrameLayout.LayoutParams(-1, -1));
    }

    private String loopLabel() {
        if (loopMode == 1) return "РџРѕРІС‚РѕСЂ: РїРµСЃРЅСЏ";
        if (loopMode == 2) return "РџРѕРІС‚РѕСЂ: СЃРїРёСЃРѕРє";
        return "РџРѕРІС‚РѕСЂ: РІС‹РєР»";
    }

    private String formatMs(int value) {
        int seconds = Math.max(0, value / 1000);
        return (seconds / 60) + ":" + String.format(Locale.ROOT, "%02d", seconds % 60);
    }

    private String formatSeconds(long seconds) {
        seconds = Math.max(0L, seconds);
        return (seconds / 60) + ":" + String.format(Locale.ROOT, "%02d", seconds % 60);
    }

    private void timerDialog() {
        String[] items = sleepTimerEndsAt > 0L
                ? new String[]{"5 РјРёРЅСѓС‚", "15 РјРёРЅСѓС‚", "30 РјРёРЅСѓС‚", customTimerMinutes + " РјРёРЅСѓС‚", "РЎРІРѕРµ", "Р’С‹РєР»СЋС‡РёС‚СЊ С‚Р°Р№РјРµСЂ"}
                : new String[]{"5 РјРёРЅСѓС‚", "15 РјРёРЅСѓС‚", "30 РјРёРЅСѓС‚", customTimerMinutes + " РјРёРЅСѓС‚", "РЎРІРѕРµ"};
        new AlertDialog.Builder(this).setTitle("РўР°Р№РјРµСЂ СЃРЅР°").setItems(items, (dialog, which) -> {
            if (which == 0) startSleepTimer(5);
            else if (which == 1) startSleepTimer(15);
            else if (which == 2) startSleepTimer(30);
            else if (which == 3) startSleepTimer(customTimerMinutes);
            else if (which == 4) customTimerDialog();
            else cancelSleepTimer();
        }).show();
    }

    private void customTimerDialog() {
        showInputPanel("РЎРІРѕРµ РІСЂРµРјСЏ", "РњРёРЅСѓС‚С‹", String.valueOf(customTimerMinutes), true, new InputDone() {
            @Override public void done(String value) {
                try {
                    customTimerMinutes = Math.max(1, Integer.parseInt(value.trim()));
                    saveState();
                    startSleepTimer(customTimerMinutes);
                } catch (Exception ignored) {}
            }
        });
    }

    private void startSleepTimer(int minutes) {
        sleepTimerEndsAt = System.currentTimeMillis() + minutes * 60L * 1000L;
        sleepHandler.removeCallbacksAndMessages(null);
        sleepHandler.postDelayed(new Runnable() {
            @Override public void run() {
                Intent intent = new Intent(MainActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_STOP);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
                else startService(intent);
                playing = false;
                currentIndex = -1;
                sleepTimerEndsAt = 0L;
                updateMini();
                render();
            }
        }, minutes * 60L * 1000L);
    }

    private void cancelSleepTimer() {
        sleepTimerEndsAt = 0L;
        sleepHandler.removeCallbacksAndMessages(null);
    }

    private String timerButtonText() {
        if (sleepTimerEndsAt <= 0L) return "РўР°Р№РјРµСЂ";
        long left = Math.max(0L, sleepTimerEndsAt - System.currentTimeMillis());
        return "РўР°Р№РјРµСЂ\n" + formatSeconds((left + 999L) / 1000L);
    }

    private void playTrack(Track track) {
        playTrack(track, true);
    }

    private void playTrack(Track track, boolean refreshScreen) {
        int index = tracks.indexOf(track);
        if (index < 0) return;
        playbackQueue.clear();
        playbackQueue.add(track);
        currentIndex = index;
        playing = true;
        startServiceAction(PlayerService.ACTION_PLAY_INDEX, 0, true);
        startPlaybackWatcher();
        updateMini();
        if (refreshScreen) render();
    }

    private void playList(ArrayList<Track> source, boolean shuffle) {
        if (source.isEmpty()) return;
        ArrayList<Track> queue = new ArrayList<>(source);
        if (shuffle) Collections.shuffle(queue, new Random());
        Track track = queue.get(0);
        int index = tracks.indexOf(track);
        if (index < 0) return;
        playbackQueue.clear();
        playbackQueue.addAll(queue);
        currentIndex = index;
        playing = true;
        startServiceAction(PlayerService.ACTION_PLAY_INDEX, 0, false);
        startPlaybackWatcher();
        render();
    }

    private void toggleCurrent() {
        if (currentIndex < 0 && !tracks.isEmpty()) {
            playList(tracks, false);
            return;
        }
        if (currentIndex < 0) return;
        playing = !playing;
        startServiceAction(PlayerService.ACTION_TOGGLE, currentIndex, false);
        startPlaybackWatcher();
        updateMini();
        render();
    }

    private void next() {
        ArrayList<Track> queue = activeQueue();
        if (queue.isEmpty()) return;
        int nextIndex = currentIndex < 0 ? 0 : (queueIndexOf(tracks.get(currentIndex)) + 1) % queue.size();
        Track nextTrack = queue.get(nextIndex);
        currentIndex = tracks.indexOf(nextTrack);
        playing = true;
        startServiceAction(PlayerService.ACTION_PLAY_INDEX, nextIndex, false);
        startPlaybackWatcher();
        render();
    }

    private void previous() {
        ArrayList<Track> queue = activeQueue();
        if (queue.isEmpty()) return;
        int oldIndex = currentIndex < 0 ? 0 : queueIndexOf(tracks.get(currentIndex));
        int previousIndex = oldIndex <= 0 ? queue.size() - 1 : oldIndex - 1;
        Track previousTrack = queue.get(previousIndex);
        currentIndex = tracks.indexOf(previousTrack);
        playing = true;
        startServiceAction(PlayerService.ACTION_PLAY_INDEX, previousIndex, false);
        startPlaybackWatcher();
        render();
    }

    private void startPlaybackWatcher() {
        playbackHandler.removeCallbacksAndMessages(null);
        playbackHandler.postDelayed(new Runnable() {
            @Override public void run() {
                PlayerService.refreshSnapshot();
                if (PlayerService.lastIndex < 0) {
                    currentIndex = -1;
                    playing = false;
                    updateMini();
                    render();
                    return;
                }
                if (PlayerService.lastUri != null && !PlayerService.lastUri.isEmpty()) {
                    Track serviceTrack = findTrack(PlayerService.lastUri);
                    if (serviceTrack != null && !isCurrent(serviceTrack)) {
                        currentIndex = tracks.indexOf(serviceTrack);
                        render();
                        return;
                    }
                }
                if (playing || currentIndex >= 0) playbackHandler.postDelayed(this, 900);
            }
        }, 900);
    }

    private void startServiceAction(String action, int index) {
        startServiceAction(action, index, false);
    }

    private void startServiceAction(String action, int index, boolean oneShot) {
        Intent intent = new Intent(this, PlayerService.class);
        intent.setAction(action);
        intent.putExtra(PlayerService.EXTRA_INDEX, index);
        intent.putExtra(PlayerService.EXTRA_ONE_SHOT, oneShot);
        intent.putStringArrayListExtra(PlayerService.EXTRA_QUEUE_URIS, queueUris());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
        else startService(intent);
    }

    private ArrayList<String> queueUris() {
        ArrayList<String> uris = new ArrayList<>();
        ArrayList<Track> queue = activeQueue();
        for (Track track : queue) uris.add(track.uri);
        return uris;
    }

    private ArrayList<Track> activeQueue() {
        return playbackQueue.isEmpty() ? tracks : playbackQueue;
    }

    private int queueIndexOf(Track track) {
        ArrayList<Track> queue = activeQueue();
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).uri.equals(track.uri)) return i;
        }
        return Math.max(0, tracks.indexOf(track));
    }

    private void updateMini() {
        if (miniPlayer == null) return;
        if (currentIndex < 0 || currentIndex >= tracks.size() || overlayHost.getChildCount() > 0) {
            miniPlayer.setVisibility(View.GONE);
            return;
        }
        Track track = tracks.get(currentIndex);
        miniTitle.setText(track.title);
        miniSub.setText(track.artist);
        miniButton.setText(playing ? "в…Ў" : "в–¶");
        miniPlayer.setVisibility(View.VISIBLE);
    }

    private void toggleFavorite(Track track) {
        if (favorites.contains(track.uri)) favorites.remove(track.uri);
        else favorites.add(track.uri);
        saveState();
    }

    private boolean isCurrent(Track track) {
        return currentIndex >= 0 && currentIndex < tracks.size() && tracks.get(currentIndex).uri.equals(track.uri);
    }

    private Track findTrack(String uri) {
        for (Track track : tracks) if (track.uri.equals(uri)) return track;
        return null;
    }

    private Bitmap cachedCover(Track track) {
        return coverCache.get(track.uri);
    }

    private void preloadCoverCacheAsync() {
        new Thread(new Runnable() {
            @Override public void run() {
                int loaded = 0;
                for (Track track : new ArrayList<>(tracks)) {
                    if (coverCache.containsKey(track.uri)) continue;
                    Bitmap bitmap = readCover(track);
                    if (bitmap != null) {
                        synchronized (coverCache) {
                            coverCache.put(track.uri, bitmap);
                        }
                        loaded++;
                    }
                    if (loaded > 0 && loaded % 12 == 0) {
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                if (overlayHost != null && overlayHost.getChildCount() == 0) render();
                            }
                        });
                    }
                }
                if (loaded > 0) {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (overlayHost != null && overlayHost.getChildCount() == 0) render();
                        }
                    });
                }
            }
        }).start();
    }

    private Bitmap cover(Track track) {
        if (coverCache.containsKey(track.uri)) return coverCache.get(track.uri);
        Bitmap bitmap = readCover(track);
        if (bitmap != null) coverCache.put(track.uri, bitmap);
        return bitmap;
    }

    private Bitmap readCover(Track track) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, track.asUri());
            byte[] bytes = retriever.getEmbeddedPicture();
            if (bytes == null) return null;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception ignored) {
            return null;
        } finally {
            try { retriever.release(); } catch (Exception ignored) {}
        }
    }

    private View wave(Track track, boolean active) {
        WaveformView wave = new WaveformView(this, track.title + track.uri, active ? bg : fg, active && playing);
        wave.setMinimumHeight(dp(28));
        wave.setPadding(0, dp(3), 0, dp(3));
        wave.setLayoutParams(new LinearLayout.LayoutParams(dp(190), dp(30)));
        return wave;
    }

    private void openPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(intent, "Р’С‹Р±РµСЂРёС‚Рµ РјСѓР·С‹РєСѓ"), PICK_AUDIO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_AUDIO || resultCode != RESULT_OK || data == null) return;
        if (data.getClipData() != null) {
            for (int index = 0; index < data.getClipData().getItemCount(); index++) addTrack(data.getClipData().getItemAt(index).getUri());
        } else if (data.getData() != null) {
            addTrack(data.getData());
        }
        TrackStore.sort(tracks);
        TrackStore.save(this, tracks);
        render();
    }

    private void addTrack(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {}
        String value = uri.toString();
        for (Track track : tracks) if (track.uri.equals(value)) return;
        tracks.add(TrackStore.fromUri(this, uri));
    }

    private LinearLayout row() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    private TextView text(String value, int size, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextColor(fg);
        text.setTextSize(size);
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setTypeface(null, bold ? 1 : 0);
        text.setSingleLine(false);
        return text;
    }

    private Button button(String value) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextColor(fg);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setIncludeFontPadding(false);
        button.setPadding(0, 0, 0, dp(1));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setStateListAnimator(null);
            button.setElevation(0f);
            button.setTranslationZ(0f);
        }
        button.setMinWidth(0);
        button.setMinHeight(0);
        setSurface(button, bg, false);
        return button;
    }

    private Button icon(String value) {
        Button button = button(value);
        button.setTextSize(24);
        return button;
    }

    private Button shuffleButton() {
        Button button = icon("в‡„");
        button.setTextSize(31);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    private Button searchButton() {
        Button button = icon("вЊ•");
        button.setTextSize(31);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setPadding(0, 0, 0, dp(1));
        return button;
    }

    private void applyButtonColors(Button button, int fill, int text) {
        button.setTextColor(text);
        GradientDrawable drawable = rounded(fill, false);
        drawable.setStroke(1, fill);
        button.setBackground(drawable);
    }

    private LinearLayout.LayoutParams square(int dp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(dp), dp(dp));
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        return params;
    }

    private View framed(View child) {
        return spaced(child);
    }

    private View spaced(View child) {
        LinearLayout.LayoutParams outer = new LinearLayout.LayoutParams(-1, -2);
        outer.setMargins(0, dp(5), 0, dp(5));
        child.setLayoutParams(outer);
        return child;
    }

    private GradientDrawable rounded(int fill, boolean stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(10));
        drawable.setStroke(stroke ? 1 : 0, stroke ? line : fill);
        return drawable;
    }

    private void setSurface(View view, int fill, boolean stroke) {
        view.setBackground(rounded(fill, stroke));
    }

    private View lineView() {
        View view = new View(this);
        view.setBackgroundColor(line);
        return view;
    }

    private ImageView coverView() {
        ImageView cover = new ImageView(this);
        cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            GradientDrawable outline = new GradientDrawable();
            outline.setColor(Color.TRANSPARENT);
            outline.setCornerRadius(dp(12));
            cover.setBackground(outline);
            cover.setClipToOutline(true);
        }
        return cover;
    }

    private FrameLayout shade() {
        FrameLayout shade = new FrameLayout(this);
        shade.setBackgroundColor(dark ? Color.argb(190, 0, 0, 0) : Color.argb(190, 255, 255, 255));
        shade.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                overlayHost.removeView(v);
                updateMini();
            }
        });
        return shade;
    }

    private LinearLayout panelCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        setSurface(card, bg, true);
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {}
        });
        return card;
    }

    private FrameLayout.LayoutParams bottomParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, (int) (getResources().getDisplayMetrics().heightPixels * 0.78f), Gravity.BOTTOM);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class Playlist {
        final String name;
        final ArrayList<String> uris = new ArrayList<>();
        Playlist(String name) { this.name = name; }
    }

    private interface PanelAction {
        void add();
    }

    private interface PickDone {
        void done(Set<String> uris);
    }

    private interface InputDone {
        void done(String value);
    }
}

