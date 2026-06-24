package com.dumuzeyn.mp3player;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final String CUSTOM_TIMER = "customTimer";
    private static final String ANIMATIONS = "animations";
    private static final String FAVORITES = "favorites";
    private static final String LANGUAGE = "language";
    private static final int PICK_AUDIO = 2001;
    private static final String PLAYLISTS = "playlists";
    private static final String PREFS = "mp3_player_ui";
    private static final int TAB_CYCLES = 21;
    private static final String THEME = "theme";
    private int bg;
    private int fg;
    private int line;
    private LinearLayout list;
    private Button miniButton;
    private LinearLayout miniPlayer;
    private TextView miniSub;
    private TextView miniTitle;
    private int muted;
    private FrameLayout overlayHost;
    private LinearLayout page;
    private int panel;
    private SharedPreferences prefs;
    private FrameLayout root;
    private LinearLayout tabRow;
    private ValueAnimator tabScrollAnimator;
    private String[] tabs;
    private HorizontalScrollView tabsScroll;
    private final ArrayList<Track> tracks = new ArrayList<>();
    private final HashSet<String> favorites = new HashSet<>();
    private final ArrayList<Playlist> playlists = new ArrayList<>();
    private final ArrayList<Track> playbackQueue = new ArrayList<>();
    private final HashMap<String, Bitmap> coverCache = new HashMap<>();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Handler playbackHandler = new Handler(Looper.getMainLooper());
    private final Handler sleepHandler = new Handler(Looper.getMainLooper());
    private int tabIndex = 0;
    private int currentIndex = -1;
    private boolean playing = false;
    private int loopMode = 0;
    private int customTimerMinutes = 10;
    private long sleepTimerEndsAt = 0;
    private boolean dark = false;
    private boolean animations = true;
    private String language = "en";
    private float swipeStartX = 0.0f;
    private float swipeStartY = 0.0f;
    private boolean tabAnimating = false;
    private boolean swipeStartedOnTabs = false;
    private boolean pageSwipeConsuming = false;
    private String search = "";

    private interface InputDone {
        void done(String str);
    }

    private interface PanelAction {
        void add();

        void remove(Track track);
    }

    private interface PickDone {
        void done(Set<String> set);
    }

    static int m0$$Nest$fgetbg(MainActivity mainActivity) {
        return mainActivity.bg;
    }

    static HashMap m1$$Nest$fgetcoverCache(MainActivity mainActivity) {
        return mainActivity.coverCache;
    }

    static int m2$$Nest$fgetcurrentIndex(MainActivity mainActivity) {
        return mainActivity.currentIndex;
    }

    static int m3$$Nest$fgetcustomTimerMinutes(MainActivity mainActivity) {
        return mainActivity.customTimerMinutes;
    }

    static boolean m4$$Nest$fgetdark(MainActivity mainActivity) {
        return mainActivity.dark;
    }

    static HashSet m5$$Nest$fgetfavorites(MainActivity mainActivity) {
        return mainActivity.favorites;
    }

    static int m6$$Nest$fgetfg(MainActivity mainActivity) {
        return mainActivity.fg;
    }

    static LinearLayout m7$$Nest$fgetlist(MainActivity mainActivity) {
        return mainActivity.list;
    }

    static int m8$$Nest$fgetloopMode(MainActivity mainActivity) {
        return mainActivity.loopMode;
    }

    static FrameLayout m9$$Nest$fgetoverlayHost(MainActivity mainActivity) {
        return mainActivity.overlayHost;
    }

    static int m10$$Nest$fgetpanel(MainActivity mainActivity) {
        return mainActivity.panel;
    }

    static Handler m11$$Nest$fgetplaybackHandler(MainActivity mainActivity) {
        return mainActivity.playbackHandler;
    }

    static ArrayList m12$$Nest$fgetplaybackQueue(MainActivity mainActivity) {
        return mainActivity.playbackQueue;
    }

    static boolean m13$$Nest$fgetplaying(MainActivity mainActivity) {
        return mainActivity.playing;
    }

    static ArrayList m14$$Nest$fgetplaylists(MainActivity mainActivity) {
        return mainActivity.playlists;
    }

    static int m15$$Nest$fgettabIndex(MainActivity mainActivity) {
        return mainActivity.tabIndex;
    }

    static LinearLayout m16$$Nest$fgettabRow(MainActivity mainActivity) {
        return mainActivity.tabRow;
    }

    static String[] m17$$Nest$fgettabs(MainActivity mainActivity) {
        return mainActivity.tabs;
    }

    static HorizontalScrollView m18$$Nest$fgettabsScroll(MainActivity mainActivity) {
        return mainActivity.tabsScroll;
    }

    static ArrayList m19$$Nest$fgettracks(MainActivity mainActivity) {
        return mainActivity.tracks;
    }

    static void m20$$Nest$fputcurrentIndex(MainActivity mainActivity, int i) {
        mainActivity.currentIndex = i;
    }

    static void m21$$Nest$fputcustomTimerMinutes(MainActivity mainActivity, int i) {
        mainActivity.customTimerMinutes = i;
    }

    static void m22$$Nest$fputdark(MainActivity mainActivity, boolean z) {
        mainActivity.dark = z;
    }

    static void m23$$Nest$fputlanguage(MainActivity mainActivity, String str) {
        mainActivity.language = str;
    }

    static void m24$$Nest$fputloopMode(MainActivity mainActivity, int i) {
        mainActivity.loopMode = i;
    }

    static void m25$$Nest$fputplaying(MainActivity mainActivity, boolean z) {
        mainActivity.playing = z;
    }

    static void m26$$Nest$fputsearch(MainActivity mainActivity, String str) {
        mainActivity.search = str;
    }

    static void m27$$Nest$fputsleepTimerEndsAt(MainActivity mainActivity, long j) {
        mainActivity.sleepTimerEndsAt = j;
    }

    static void m28$$Nest$fputtabAnimating(MainActivity mainActivity, boolean z) {
        mainActivity.tabAnimating = z;
    }

    static void m29$$Nest$fputtabIndex(MainActivity mainActivity, int i) {
        mainActivity.tabIndex = i;
    }

    static void m30$$Nest$manimateTabsScrollTo(MainActivity mainActivity, int i) {
        mainActivity.animateTabsScrollTo(i);
    }

    static void m31$$Nest$mapplyButtonColors(MainActivity mainActivity, Button button, int i, int i2) {
        mainActivity.applyButtonColors(button, i, i2);
    }

    static void m32$$Nest$mbuildUi(MainActivity mainActivity) {
        mainActivity.buildUi();
    }

    static void m33$$Nest$mcancelSleepTimer(MainActivity mainActivity) {
        mainActivity.cancelSleepTimer();
    }

    static void m34$$Nest$mchoosePlaylistForTrack(MainActivity mainActivity, Track track) {
        mainActivity.choosePlaylistForTrack(track);
    }

    static void m35$$Nest$mconfirmDeleteAllPlaylists(MainActivity mainActivity) {
        mainActivity.confirmDeleteAllPlaylists();
    }

    static void m36$$Nest$mconfirmDeleteAllSongs(MainActivity mainActivity) {
        mainActivity.confirmDeleteAllSongs();
    }

    static void m37$$Nest$mconfirmDeletePlaylist(MainActivity mainActivity, Playlist playlist) {
        mainActivity.confirmDeletePlaylist(playlist);
    }

    static void m38$$Nest$mconfirmDeleteTrack(MainActivity mainActivity, Track track) {
        mainActivity.confirmDeleteTrack(track);
    }

    static void m39$$Nest$mcreatePlaylistAndAdd(MainActivity mainActivity, Track track) {
        mainActivity.createPlaylistAndAdd(track);
    }

    static void m40$$Nest$mcreatePlaylistDialog(MainActivity mainActivity) {
        mainActivity.createPlaylistDialog();
    }

    static ArrayList m41$$Nest$mcurrentVisibleTracks(MainActivity mainActivity) {
        return mainActivity.currentVisibleTracks();
    }

    static void m42$$Nest$mcustomTimerDialog(MainActivity mainActivity) {
        mainActivity.customTimerDialog();
    }

    static Track m43$$Nest$mfindTrack(MainActivity mainActivity, String str) {
        return mainActivity.findTrack(str);
    }

    static String m44$$Nest$mformatMs(MainActivity mainActivity, int i) {
        return mainActivity.formatMs(i);
    }

    static boolean m45$$Nest$misCurrent(MainActivity mainActivity, Track track) {
        return mainActivity.isCurrent(track);
    }

    static boolean m46$$Nest$misInPlaybackQueue(MainActivity mainActivity, Track track) {
        return mainActivity.isInPlaybackQueue(track);
    }

    static void m47$$Nest$mnext(MainActivity mainActivity) {
        mainActivity.next();
    }

    static void m48$$Nest$mopenAddFavorites(MainActivity mainActivity) {
        mainActivity.openAddFavorites();
    }

    static void m49$$Nest$mopenAddToPlaylist(MainActivity mainActivity, Playlist playlist) {
        mainActivity.openAddToPlaylist(playlist);
    }

    static void m50$$Nest$mopenAddToQueue(MainActivity mainActivity) {
        mainActivity.openAddToQueue();
    }

    static void m51$$Nest$mopenFullPlayer(MainActivity mainActivity) {
        mainActivity.openFullPlayer();
    }

    static void m52$$Nest$mopenGithub(MainActivity mainActivity) {
        mainActivity.openGithub();
    }

    static void m53$$Nest$mopenGroupSongs(MainActivity mainActivity, String str, ArrayList arrayList) {
        mainActivity.openGroupSongs(str, arrayList);
    }

    static void m54$$Nest$mopenPicker(MainActivity mainActivity) {
        mainActivity.openPicker();
    }

    static void m55$$Nest$mopenPlaylist(MainActivity mainActivity, Playlist playlist) {
        mainActivity.openPlaylist(playlist);
    }

    static void m56$$Nest$mopenQueuePanel(MainActivity mainActivity) {
        mainActivity.openQueuePanel();
    }

    static void m57$$Nest$mopenSearch(MainActivity mainActivity) {
        mainActivity.openSearch();
    }

    static void m58$$Nest$mopenSongActions(MainActivity mainActivity, Track track) {
        mainActivity.openSongActions(track);
    }

    static void m59$$Nest$mplayList(MainActivity mainActivity, ArrayList arrayList, boolean z) {
        mainActivity.playList(arrayList, z);
    }

    static void m60$$Nest$mplayQueueTrack(MainActivity mainActivity, Track track) {
        mainActivity.playQueueTrack(track);
    }

    static void m61$$Nest$mplayTrack(MainActivity mainActivity, Track track) {
        mainActivity.playTrack(track);
    }

    static void m62$$Nest$mplayTrack(MainActivity mainActivity, Track track, boolean z) {
        mainActivity.playTrack(track, z);
    }

    static ArrayList m63$$Nest$mplaylistTracks(MainActivity mainActivity, Playlist playlist) {
        return mainActivity.playlistTracks(playlist);
    }

    static void m64$$Nest$mprevious(MainActivity mainActivity) {
        mainActivity.previous();
    }

    static Bitmap m65$$Nest$mreadCover(MainActivity mainActivity, Track track) {
        return mainActivity.readCover(track);
    }

    static void m66$$Nest$mremoveFromQueue(MainActivity mainActivity, Track track) {
        mainActivity.removeFromQueue(track);
    }

    static void m67$$Nest$mrender(MainActivity mainActivity) {
        mainActivity.render();
    }

    static void m68$$Nest$msaveState(MainActivity mainActivity) {
        mainActivity.saveState();
    }

    static void m69$$Nest$mscrollTabsToActive(MainActivity mainActivity, boolean z) {
        mainActivity.scrollTabsToActive(z);
    }

    static void m70$$Nest$msetSurface(MainActivity mainActivity, View view, int i, boolean z) {
        mainActivity.setSurface(view, i, z);
    }

    static void m71$$Nest$mshowPanel(MainActivity mainActivity, String str, ArrayList arrayList, PanelAction panelAction) {
        mainActivity.showPanel(str, arrayList, panelAction);
    }

    static void m72$$Nest$mstartSleepTimer(MainActivity mainActivity, int i) {
        mainActivity.startSleepTimer(i);
    }

    static void m73$$Nest$mstopPlaybackAndClearQueue(MainActivity mainActivity) {
        mainActivity.stopPlaybackAndClearQueue();
    }

    static void m74$$Nest$mswitchTabAnimated(MainActivity mainActivity, int i, int i2) {
        mainActivity.switchTabAnimated(i, i2);
    }

    static String m75$$Nest$mtimerButtonText(MainActivity mainActivity) {
        return mainActivity.timerButtonText();
    }

    static void m76$$Nest$mtimerDialog(MainActivity mainActivity) {
        mainActivity.timerDialog();
    }

    static void m77$$Nest$mtoggleCurrent(MainActivity mainActivity) {
        mainActivity.toggleCurrent();
    }

    static void m78$$Nest$mtoggleFavorite(MainActivity mainActivity, Track track) {
        mainActivity.toggleFavorite(track);
    }

    static String m79$$Nest$mtr(MainActivity mainActivity, String str, String str2) {
        return mainActivity.tr(str, str2);
    }

    static void m80$$Nest$mupdateMini(MainActivity mainActivity) {
        mainActivity.updateMini();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.prefs = getSharedPreferences(PREFS, 0);
        this.dark = "dark".equals(this.prefs.getString(THEME, "light"));
        this.animations = this.prefs.getBoolean(ANIMATIONS, true);
        this.language = this.prefs.getString(LANGUAGE, "en");
        this.customTimerMinutes = this.prefs.getInt(CUSTOM_TIMER, 10);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 33);
        }
        loadState();
        buildUi();
        refreshMissingMetadataAsync();
        preloadCoverCacheAsync();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (handlePageSwipe(motionEvent)) {
            return true;
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    private void loadState() {
        this.tracks.clear();
        this.tracks.addAll(TrackStore.load(this));
        this.favorites.clear();
        this.favorites.addAll(this.prefs.getStringSet(FAVORITES, new HashSet()));
        this.playlists.clear();
        try {
            JSONArray jSONArray = new JSONArray(this.prefs.getString(PLAYLISTS, "[]"));
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                Playlist playlist = new Playlist(cleanPlaylistName(jSONObject.optString("name", "Playlist")));
                JSONArray jSONArrayOptJSONArray = jSONObject.optJSONArray("songs");
                if (jSONArrayOptJSONArray != null) {
                    for (int i2 = 0; i2 < jSONArrayOptJSONArray.length(); i2++) {
                        playlist.uris.add(jSONArrayOptJSONArray.getString(i2));
                    }
                }
                this.playlists.add(playlist);
            }
        } catch (Exception e) {
        }
    }

    class AnonymousClass1 implements Runnable {
        AnonymousClass1() {
        }

        @Override
        public void run() {
            ArrayList arrayList = new ArrayList(MainActivity.m19$$Nest$fgettracks(MainActivity.this));
            boolean z = false;
            for (int i = 0; i < arrayList.size(); i++) {
                Track track = (Track) arrayList.get(i);
                if ("Неизвестный альбом".equals(track.album) || "Неизвестный жанр".equals(track.genre)) {
                    Track trackRefreshMetadata = TrackStore.refreshMetadata(MainActivity.this, track);
                    if (!trackRefreshMetadata.album.equals(track.album) || !trackRefreshMetadata.genre.equals(track.genre) || !trackRefreshMetadata.artist.equals(track.artist)) {
                        arrayList.set(i, trackRefreshMetadata);
                        z = true;
                    }
                }
            }
            if (z) {
                TrackStore.save(MainActivity.this, arrayList);
                MainActivity.this.runOnUiThread(new RunnableC00001(this, arrayList));
            }
        }

        class RunnableC00001 implements Runnable {
            final AnonymousClass1 this$1;
            final ArrayList val$freshTracks;

            RunnableC00001(AnonymousClass1 anonymousClass1, ArrayList arrayList) {
                this.val$freshTracks = arrayList;
                this.this$1 = anonymousClass1;
            }

            @Override
            public void run() {
                MainActivity.m19$$Nest$fgettracks(MainActivity.this).clear();
                MainActivity.m19$$Nest$fgettracks(MainActivity.this).addAll(this.val$freshTracks);
                MainActivity.m67$$Nest$mrender(MainActivity.this);
            }
        }
    }

    private void refreshMissingMetadataAsync() {
        new Thread(new AnonymousClass1()).start();
    }

    private boolean english() {
        return "en".equals(this.language);
    }

    private String tr(String str, String str2) {
        return english() ? str : str2;
    }

    private void refreshTabLabels() {
        this.tabs = new String[]{tr("Songs", "Песни"), tr("Favorites", "Избранное"), tr("Playlists", "Плейлисты"), tr("Genres", "Жанры"), tr("Artists", "Исполнители"), tr("Albums", "Альбомы"), tr("Settings", "Настройки")};
        if (this.tabIndex >= this.tabs.length) {
            this.tabIndex = 0;
        }
    }

    private boolean handlePageSwipe(MotionEvent motionEvent) {
        int length;
        int length2;
        if (this.tabs == null || this.tabs.length == 0) {
            return false;
        }
        if (this.overlayHost != null && this.overlayHost.getChildCount() > 0) {
            return false;
        }
        if (motionEvent.getActionMasked() == 0) {
            this.swipeStartedOnTabs = isInsideTabs(motionEvent);
            this.pageSwipeConsuming = false;
            this.swipeStartX = motionEvent.getX();
            this.swipeStartY = motionEvent.getY();
            if (this.tabScrollAnimator != null) {
                this.tabScrollAnimator.cancel();
            }
            return false;
        }
        if (motionEvent.getActionMasked() == 2) {
            if (this.swipeStartedOnTabs) {
                return false;
            }
            float x = motionEvent.getX() - this.swipeStartX;
            float y = motionEvent.getY() - this.swipeStartY;
            if (!this.pageSwipeConsuming && Math.abs(x) > dp(28) && Math.abs(x) > Math.abs(y) * 1.25f) {
                this.pageSwipeConsuming = true;
                if (this.root != null && this.root.getParent() != null) {
                    this.root.getParent().requestDisallowInterceptTouchEvent(true);
                }
            }
            return this.pageSwipeConsuming;
        }
        if (motionEvent.getActionMasked() != 1 && motionEvent.getActionMasked() != 3) {
            return false;
        }
        if (this.swipeStartedOnTabs) {
            this.swipeStartedOnTabs = false;
            this.pageSwipeConsuming = false;
            return false;
        }
        float x2 = motionEvent.getX() - this.swipeStartX;
        float y2 = motionEvent.getY() - this.swipeStartY;
        boolean z = this.pageSwipeConsuming;
        this.pageSwipeConsuming = false;
        if (motionEvent.getActionMasked() == 1 && Math.abs(x2) > dp(70) && Math.abs(x2) > Math.abs(y2) * 1.35f) {
            int i = this.tabIndex;
            if (x2 < 0.0f) {
                length = i + 1;
                length2 = this.tabs.length;
            } else {
                length = (i - 1) + this.tabs.length;
                length2 = this.tabs.length;
            }
            switchTabAnimated(length % length2, x2 < 0.0f ? 1 : -1);
            return true;
        }
        return z;
    }

    private boolean isInsideTabs(MotionEvent motionEvent) {
        if (this.tabsScroll == null) {
            return false;
        }
        int[] iArr = new int[2];
        this.tabsScroll.getLocationOnScreen(iArr);
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        return rawX >= ((float) iArr[0]) && rawX <= ((float) (iArr[0] + this.tabsScroll.getWidth())) && rawY >= ((float) iArr[1]) && rawY <= ((float) (iArr[1] + this.tabsScroll.getHeight()));
    }

    private void saveState() {
        this.prefs.edit().putStringSet(FAVORITES, new HashSet(this.favorites)).putString(PLAYLISTS, playlistsJson()).putString(THEME, this.dark ? "dark" : "light").putBoolean(ANIMATIONS, this.animations).putString(LANGUAGE, this.language).putInt(CUSTOM_TIMER, this.customTimerMinutes).apply();
    }

    private String playlistsJson() {
        JSONArray jSONArray = new JSONArray();
        for (Playlist playlist : this.playlists) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("name", playlist.name);
                JSONArray jSONArray2 = new JSONArray();
                Iterator<String> it = playlist.uris.iterator();
                while (it.hasNext()) {
                    jSONArray2.put(it.next());
                }
                jSONObject.put("songs", jSONArray2);
                jSONArray.put(jSONObject);
            } catch (Exception e) {
            }
        }
        return jSONArray.toString();
    }

    private void colors() {
        this.bg = this.dark ? -16777216 : -1;
        this.fg = this.dark ? -1 : -16777216;
        this.muted = this.dark ? Color.rgb(190, 190, 190) : Color.rgb(80, 80, 80);
        this.line = this.dark ? Color.rgb(90, 90, 90) : Color.rgb(190, 190, 190);
        this.panel = this.bg;
    }

    private void buildUi() {
        colors();
        refreshTabLabels();
        this.root = new FrameLayout(this);
        this.root.setBackgroundColor(this.bg);
        this.page = new LinearLayout(this);
        this.page.setOrientation(1);
        this.page.setPadding(dp(8), dp(14), dp(8), dp(8));
        this.root.addView(this.page, new FrameLayout.LayoutParams(-1, -1));
        buildHeader();
        buildTabs();
        ScrollView scrollView = new ScrollView(this);
        this.list = new LinearLayout(this);
        this.list.setOrientation(1);
        scrollView.addView(this.list, new FrameLayout.LayoutParams(-1, -2));
        this.page.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        this.overlayHost = new FrameLayout(this);
        this.root.addView(this.overlayHost, new FrameLayout.LayoutParams(-1, -1));
        buildMiniPlayer();
        setContentView(this.root);
        render();
    }

    private void buildHeader() {
        LinearLayout linearLayoutRow = row();
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(getResources().getIdentifier("ic_music_vector", "drawable", getPackageName()));
        imageView.setColorFilter(this.fg);
        linearLayoutRow.addView(imageView, square(42));
        linearLayoutRow.addView(text("MP3 Player", 20, true), new LinearLayout.LayoutParams(0, dp(60), 1.0f));
        this.page.addView(linearLayoutRow, new LinearLayout.LayoutParams(-1, dp(66)));
    }

    private void buildTabs() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.addView(lineView(), new LinearLayout.LayoutParams(-1, 1));
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        this.tabsScroll = horizontalScrollView;
        horizontalScrollView.setHorizontalScrollBarEnabled(false);
        this.tabRow = new LinearLayout(this);
        this.tabRow.setOrientation(0);
        horizontalScrollView.addView(this.tabRow);
        linearLayout.addView(horizontalScrollView, new LinearLayout.LayoutParams(-1, dp(48)));
        linearLayout.addView(lineView(), new LinearLayout.LayoutParams(-1, 1));
        this.page.addView(linearLayout, new LinearLayout.LayoutParams(-1, dp(50)));
        for (int i = 0; i < TAB_CYCLES; i++) {
            for (int i2 = 0; i2 < this.tabs.length; i2++) {
                Button button = button(this.tabs[i2]);
                button.setTag(Integer.valueOf(i2));
                styleTab(button, i2);
                button.setOnClickListener(new AnonymousClass2(this, i2));
                this.tabRow.addView(button, new LinearLayout.LayoutParams(dp(132), dp(48)));
            }
        }
        horizontalScrollView.post(new AnonymousClass3());
    }

    class AnonymousClass2 implements View.OnClickListener {
        final MainActivity this$0;
        final int val$index;

        AnonymousClass2(MainActivity mainActivity, int i) {
            this.val$index = i;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m74$$Nest$mswitchTabAnimated(this.this$0, this.val$index, this.val$index < MainActivity.m15$$Nest$fgettabIndex(this.this$0) ? -1 : 1);
        }
    }

    class AnonymousClass3 implements Runnable {
        AnonymousClass3() {
        }

        @Override
        public void run() {
            int iMax = Math.max(1, MainActivity.m16$$Nest$fgettabRow(MainActivity.this).getWidth() / MainActivity.TAB_CYCLES);
            MainActivity.m69$$Nest$mscrollTabsToActive(MainActivity.this, false);
            MainActivity.m18$$Nest$fgettabsScroll(MainActivity.this).setOnScrollChangeListener(new AnonymousClass1(this, iMax));
        }

        class AnonymousClass1 implements View.OnScrollChangeListener {
            final AnonymousClass3 this$1;
            final int val$cycleWidth;

            AnonymousClass1(AnonymousClass3 anonymousClass3, int i) {
                this.val$cycleWidth = i;
                this.this$1 = anonymousClass3;
            }

            @Override
            public void onScrollChange(View view, int i, int i2, int i3, int i4) {
                int i5 = this.val$cycleWidth * 8;
                int i6 = this.val$cycleWidth * 12;
                if (i < i5) {
                    MainActivity.m18$$Nest$fgettabsScroll(MainActivity.this).scrollTo(i + this.val$cycleWidth, 0);
                } else if (i > i6) {
                    MainActivity.m18$$Nest$fgettabsScroll(MainActivity.this).scrollTo(i - this.val$cycleWidth, 0);
                }
            }
        }
    }

    private void styleTab(Button button, int i) {
        button.setTextSize(15.0f);
        button.setGravity(17);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i == this.tabIndex ? this.fg : 0);
        gradientDrawable.setCornerRadius(dp(10));
        button.setBackground(gradientDrawable);
        button.setTextColor(i == this.tabIndex ? this.bg : this.muted);
    }

    private void refreshTabs() {
        if (this.tabRow == null) {
            return;
        }
        for (int i = 0; i < this.tabRow.getChildCount(); i++) {
            View childAt = this.tabRow.getChildAt(i);
            if ((childAt instanceof Button) && (childAt.getTag() instanceof Integer)) {
                styleTab((Button) childAt, ((Integer) childAt.getTag()).intValue());
            }
        }
    }

    private void switchTabAnimated(int i, int i2) {
        if (this.tabs == null || i == this.tabIndex || this.tabAnimating) {
            return;
        }
        if (!this.animations) {
            this.tabIndex = i;
            this.search = "";
            scrollTabsToActive(false, i);
            render();
            return;
        }
        this.tabAnimating = true;
        int width = (this.root == null || this.root.getWidth() <= 0) ? getResources().getDisplayMetrics().widthPixels : this.root.getWidth();
        if (this.list == null) {
            this.tabIndex = i;
            this.search = "";
            render();
            this.tabAnimating = false;
            return;
        }
        scrollTabsToActive(true, i);
        this.list.animate().translationX(i2 < 0 ? width : -width).alpha(0.0f).setDuration(48L).setInterpolator(new DecelerateInterpolator()).withEndAction(new AnonymousClass4(this, i, i2, width)).start();
    }

    class AnonymousClass4 implements Runnable {
        final MainActivity this$0;
        final int val$direction;
        final int val$nextIndex;
        final int val$width;

        AnonymousClass4(MainActivity mainActivity, int i, int i2, int i3) {
            this.val$nextIndex = i;
            this.val$direction = i2;
            this.val$width = i3;
            this.this$0 = mainActivity;
        }

        @Override
        public void run() {
            MainActivity.m29$$Nest$fputtabIndex(this.this$0, this.val$nextIndex);
            MainActivity.m26$$Nest$fputsearch(this.this$0, "");
            MainActivity.m67$$Nest$mrender(this.this$0);
            MainActivity.m7$$Nest$fgetlist(this.this$0).setTranslationX(this.val$direction < 0 ? -this.val$width : this.val$width);
            MainActivity.m7$$Nest$fgetlist(this.this$0).setAlpha(0.0f);
            MainActivity.m7$$Nest$fgetlist(this.this$0).animate().translationX(0.0f).alpha(1.0f).setDuration(92L).setInterpolator(new DecelerateInterpolator()).withEndAction(new AnonymousClass1()).start();
        }

        class AnonymousClass1 implements Runnable {
            AnonymousClass1() {
            }

            @Override
            public void run() {
                MainActivity.m28$$Nest$fputtabAnimating(AnonymousClass4.this.this$0, false);
            }
        }
    }

    private void scrollTabsToActive(boolean z) {
        scrollTabsToActive(z, this.tabIndex);
    }

    class AnonymousClass5 implements Runnable {
        final MainActivity this$0;
        final boolean val$smooth;
        final int val$targetIndex;

        AnonymousClass5(MainActivity mainActivity, int i, boolean z) {
            this.val$targetIndex = i;
            this.val$smooth = z;
            this.this$0 = mainActivity;
        }

        @Override
        public void run() {
            int iMax = Math.max(0, Math.min(this.val$targetIndex, MainActivity.m17$$Nest$fgettabs(this.this$0).length - 1));
            int left = -1;
            if (this.val$smooth) {
                int scrollX = MainActivity.m18$$Nest$fgettabsScroll(this.this$0).getScrollX() + (MainActivity.m18$$Nest$fgettabsScroll(this.this$0).getWidth() / 2);
                int i = Integer.MAX_VALUE;
                for (int i2 = 0; i2 < MainActivity.m16$$Nest$fgettabRow(this.this$0).getChildCount(); i2++) {
                    View childAt = MainActivity.m16$$Nest$fgettabRow(this.this$0).getChildAt(i2);
                    Object tag = childAt.getTag();
                    if ((tag instanceof Integer) && ((Integer) tag).intValue() == iMax) {
                        int left2 = childAt.getLeft() + (childAt.getWidth() / 2);
                        int left3 = childAt.getLeft() - Math.max(0, (MainActivity.m18$$Nest$fgettabsScroll(this.this$0).getWidth() - childAt.getWidth()) / 2);
                        int iAbs = Math.abs(left2 - scrollX);
                        if (iAbs < i) {
                            i = iAbs;
                            left = left3;
                        }
                    }
                }
            }
            if (left < 0) {
                int length = (MainActivity.m17$$Nest$fgettabs(this.this$0).length * 10) + iMax;
                if (length >= MainActivity.m16$$Nest$fgettabRow(this.this$0).getChildCount()) {
                    return;
                }
                View childAt2 = MainActivity.m16$$Nest$fgettabRow(this.this$0).getChildAt(length);
                left = childAt2.getLeft() - Math.max(0, (MainActivity.m18$$Nest$fgettabsScroll(this.this$0).getWidth() - childAt2.getWidth()) / 2);
            }
            if (this.val$smooth) {
                MainActivity.m30$$Nest$manimateTabsScrollTo(this.this$0, left);
            } else {
                MainActivity.m18$$Nest$fgettabsScroll(this.this$0).scrollTo(left, 0);
            }
        }
    }

    private void scrollTabsToActive(boolean z, int i) {
        if (this.tabsScroll == null || this.tabRow == null || this.tabs == null || this.tabs.length == 0) {
            return;
        }
        this.tabsScroll.post(new AnonymousClass5(this, i, z));
    }

    private void animateTabsScrollTo(int i) {
        if (this.tabsScroll == null) {
            return;
        }
        if (this.tabScrollAnimator != null) {
            this.tabScrollAnimator.cancel();
        }
        int scrollX = this.tabsScroll.getScrollX();
        if (Math.abs(i - scrollX) < 2) {
            this.tabsScroll.scrollTo(i, 0);
            return;
        }
        if (!this.animations) {
            this.tabsScroll.scrollTo(i, 0);
            return;
        }
        this.tabScrollAnimator = ValueAnimator.ofInt(scrollX, i);
        this.tabScrollAnimator.setDuration(96L);
        this.tabScrollAnimator.setInterpolator(new DecelerateInterpolator());
        this.tabScrollAnimator.addUpdateListener(new AnonymousClass6());
        this.tabScrollAnimator.start();
    }

    class AnonymousClass6 implements ValueAnimator.AnimatorUpdateListener {
        AnonymousClass6() {
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            if (MainActivity.m18$$Nest$fgettabsScroll(MainActivity.this) != null) {
                MainActivity.m18$$Nest$fgettabsScroll(MainActivity.this).scrollTo(((Integer) valueAnimator.getAnimatedValue()).intValue(), 0);
            }
        }
    }

    private void buildMiniPlayer() {
        this.miniPlayer = new LinearLayout(this);
        this.miniPlayer.setOrientation(0);
        this.miniPlayer.setGravity(16);
        this.miniPlayer.setPadding(dp(14), 0, dp(10), 0);
        setSurface(this.miniPlayer, this.bg, true);
        this.miniPlayer.setVisibility(8);
        this.miniPlayer.setOnClickListener(new AnonymousClass7());
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        this.miniTitle = text(tr("Song", "Песня"), 16, true);
        this.miniSub = text(tr("Unknown artist", "Неизвестный исполнитель"), 12, false);
        this.miniTitle.setSingleLine(true);
        this.miniTitle.setEllipsize(TextUtils.TruncateAt.END);
        this.miniSub.setSingleLine(true);
        this.miniSub.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout.addView(this.miniTitle);
        linearLayout.addView(this.miniSub);
        this.miniPlayer.addView(linearLayout, new LinearLayout.LayoutParams(0, -2, 1.0f));
        this.miniButton = icon("▶");
        this.miniButton.setOnClickListener(new AnonymousClass8());
        this.miniPlayer.addView(this.miniButton, square(52));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, dp(74), 80);
        layoutParams.setMargins(0, 0, 0, 0);
        this.root.addView(this.miniPlayer, layoutParams);
    }

    class AnonymousClass7 implements View.OnClickListener {
        AnonymousClass7() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m51$$Nest$mopenFullPlayer(MainActivity.this);
        }
    }

    class AnonymousClass8 implements View.OnClickListener {
        AnonymousClass8() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m77$$Nest$mtoggleCurrent(MainActivity.this);
        }
    }

    private void render() {
        refreshTabs();
        this.list.removeAllViews();
        renderSectionHeader();
        if (this.tabIndex == 0) {
            renderSongs(filter(this.tracks));
        } else if (this.tabIndex == 1) {
            renderSongs(filter(favoriteTracks()));
        } else if (this.tabIndex == 2) {
            renderPlaylists();
        } else if (this.tabIndex == 6) {
            renderSettings();
        } else {
            renderGroups(this.tabs[this.tabIndex]);
        }
        addMiniSpacerIfNeeded();
        updateMini();
    }

    private void renderSectionHeader() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        String str = this.tabs[this.tabIndex];
        if (this.tabIndex == 0) {
            str = tr("Songs ", "Песен ") + this.tracks.size();
        }
        TextView textViewText = text(str, 22, true);
        textViewText.setSingleLine(true);
        linearLayout.addView(textViewText, new LinearLayout.LayoutParams(-1, dp(48)));
        if (this.tabIndex == 0 || this.tabIndex == 1) {
            LinearLayout linearLayoutRow = row();
            Button buttonIcon = icon("▶");
            buttonIcon.setOnClickListener(new AnonymousClass9());
            linearLayoutRow.addView(buttonIcon, square(52));
            Button buttonShuffleButton = shuffleButton();
            buttonShuffleButton.setOnClickListener(new AnonymousClass10());
            linearLayoutRow.addView(buttonShuffleButton, square(52));
            if (this.tabIndex == 0) {
                Button buttonIcon2 = icon("+");
                buttonIcon2.setOnClickListener(new AnonymousClass11());
                linearLayoutRow.addView(buttonIcon2, square(52));
            } else {
                Button buttonIcon3 = icon("+");
                buttonIcon3.setOnClickListener(new AnonymousClass12());
                linearLayoutRow.addView(buttonIcon3, square(52));
            }
            Button buttonSearchButton = searchButton();
            buttonSearchButton.setOnClickListener(new AnonymousClass13());
            linearLayoutRow.addView(buttonSearchButton, square(52));
            linearLayout.addView(linearLayoutRow, new LinearLayout.LayoutParams(-1, dp(62)));
        } else if (this.tabIndex == 2) {
            LinearLayout linearLayoutRow2 = row();
            Button buttonIcon4 = icon("+");
            buttonIcon4.setOnClickListener(new AnonymousClass14());
            linearLayoutRow2.addView(buttonIcon4, square(52));
            Button buttonSearchButton2 = searchButton();
            buttonSearchButton2.setOnClickListener(new AnonymousClass15());
            linearLayoutRow2.addView(buttonSearchButton2, square(52));
            linearLayout.addView(linearLayoutRow2, new LinearLayout.LayoutParams(-1, dp(62)));
        }
        this.list.addView(linearLayout);
    }

    class AnonymousClass9 implements View.OnClickListener {
        AnonymousClass9() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m59$$Nest$mplayList(MainActivity.this, MainActivity.m41$$Nest$mcurrentVisibleTracks(MainActivity.this), false);
        }
    }

    class AnonymousClass10 implements View.OnClickListener {
        AnonymousClass10() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m59$$Nest$mplayList(MainActivity.this, MainActivity.m41$$Nest$mcurrentVisibleTracks(MainActivity.this), true);
        }
    }

    class AnonymousClass11 implements View.OnClickListener {
        AnonymousClass11() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m54$$Nest$mopenPicker(MainActivity.this);
        }
    }

    class AnonymousClass12 implements View.OnClickListener {
        AnonymousClass12() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m48$$Nest$mopenAddFavorites(MainActivity.this);
        }
    }

    class AnonymousClass13 implements View.OnClickListener {
        AnonymousClass13() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m57$$Nest$mopenSearch(MainActivity.this);
        }
    }

    class AnonymousClass14 implements View.OnClickListener {
        AnonymousClass14() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m40$$Nest$mcreatePlaylistDialog(MainActivity.this);
        }
    }

    class AnonymousClass15 implements View.OnClickListener {
        AnonymousClass15() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m57$$Nest$mopenSearch(MainActivity.this);
        }
    }

    class AnonymousClass16 implements View.OnClickListener {
        AnonymousClass16() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m22$$Nest$fputdark(MainActivity.this, !MainActivity.m4$$Nest$fgetdark(MainActivity.this));
            MainActivity.m68$$Nest$msaveState(MainActivity.this);
            MainActivity.m32$$Nest$mbuildUi(MainActivity.this);
        }
    }

    private void renderSettings() {
        addSettingsButton(tr(this.dark ? "Switch to light theme" : "Switch to dark theme", this.dark ? "Светлая тема" : "Темная тема"), new AnonymousClass16());
        addSettingsButton(tr(this.animations ? "Turn animations off" : "Turn animations on", this.animations ? "Отключить анимации" : "Включить анимации"), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.animations = !MainActivity.this.animations;
                MainActivity.this.saveState();
                MainActivity.this.render();
            }
        });
        TextView textViewText = text(tr("Language", "Язык"), 18, true);
        textViewText.setGravity(8388627);
        textViewText.setPadding(dp(4), dp(18), dp(4), dp(6));
        this.list.addView(textViewText);
        LinearLayout linearLayoutRow = row();
        Button button = button("English");
        button.setGravity(8388627);
        button.setPadding(dp(18), 0, dp(12), 0);
        applyButtonColors(button, english() ? this.fg : this.bg, english() ? this.bg : this.fg);
        button.setOnClickListener(new AnonymousClass17());
        linearLayoutRow.addView(button, new LinearLayout.LayoutParams(0, dp(56), 1.0f));
        Button button2 = button("Русский");
        button2.setGravity(8388627);
        button2.setPadding(dp(18), 0, dp(12), 0);
        applyButtonColors(button2, english() ? this.bg : this.fg, english() ? this.fg : this.bg);
        button2.setOnClickListener(new AnonymousClass18());
        linearLayoutRow.addView(button2, new LinearLayout.LayoutParams(0, dp(56), 1.0f));
        this.list.addView(spaced(linearLayoutRow));
        addSettingsButton(tr("Delete all songs from app", "Удалить все песни из приложения"), new AnonymousClass20());
        addSettingsButton(tr("Delete all playlists", "Удалить все плейлисты"), new AnonymousClass21());
        addSettingsButton(tr("GitHub project", "GitHub проект"), new AnonymousClass19());
    }

    class AnonymousClass17 implements View.OnClickListener {
        AnonymousClass17() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m23$$Nest$fputlanguage(MainActivity.this, "en");
            MainActivity.m68$$Nest$msaveState(MainActivity.this);
            MainActivity.m32$$Nest$mbuildUi(MainActivity.this);
        }
    }

    class AnonymousClass18 implements View.OnClickListener {
        AnonymousClass18() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m23$$Nest$fputlanguage(MainActivity.this, "ru");
            MainActivity.m68$$Nest$msaveState(MainActivity.this);
            MainActivity.m32$$Nest$mbuildUi(MainActivity.this);
        }
    }

    class AnonymousClass19 implements View.OnClickListener {
        AnonymousClass19() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m52$$Nest$mopenGithub(MainActivity.this);
        }
    }

    class AnonymousClass20 implements View.OnClickListener {
        AnonymousClass20() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m36$$Nest$mconfirmDeleteAllSongs(MainActivity.this);
        }
    }

    class AnonymousClass21 implements View.OnClickListener {
        AnonymousClass21() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m35$$Nest$mconfirmDeleteAllPlaylists(MainActivity.this);
        }
    }

    private void addSettingsButton(String str, View.OnClickListener onClickListener) {
        Button button = button(str);
        button.setTextSize(17.0f);
        button.setGravity(8388627);
        button.setPadding(dp(18), 0, dp(12), 0);
        button.setOnClickListener(onClickListener);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(58));
        layoutParams.setMargins(0, dp(6), 0, dp(6));
        this.list.addView(button, layoutParams);
    }

    private void openGithub() {
        try {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://github.com/dumuzeyn/MP3-player"));
            intent.addCategory("android.intent.category.BROWSABLE");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (Exception e) {
        }
    }

    private void confirmDeleteAllSongs() {
        showConfirmPanel(tr("Delete all songs?", "Удалить все песни?"), tr("Songs will disappear only from this app. Files on the phone will stay untouched.", "Песни исчезнут только из приложения. Файлы на телефоне останутся."), new AnonymousClass22());
    }

    class AnonymousClass22 implements Runnable {
        AnonymousClass22() {
        }

        @Override
        public void run() {
            MainActivity.m73$$Nest$mstopPlaybackAndClearQueue(MainActivity.this);
            MainActivity.m19$$Nest$fgettracks(MainActivity.this).clear();
            MainActivity.m5$$Nest$fgetfavorites(MainActivity.this).clear();
            Iterator it = MainActivity.m14$$Nest$fgetplaylists(MainActivity.this).iterator();
            while (it.hasNext()) {
                ((Playlist) it.next()).uris.clear();
            }
            TrackStore.save(MainActivity.this, MainActivity.m19$$Nest$fgettracks(MainActivity.this));
            MainActivity.m68$$Nest$msaveState(MainActivity.this);
            MainActivity.m67$$Nest$mrender(MainActivity.this);
        }
    }

    private void confirmDeleteAllPlaylists() {
        showConfirmPanel(tr("Delete all playlists?", "Удалить все плейлисты?"), tr("Songs will stay in the app.", "Песни останутся в приложении."), new AnonymousClass23());
    }

    class AnonymousClass23 implements Runnable {
        AnonymousClass23() {
        }

        @Override
        public void run() {
            MainActivity.m14$$Nest$fgetplaylists(MainActivity.this).clear();
            MainActivity.m68$$Nest$msaveState(MainActivity.this);
            MainActivity.m67$$Nest$mrender(MainActivity.this);
        }
    }

    private void stopPlaybackAndClearQueue() {
        this.playbackQueue.clear();
        this.currentIndex = -1;
        this.playing = false;
        this.playbackHandler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(this, (Class<?>) PlayerService.class);
        intent.setAction(PlayerService.ACTION_STOP);
        try {
            startService(intent);
        } catch (Exception e) {
        }
        updateMini();
    }

    private ArrayList<Track> currentVisibleTracks() {
        return this.tabIndex == 1 ? filter(favoriteTracks()) : filter(this.tracks);
    }

    private ArrayList<Track> favoriteTracks() {
        ArrayList<Track> arrayList = new ArrayList<>();
        for (Track track : this.tracks) {
            if (this.favorites.contains(track.uri)) {
                arrayList.add(track);
            }
        }
        return arrayList;
    }

    private ArrayList<Track> filter(ArrayList<Track> arrayList) {
        if (this.search.trim().isEmpty()) {
            return arrayList;
        }
        ArrayList<Track> arrayList2 = new ArrayList<>();
        String lowerCase = this.search.toLowerCase(Locale.ROOT);
        for (Track track : arrayList) {
            if (track.title.toLowerCase(Locale.ROOT).contains(lowerCase) || track.artist.toLowerCase(Locale.ROOT).contains(lowerCase)) {
                arrayList2.add(track);
            }
        }
        return arrayList2;
    }

    private void renderSongs(ArrayList<Track> arrayList) {
        String str;
        String str2;
        if (arrayList.isEmpty()) {
            if (this.tabIndex == 0) {
                str = "Add MP3 or another audio file";
                str2 = "Добавьте MP3 или другой аудиофайл";
            } else {
                str = "Nothing here yet";
                str2 = "Здесь пока пусто";
            }
            TextView textViewText = text(tr(str, str2), 18, true);
            textViewText.setPadding(dp(12), dp(24), dp(12), dp(24));
            this.list.addView(textViewText);
            return;
        }
        for (int i = 0; i < arrayList.size(); i++) {
            this.list.addView(songRow(arrayList.get(i), true, true));
        }
    }

    private View songRow(Track track, boolean z, boolean z2) {
        return songRow(track, z, z2, null);
    }

    private View songRow(Track track, boolean z, boolean z2, Runnable runnable) {
        int iRgb;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.setPadding(dp(10), dp(8), dp(10), dp(8));
        setSurface(linearLayout, isCurrent(track) ? this.fg : this.panel, false);
        ImageView imageViewCoverView = coverView();
        Bitmap bitmapCachedCover = cachedCover(track);
        if (bitmapCachedCover != null) {
            imageViewCoverView.setImageBitmap(bitmapCachedCover);
        } else {
            if (isCurrent(track)) {
                iRgb = this.bg;
            } else {
                int i = this.dark ? 28 : 235;
                iRgb = Color.rgb(i, i, i);
            }
            imageViewCoverView.setBackgroundColor(iRgb);
        }
        imageViewCoverView.setOnClickListener(new AnonymousClass24(this, track));
        linearLayout.addView(imageViewCoverView, square(58));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(dp(12), 0, dp(8), 0);
        TextView textViewText = text(track.title, 17, true);
        textViewText.setTextColor(isCurrent(track) ? this.bg : this.fg);
        textViewText.setSingleLine(true);
        textViewText.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout2.addView(textViewText);
        linearLayout2.addView(wave(track, isCurrent(track)));
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(0, dp(70), 1.0f));
        if (this.tabIndex == 1) {
            Button buttonIcon = icon(this.favorites.contains(track.uri) ? "♥︎" : "♡︎");
            buttonIcon.setTextSize(14.0f);
            applyButtonColors(buttonIcon, isCurrent(track) ? this.fg : this.bg, isCurrent(track) ? this.bg : this.fg);
            buttonIcon.setOnClickListener(new AnonymousClass25(this, track));
            linearLayout.addView(buttonIcon, square(42));
        } else if (z) {
            Button buttonIcon2 = icon("⋯");
            applyButtonColors(buttonIcon2, isCurrent(track) ? this.fg : this.bg, isCurrent(track) ? this.bg : this.fg);
            buttonIcon2.setOnClickListener(new AnonymousClass26(this, track));
            linearLayout.addView(buttonIcon2, square(48));
        }
        Button buttonIcon3 = icon((isCurrent(track) && this.playing) ? "Ⅱ" : "▶");
        applyButtonColors(buttonIcon3, isCurrent(track) ? this.fg : this.bg, isCurrent(track) ? this.bg : this.fg);
        buttonIcon3.setOnClickListener(new AnonymousClass27(this, track, runnable));
        linearLayout.addView(buttonIcon3, square(48));
        return spaced(linearLayout);
    }

    class AnonymousClass24 implements View.OnClickListener {
        final MainActivity this$0;
        final Track val$track;

        AnonymousClass24(MainActivity mainActivity, Track track) {
            this.val$track = track;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m61$$Nest$mplayTrack(this.this$0, this.val$track);
            MainActivity.m51$$Nest$mopenFullPlayer(this.this$0);
        }
    }

    class AnonymousClass25 implements View.OnClickListener {
        final MainActivity this$0;
        final Track val$track;

        AnonymousClass25(MainActivity mainActivity, Track track) {
            this.val$track = track;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m78$$Nest$mtoggleFavorite(this.this$0, this.val$track);
            MainActivity.m67$$Nest$mrender(this.this$0);
        }
    }

    class AnonymousClass26 implements View.OnClickListener {
        final MainActivity this$0;
        final Track val$track;

        AnonymousClass26(MainActivity mainActivity, Track track) {
            this.val$track = track;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m58$$Nest$mopenSongActions(this.this$0, this.val$track);
        }
    }

    class AnonymousClass27 implements View.OnClickListener {
        final MainActivity this$0;
        final Runnable val$afterPlay;
        final Track val$track;

        AnonymousClass27(MainActivity mainActivity, Track track, Runnable runnable) {
            this.val$track = track;
            this.val$afterPlay = runnable;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            if (MainActivity.m45$$Nest$misCurrent(this.this$0, this.val$track)) {
                MainActivity.m77$$Nest$mtoggleCurrent(this.this$0);
            } else {
                MainActivity.m61$$Nest$mplayTrack(this.this$0, this.val$track);
            }
            if (this.val$afterPlay != null) {
                this.val$afterPlay.run();
            }
        }
    }

    private void renderPlaylists() {
        ArrayList<Playlist> arrayList = new ArrayList();
        for (Playlist playlist : this.playlists) {
            if (this.search.trim().isEmpty() || playlist.name.toLowerCase(Locale.ROOT).contains(this.search.toLowerCase(Locale.ROOT))) {
                arrayList.add(playlist);
            }
        }
        if (arrayList.isEmpty()) {
            TextView textViewText = text(tr("No playlists yet", "Плейлистов пока нет"), 18, true);
            textViewText.setPadding(dp(12), dp(24), dp(12), dp(24));
            this.list.addView(textViewText);
            return;
        }
        for (Playlist playlist2 : arrayList) {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(1);
            linearLayout.setPadding(dp(14), dp(12), dp(14), dp(12));
            setSurface(linearLayout, this.panel, false);
            LinearLayout linearLayoutRow = row();
            LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setOrientation(1);
            TextView textViewText2 = text(playlist2.name, 22, true);
            makeMarquee(textViewText2);
            TextView textViewText3 = text(playlist2.uris.size() + " " + tr("songs", "песен"), 13, false);
            linearLayout2.addView(textViewText2);
            linearLayout2.addView(textViewText3);
            linearLayoutRow.addView(linearLayout2, new LinearLayout.LayoutParams(0, -2, 1.0f));
            Button buttonIcon = icon("▶");
            buttonIcon.setOnClickListener(new AnonymousClass28(this, playlist2));
            linearLayoutRow.addView(buttonIcon, square(48));
            Button buttonShuffleButton = shuffleButton();
            buttonShuffleButton.setOnClickListener(new AnonymousClass29(this, playlist2));
            linearLayoutRow.addView(buttonShuffleButton, square(48));
            Button rename = icon("✎");
            rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.renamePlaylistDialog(playlist2);
                }
            });
            linearLayoutRow.addView(rename, square(48));
            Button buttonIcon2 = icon("×");
            buttonIcon2.setOnClickListener(new AnonymousClass30(this, playlist2));
            linearLayoutRow.addView(buttonIcon2, square(48));
            linearLayout.addView(linearLayoutRow);
            LinearLayout linearLayoutRow2 = row();
            ImageView imageViewCoverView = coverView();
            ArrayList<Track> arrayListPlaylistTracks = playlistTracks(playlist2);
            Bitmap bitmapCachedCover = arrayListPlaylistTracks.isEmpty() ? null : cachedCover(arrayListPlaylistTracks.get(0));
            if (bitmapCachedCover != null) {
                imageViewCoverView.setImageBitmap(bitmapCachedCover);
            } else {
                int i = this.dark ? 28 : 235;
                imageViewCoverView.setBackgroundColor(Color.rgb(i, i, i));
            }
            linearLayoutRow2.addView(imageViewCoverView, square(86));
            TextView textViewText4 = text(previewText(arrayListPlaylistTracks), 16, true);
            textViewText4.setPadding(dp(12), 0, 0, 0);
            linearLayoutRow2.addView(textViewText4, new LinearLayout.LayoutParams(0, dp(96), 1.0f));
            linearLayout.addView(linearLayoutRow2);
            linearLayout.setOnClickListener(new AnonymousClass31(this, playlist2));
            this.list.addView(spaced(linearLayout));
        }
    }

    class AnonymousClass28 implements View.OnClickListener {
        final MainActivity this$0;
        final Playlist val$playlist;

        AnonymousClass28(MainActivity mainActivity, Playlist playlist) {
            this.val$playlist = playlist;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m59$$Nest$mplayList(this.this$0, MainActivity.m63$$Nest$mplaylistTracks(this.this$0, this.val$playlist), false);
        }
    }

    class AnonymousClass29 implements View.OnClickListener {
        final MainActivity this$0;
        final Playlist val$playlist;

        AnonymousClass29(MainActivity mainActivity, Playlist playlist) {
            this.val$playlist = playlist;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m59$$Nest$mplayList(this.this$0, MainActivity.m63$$Nest$mplaylistTracks(this.this$0, this.val$playlist), true);
        }
    }

    class AnonymousClass30 implements View.OnClickListener {
        final MainActivity this$0;
        final Playlist val$playlist;

        AnonymousClass30(MainActivity mainActivity, Playlist playlist) {
            this.val$playlist = playlist;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m37$$Nest$mconfirmDeletePlaylist(this.this$0, this.val$playlist);
        }
    }

    class AnonymousClass31 implements View.OnClickListener {
        final MainActivity this$0;
        final Playlist val$playlist;

        AnonymousClass31(MainActivity mainActivity, Playlist playlist) {
            this.val$playlist = playlist;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m55$$Nest$mopenPlaylist(this.this$0, this.val$playlist);
        }
    }

    private String previewText(ArrayList<Track> arrayList) {
        if (arrayList.isEmpty()) {
            return tr("No songs in this playlist yet.", "В плейлисте пока нет песен.");
        }
        StringBuilder sb = new StringBuilder();
        int iMin = Math.min(3, arrayList.size());
        for (int i = 0; i < iMin; i++) {
            if (i > 0) {
                sb.append("\n");
            }
            sb.append(arrayList.get(i).title);
        }
        return sb.toString();
    }

    private ArrayList<Track> playlistTracks(Playlist playlist) {
        ArrayList<Track> arrayList = new ArrayList<>();
        Iterator<String> it = playlist.uris.iterator();
        while (it.hasNext()) {
            Track trackFindTrack = findTrack(it.next());
            if (trackFindTrack != null) {
                arrayList.add(trackFindTrack);
            }
        }
        return arrayList;
    }

    private void renderGroups(String str) {
        for (Map.Entry<String, ArrayList<Track>> entry : groupedTracks().entrySet()) {
            LinearLayout linearLayoutRow = row();
            linearLayoutRow.setPadding(dp(12), dp(12), dp(12), dp(12));
            setSurface(linearLayoutRow, this.panel, false);
            ImageView imageViewCoverView = coverView();
            Bitmap bitmapCachedCover = entry.getValue().isEmpty() ? null : cachedCover(entry.getValue().get(0));
            if (bitmapCachedCover != null) {
                imageViewCoverView.setImageBitmap(bitmapCachedCover);
            } else {
                int i = this.dark ? 28 : 235;
                imageViewCoverView.setBackgroundColor(Color.rgb(i, i, i));
            }
            linearLayoutRow.addView(imageViewCoverView, square(72));
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(1);
            linearLayout.setPadding(dp(12), 0, dp(8), 0);
            TextView textViewText = text(entry.getKey(), 20, true);
            textViewText.setSingleLine(true);
            textViewText.setEllipsize(TextUtils.TruncateAt.END);
            TextView textViewText2 = text(entry.getValue().size() + " " + tr("songs", "песен"), 13, false);
            linearLayout.addView(textViewText);
            linearLayout.addView(textViewText2);
            linearLayoutRow.addView(linearLayout, new LinearLayout.LayoutParams(0, dp(72), 1.0f));
            Button buttonIcon = icon("▶");
            buttonIcon.setOnClickListener(new AnonymousClass32(this, entry));
            linearLayoutRow.addView(buttonIcon, square(52));
            Button buttonShuffleButton = shuffleButton();
            buttonShuffleButton.setOnClickListener(new AnonymousClass33(this, entry));
            linearLayoutRow.addView(buttonShuffleButton, square(52));
            linearLayoutRow.setOnClickListener(new AnonymousClass34(this, entry));
            this.list.addView(spaced(linearLayoutRow));
        }
    }

    class AnonymousClass32 implements View.OnClickListener {
        final MainActivity this$0;
        final Map.Entry val$entry;

        AnonymousClass32(MainActivity mainActivity, Map.Entry entry) {
            this.val$entry = entry;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m59$$Nest$mplayList(this.this$0, (ArrayList) this.val$entry.getValue(), false);
        }
    }

    class AnonymousClass33 implements View.OnClickListener {
        final MainActivity this$0;
        final Map.Entry val$entry;

        AnonymousClass33(MainActivity mainActivity, Map.Entry entry) {
            this.val$entry = entry;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m59$$Nest$mplayList(this.this$0, (ArrayList) this.val$entry.getValue(), true);
        }
    }

    class AnonymousClass34 implements View.OnClickListener {
        final MainActivity this$0;
        final Map.Entry val$entry;

        AnonymousClass34(MainActivity mainActivity, Map.Entry entry) {
            this.val$entry = entry;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m53$$Nest$mopenGroupSongs(this.this$0, (String) this.val$entry.getKey(), (ArrayList) this.val$entry.getValue());
        }
    }

    private Map<String, ArrayList<Track>> groupedTracks() {
        String strCleanGroup;
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (Track track : this.tracks) {
            if (this.tabIndex == 4) {
                strCleanGroup = cleanGroup(track.artist, tr("Unknown artist", "Неизвестный исполнитель"));
            } else {
                strCleanGroup = this.tabIndex == 5 ? cleanGroup(track.album, tr("Unknown album", "Неизвестный альбом")) : cleanGroup(track.genre, tr("Unknown genre", "Неизвестный жанр"));
            }
            if (!linkedHashMap.containsKey(strCleanGroup)) {
                linkedHashMap.put(strCleanGroup, new ArrayList());
            }
            ((ArrayList) linkedHashMap.get(strCleanGroup)).add(track);
        }
        return linkedHashMap;
    }

    private String cleanGroup(String str, String str2) {
        return (str == null || str.trim().isEmpty()) ? str2 : str.trim();
    }

    private void openGroupSongs(String str, ArrayList<Track> arrayList) {
        showPanel(str, arrayList, null);
    }

    class AnonymousClass35 implements PanelAction {
        final MainActivity this$0;
        final Playlist val$playlist;

        AnonymousClass35(MainActivity mainActivity, Playlist playlist) {
            this.val$playlist = playlist;
            this.this$0 = mainActivity;
        }

        @Override
        public void add() {
            MainActivity.m49$$Nest$mopenAddToPlaylist(this.this$0, this.val$playlist);
        }

        @Override
        public void remove(Track track) {
            this.val$playlist.uris.remove(track.uri);
            MainActivity.m68$$Nest$msaveState(this.this$0);
            MainActivity.m67$$Nest$mrender(this.this$0);
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeAllViews();
            MainActivity.m55$$Nest$mopenPlaylist(this.this$0, this.val$playlist);
        }
    }

    private void openPlaylist(Playlist playlist) {
        showPanel(playlist.name, playlistTracks(playlist), new AnonymousClass35(this, playlist));
    }

    private void showPanel(String str, ArrayList<Track> arrayList, PanelAction panelAction) {
        PanelAction panelAction2 = panelAction;
        FrameLayout frameLayoutShade = shade();
        LinearLayout linearLayoutPanelCard = panelCard();
        LinearLayout linearLayoutRow = row();
        TextView panelTitle = text(str, 20, true);
        makeMarquee(panelTitle);
        linearLayoutRow.addView(panelTitle, new LinearLayout.LayoutParams(0, dp(58), 1.0f));
        Button buttonIcon = icon("▶");
        buttonIcon.setOnClickListener(new AnonymousClass36(this, arrayList, frameLayoutShade, str, panelAction));
        linearLayoutRow.addView(buttonIcon, square(52));
        Button buttonShuffleButton = shuffleButton();
        buttonShuffleButton.setOnClickListener(new AnonymousClass37(this, arrayList, frameLayoutShade, str, panelAction));
        linearLayoutRow.addView(buttonShuffleButton, square(52));
        if (panelAction2 != null) {
            Button buttonIcon2 = icon("+");
            buttonIcon2.setOnClickListener(new AnonymousClass38(this, panelAction2));
            linearLayoutRow.addView(buttonIcon2, square(52));
        }
        Button buttonIcon3 = icon("×");
        buttonIcon3.setOnClickListener(new AnonymousClass39(this, frameLayoutShade));
        linearLayoutRow.addView(buttonIcon3, square(52));
        linearLayoutPanelCard.addView(linearLayoutRow);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        for (Track track : arrayList) {
            if (panelAction2 == null) {
                linearLayout.addView(songRow(track, false, true, new AnonymousClass40(this, frameLayoutShade, str, arrayList, panelAction)));
            } else {
                LinearLayout linearLayout2 = new LinearLayout(this);
                linearLayout2.setOrientation(0);
                linearLayout2.setGravity(16);
                linearLayout2.setPadding(dp(10), dp(8), dp(10), dp(8));
                setSurface(linearLayout2, isCurrent(track) ? this.fg : this.panel, false);
                ImageView imageViewCoverView = coverView();
                Bitmap bitmapCachedCover = cachedCover(track);
                if (bitmapCachedCover != null) {
                    imageViewCoverView.setImageBitmap(bitmapCachedCover);
                } else {
                    imageViewCoverView.setBackgroundColor(isCurrent(track) ? this.bg : this.dark ? -16777216 : Color.rgb(235, 235, 235));
                }
                imageViewCoverView.setOnClickListener(new AnonymousClass41(this, track, frameLayoutShade));
                linearLayout2.addView(imageViewCoverView, square(58));
                LinearLayout linearLayout3 = new LinearLayout(this);
                linearLayout3.setOrientation(LinearLayout.VERTICAL);
                linearLayout3.setPadding(dp(12), 0, dp(8), 0);
                TextView textViewText = text(track.title, 17, true);
                textViewText.setSingleLine(true);
                textViewText.setEllipsize(TextUtils.TruncateAt.END);
                textViewText.setTextColor(isCurrent(track) ? this.bg : this.fg);
                linearLayout3.addView(textViewText);
                linearLayout3.addView(wave(track, isCurrent(track)));
                linearLayout2.addView(linearLayout3, new LinearLayout.LayoutParams(0, dp(70), 1.0f));
                Button buttonIcon4 = icon("−");
                applyButtonColors(buttonIcon4, isCurrent(track) ? this.fg : this.bg, isCurrent(track) ? this.bg : this.fg);
                buttonIcon4.setOnClickListener(new AnonymousClass42(this, panelAction2, track));
                linearLayout2.addView(buttonIcon4, square(48));
                Button buttonIcon5 = icon((isCurrent(track) && this.playing) ? "⏸" : "▶");
                applyButtonColors(buttonIcon5, isCurrent(track) ? this.fg : this.bg, isCurrent(track) ? this.bg : this.fg);
                buttonIcon5.setOnClickListener(new AnonymousClass43(this, track, frameLayoutShade, str, arrayList, panelAction));
                linearLayout2.addView(buttonIcon5, square(48));
                linearLayout.addView(spaced(linearLayout2));
            }
            panelAction2 = panelAction;
        }
        scrollView.addView(linearLayout);
        linearLayoutPanelCard.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        frameLayoutShade.addView(linearLayoutPanelCard, bottomParams());
        this.overlayHost.addView(frameLayoutShade);
        updateMini();
    }

    class AnonymousClass36 implements View.OnClickListener {
        final MainActivity this$0;
        final PanelAction val$action;
        final FrameLayout val$shade;
        final ArrayList val$source;
        final String val$title;

        AnonymousClass36(MainActivity mainActivity, ArrayList arrayList, FrameLayout frameLayout, String str, PanelAction panelAction) {
            this.val$source = arrayList;
            this.val$shade = frameLayout;
            this.val$title = str;
            this.val$action = panelAction;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m59$$Nest$mplayList(this.this$0, this.val$source, false);
            if (this.val$shade.getParent() != null) {
                MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            }
            MainActivity.m71$$Nest$mshowPanel(this.this$0, this.val$title, this.val$source, this.val$action);
        }
    }

    class AnonymousClass37 implements View.OnClickListener {
        final MainActivity this$0;
        final PanelAction val$action;
        final FrameLayout val$shade;
        final ArrayList val$source;
        final String val$title;

        AnonymousClass37(MainActivity mainActivity, ArrayList arrayList, FrameLayout frameLayout, String str, PanelAction panelAction) {
            this.val$source = arrayList;
            this.val$shade = frameLayout;
            this.val$title = str;
            this.val$action = panelAction;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m59$$Nest$mplayList(this.this$0, this.val$source, true);
            if (this.val$shade.getParent() != null) {
                MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            }
            MainActivity.m71$$Nest$mshowPanel(this.this$0, this.val$title, this.val$source, this.val$action);
        }
    }

    class AnonymousClass38 implements View.OnClickListener {
        final MainActivity this$0;
        final PanelAction val$action;

        AnonymousClass38(MainActivity mainActivity, PanelAction panelAction) {
            this.val$action = panelAction;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            this.val$action.add();
        }
    }

    class AnonymousClass39 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;

        AnonymousClass39(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m80$$Nest$mupdateMini(this.this$0);
        }
    }

    class AnonymousClass40 implements Runnable {
        final MainActivity this$0;
        final PanelAction val$action;
        final FrameLayout val$shade;
        final ArrayList val$source;
        final String val$title;

        AnonymousClass40(MainActivity mainActivity, FrameLayout frameLayout, String str, ArrayList arrayList, PanelAction panelAction) {
            this.val$shade = frameLayout;
            this.val$title = str;
            this.val$source = arrayList;
            this.val$action = panelAction;
            this.this$0 = mainActivity;
        }

        @Override
        public void run() {
            if (this.val$shade.getParent() != null) {
                MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            }
            MainActivity.m71$$Nest$mshowPanel(this.this$0, this.val$title, this.val$source, this.val$action);
        }
    }

    class AnonymousClass41 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;
        final Track val$track;

        AnonymousClass41(MainActivity mainActivity, Track track, FrameLayout frameLayout) {
            this.val$track = track;
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m61$$Nest$mplayTrack(this.this$0, this.val$track);
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m51$$Nest$mopenFullPlayer(this.this$0);
        }
    }

    class AnonymousClass42 implements View.OnClickListener {
        final MainActivity this$0;
        final PanelAction val$action;
        final Track val$track;

        AnonymousClass42(MainActivity mainActivity, PanelAction panelAction, Track track) {
            this.val$action = panelAction;
            this.val$track = track;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            this.val$action.remove(this.val$track);
        }
    }

    class AnonymousClass43 implements View.OnClickListener {
        final MainActivity this$0;
        final PanelAction val$action;
        final FrameLayout val$shade;
        final ArrayList val$source;
        final String val$title;
        final Track val$track;

        AnonymousClass43(MainActivity mainActivity, Track track, FrameLayout frameLayout, String str, ArrayList arrayList, PanelAction panelAction) {
            this.val$track = track;
            this.val$shade = frameLayout;
            this.val$title = str;
            this.val$source = arrayList;
            this.val$action = panelAction;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            if (MainActivity.m45$$Nest$misCurrent(this.this$0, this.val$track)) {
                MainActivity.m77$$Nest$mtoggleCurrent(this.this$0);
            } else {
                MainActivity.m61$$Nest$mplayTrack(this.this$0, this.val$track);
            }
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m71$$Nest$mshowPanel(this.this$0, this.val$title, this.val$source, this.val$action);
        }
    }

    private void openQueuePanel() {
        if (this.playbackQueue.isEmpty() && this.currentIndex >= 0 && this.currentIndex < this.tracks.size()) {
            this.playbackQueue.add(this.tracks.get(this.currentIndex));
        }
        FrameLayout frameLayoutShade = shade();
        LinearLayout linearLayoutPanelCard = panelCard();
        LinearLayout linearLayoutRow = row();
        linearLayoutRow.addView(text(tr("Now playing", "Список проигрывания"), 20, true), new LinearLayout.LayoutParams(0, dp(58), 1.0f));
        Button buttonIcon = icon("+");
        buttonIcon.setOnClickListener(new AnonymousClass44());
        linearLayoutRow.addView(buttonIcon, square(52));
        Button buttonIcon2 = icon("×");
        buttonIcon2.setOnClickListener(new AnonymousClass45(this, frameLayoutShade));
        linearLayoutRow.addView(buttonIcon2, square(52));
        linearLayoutPanelCard.addView(linearLayoutRow);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        for (Track track : new ArrayList<Track>(activeQueue())) {
            LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setOrientation(0);
            linearLayout2.setGravity(16);
            linearLayout2.setPadding(dp(10), dp(8), dp(10), dp(8));
            setSurface(linearLayout2, isCurrent(track) ? this.fg : this.panel, false);
            ImageView imageViewCoverView = coverView();
            Bitmap bitmapCachedCover = cachedCover(track);
            if (bitmapCachedCover != null) {
                imageViewCoverView.setImageBitmap(bitmapCachedCover);
            }
            linearLayout2.addView(imageViewCoverView, square(58));
            TextView textViewText = text(track.title, 17, true);
            textViewText.setPadding(dp(12), 0, dp(8), 0);
            textViewText.setTextColor(isCurrent(track) ? this.bg : this.fg);
            linearLayout2.addView(textViewText, new LinearLayout.LayoutParams(0, dp(70), 1.0f));
            Button buttonIcon3 = icon("−");
            buttonIcon3.setOnClickListener(new AnonymousClass46(this, track, frameLayoutShade));
            linearLayout2.addView(buttonIcon3, square(48));
            Button buttonIcon4 = icon((isCurrent(track) && this.playing) ? "Ⅱ" : "▶");
            buttonIcon4.setOnClickListener(new AnonymousClass47(this, track, frameLayoutShade));
            linearLayout2.addView(buttonIcon4, square(48));
            linearLayout.addView(spaced(linearLayout2));
        }
        scrollView.addView(linearLayout);
        linearLayoutPanelCard.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        frameLayoutShade.addView(linearLayoutPanelCard, bottomParams());
        this.overlayHost.addView(frameLayoutShade);
        updateMini();
    }

    class AnonymousClass44 implements View.OnClickListener {
        AnonymousClass44() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m50$$Nest$mopenAddToQueue(MainActivity.this);
        }
    }

    class AnonymousClass45 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;

        AnonymousClass45(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m80$$Nest$mupdateMini(this.this$0);
        }
    }

    class AnonymousClass46 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;
        final Track val$track;

        AnonymousClass46(MainActivity mainActivity, Track track, FrameLayout frameLayout) {
            this.val$track = track;
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m66$$Nest$mremoveFromQueue(this.this$0, this.val$track);
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m56$$Nest$mopenQueuePanel(this.this$0);
        }
    }

    class AnonymousClass47 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;
        final Track val$track;

        AnonymousClass47(MainActivity mainActivity, Track track, FrameLayout frameLayout) {
            this.val$track = track;
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m60$$Nest$mplayQueueTrack(this.this$0, this.val$track);
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m56$$Nest$mopenQueuePanel(this.this$0);
        }
    }

    class AnonymousClass48 implements PickDone {
        AnonymousClass48() {
        }

        @Override
        public void done(Set<String> set) {
            if (MainActivity.m12$$Nest$fgetplaybackQueue(MainActivity.this).isEmpty() && MainActivity.m2$$Nest$fgetcurrentIndex(MainActivity.this) >= 0 && MainActivity.m2$$Nest$fgetcurrentIndex(MainActivity.this) < MainActivity.m19$$Nest$fgettracks(MainActivity.this).size()) {
                MainActivity.m12$$Nest$fgetplaybackQueue(MainActivity.this).add((Track) MainActivity.m19$$Nest$fgettracks(MainActivity.this).get(MainActivity.m2$$Nest$fgetcurrentIndex(MainActivity.this)));
            }
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                Track trackM43$$Nest$mfindTrack = MainActivity.m43$$Nest$mfindTrack(MainActivity.this, it.next());
                if (trackM43$$Nest$mfindTrack != null && !MainActivity.m46$$Nest$misInPlaybackQueue(MainActivity.this, trackM43$$Nest$mfindTrack)) {
                    MainActivity.m12$$Nest$fgetplaybackQueue(MainActivity.this).add(trackM43$$Nest$mfindTrack);
                }
            }
            MainActivity.m9$$Nest$fgetoverlayHost(MainActivity.this).removeAllViews();
            MainActivity.m56$$Nest$mopenQueuePanel(MainActivity.this);
        }
    }

    private void openAddToQueue() {
        showPickPanel(tr("Add to queue", "Добавить в список"), new HashSet<>(), new AnonymousClass48());
    }

    private void removeFromQueue(Track track) {
        for (int size = this.playbackQueue.size() - 1; size >= 0; size--) {
            if (this.playbackQueue.get(size).uri.equals(track.uri)) {
                this.playbackQueue.remove(size);
            }
        }
        if (isCurrent(track)) {
            Intent intent = new Intent(this, (Class<?>) PlayerService.class);
            intent.setAction(PlayerService.ACTION_STOP);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            this.currentIndex = -1;
            this.playing = false;
        }
    }

    private void playQueueTrack(Track track) {
        int iQueueIndexOf = queueIndexOf(track);
        if (iQueueIndexOf < 0) {
            return;
        }
        this.currentIndex = this.tracks.indexOf(track);
        this.playing = true;
        startServiceAction(PlayerService.ACTION_PLAY_INDEX, iQueueIndexOf, false);
        startPlaybackWatcher();
        render();
    }

    private boolean isInPlaybackQueue(Track track) {
        Iterator<Track> it = this.playbackQueue.iterator();
        while (it.hasNext()) {
            if (it.next().uri.equals(track.uri)) {
                return true;
            }
        }
        return false;
    }

    class AnonymousClass49 implements PickDone {
        AnonymousClass49() {
        }

        @Override
        public void done(Set<String> set) {
            MainActivity.m5$$Nest$fgetfavorites(MainActivity.this).addAll(set);
            MainActivity.m68$$Nest$msaveState(MainActivity.this);
            MainActivity.m67$$Nest$mrender(MainActivity.this);
        }
    }

    private void openAddFavorites() {
        showPickPanel(tr("Add to favorites", "Добавить в избранное"), new HashSet<>(), new AnonymousClass49());
    }

    class AnonymousClass50 implements PickDone {
        final MainActivity this$0;
        final Playlist val$playlist;

        AnonymousClass50(MainActivity mainActivity, Playlist playlist) {
            this.val$playlist = playlist;
            this.this$0 = mainActivity;
        }

        @Override
        public void done(Set<String> set) {
            for (String str : set) {
                if (!this.val$playlist.uris.contains(str)) {
                    this.val$playlist.uris.add(str);
                }
            }
            MainActivity.m68$$Nest$msaveState(this.this$0);
            MainActivity.m67$$Nest$mrender(this.this$0);
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeAllViews();
            MainActivity.m55$$Nest$mopenPlaylist(this.this$0, this.val$playlist);
        }
    }

    private void openAddToPlaylist(Playlist playlist) {
        showPickPanel(tr("Add to ", "Добавить в ") + playlist.name, new HashSet<>(), new AnonymousClass50(this, playlist));
    }

    private void showPickPanel(String str, HashSet<String> hashSet, PickDone pickDone) {
        FrameLayout frameLayoutShade = shade();
        LinearLayout linearLayoutPanelCard = panelCard();
        LinearLayout linearLayoutRow = row();
        linearLayoutRow.addView(text(str, 20, true), new LinearLayout.LayoutParams(0, dp(58), 1.0f));
        Button buttonIcon = icon("+");
        buttonIcon.setOnClickListener(new AnonymousClass51(this, frameLayoutShade, pickDone, hashSet));
        linearLayoutRow.addView(buttonIcon, square(52));
        Button buttonIcon2 = icon("×");
        buttonIcon2.setOnClickListener(new AnonymousClass52(this, frameLayoutShade));
        linearLayoutRow.addView(buttonIcon2, square(52));
        linearLayoutPanelCard.addView(linearLayoutRow);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        Iterator<Track> it = this.tracks.iterator();
        while (it.hasNext()) {
            linearLayout.addView(pickSongRow(it.next(), hashSet));
        }
        scrollView.addView(linearLayout);
        linearLayoutPanelCard.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        frameLayoutShade.addView(linearLayoutPanelCard, bottomParams());
        this.overlayHost.addView(frameLayoutShade);
        updateMini();
    }

    class AnonymousClass51 implements View.OnClickListener {
        final MainActivity this$0;
        final PickDone val$done;
        final HashSet val$selected;
        final FrameLayout val$shade;

        AnonymousClass51(MainActivity mainActivity, FrameLayout frameLayout, PickDone pickDone, HashSet hashSet) {
            this.val$shade = frameLayout;
            this.val$done = pickDone;
            this.val$selected = hashSet;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            this.val$done.done(this.val$selected);
            MainActivity.m80$$Nest$mupdateMini(this.this$0);
        }
    }

    class AnonymousClass52 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;

        AnonymousClass52(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m80$$Nest$mupdateMini(this.this$0);
        }
    }

    private View pickSongRow(Track track, HashSet<String> hashSet) {
        int iRgb;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.setPadding(dp(10), dp(8), dp(10), dp(8));
        setSurface(linearLayout, hashSet.contains(track.uri) ? this.fg : this.panel, false);
        ImageView imageViewCoverView = coverView();
        Bitmap bitmapCachedCover = cachedCover(track);
        if (bitmapCachedCover != null) {
            imageViewCoverView.setImageBitmap(bitmapCachedCover);
        } else {
            if (hashSet.contains(track.uri)) {
                iRgb = this.bg;
            } else {
                int i = this.dark ? 28 : 235;
                iRgb = Color.rgb(i, i, i);
            }
            imageViewCoverView.setBackgroundColor(iRgb);
        }
        linearLayout.addView(imageViewCoverView, square(58));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(dp(12), 0, dp(8), 0);
        TextView textViewText = text(track.title, 17, true);
        textViewText.setTextColor(hashSet.contains(track.uri) ? this.bg : this.fg);
        textViewText.setSingleLine(true);
        textViewText.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout2.addView(textViewText);
        linearLayout2.addView(wave(track, hashSet.contains(track.uri)));
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(0, dp(70), 1.0f));
        Button buttonIcon = icon(hashSet.contains(track.uri) ? "✔" : "+");
        buttonIcon.setOnClickListener(new AnonymousClass53(this, hashSet, track, linearLayout, textViewText, buttonIcon));
        applyButtonColors(buttonIcon, hashSet.contains(track.uri) ? this.fg : this.bg, hashSet.contains(track.uri) ? this.bg : this.fg);
        linearLayout.addView(buttonIcon, square(48));
        Button buttonIcon2 = icon((isCurrent(track) && this.playing) ? "Ⅱ" : "▶");
        buttonIcon2.setTag(track.uri);
        buttonIcon2.setOnClickListener(new AnonymousClass54(this, track));
        linearLayout.addView(buttonIcon2, square(48));
        return spaced(linearLayout);
    }

    class AnonymousClass53 implements View.OnClickListener {
        final MainActivity this$0;
        final Button val$mark;
        final LinearLayout val$row;
        final HashSet val$selected;
        final TextView val$title;
        final Track val$track;

        AnonymousClass53(MainActivity mainActivity, HashSet hashSet, Track track, LinearLayout linearLayout, TextView textView, Button button) {
            this.val$selected = hashSet;
            this.val$track = track;
            this.val$row = linearLayout;
            this.val$title = textView;
            this.val$mark = button;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            if (this.val$selected.contains(this.val$track.uri)) {
                this.val$selected.remove(this.val$track.uri);
            } else {
                this.val$selected.add(this.val$track.uri);
            }
            boolean zContains = this.val$selected.contains(this.val$track.uri);
            MainActivity mainActivity = this.this$0;
            LinearLayout linearLayout = this.val$row;
            MainActivity mainActivity2 = this.this$0;
            MainActivity.m70$$Nest$msetSurface(mainActivity, linearLayout, zContains ? MainActivity.m6$$Nest$fgetfg(mainActivity2) : MainActivity.m10$$Nest$fgetpanel(mainActivity2), false);
            TextView textView = this.val$title;
            MainActivity mainActivity3 = this.this$0;
            textView.setTextColor(zContains ? MainActivity.m0$$Nest$fgetbg(mainActivity3) : MainActivity.m6$$Nest$fgetfg(mainActivity3));
            this.val$mark.setText(zContains ? "✔" : "+");
            MainActivity mainActivity4 = this.this$0;
            Button button = this.val$mark;
            MainActivity mainActivity5 = this.this$0;
            MainActivity.m31$$Nest$mapplyButtonColors(mainActivity4, button, zContains ? MainActivity.m6$$Nest$fgetfg(mainActivity5) : MainActivity.m0$$Nest$fgetbg(mainActivity5), zContains ? MainActivity.m0$$Nest$fgetbg(this.this$0) : MainActivity.m6$$Nest$fgetfg(this.this$0));
        }
    }

    class AnonymousClass54 implements View.OnClickListener {
        final MainActivity this$0;
        final Track val$track;

        AnonymousClass54(MainActivity mainActivity, Track track) {
            this.val$track = track;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m62$$Nest$mplayTrack(this.this$0, this.val$track, false);
            this.this$0.syncPickPlayButtons(this.val$track);
        }
    }

    private void syncPickPlayButtons(Track activeTrack) {
        syncPickPlayButtons(this.overlayHost, activeTrack == null ? "" : activeTrack.uri);
    }

    private void syncPickPlayButtons(View view, String activeUri) {
        if (view == null) {
            return;
        }
        if (view instanceof Button && ((Button) view).getTag() instanceof String) {
            String uri = (String) ((Button) view).getTag();
            ((Button) view).setText(uri.equals(activeUri) && this.playing ? "Ⅱ" : "▶");
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                syncPickPlayButtons(group.getChildAt(i), activeUri);
            }
        }
    }

    private void openSongActions(Track track) {
        String str;
        String str2;
        FrameLayout frameLayoutShade = shade();
        LinearLayout linearLayoutPanelCard = panelCard();
        linearLayoutPanelCard.addView(text(track.title, 20, true), new LinearLayout.LayoutParams(-1, dp(52)));
        if (this.favorites.contains(track.uri)) {
            str = "? Remove from favorites";
            str2 = "♥ Убрать из избранного";
        } else {
            str = "? Add to favorites";
            str2 = "♡ Добавить в избранное";
        }
        Button button = button(tr(str, str2));
        button.setOnClickListener(new AnonymousClass55(this, track, frameLayoutShade));
        linearLayoutPanelCard.addView(button, new LinearLayout.LayoutParams(-1, dp(54)));
        Button button2 = button(tr("+ Add to playlist", "+ Добавить в плейлист"));
        button2.setOnClickListener(new AnonymousClass56(this, frameLayoutShade, track));
        linearLayoutPanelCard.addView(button2, new LinearLayout.LayoutParams(-1, dp(54)));
        Button button3 = button(tr("? Remove from app", "× Удалить из приложения"));
        button3.setOnClickListener(new AnonymousClass57(this, frameLayoutShade, track));
        linearLayoutPanelCard.addView(button3, new LinearLayout.LayoutParams(-1, dp(54)));
        Button button4 = button(tr("Close", "Закрыть"));
        button4.setOnClickListener(new AnonymousClass58(this, frameLayoutShade));
        linearLayoutPanelCard.addView(button4, new LinearLayout.LayoutParams(-1, dp(54)));
        frameLayoutShade.addView(linearLayoutPanelCard, bottomParams());
        this.overlayHost.addView(frameLayoutShade);
        updateMini();
    }

    class AnonymousClass55 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;
        final Track val$track;

        AnonymousClass55(MainActivity mainActivity, Track track, FrameLayout frameLayout) {
            this.val$track = track;
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m78$$Nest$mtoggleFavorite(this.this$0, this.val$track);
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m67$$Nest$mrender(this.this$0);
        }
    }

    class AnonymousClass56 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;
        final Track val$track;

        AnonymousClass56(MainActivity mainActivity, FrameLayout frameLayout, Track track) {
            this.val$shade = frameLayout;
            this.val$track = track;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m34$$Nest$mchoosePlaylistForTrack(this.this$0, this.val$track);
        }
    }

    class AnonymousClass57 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;
        final Track val$track;

        AnonymousClass57(MainActivity mainActivity, FrameLayout frameLayout, Track track) {
            this.val$shade = frameLayout;
            this.val$track = track;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m38$$Nest$mconfirmDeleteTrack(this.this$0, this.val$track);
        }
    }

    class AnonymousClass58 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;

        AnonymousClass58(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m80$$Nest$mupdateMini(this.this$0);
        }
    }

    private void choosePlaylistForTrack(Track track) {
        FrameLayout frameLayoutShade = shade();
        LinearLayout linearLayoutPanelCard = panelCard();
        linearLayoutPanelCard.setPadding(dp(16), dp(16), dp(16), dp(16));
        linearLayoutPanelCard.addView(text(tr("Add to playlist", "Добавить в плейлист"), 22, true), new LinearLayout.LayoutParams(-1, dp(48)));
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        for (Playlist playlist : this.playlists) {
            Button button = button(playlist.name);
            button.setOnClickListener(new AnonymousClass59(this, playlist, track, frameLayoutShade));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(52));
            layoutParams.setMargins(0, dp(4), 0, dp(4));
            linearLayout.addView(button, layoutParams);
        }
        Button button2 = button(tr("Create new", "Создать новый"));
        applyButtonColors(button2, this.fg, this.bg);
        button2.setOnClickListener(new AnonymousClass60(this, frameLayoutShade, track));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, dp(52));
        layoutParams2.setMargins(0, dp(8), 0, 0);
        linearLayout.addView(button2, layoutParams2);
        scrollView.addView(linearLayout);
        linearLayoutPanelCard.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        frameLayoutShade.addView(linearLayoutPanelCard, centerParams(dp(330), dp(420)));
        this.overlayHost.addView(frameLayoutShade);
        updateMini();
    }

    class AnonymousClass59 implements View.OnClickListener {
        final MainActivity this$0;
        final Playlist val$playlist;
        final FrameLayout val$shade;
        final Track val$track;

        AnonymousClass59(MainActivity mainActivity, Playlist playlist, Track track, FrameLayout frameLayout) {
            this.val$playlist = playlist;
            this.val$track = track;
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            if (!this.val$playlist.uris.contains(this.val$track.uri)) {
                this.val$playlist.uris.add(this.val$track.uri);
            }
            MainActivity.m68$$Nest$msaveState(this.this$0);
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m67$$Nest$mrender(this.this$0);
        }
    }

    class AnonymousClass60 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;
        final Track val$track;

        AnonymousClass60(MainActivity mainActivity, FrameLayout frameLayout, Track track) {
            this.val$shade = frameLayout;
            this.val$track = track;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m39$$Nest$mcreatePlaylistAndAdd(this.this$0, this.val$track);
        }
    }

    class AnonymousClass61 implements InputDone {
        final MainActivity this$0;
        final Track val$track;

        AnonymousClass61(MainActivity mainActivity, Track track) {
            this.val$track = track;
            this.this$0 = mainActivity;
        }

        @Override
        public void done(String str) {
            String strTrim = MainActivity.this.cleanPlaylistName(str);
            if (strTrim.isEmpty()) {
                strTrim = MainActivity.m79$$Nest$mtr(this.this$0, "Playlist", "Плейлист");
            }
            Playlist playlist = new Playlist(strTrim);
            playlist.uris.add(this.val$track.uri);
            MainActivity.m14$$Nest$fgetplaylists(this.this$0).add(playlist);
            MainActivity.m68$$Nest$msaveState(this.this$0);
            MainActivity.m67$$Nest$mrender(this.this$0);
        }
    }

    private void createPlaylistAndAdd(Track track) {
        showInputPanel(tr("New playlist", "Новый плейлист"), tr("Playlist name", "Название плейлиста"), "", false, new AnonymousClass61(this, track));
    }

    private void confirmDeleteTrack(Track track) {
        showConfirmPanel("Удалить песню?", "Песня исчезнет из приложения, но файл останется на телефоне.", new AnonymousClass62(this, track));
    }

    class AnonymousClass62 implements Runnable {
        final MainActivity this$0;
        final Track val$track;

        AnonymousClass62(MainActivity mainActivity, Track track) {
            this.val$track = track;
            this.this$0 = mainActivity;
        }

        @Override
        public void run() {
            MainActivity.m19$$Nest$fgettracks(this.this$0).remove(this.val$track);
            MainActivity.m5$$Nest$fgetfavorites(this.this$0).remove(this.val$track.uri);
            Iterator it = MainActivity.m14$$Nest$fgetplaylists(this.this$0).iterator();
            while (it.hasNext()) {
                ((Playlist) it.next()).uris.remove(this.val$track.uri);
            }
            TrackStore.save(this.this$0, MainActivity.m19$$Nest$fgettracks(this.this$0));
            MainActivity.m68$$Nest$msaveState(this.this$0);
            MainActivity.m67$$Nest$mrender(this.this$0);
        }
    }

    private void confirmDeletePlaylist(Playlist playlist) {
        showConfirmPanel("Удалить плейлист?", "Песни останутся в приложении.", new AnonymousClass63(this, playlist));
    }

    class AnonymousClass63 implements Runnable {
        final MainActivity this$0;
        final Playlist val$playlist;

        AnonymousClass63(MainActivity mainActivity, Playlist playlist) {
            this.val$playlist = playlist;
            this.this$0 = mainActivity;
        }

        @Override
        public void run() {
            MainActivity.m14$$Nest$fgetplaylists(this.this$0).remove(this.val$playlist);
            MainActivity.m68$$Nest$msaveState(this.this$0);
            MainActivity.m67$$Nest$mrender(this.this$0);
        }
    }

    class AnonymousClass64 implements InputDone {
        AnonymousClass64() {
        }

        @Override
        public void done(String str) {
            String strTrim = MainActivity.this.cleanPlaylistName(str);
            ArrayList arrayListM14$$Nest$fgetplaylists = MainActivity.m14$$Nest$fgetplaylists(MainActivity.this);
            if (strTrim.isEmpty()) {
                strTrim = MainActivity.m79$$Nest$mtr(MainActivity.this, "Playlist", "Плейлист");
            }
            arrayListM14$$Nest$fgetplaylists.add(new Playlist(strTrim));
            MainActivity.m68$$Nest$msaveState(MainActivity.this);
            MainActivity.m67$$Nest$mrender(MainActivity.this);
        }
    }

    private void createPlaylistDialog() {
        showInputPanel(tr("Create playlist", "Создать плейлист"), tr("Playlist name", "Название плейлиста"), "", false, new AnonymousClass64());
    }

    private void renamePlaylistDialog(final Playlist playlist) {
        showInputPanel(tr("Rename playlist", "Переименовать плейлист"), tr("Playlist name", "Название плейлиста"), playlist.name, false, new InputDone() {
            @Override
            public void done(String value) {
                String name = cleanPlaylistName(value);
                playlist.name = name.isEmpty() ? tr("Playlist", "Плейлист") : name;
                saveState();
                render();
            }
        });
    }

    private String cleanPlaylistName(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.replaceAll("[\\p{Cntrl}&&[^\n\t]]", "").replace('\n', ' ').replace('\t', ' ').trim();
        return cleaned.length() > 80 ? cleaned.substring(0, 80).trim() : cleaned;
    }

    private void openSearch() {
        FrameLayout frameLayoutShade = shade();
        LinearLayout linearLayoutPanelCard = panelCard();
        linearLayoutPanelCard.setPadding(dp(16), dp(16), dp(16), dp(16));
        linearLayoutPanelCard.addView(text(tr("Search", "Поиск"), 22, true), new LinearLayout.LayoutParams(-1, dp(48)));
        EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setText(this.search);
        editText.setHint(tr("Find", "Найти"));
        editText.setTextColor(this.fg);
        editText.setHintTextColor(this.muted);
        editText.setTextSize(18.0f);
        editText.setPadding(dp(14), 0, dp(14), 0);
        editText.setInputType(1);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(80)});
        setSurface(editText, this.panel, true);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(58));
        layoutParams.setMargins(0, dp(10), 0, dp(16));
        linearLayoutPanelCard.addView(editText, layoutParams);
        LinearLayout linearLayoutRow = row();
        Button button = button(tr("Reset", "Сброс"));
        button.setOnClickListener(new AnonymousClass65(this, frameLayoutShade));
        linearLayoutRow.addView(button, new LinearLayout.LayoutParams(0, dp(54), 1.0f));
        Button button2 = button(tr("Find", "Найти"));
        applyButtonColors(button2, this.fg, this.bg);
        button2.setOnClickListener(new AnonymousClass66(this, editText, frameLayoutShade));
        linearLayoutRow.addView(button2, new LinearLayout.LayoutParams(0, dp(54), 1.0f));
        linearLayoutPanelCard.addView(linearLayoutRow);
        frameLayoutShade.addView(linearLayoutPanelCard, centerParams(dp(330), dp(230)));
        this.overlayHost.addView(frameLayoutShade);
        editText.requestFocus();
        updateMini();
    }

    class AnonymousClass65 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;

        AnonymousClass65(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m26$$Nest$fputsearch(this.this$0, "");
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m67$$Nest$mrender(this.this$0);
        }
    }

    class AnonymousClass66 implements View.OnClickListener {
        final MainActivity this$0;
        final EditText val$input;
        final FrameLayout val$shade;

        AnonymousClass66(MainActivity mainActivity, EditText editText, FrameLayout frameLayout) {
            this.val$input = editText;
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m26$$Nest$fputsearch(this.this$0, this.val$input.getText().toString());
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m67$$Nest$mrender(this.this$0);
        }
    }

    private void showInputPanel(String str, String str2, String str3, boolean z, InputDone inputDone) {
        FrameLayout frameLayoutShade = shade();
        LinearLayout linearLayoutPanelCard = panelCard();
        linearLayoutPanelCard.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout linearLayoutRow = row();
        linearLayoutRow.addView(text(str, 22, true), new LinearLayout.LayoutParams(0, dp(58), 1.0f));
        Button buttonIcon = icon("×");
        buttonIcon.setOnClickListener(new AnonymousClass67(this, frameLayoutShade));
        linearLayoutRow.addView(buttonIcon, square(52));
        linearLayoutPanelCard.addView(linearLayoutRow);
        EditText editText = new EditText(this);
        editText.setSingleLine(true);
        editText.setText(str3);
        editText.setHint(str2);
        editText.setTextColor(this.fg);
        editText.setHintTextColor(this.muted);
        editText.setTextSize(18.0f);
        editText.setPadding(dp(14), 0, dp(14), 0);
        editText.setInputType(z ? 2 : 1);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(z ? 3 : 80)});
        setSurface(editText, this.panel, true);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(58));
        layoutParams.setMargins(0, dp(10), 0, dp(16));
        linearLayoutPanelCard.addView(editText, layoutParams);
        LinearLayout linearLayoutRow2 = row();
        Button button = button(tr("Cancel", "Отмена"));
        button.setOnClickListener(new AnonymousClass68(this, frameLayoutShade));
        linearLayoutRow2.addView(button, new LinearLayout.LayoutParams(0, dp(54), 1.0f));
        Button button2 = button(tr("Done", "Готово"));
        applyButtonColors(button2, this.fg, this.bg);
        button2.setOnClickListener(new AnonymousClass69(this, editText, frameLayoutShade, inputDone));
        linearLayoutRow2.addView(button2, new LinearLayout.LayoutParams(0, dp(54), 1.0f));
        linearLayoutPanelCard.addView(linearLayoutRow2);
        frameLayoutShade.addView(linearLayoutPanelCard, centerParams(dp(330), dp(230)));
        this.overlayHost.addView(frameLayoutShade);
        editText.requestFocus();
    }

    class AnonymousClass67 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;

        AnonymousClass67(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m80$$Nest$mupdateMini(this.this$0);
        }
    }

    class AnonymousClass68 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;

        AnonymousClass68(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m80$$Nest$mupdateMini(this.this$0);
        }
    }

    class AnonymousClass69 implements View.OnClickListener {
        final MainActivity this$0;
        final InputDone val$done;
        final EditText val$input;
        final FrameLayout val$shade;

        AnonymousClass69(MainActivity mainActivity, EditText editText, FrameLayout frameLayout, InputDone inputDone) {
            this.val$input = editText;
            this.val$shade = frameLayout;
            this.val$done = inputDone;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            String string = this.val$input.getText().toString();
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            this.val$done.done(string);
            MainActivity.m80$$Nest$mupdateMini(this.this$0);
        }
    }

    private void openFullPlayer() {
        if (this.currentIndex < 0 && !this.tracks.isEmpty()) {
            this.currentIndex = 0;
        }
        if (this.currentIndex < 0) {
            return;
        }
        this.miniPlayer.setVisibility(8);
        Track track = this.tracks.get(this.currentIndex);
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(this.bg);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(16), dp(18), dp(16), dp(20));
        frameLayout.addView(linearLayout, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout linearLayoutRow = row();
        Button buttonIcon = icon("←");
        buttonIcon.setTextSize(34.0f);
        buttonIcon.setTypeface(Typeface.DEFAULT_BOLD);
        buttonIcon.setOnClickListener(new AnonymousClass70(this, frameLayout));
        linearLayoutRow.addView(buttonIcon, square(58));
        linearLayoutRow.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1.0f));
        Button buttonIcon2 = icon("☰");
        buttonIcon2.setOnClickListener(new AnonymousClass71());
        linearLayoutRow.addView(buttonIcon2, square(58));
        linearLayout.addView(linearLayoutRow, new LinearLayout.LayoutParams(-1, dp(72)));
        ImageView imageViewCoverView = coverView();
        Bitmap bitmapCover = cover(track);
        if (bitmapCover != null) {
            imageViewCoverView.setImageBitmap(bitmapCover);
        } else {
            imageViewCoverView.setBackgroundColor(this.dark ? Color.rgb(28, 28, 28) : Color.rgb(235, 235, 235));
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(280), dp(280));
        layoutParams.gravity = 1;
        linearLayout.addView(imageViewCoverView, layoutParams);
        TextView textViewText = text(track.title, 24, true);
        textViewText.setGravity(17);
        linearLayout.addView(textViewText, new LinearLayout.LayoutParams(-1, dp(54)));
        TextView textViewText2 = text(track.artist + " ? " + (queueIndexOf(track) + 1) + " " + tr("of", "из") + " " + (this.playbackQueue.isEmpty() ? this.tracks : this.playbackQueue).size(), 15, false);
        textViewText2.setGravity(17);
        linearLayout.addView(textViewText2, new LinearLayout.LayoutParams(-1, dp(34)));
        LinearLayout linearLayoutRow2 = row();
        Button button = button(timerButtonText());
        button.setOnClickListener(new AnonymousClass72());
        linearLayoutRow2.addView(button, new LinearLayout.LayoutParams(0, dp(58), 1.0f));
        Button button2 = button(tr("? Like", this.favorites.contains(track.uri) ? "♥︎ Лайк" : "♡︎ Лайк"));
        button2.setOnClickListener(new AnonymousClass73(this, track, frameLayout));
        linearLayoutRow2.addView(button2, new LinearLayout.LayoutParams(0, dp(58), 1.0f));
        Button button3 = button(loopLabel());
        button3.setOnClickListener(new AnonymousClass74(this, frameLayout));
        linearLayoutRow2.addView(button3, new LinearLayout.LayoutParams(0, dp(58), 1.0f));
        linearLayout.addView(linearLayoutRow2);
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(Math.max(1, PlayerService.lastDuration));
        seekBar.setProgress(Math.max(0, PlayerService.lastPosition));
        linearLayout.addView(seekBar, new LinearLayout.LayoutParams(-1, dp(42)));
        LinearLayout linearLayoutRow3 = row();
        TextView textViewText3 = text(formatMs(PlayerService.lastPosition), 13, false);
        TextView textViewText4 = text("-" + formatMs(Math.max(0, PlayerService.lastDuration - PlayerService.lastPosition)), 13, false);
        textViewText4.setGravity(TAB_CYCLES);
        linearLayoutRow3.addView(textViewText3, new LinearLayout.LayoutParams(0, dp(28), 1.0f));
        linearLayoutRow3.addView(textViewText4, new LinearLayout.LayoutParams(0, dp(28), 1.0f));
        linearLayout.addView(linearLayoutRow3);
        seekBar.setOnSeekBarChangeListener(new AnonymousClass75(this, textViewText3, textViewText4));
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new AnonymousClass76(this, frameLayout, track, seekBar, textViewText3, textViewText4, button, handler), 700L);
        linearLayout.addView(new View(this), new LinearLayout.LayoutParams(-1, 0, 1.0f));
        LinearLayout linearLayoutRow4 = row();
        linearLayoutRow4.setGravity(17);
        Button buttonIcon3 = icon("⏮");
        buttonIcon3.setOnClickListener(new AnonymousClass77(this, frameLayout));
        linearLayoutRow4.addView(buttonIcon3, square(68));
        Button buttonIcon4 = icon(this.playing ? "Ⅱ" : "▶");
        buttonIcon4.setOnClickListener(new AnonymousClass78(this, frameLayout));
        linearLayoutRow4.addView(buttonIcon4, square(84));
        Button buttonIcon5 = icon("⏭");
        buttonIcon5.setOnClickListener(new AnonymousClass79(this, frameLayout));
        linearLayoutRow4.addView(buttonIcon5, square(68));
        linearLayout.addView(linearLayoutRow4, new LinearLayout.LayoutParams(-1, dp(112)));
        this.overlayHost.addView(frameLayout, new FrameLayout.LayoutParams(-1, -1));
    }

    class AnonymousClass70 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$sheet;

        AnonymousClass70(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$sheet = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$sheet);
            MainActivity.m80$$Nest$mupdateMini(this.this$0);
        }
    }

    class AnonymousClass71 implements View.OnClickListener {
        AnonymousClass71() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m56$$Nest$mopenQueuePanel(MainActivity.this);
        }
    }

    class AnonymousClass72 implements View.OnClickListener {
        AnonymousClass72() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m76$$Nest$mtimerDialog(MainActivity.this);
        }
    }

    class AnonymousClass73 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$sheet;
        final Track val$track;

        AnonymousClass73(MainActivity mainActivity, Track track, FrameLayout frameLayout) {
            this.val$track = track;
            this.val$sheet = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m78$$Nest$mtoggleFavorite(this.this$0, this.val$track);
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$sheet);
            MainActivity.m51$$Nest$mopenFullPlayer(this.this$0);
        }
    }

    class AnonymousClass74 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$sheet;

        AnonymousClass74(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$sheet = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m24$$Nest$fputloopMode(this.this$0, (MainActivity.m8$$Nest$fgetloopMode(this.this$0) + 1) % 3);
            Intent intent = new Intent(this.this$0, (Class<?>) PlayerService.class);
            intent.setAction(PlayerService.ACTION_LOOP);
            intent.putExtra(PlayerService.EXTRA_LOOP_MODE, MainActivity.m8$$Nest$fgetloopMode(this.this$0));
            if (Build.VERSION.SDK_INT >= 26) {
                this.this$0.startForegroundService(intent);
            } else {
                this.this$0.startService(intent);
            }
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$sheet);
            MainActivity.m51$$Nest$mopenFullPlayer(this.this$0);
        }
    }

    class AnonymousClass75 implements SeekBar.OnSeekBarChangeListener {
        final MainActivity this$0;
        final TextView val$elapsed;
        final TextView val$remain;

        AnonymousClass75(MainActivity mainActivity, TextView textView, TextView textView2) {
            this.val$elapsed = textView;
            this.val$remain = textView2;
            this.this$0 = mainActivity;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            if (z) {
                this.val$elapsed.setText(MainActivity.m44$$Nest$mformatMs(this.this$0, i));
                this.val$remain.setText("-" + MainActivity.m44$$Nest$mformatMs(this.this$0, Math.max(0, PlayerService.lastDuration - i)));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Intent intent = new Intent(this.this$0, (Class<?>) PlayerService.class);
            intent.setAction(PlayerService.ACTION_SEEK);
            intent.putExtra(PlayerService.EXTRA_POSITION, seekBar.getProgress());
            if (Build.VERSION.SDK_INT < 26) {
                this.this$0.startService(intent);
            } else {
                this.this$0.startForegroundService(intent);
            }
        }
    }

    class AnonymousClass76 implements Runnable {
        final MainActivity this$0;
        final TextView val$elapsed;
        final Handler val$handler;
        final TextView val$remain;
        final SeekBar val$seek;
        final FrameLayout val$sheet;
        final Button val$timer;
        final Track val$track;

        AnonymousClass76(MainActivity mainActivity, FrameLayout frameLayout, Track track, SeekBar seekBar, TextView textView, TextView textView2, Button button, Handler handler) {
            this.val$sheet = frameLayout;
            this.val$track = track;
            this.val$seek = seekBar;
            this.val$elapsed = textView;
            this.val$remain = textView2;
            this.val$timer = button;
            this.val$handler = handler;
            this.this$0 = mainActivity;
        }

        @Override
        public void run() {
            Track trackM43$$Nest$mfindTrack;
            if (this.val$sheet.getParent() == null) {
                return;
            }
            PlayerService.refreshSnapshot();
            if (PlayerService.lastIndex < 0) {
                MainActivity.m20$$Nest$fputcurrentIndex(this.this$0, -1);
                MainActivity.m25$$Nest$fputplaying(this.this$0, false);
                MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$sheet);
                MainActivity.m80$$Nest$mupdateMini(this.this$0);
                MainActivity.m67$$Nest$mrender(this.this$0);
                return;
            }
            if (PlayerService.lastUri != null && !PlayerService.lastUri.isEmpty() && !PlayerService.lastUri.equals(this.val$track.uri) && (trackM43$$Nest$mfindTrack = MainActivity.m43$$Nest$mfindTrack(this.this$0, PlayerService.lastUri)) != null) {
                MainActivity.m20$$Nest$fputcurrentIndex(this.this$0, MainActivity.m19$$Nest$fgettracks(this.this$0).indexOf(trackM43$$Nest$mfindTrack));
                MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$sheet);
                MainActivity.m51$$Nest$mopenFullPlayer(this.this$0);
                MainActivity.m67$$Nest$mrender(this.this$0);
                return;
            }
            this.val$seek.setMax(Math.max(1, PlayerService.lastDuration));
            this.val$seek.setProgress(Math.max(0, PlayerService.lastPosition));
            this.val$elapsed.setText(MainActivity.m44$$Nest$mformatMs(this.this$0, PlayerService.lastPosition));
            this.val$remain.setText("-" + MainActivity.m44$$Nest$mformatMs(this.this$0, Math.max(0, PlayerService.lastDuration - PlayerService.lastPosition)));
            this.val$timer.setText(MainActivity.m75$$Nest$mtimerButtonText(this.this$0));
            this.val$handler.postDelayed(this, 250L);
        }
    }

    class AnonymousClass77 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$sheet;

        AnonymousClass77(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$sheet = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m64$$Nest$mprevious(this.this$0);
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$sheet);
            MainActivity.m51$$Nest$mopenFullPlayer(this.this$0);
        }
    }

    class AnonymousClass78 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$sheet;

        AnonymousClass78(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$sheet = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m77$$Nest$mtoggleCurrent(this.this$0);
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$sheet);
            MainActivity.m51$$Nest$mopenFullPlayer(this.this$0);
        }
    }

    class AnonymousClass79 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$sheet;

        AnonymousClass79(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$sheet = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m47$$Nest$mnext(this.this$0);
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$sheet);
            MainActivity.m51$$Nest$mopenFullPlayer(this.this$0);
        }
    }

    private String loopLabel() {
        return this.loopMode == 1 ? tr("Repeat: song", "Повтор: песня") : this.loopMode == 2 ? tr("Repeat: list", "Повтор: список") : tr("Repeat: off", "Повтор: выкл");
    }

    private String formatMs(int i) {
        int iMax = Math.max(0, i / 1000);
        return (iMax / 60) + ":" + String.format(Locale.ROOT, "%02d", Integer.valueOf(iMax % 60));
    }

    private String formatSeconds(long j) {
        long jMax = Math.max(0L, j);
        return (jMax / 60) + ":" + String.format(Locale.ROOT, "%02d", Long.valueOf(jMax % 60));
    }

    private void timerDialog() {
        FrameLayout frameLayoutShade = shade();
        LinearLayout linearLayoutPanelCard = panelCard();
        linearLayoutPanelCard.setPadding(dp(16), dp(16), dp(16), dp(16));
        linearLayoutPanelCard.addView(text("Таймер сна", 22, true), new LinearLayout.LayoutParams(-1, dp(46)));
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        int[] iArr = {5, 15, 30, this.customTimerMinutes};
        String[] strArr = {"5 минут", "15 минут", "30 минут", this.customTimerMinutes + " минут"};
        for (int i = 0; i < 4; i++) {
            int i2 = iArr[i];
            Button button = button(strArr[i]);
            button.setOnClickListener(new AnonymousClass80(this, frameLayoutShade, i2));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(50));
            layoutParams.setMargins(0, dp(4), 0, dp(4));
            linearLayout.addView(button, layoutParams);
        }
        Button button2 = button("Свое время");
        button2.setOnClickListener(new AnonymousClass81(this, frameLayoutShade));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, dp(50));
        layoutParams2.setMargins(0, dp(4), 0, dp(4));
        linearLayout.addView(button2, layoutParams2);
        if (this.sleepTimerEndsAt > 0) {
            Button button3 = button("Выключить таймер");
            button3.setOnClickListener(new AnonymousClass82(this, frameLayoutShade));
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, dp(50));
            layoutParams3.setMargins(0, dp(4), 0, 0);
            linearLayout.addView(button3, layoutParams3);
        }
        linearLayoutPanelCard.addView(linearLayout);
        frameLayoutShade.addView(linearLayoutPanelCard, centerParams(dp(330), -2));
        this.overlayHost.addView(frameLayoutShade);
        updateMini();
    }

    class AnonymousClass80 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;
        final int val$value;

        AnonymousClass80(MainActivity mainActivity, FrameLayout frameLayout, int i) {
            this.val$shade = frameLayout;
            this.val$value = i;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m72$$Nest$mstartSleepTimer(this.this$0, this.val$value);
        }
    }

    class AnonymousClass81 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;

        AnonymousClass81(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m42$$Nest$mcustomTimerDialog(this.this$0);
        }
    }

    class AnonymousClass82 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;

        AnonymousClass82(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m33$$Nest$mcancelSleepTimer(this.this$0);
        }
    }

    class AnonymousClass83 implements InputDone {
        AnonymousClass83() {
        }

        @Override
        public void done(String str) {
            try {
                MainActivity.m21$$Nest$fputcustomTimerMinutes(MainActivity.this, Math.max(1, Integer.parseInt(str.trim())));
                MainActivity.m68$$Nest$msaveState(MainActivity.this);
                MainActivity.m72$$Nest$mstartSleepTimer(MainActivity.this, MainActivity.m3$$Nest$fgetcustomTimerMinutes(MainActivity.this));
            } catch (Exception e) {
            }
        }
    }

    private void customTimerDialog() {
        showInputPanel(tr("Custom time", "Свое время"), tr("Minutes", "Минуты"), String.valueOf(this.customTimerMinutes), true, new AnonymousClass83());
    }

    private void startSleepTimer(int i) {
        long j = ((long) i) * 60 * 1000;
        this.sleepTimerEndsAt = System.currentTimeMillis() + j;
        this.sleepHandler.removeCallbacksAndMessages(null);
        this.sleepHandler.postDelayed(new AnonymousClass84(), j);
    }

    class AnonymousClass84 implements Runnable {
        AnonymousClass84() {
        }

        @Override
        public void run() {
            Intent intent = new Intent(MainActivity.this, (Class<?>) PlayerService.class);
            intent.setAction(PlayerService.ACTION_STOP);
            if (Build.VERSION.SDK_INT >= 26) {
                MainActivity.this.startForegroundService(intent);
            } else {
                MainActivity.this.startService(intent);
            }
            MainActivity.m25$$Nest$fputplaying(MainActivity.this, false);
            MainActivity.m20$$Nest$fputcurrentIndex(MainActivity.this, -1);
            MainActivity.m27$$Nest$fputsleepTimerEndsAt(MainActivity.this, 0L);
            MainActivity.m80$$Nest$mupdateMini(MainActivity.this);
            MainActivity.m67$$Nest$mrender(MainActivity.this);
        }
    }

    private void cancelSleepTimer() {
        this.sleepTimerEndsAt = 0L;
        this.sleepHandler.removeCallbacksAndMessages(null);
    }

    private String timerButtonText() {
        if (this.sleepTimerEndsAt <= 0) {
            return tr("Timer", "Таймер");
        }
        long jMax = Math.max(0L, this.sleepTimerEndsAt - System.currentTimeMillis());
        return tr("Timer", "Таймер") + "\n" + formatSeconds((jMax + 999) / 1000);
    }

    private void playTrack(Track track) {
        playTrack(track, true);
    }

    private void playTrack(Track track, boolean z) {
        int iIndexOf = this.tracks.indexOf(track);
        if (iIndexOf < 0) {
            return;
        }
        this.playbackQueue.clear();
        this.playbackQueue.add(track);
        this.currentIndex = iIndexOf;
        this.playing = true;
        startServiceAction(PlayerService.ACTION_PLAY_INDEX, 0, true);
        startPlaybackWatcher();
        updateMini();
        if (z) {
            render();
        }
    }

    private void playList(ArrayList<Track> arrayList, boolean z) {
        if (arrayList.isEmpty()) {
            return;
        }
        ArrayList arrayList2 = new ArrayList(arrayList);
        if (z) {
            Collections.shuffle(arrayList2, new Random());
        }
        int iIndexOf = this.tracks.indexOf((Track) arrayList2.get(0));
        if (iIndexOf < 0) {
            return;
        }
        this.playbackQueue.clear();
        this.playbackQueue.addAll(arrayList2);
        this.currentIndex = iIndexOf;
        this.playing = true;
        startServiceAction(PlayerService.ACTION_PLAY_INDEX, 0, false);
        startPlaybackWatcher();
        render();
    }

    private void toggleCurrent() {
        if (this.currentIndex < 0 && !this.tracks.isEmpty()) {
            playList(this.tracks, false);
            return;
        }
        if (this.currentIndex < 0) {
            return;
        }
        this.playing = !this.playing;
        startServiceAction(PlayerService.ACTION_TOGGLE, this.currentIndex, false);
        startPlaybackWatcher();
        updateMini();
        render();
    }

    private void next() {
        ArrayList<Track> arrayListActiveQueue = activeQueue();
        if (arrayListActiveQueue.isEmpty()) {
            return;
        }
        int iQueueIndexOf = this.currentIndex < 0 ? 0 : (queueIndexOf(this.tracks.get(this.currentIndex)) + 1) % arrayListActiveQueue.size();
        this.currentIndex = this.tracks.indexOf(arrayListActiveQueue.get(iQueueIndexOf));
        this.playing = true;
        startServiceAction(PlayerService.ACTION_PLAY_INDEX, iQueueIndexOf, false);
        startPlaybackWatcher();
        render();
    }

    private void previous() {
        ArrayList<Track> arrayListActiveQueue = activeQueue();
        if (arrayListActiveQueue.isEmpty()) {
            return;
        }
        int iQueueIndexOf = this.currentIndex < 0 ? 0 : queueIndexOf(this.tracks.get(this.currentIndex));
        if (iQueueIndexOf <= 0) {
            iQueueIndexOf = arrayListActiveQueue.size();
        }
        int i = iQueueIndexOf - 1;
        this.currentIndex = this.tracks.indexOf(arrayListActiveQueue.get(i));
        this.playing = true;
        startServiceAction(PlayerService.ACTION_PLAY_INDEX, i, false);
        startPlaybackWatcher();
        render();
    }

    class AnonymousClass85 implements Runnable {
        AnonymousClass85() {
        }

        @Override
        public void run() {
            Track trackM43$$Nest$mfindTrack;
            PlayerService.refreshSnapshot();
            if (PlayerService.lastIndex < 0) {
                MainActivity.m20$$Nest$fputcurrentIndex(MainActivity.this, -1);
                MainActivity.m25$$Nest$fputplaying(MainActivity.this, false);
                MainActivity.m80$$Nest$mupdateMini(MainActivity.this);
                MainActivity.m67$$Nest$mrender(MainActivity.this);
                return;
            }
            if (PlayerService.lastUri != null && !PlayerService.lastUri.isEmpty() && (trackM43$$Nest$mfindTrack = MainActivity.m43$$Nest$mfindTrack(MainActivity.this, PlayerService.lastUri)) != null && !MainActivity.m45$$Nest$misCurrent(MainActivity.this, trackM43$$Nest$mfindTrack)) {
                MainActivity.m20$$Nest$fputcurrentIndex(MainActivity.this, MainActivity.m19$$Nest$fgettracks(MainActivity.this).indexOf(trackM43$$Nest$mfindTrack));
                MainActivity.m67$$Nest$mrender(MainActivity.this);
            } else if (MainActivity.m13$$Nest$fgetplaying(MainActivity.this) || MainActivity.m2$$Nest$fgetcurrentIndex(MainActivity.this) >= 0) {
                MainActivity.m11$$Nest$fgetplaybackHandler(MainActivity.this).postDelayed(this, 900L);
            }
        }
    }

    private void startPlaybackWatcher() {
        this.playbackHandler.removeCallbacksAndMessages(null);
        this.playbackHandler.postDelayed(new AnonymousClass85(), 900L);
    }

    private void startServiceAction(String str, int i) {
        startServiceAction(str, i, false);
    }

    private void startServiceAction(String str, int i, boolean z) {
        Intent intent = new Intent(this, (Class<?>) PlayerService.class);
        intent.setAction(str);
        intent.putExtra(PlayerService.EXTRA_INDEX, i);
        intent.putExtra(PlayerService.EXTRA_ONE_SHOT, z);
        intent.putStringArrayListExtra(PlayerService.EXTRA_QUEUE_URIS, queueUris());
        if (Build.VERSION.SDK_INT < 26) {
            startService(intent);
        } else {
            startForegroundService(intent);
        }
    }

    private ArrayList<String> queueUris() {
        ArrayList<String> arrayList = new ArrayList<>();
        Iterator<Track> it = activeQueue().iterator();
        while (it.hasNext()) {
            arrayList.add(it.next().uri);
        }
        return arrayList;
    }

    private ArrayList<Track> activeQueue() {
        return this.playbackQueue.isEmpty() ? this.tracks : this.playbackQueue;
    }

    private int queueIndexOf(Track track) {
        ArrayList<Track> arrayListActiveQueue = activeQueue();
        for (int i = 0; i < arrayListActiveQueue.size(); i++) {
            if (arrayListActiveQueue.get(i).uri.equals(track.uri)) {
                return i;
            }
        }
        return Math.max(0, this.tracks.indexOf(track));
    }

    private void updateMini() {
        if (this.miniPlayer == null) {
            return;
        }
        if (this.currentIndex < 0 || this.currentIndex >= this.tracks.size() || this.overlayHost.getChildCount() > 0) {
            this.miniPlayer.setVisibility(8);
            return;
        }
        Track track = this.tracks.get(this.currentIndex);
        this.miniTitle.setText(track.title);
        this.miniSub.setText(track.artist);
        this.miniButton.setText(this.playing ? "Ⅱ" : "▶");
        this.miniPlayer.setVisibility(0);
    }

    private void toggleFavorite(Track track) {
        if (this.favorites.contains(track.uri)) {
            this.favorites.remove(track.uri);
        } else {
            this.favorites.add(track.uri);
        }
        saveState();
    }

    private boolean isCurrent(Track track) {
        return this.currentIndex >= 0 && this.currentIndex < this.tracks.size() && this.tracks.get(this.currentIndex).uri.equals(track.uri);
    }

    private Track findTrack(String str) {
        for (Track track : this.tracks) {
            if (track.uri.equals(str)) {
                return track;
            }
        }
        return null;
    }

    private Bitmap cachedCover(Track track) {
        return this.coverCache.get(track.uri);
    }

    class AnonymousClass86 implements Runnable {
        AnonymousClass86() {
        }

        @Override
        public void run() {
            int i = 0;
            for (Track track : new ArrayList<Track>(MainActivity.m19$$Nest$fgettracks(MainActivity.this))) {
                if (!MainActivity.m1$$Nest$fgetcoverCache(MainActivity.this).containsKey(track.uri)) {
                    Bitmap bitmapM65$$Nest$mreadCover = MainActivity.m65$$Nest$mreadCover(MainActivity.this, track);
                    if (bitmapM65$$Nest$mreadCover != null) {
                        synchronized (MainActivity.m1$$Nest$fgetcoverCache(MainActivity.this)) {
                            MainActivity.m1$$Nest$fgetcoverCache(MainActivity.this).put(track.uri, bitmapM65$$Nest$mreadCover);
                        }
                        i++;
                    }
                    if (i > 0 && i % 12 == 0) {
                        MainActivity.this.runOnUiThread(new AnonymousClass1());
                    }
                }
            }
            if (i > 0) {
                MainActivity.this.runOnUiThread(new AnonymousClass2());
            }
        }

        class AnonymousClass1 implements Runnable {
            AnonymousClass1() {
            }

            @Override
            public void run() {
                if (MainActivity.m9$$Nest$fgetoverlayHost(MainActivity.this) == null || MainActivity.m9$$Nest$fgetoverlayHost(MainActivity.this).getChildCount() != 0) {
                    return;
                }
                MainActivity.m67$$Nest$mrender(MainActivity.this);
            }
        }

        class AnonymousClass2 implements Runnable {
            AnonymousClass2() {
            }

            @Override
            public void run() {
                if (MainActivity.m9$$Nest$fgetoverlayHost(MainActivity.this) == null || MainActivity.m9$$Nest$fgetoverlayHost(MainActivity.this).getChildCount() != 0) {
                    return;
                }
                MainActivity.m67$$Nest$mrender(MainActivity.this);
            }
        }
    }

    private void preloadCoverCacheAsync() {
        new Thread(new AnonymousClass86()).start();
    }

    private Bitmap cover(Track track) {
        if (this.coverCache.containsKey(track.uri)) {
            return this.coverCache.get(track.uri);
        }
        Bitmap cover = readCover(track);
        if (cover != null) {
            this.coverCache.put(track.uri, cover);
        }
        return cover;
    }

    private Bitmap readCover(Track track) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(this, track.asUri());
            byte[] embeddedPicture = mediaMetadataRetriever.getEmbeddedPicture();
            if (embeddedPicture == null) {
                try {
                    mediaMetadataRetriever.release();
                } catch (Exception e) {
                }
                return null;
            }
            Bitmap bitmapDecodeByteArray = BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.length);
            try {
                mediaMetadataRetriever.release();
            } catch (Exception e2) {
            }
            return bitmapDecodeByteArray;
        } catch (Exception e3) {
            try {
                mediaMetadataRetriever.release();
            } catch (Exception e4) {
            }
            return null;
        } catch (Throwable th) {
            try {
                mediaMetadataRetriever.release();
            } catch (Exception e5) {
            }
            throw th;
        }
    }

    private View wave(Track track, boolean z) {
        WaveformView waveformView = new WaveformView(this, track.title + track.uri, z ? this.bg : this.fg, z && this.playing);
        waveformView.setMinimumHeight(dp(28));
        waveformView.setPadding(0, dp(3), 0, dp(3));
        waveformView.setLayoutParams(new LinearLayout.LayoutParams(dp(190), dp(30)));
        return waveformView;
    }

    private void openPicker() {
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("audio/*");
        intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
        intent.addFlags(65);
        startActivityForResult(Intent.createChooser(intent, tr("Choose music", "Выберите музыку")), PICK_AUDIO);
    }

    @Override
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i != PICK_AUDIO || i2 != -1 || intent == null) {
            return;
        }
        if (intent.getClipData() != null) {
            for (int i3 = 0; i3 < intent.getClipData().getItemCount(); i3++) {
                addTrack(intent.getClipData().getItemAt(i3).getUri());
            }
        } else if (intent.getData() != null) {
            addTrack(intent.getData());
        }
        TrackStore.sort(this.tracks);
        TrackStore.save(this, this.tracks);
        render();
    }

    private void addTrack(Uri uri) {
        if (!isSafeAudioUri(uri)) {
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(uri, 1);
        } catch (Exception e) {
        }
        String string = uri.toString();
        Iterator<Track> it = this.tracks.iterator();
        while (it.hasNext()) {
            if (it.next().uri.equals(string)) {
                return;
            }
        }
        this.tracks.add(TrackStore.fromUri(this, uri));
    }

    private boolean isSafeAudioUri(Uri uri) {
        if (uri == null || uri.getScheme() == null || !"content".equalsIgnoreCase(uri.getScheme())) {
            return false;
        }
        try {
            String type = getContentResolver().getType(uri);
            return type == null || type.toLowerCase(Locale.ROOT).startsWith("audio/");
        } catch (Exception ignored) {
            return false;
        }
    }

    private LinearLayout row() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        return linearLayout;
    }

    private TextView text(String str, int i, boolean z) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextColor(this.fg);
        textView.setTextSize(i);
        textView.setGravity(16);
        textView.setTypeface(null, z ? 1 : 0);
        textView.setSingleLine(false);
        return textView;
    }

    private void makeMarquee(TextView textView) {
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textView.setMarqueeRepeatLimit(-1);
        textView.setSelected(true);
        textView.setFocusable(true);
        textView.setFocusableInTouchMode(true);
    }

    private Button button(String str) {
        Button button = new Button(this);
        button.setText(str);
        button.setTextColor(this.fg);
        button.setTextSize(14.0f);
        button.setAllCaps(false);
        button.setGravity(17);
        button.setIncludeFontPadding(true);
        button.setPadding(0, 0, 0, 0);
        button.setStateListAnimator(null);
        button.setElevation(0.0f);
        button.setTranslationZ(0.0f);
        button.setMinWidth(0);
        button.setMinHeight(0);
        setSurface(button, this.bg, false);
        attachPressAnimation(button);
        return button;
    }

    private void attachPressAnimation(final View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View touched, MotionEvent event) {
                if (!MainActivity.this.animations) {
                    touched.setScaleX(1.0f);
                    touched.setScaleY(1.0f);
                    touched.setAlpha(1.0f);
                    return false;
                }
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN) {
                    touched.animate().scaleX(0.96f).scaleY(0.96f).alpha(0.88f).setDuration(45L).start();
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    touched.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f).setDuration(85L).setInterpolator(new DecelerateInterpolator()).start();
                }
                return false;
            }
        });
    }

    private void animateAppear(View view) {
        if (!this.animations) {
            return;
        }
        view.setAlpha(0.0f);
        view.setTranslationY(dp(4));
        view.animate().alpha(1.0f).translationY(0.0f).setDuration(110L).setInterpolator(new DecelerateInterpolator()).start();
    }

    private Button icon(String str) {
        Button button = button(str);
        button.setTextSize(24.0f);
        return button;
    }

    private Button shuffleButton() {
        Button buttonIcon = icon("⇄");
        buttonIcon.setTextSize(31.0f);
        buttonIcon.setTypeface(Typeface.DEFAULT_BOLD);
        buttonIcon.setPadding(0, 0, 0, 0);
        return buttonIcon;
    }

    private Button searchButton() {
        Button buttonIcon = icon("⌕");
        buttonIcon.setTextSize(31.0f);
        buttonIcon.setTypeface(Typeface.DEFAULT_BOLD);
        buttonIcon.setPadding(0, 0, 0, 0);
        return buttonIcon;
    }

    private void applyButtonColors(Button button, int i, int i2) {
        button.setTextColor(i2);
        GradientDrawable gradientDrawableRounded = rounded(i, false);
        gradientDrawableRounded.setStroke(1, i);
        button.setBackground(gradientDrawableRounded);
    }

    private LinearLayout.LayoutParams square(int i) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(i), dp(i));
        layoutParams.setMargins(dp(4), dp(4), dp(4), dp(4));
        return layoutParams;
    }

    private View framed(View view) {
        return spaced(view);
    }

    private View spaced(View view) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, dp(5), 0, dp(5));
        view.setLayoutParams(layoutParams);
        animateAppear(view);
        return view;
    }

    private GradientDrawable rounded(int i, boolean z) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(dp(10));
        if (z) {
            i = this.line;
        }
        gradientDrawable.setStroke(z ? 1 : 0, i);
        return gradientDrawable;
    }

    private void setSurface(View view, int i, boolean z) {
        view.setBackground(rounded(i, z));
    }

    private View lineView() {
        View view = new View(this);
        view.setBackgroundColor(this.line);
        return view;
    }

    private ImageView coverView() {
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(0);
        gradientDrawable.setCornerRadius(dp(12));
        imageView.setBackground(gradientDrawable);
        imageView.setClipToOutline(true);
        return imageView;
    }

    private FrameLayout shade() {
        FrameLayout frameLayout = new FrameLayout(this);
        int i = this.dark ? 0 : 255;
        frameLayout.setBackgroundColor(Color.argb(190, i, i, i));
        frameLayout.setOnClickListener(new AnonymousClass87());
        return frameLayout;
    }

    class AnonymousClass87 implements View.OnClickListener {
        AnonymousClass87() {
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(MainActivity.this).removeView(view);
            MainActivity.m80$$Nest$mupdateMini(MainActivity.this);
        }
    }

    private LinearLayout panelCard() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(12), dp(12), dp(12), dp(12));
        setSurface(linearLayout, this.bg, true);
        linearLayout.setOnClickListener(new AnonymousClass88());
        if (this.animations) {
            linearLayout.setAlpha(0.0f);
            linearLayout.setScaleX(0.98f);
            linearLayout.setScaleY(0.98f);
            linearLayout.post(new Runnable() {
                @Override
                public void run() {
                    linearLayout.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(115L).setInterpolator(new DecelerateInterpolator()).start();
                }
            });
        }
        return linearLayout;
    }

    class AnonymousClass88 implements View.OnClickListener {
        AnonymousClass88() {
        }

        @Override
        public void onClick(View view) {
        }
    }

    private void addMiniSpacerIfNeeded() {
        if (this.currentIndex < 0 || this.currentIndex >= this.tracks.size() || this.overlayHost.getChildCount() > 0) {
            return;
        }
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(88)));
        this.list.addView(view);
    }

    private void showConfirmPanel(String str, String str2, Runnable runnable) {
        FrameLayout frameLayoutShade = shade();
        LinearLayout linearLayoutPanelCard = panelCard();
        linearLayoutPanelCard.setPadding(dp(16), dp(16), dp(16), dp(16));
        linearLayoutPanelCard.addView(text(str, 22, true), new LinearLayout.LayoutParams(-1, dp(46)));
        TextView textViewText = text(str2, 16, false);
        textViewText.setTextColor(this.muted);
        textViewText.setPadding(0, dp(4), 0, dp(14));
        linearLayoutPanelCard.addView(textViewText, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout linearLayoutRow = row();
        Button button = button(tr("No", "Нет"));
        button.setOnClickListener(new AnonymousClass89(this, frameLayoutShade));
        linearLayoutRow.addView(button, new LinearLayout.LayoutParams(0, dp(54), 1.0f));
        Button button2 = button(tr("Yes", "Да"));
        applyButtonColors(button2, this.fg, this.bg);
        button2.setOnClickListener(new AnonymousClass90(this, frameLayoutShade, runnable));
        linearLayoutRow.addView(button2, new LinearLayout.LayoutParams(0, dp(54), 1.0f));
        linearLayoutPanelCard.addView(linearLayoutRow);
        frameLayoutShade.addView(linearLayoutPanelCard, centerParams(dp(330), -2));
        this.overlayHost.addView(frameLayoutShade);
        updateMini();
    }

    class AnonymousClass89 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;

        AnonymousClass89(MainActivity mainActivity, FrameLayout frameLayout) {
            this.val$shade = frameLayout;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            MainActivity.m80$$Nest$mupdateMini(this.this$0);
        }
    }

    class AnonymousClass90 implements View.OnClickListener {
        final MainActivity this$0;
        final FrameLayout val$shade;
        final Runnable val$yesAction;

        AnonymousClass90(MainActivity mainActivity, FrameLayout frameLayout, Runnable runnable) {
            this.val$shade = frameLayout;
            this.val$yesAction = runnable;
            this.this$0 = mainActivity;
        }

        @Override
        public void onClick(View view) {
            MainActivity.m9$$Nest$fgetoverlayHost(this.this$0).removeView(this.val$shade);
            this.val$yesAction.run();
            MainActivity.m80$$Nest$mupdateMini(this.this$0);
        }
    }

    private FrameLayout.LayoutParams centerParams(int i, int i2) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(i, i2, 17);
        layoutParams.setMargins(dp(14), dp(14), dp(14), dp(14));
        return layoutParams;
    }

    private FrameLayout.LayoutParams bottomParams() {
        return new FrameLayout.LayoutParams(-1, (int) (getResources().getDisplayMetrics().heightPixels * 0.78f), 80);
    }

    private int dp(int i) {
        return Math.round(i * getResources().getDisplayMetrics().density);
    }

    private static class Playlist {
        String name;
        final ArrayList<String> uris = new ArrayList<>();

        Playlist(String str) {
            this.name = str;
        }
    }
}
