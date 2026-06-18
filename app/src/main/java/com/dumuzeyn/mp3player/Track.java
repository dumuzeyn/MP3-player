package com.dumuzeyn.mp3player;

import android.net.Uri;

public class Track {
    public final String uri;
    public final String title;
    public final String artist;
    public final String album;
    public final String genre;

    public Track(String uri, String title, String artist) {
        this(uri, title, artist, "РќРµРёР·РІРµСЃС‚РЅС‹Р№ Р°Р»СЊР±РѕРј", "РќРµРёР·РІРµСЃС‚РЅС‹Р№ Р¶Р°РЅСЂ");
    }

    public Track(String uri, String title, String artist, String album, String genre) {
        this.uri = uri;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
    }

    public Uri asUri() {
        return Uri.parse(uri);
    }
}

