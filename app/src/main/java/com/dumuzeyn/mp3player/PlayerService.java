package com.dumuzeyn.mp3player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.IBinder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private boolean noisyReceiverRegistered = false;
    private long lastResumePositionSavedAt = 0L;
    private String lastSavedQueueJson = "";
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
    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                PlayerService.this.pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                if (PlayerService.this.player != null) {
                    PlayerService.this.player.setVolume(0.35f, 0.35f);
                }
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN && PlayerService.this.player != null) {
                PlayerService.this.player.setVolume(1.0f, 1.0f);
            }
        }
    };
    private final BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                PlayerService.this.pause();
            }
        }
    };

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

    static void m85$$Nest$mplay(PlayerService playerService) {
        playerService.play();
    }

    static void m86$$Nest$mpause(PlayerService playerService) {
        playerService.pause();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
            PlayerService.m85$$Nest$mplay(PlayerService.this);
        }

        @Override
        public void onPause() {
            PlayerService.m86$$Nest$mpause(PlayerService.this);
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
            Map<String, Track> tracksByUri = new HashMap<>();
            Iterator<Track> allTracks = arrayListLoad.iterator();
            while (allTracks.hasNext()) {
                Track track = allTracks.next();
                tracksByUri.put(track.uri, track);
            }
            for (String str : arrayList) {
                Track track2 = tracksByUri.get(str);
                if (track2 != null) {
                    this.queue.add(track2);
                }
            }
            saveResumeState(true, true);
        } else if (this.queue.isEmpty()) {
            this.queue.addAll(TrackStore.load(this));
            saveResumeState(true, true);
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
            this.player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    PlayerService.this.startPreparedPlayer(mediaPlayer, startPosition);
                }
            });
            this.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    PlayerService.this.lambda$playIndex$0(mediaPlayer);
                }
            });
            this.player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                    PlayerService.this.handlePlaybackError();
                    return true;
                }
            });
            this.player.prepareAsync();
            updateState();
            startForeground(NOTIFICATION_ID, buildNotification());
        } catch (Exception e) {
            handlePlaybackError();
        }
    }

    private void startPreparedPlayer(MediaPlayer mediaPlayer, int startPosition) {
        if (this.player != mediaPlayer) {
            return;
        }
        try {
            if (startPosition > 0) {
                mediaPlayer.seekTo(Math.max(0, Math.min(startPosition, mediaPlayer.getDuration())));
            }
            if (!requestAudioFocus()) {
                pause();
                return;
            }
            mediaPlayer.start();
            mediaPlayer.setVolume(1.0f, 1.0f);
            updateState();
            saveResumeState(true, false);
            updateNoisyReceiver();
            startForeground(NOTIFICATION_ID, buildNotification());
        } catch (Exception e) {
            handlePlaybackError();
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
        if (safeIsPlaying()) {
            pause();
        } else {
            play();
        }
    }

    private void play() {
        if (this.player == null) {
            playIndex(this.currentIndex < 0 ? 0 : this.currentIndex);
            return;
        }
        if (!safeIsPlaying()) {
            if (!requestAudioFocus()) {
                return;
            }
            this.player.start();
            this.player.setVolume(1.0f, 1.0f);
            updateState();
            updateNoisyReceiver();
            startForeground(NOTIFICATION_ID, buildNotification());
        }
    }

    private void pause() {
        if (this.player != null && safeIsPlaying()) {
            this.player.pause();
            updateState();
            saveResumeState(true, false);
            updateNoisyReceiver();
            startForeground(NOTIFICATION_ID, buildNotification());
        }
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
            this.player.seekTo(Math.max(0, Math.min(i, safeDuration())));
            updateState();
            saveResumeState(true, false);
            startForeground(NOTIFICATION_ID, buildNotification());
        } catch (Exception e) {
        }
    }

    private void stopPlayback() {
        updateState();
        saveResumeState(true, false);
        releasePlayer();
        this.currentIndex = -1;
        lastPlaying = false;
        unregisterNoisyReceiver();
        abandonAudioFocus();
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

    private void handlePlaybackError() {
        releasePlayer();
        updateState();
        if (!this.oneShot && !this.queue.isEmpty() && (this.loopMode == 2 || this.currentIndex < this.queue.size() - 1)) {
            playNext();
            return;
        }
        stopPlayback();
        stopSelf();
    }

    private void updateState() {
        lastIndex = this.currentIndex;
        lastPlaying = this.player != null && safeIsPlaying();
        lastDuration = safeDuration();
        lastPosition = safePosition();
        lastLoopMode = this.loopMode;
        lastUri = (this.currentIndex < 0 || this.currentIndex >= this.queue.size()) ? "" : this.queue.get(this.currentIndex).uri;
        if (lastPlaying && System.currentTimeMillis() - this.lastResumePositionSavedAt >= 7000L) {
            saveResumeState(false, false);
        }
    }

    private void saveResumeState(boolean forcePosition, boolean includeQueue) {
        if (this.currentIndex < 0 || this.currentIndex >= this.queue.size()) {
            return;
        }
        JSONArray jSONArray = new JSONArray();
        Iterator<Track> it = this.queue.iterator();
        while (it.hasNext()) {
            jSONArray.put(it.next().uri);
        }
        String queueJson = jSONArray.toString();
        SharedPreferences.Editor edit = getSharedPreferences(RESUME_PREFS, 0).edit();
        edit.putString(RESUME_URI, this.queue.get(this.currentIndex).uri);
        edit.putInt(RESUME_POSITION, Math.max(0, lastPosition));
        edit.putInt(RESUME_DURATION, Math.max(0, lastDuration));
        edit.putInt(RESUME_LOOP_MODE, this.loopMode);
        edit.putBoolean(RESUME_PLAYING, lastPlaying);
        edit.putLong(RESUME_SAVED_AT, System.currentTimeMillis());
        if (includeQueue || !queueJson.equals(this.lastSavedQueueJson)) {
            edit.putString(RESUME_QUEUE, queueJson);
            this.lastSavedQueueJson = queueJson;
        }
        edit.apply();
        if (forcePosition || lastPlaying) {
            this.lastResumePositionSavedAt = System.currentTimeMillis();
        }
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
        Notification.Builder builderAddAction = builder.setSmallIcon(android.R.drawable.ic_media_play).setContentTitle(track.title).setContentText(track.artist).setContentIntent(activity).setOngoing(this.player != null && safeIsPlaying()).setCategory("transport").setPriority(-1).setVisibility(1).addAction(android.R.drawable.ic_media_previous, "Назад", serviceIntent(ACTION_PREV, 2));
        if (this.player != null && safeIsPlaying()) {
            i = android.R.drawable.ic_media_pause;
        }
        builderAddAction.addAction(i, (this.player == null || !safeIsPlaying()) ? "Играть" : "Пауза", serviceIntent(ACTION_TOGGLE, 3)).addAction(android.R.drawable.ic_media_next, "Дальше", serviceIntent(ACTION_NEXT, 4));
        updateMediaSession(track);
        builder.setStyle(new Notification.MediaStyle().setMediaSession(this.mediaSession.getSessionToken()).setShowActionsInCompactView(0, 1, 2));
        return builder.build();
    }

    private void updateMediaSession(Track track) {
        long currentPosition = safePosition();
        long duration = safeDuration();
        int i = (this.player == null || !safeIsPlaying()) ? 2 : 3;
        this.mediaSession.setMetadata(new MediaMetadata.Builder().putString("android.media.metadata.TITLE", track.title).putString("android.media.metadata.ARTIST", track.artist).putLong("android.media.metadata.DURATION", duration).build());
        this.mediaSession.setPlaybackState(new PlaybackState.Builder().setActions(822L).setState(i, currentPosition, 1.0f).build());
    }

    private int safeDuration() {
        if (this.player == null) {
            return 0;
        }
        try {
            return this.player.getDuration();
        } catch (Exception e) {
            return lastDuration;
        }
    }

    private int safePosition() {
        if (this.player == null) {
            return 0;
        }
        try {
            return this.player.getCurrentPosition();
        } catch (Exception e) {
            return lastPosition;
        }
    }

    private boolean safeIsPlaying() {
        if (this.player == null) {
            return false;
        }
        try {
            return this.player.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    private PendingIntent serviceIntent(String str, int i) {
        Intent intent = new Intent(this, (Class<?>) PlayerService.class);
        intent.setAction(str);
        return PendingIntent.getService(this, i, intent, 201326592);
    }

    private boolean requestAudioFocus() {
        if (this.audioManager == null) {
            return true;
        }
        int result;
        if (Build.VERSION.SDK_INT >= 26) {
            if (this.audioFocusRequest == null) {
                this.audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(new AudioAttributes.Builder().setContentType(2).setUsage(1).build())
                        .setOnAudioFocusChangeListener(this.audioFocusChangeListener)
                        .build();
            }
            result = this.audioManager.requestAudioFocus(this.audioFocusRequest);
        } else {
            result = this.audioManager.requestAudioFocus(this.audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void abandonAudioFocus() {
        if (this.audioManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 26 && this.audioFocusRequest != null) {
            this.audioManager.abandonAudioFocusRequest(this.audioFocusRequest);
        } else {
            this.audioManager.abandonAudioFocus(this.audioFocusChangeListener);
        }
    }

    private void updateNoisyReceiver() {
        if (this.player != null && safeIsPlaying()) {
            registerNoisyReceiver();
        } else {
            unregisterNoisyReceiver();
        }
    }

    private void registerNoisyReceiver() {
        if (this.noisyReceiverRegistered) {
            return;
        }
        registerReceiver(this.noisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        this.noisyReceiverRegistered = true;
    }

    private void unregisterNoisyReceiver() {
        if (!this.noisyReceiverRegistered) {
            return;
        }
        try {
            unregisterReceiver(this.noisyReceiver);
        } catch (Exception e) {
        }
        this.noisyReceiverRegistered = false;
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
        unregisterNoisyReceiver();
        abandonAudioFocus();
        if (this.mediaSession != null) {
            this.mediaSession.release();
        }
        instance = null;
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent intent) {
        if (this.player != null && safeIsPlaying()) {
            startForeground(NOTIFICATION_ID, buildNotification());
        }
        super.onTaskRemoved(intent);
    }
}
