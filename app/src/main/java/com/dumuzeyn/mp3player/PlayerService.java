package com.dumuzeyn.mp3player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.IBinder;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;

public class PlayerService extends Service {
    public static final String ACTION_LOOP = "com.dumuzeyn.mp3player.LOOP";
    public static final String ACTION_NEXT = "com.dumuzeyn.mp3player.NEXT";
    public static final String ACTION_PLAY_INDEX = "com.dumuzeyn.mp3player.PLAY_INDEX";
    public static final String ACTION_PREV = "com.dumuzeyn.mp3player.PREV";
    public static final String ACTION_SEEK = "com.dumuzeyn.mp3player.SEEK";
    public static final String ACTION_STOP = "com.dumuzeyn.mp3player.STOP";
    public static final String ACTION_TOGGLE = "com.dumuzeyn.mp3player.TOGGLE";
    private static final String CHANNEL_ID = "playback";
    public static final String EXTRA_INDEX = "index";
    public static final String EXTRA_LOOP_MODE = "loopMode";
    public static final String EXTRA_ONE_SHOT = "oneShot";
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_QUEUE_URIS = "queueUris";
    public static final String RESUME_DURATION = "duration";
    public static final String RESUME_LOOP_MODE = "loopMode";
    public static final String RESUME_PLAYING = "playing";
    public static final String RESUME_POSITION = "position";
    public static final String RESUME_PREFS = "player_resume";
    public static final String RESUME_QUEUE = "queue";
    public static final String RESUME_SAVED_AT = "savedAt";
    public static final String RESUME_URI = "uri";
    private static final int NOTIFICATION_ID = 7;
    private static PlayerService instance;
    private MediaSession mediaSession;
    private MediaPlayer player;
    public static int lastIndex = -1;
    public static boolean lastPlaying = false;
    public static int lastDuration = 0;
    public static int lastPosition = 0;
    public static int lastLoopMode = 0;
    public static String lastUri = "";
    private final ArrayList<Track> queue = new ArrayList<>();
    private int currentIndex = -1;
    private boolean oneShot = false;
    private int loopMode = 0;

    public static void $r8$lambda$HQFQnxmrRGNGOnJVwAgcbEBDHew(PlayerService playerService, MediaPlayer mediaPlayer) {
        playerService.lambda$playIndex$0(mediaPlayer);
    }

    static void m81$$Nest$mplayNext(PlayerService playerService) {
        playerService.playNext();
    }

    static void m82$$Nest$mplayPrevious(PlayerService playerService) {
        playerService.playPrevious();
    }

    static void m83$$Nest$mseekTo(PlayerService playerService, int i) {
        playerService.seekTo(i);
    }

    static void m84$$Nest$mstopPlayback(PlayerService playerService) {
        playerService.stopPlayback();
    }

    static void m85$$Nest$mtoggle(PlayerService playerService) {
        playerService.toggle();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        createChannel();
        this.mediaSession = new MediaSession(this, "MP3 Player");
        this.mediaSession.setCallback(new AnonymousClass1());
        this.mediaSession.setActive(true);
        this.queue.addAll(TrackStore.load(this));
    }

    class AnonymousClass1 extends MediaSession.Callback {
        AnonymousClass1() {
        }

        @Override
        public void onPlay() {
            PlayerService.m85$$Nest$mtoggle(PlayerService.this);
        }

        @Override
        public void onPause() {
            PlayerService.m85$$Nest$mtoggle(PlayerService.this);
        }

        @Override
        public void onSkipToNext() {
            PlayerService.m81$$Nest$mplayNext(PlayerService.this);
        }

        @Override
        public void onSkipToPrevious() {
            PlayerService.m82$$Nest$mplayPrevious(PlayerService.this);
        }

        @Override
        public void onSeekTo(long j) {
            PlayerService.m83$$Nest$mseekTo(PlayerService.this, (int) j);
        }

        @Override
        public void onStop() {
            PlayerService.m84$$Nest$mstopPlayback(PlayerService.this);
            PlayerService.this.stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        String action = intent == null ? "" : intent.getAction();
        startForeground(NOTIFICATION_ID, buildNotification());
        if (ACTION_PLAY_INDEX.equals(action)) {
            this.oneShot = intent.getBooleanExtra(EXTRA_ONE_SHOT, false);
            playIndex(intent.getIntExtra(EXTRA_INDEX, 0), intent.getStringArrayListExtra(EXTRA_QUEUE_URIS), intent.getIntExtra(EXTRA_POSITION, 0));
            return 1;
        }
        if (ACTION_TOGGLE.equals(action)) {
            toggle();
            return 1;
        }
        if (ACTION_NEXT.equals(action)) {
            this.oneShot = false;
            playNext();
            return 1;
        }
        if (ACTION_PREV.equals(action)) {
            this.oneShot = false;
            playPrevious();
            return 1;
        }
        if (ACTION_SEEK.equals(action)) {
            seekTo(intent.getIntExtra(EXTRA_POSITION, 0));
            return 1;
        }
        if (ACTION_LOOP.equals(action)) {
            this.loopMode = intent.getIntExtra(EXTRA_LOOP_MODE, 0);
            lastLoopMode = this.loopMode;
            startForeground(NOTIFICATION_ID, buildNotification());
            return 1;
        }
        if (ACTION_STOP.equals(action)) {
            stopPlayback();
            stopSelf();
            return 1;
        }
        return 1;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void playIndex(int i) {
        playIndex(i, null, 0);
    }

    private void playIndex(int i, ArrayList<String> arrayList) {
        playIndex(i, arrayList, 0);
    }

    private void playIndex(int i, ArrayList<String> arrayList, int startPosition) {
        if (arrayList != null) {
            this.queue.clear();
            ArrayList<Track> arrayListLoad = TrackStore.load(this);
            for (String str : arrayList) {
                Iterator<Track> it = arrayListLoad.iterator();
                while (true) {
                    if (it.hasNext()) {
                        Track next = it.next();
                        if (next.uri.equals(str)) {
                            this.queue.add(next);
                            break;
                        }
                    }
                }
            }
        } else if (this.queue.isEmpty()) {
            this.queue.addAll(TrackStore.load(this));
        }
        if (this.queue.isEmpty()) {
            stopPlayback();
            stopSelf();
            return;
        }
        this.currentIndex = Math.max(0, Math.min(i, this.queue.size() - 1));
        releasePlayer();
        this.player = new MediaPlayer();
        try {
            this.player.setAudioAttributes(new AudioAttributes.Builder().setContentType(2).setUsage(1).build());
            this.player.setDataSource(this, this.queue.get(this.currentIndex).asUri());
            this.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    PlayerService.this.lambda$playIndex$0(mediaPlayer);
                }
            });
            this.player.prepare();
            if (startPosition > 0) {
                this.player.seekTo(Math.max(0, Math.min(startPosition, this.player.getDuration())));
            }
            this.player.start();
            updateState();
            startForeground(NOTIFICATION_ID, buildNotification());
        } catch (Exception e) {
            stopPlayback();
            stopSelf();
        }
    }

    private void lambda$playIndex$0(MediaPlayer mediaPlayer) {
        if (this.loopMode == 1) {
            playIndex(this.currentIndex);
            return;
        }
        if (this.oneShot) {
            stopPlayback();
            stopSelf();
        } else if (this.loopMode == 2 || this.currentIndex < this.queue.size() - 1) {
            playNext();
        } else {
            stopPlayback();
            stopSelf();
        }
    }

    private void toggle() {
        if (this.player == null) {
            playIndex(this.currentIndex < 0 ? 0 : this.currentIndex);
            return;
        }
        if (this.player.isPlaying()) {
            this.player.pause();
        } else {
            this.player.start();
        }
        updateState();
        saveResumeState();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    private void playNext() {
        if (this.queue.isEmpty()) {
            this.queue.addAll(TrackStore.load(this));
        }
        if (this.queue.isEmpty()) {
            return;
        }
        playIndex(this.currentIndex < 0 ? 0 : (this.currentIndex + 1) % this.queue.size());
    }

    private void playPrevious() {
        if (this.queue.isEmpty()) {
            this.queue.addAll(TrackStore.load(this));
        }
        if (this.queue.isEmpty()) {
            return;
        }
        playIndex((this.currentIndex <= 0 ? this.queue.size() : this.currentIndex) - 1);
    }

    private void seekTo(int i) {
        if (this.player == null) {
            return;
        }
        try {
            this.player.seekTo(Math.max(0, Math.min(i, this.player.getDuration())));
            updateState();
            saveResumeState();
            startForeground(NOTIFICATION_ID, buildNotification());
        } catch (Exception e) {
        }
    }

    private void stopPlayback() {
        updateState();
        saveResumeState();
        releasePlayer();
        this.currentIndex = -1;
        lastPlaying = false;
        stopForeground(true);
    }

    private void releasePlayer() {
        if (this.player == null) {
            return;
        }
        try {
            this.player.stop();
        } catch (Exception e) {
        }
        this.player.release();
        this.player = null;
    }

    private void updateState() {
        lastIndex = this.currentIndex;
        lastPlaying = this.player != null && this.player.isPlaying();
        lastDuration = this.player == null ? 0 : this.player.getDuration();
        lastPosition = this.player != null ? this.player.getCurrentPosition() : 0;
        lastLoopMode = this.loopMode;
        lastUri = (this.currentIndex < 0 || this.currentIndex >= this.queue.size()) ? "" : this.queue.get(this.currentIndex).uri;
        saveResumeState();
    }

    private void saveResumeState() {
        if (this.currentIndex < 0 || this.currentIndex >= this.queue.size()) {
            return;
        }
        JSONArray jSONArray = new JSONArray();
        Iterator<Track> it = this.queue.iterator();
        while (it.hasNext()) {
            jSONArray.put(it.next().uri);
        }
        SharedPreferences.Editor edit = getSharedPreferences(RESUME_PREFS, 0).edit();
        edit.putString(RESUME_URI, this.queue.get(this.currentIndex).uri);
        edit.putInt(RESUME_POSITION, Math.max(0, lastPosition));
        edit.putInt(RESUME_DURATION, Math.max(0, lastDuration));
        edit.putInt(RESUME_LOOP_MODE, this.loopMode);
        edit.putBoolean(RESUME_PLAYING, lastPlaying);
        edit.putLong(RESUME_SAVED_AT, System.currentTimeMillis());
        edit.putString(RESUME_QUEUE, jSONArray.toString());
        edit.apply();
    }

    public static void refreshSnapshot() {
        if (instance != null) {
            instance.updateState();
        }
    }

    private Notification buildNotification() {
        Track track;
        Notification.Builder builder;
        updateState();
        if (this.currentIndex >= 0 && this.currentIndex < this.queue.size()) {
            track = this.queue.get(this.currentIndex);
        } else {
            track = new Track("", "MP3 Player", "Музыка готова");
        }
        Class<?> launchClass = "dark".equals(getSharedPreferences("mp3_player_ui", 0).getString("theme", "light")) ? DarkMainActivity.class : MainActivity.class;
        PendingIntent activity = PendingIntent.getActivity(this, 1, new Intent(this, launchClass), 201326592);
        if (Build.VERSION.SDK_INT >= 26) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        int i = android.R.drawable.ic_media_play;
        Notification.Builder builderAddAction = builder.setSmallIcon(android.R.drawable.ic_media_play).setContentTitle(track.title).setContentText(track.artist).setContentIntent(activity).setOngoing(this.player != null && this.player.isPlaying()).setCategory("transport").setPriority(-1).setVisibility(1).addAction(android.R.drawable.ic_media_previous, "Назад", serviceIntent(ACTION_PREV, 2));
        if (this.player != null && this.player.isPlaying()) {
            i = android.R.drawable.ic_media_pause;
        }
        builderAddAction.addAction(i, (this.player == null || !this.player.isPlaying()) ? "Играть" : "Пауза", serviceIntent(ACTION_TOGGLE, 3)).addAction(android.R.drawable.ic_media_next, "Дальше", serviceIntent(ACTION_NEXT, 4));
        updateMediaSession(track);
        builder.setStyle(new Notification.MediaStyle().setMediaSession(this.mediaSession.getSessionToken()).setShowActionsInCompactView(0, 1, 2));
        return builder.build();
    }

    private void updateMediaSession(Track track) {
        long currentPosition = this.player == null ? 0L : this.player.getCurrentPosition();
        long duration = this.player != null ? this.player.getDuration() : 0L;
        int i = (this.player == null || !this.player.isPlaying()) ? 2 : 3;
        this.mediaSession.setMetadata(new MediaMetadata.Builder().putString("android.media.metadata.TITLE", track.title).putString("android.media.metadata.ARTIST", track.artist).putLong("android.media.metadata.DURATION", duration).build());
        this.mediaSession.setPlaybackState(new PlaybackState.Builder().setActions(822L).setState(i, currentPosition, 1.0f).build());
    }

    private PendingIntent serviceIntent(String str, int i) {
        Intent intent = new Intent(this, (Class<?>) PlayerService.class);
        intent.setAction(str);
        return PendingIntent.getService(this, i, intent, 201326592);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Музыка", 2);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        if (this.mediaSession != null) {
            this.mediaSession.release();
        }
        instance = null;
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent intent) {
        if (this.player != null && this.player.isPlaying()) {
            startForeground(NOTIFICATION_ID, buildNotification());
        }
        super.onTaskRemoved(intent);
    }
}
