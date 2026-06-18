package com.dumuzeyn.mp3player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.IBinder;

import java.util.ArrayList;

public class PlayerService extends Service {
    public static final String ACTION_PLAY_INDEX = "com.dumuzeyn.mp3player.PLAY_INDEX";
    public static final String ACTION_TOGGLE = "com.dumuzeyn.mp3player.TOGGLE";
    public static final String ACTION_NEXT = "com.dumuzeyn.mp3player.NEXT";
    public static final String ACTION_PREV = "com.dumuzeyn.mp3player.PREV";
    public static final String ACTION_STOP = "com.dumuzeyn.mp3player.STOP";
    public static final String ACTION_SEEK = "com.dumuzeyn.mp3player.SEEK";
    public static final String ACTION_LOOP = "com.dumuzeyn.mp3player.LOOP";
    public static final String EXTRA_INDEX = "index";
    public static final String EXTRA_ONE_SHOT = "oneShot";
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_LOOP_MODE = "loopMode";
    public static final String EXTRA_QUEUE_URIS = "queueUris";

    private static final String CHANNEL_ID = "playback";
    private static final int NOTIFICATION_ID = 7;

    public static int lastIndex = -1;
    public static boolean lastPlaying = false;
    public static int lastDuration = 0;
    public static int lastPosition = 0;
    public static int lastLoopMode = 0;
    public static String lastUri = "";
    private static PlayerService instance;

    private final ArrayList<Track> queue = new ArrayList<>();
    private MediaPlayer player;
    private MediaSession mediaSession;
    private int currentIndex = -1;
    private boolean oneShot = false;
    private int loopMode = 0; // 0 off, 1 one song, 2 list

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        createChannel();
        mediaSession = new MediaSession(this, "MP3 Player");
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override public void onPlay() { toggle(); }
            @Override public void onPause() { toggle(); }
            @Override public void onSkipToNext() { playNext(); }
            @Override public void onSkipToPrevious() { playPrevious(); }
            @Override public void onSeekTo(long pos) { seekTo((int) pos); }
            @Override public void onStop() {
                stopPlayback();
                stopSelf();
            }
        });
        mediaSession.setActive(true);
        queue.addAll(TrackStore.load(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? "" : intent.getAction();
        startForeground(NOTIFICATION_ID, buildNotification());

        if (ACTION_PLAY_INDEX.equals(action)) {
            oneShot = intent.getBooleanExtra(EXTRA_ONE_SHOT, false);
            playIndex(intent.getIntExtra(EXTRA_INDEX, 0), intent.getStringArrayListExtra(EXTRA_QUEUE_URIS));
        } else if (ACTION_TOGGLE.equals(action)) {
            toggle();
        } else if (ACTION_NEXT.equals(action)) {
            oneShot = false;
            playNext();
        } else if (ACTION_PREV.equals(action)) {
            oneShot = false;
            playPrevious();
        } else if (ACTION_SEEK.equals(action)) {
            seekTo(intent.getIntExtra(EXTRA_POSITION, 0));
        } else if (ACTION_LOOP.equals(action)) {
            loopMode = intent.getIntExtra(EXTRA_LOOP_MODE, 0);
            lastLoopMode = loopMode;
            startForeground(NOTIFICATION_ID, buildNotification());
        } else if (ACTION_STOP.equals(action)) {
            stopPlayback();
            stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void playIndex(int index) {
        playIndex(index, null);
    }

    private void playIndex(int index, ArrayList<String> queueUris) {
        if (queueUris != null) {
            queue.clear();
            ArrayList<Track> allTracks = TrackStore.load(this);
            for (String uri : queueUris) {
                for (Track track : allTracks) {
                    if (track.uri.equals(uri)) {
                        queue.add(track);
                        break;
                    }
                }
            }
        } else if (queue.isEmpty()) {
            queue.addAll(TrackStore.load(this));
        }
        if (queue.isEmpty()) {
            stopPlayback();
            stopSelf();
            return;
        }

        currentIndex = Math.max(0, Math.min(index, queue.size() - 1));
        releasePlayer();
        player = new MediaPlayer();

        try {
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            player.setDataSource(this, queue.get(currentIndex).asUri());
            player.setOnCompletionListener(mp -> {
                if (loopMode == 1) {
                    playIndex(currentIndex);
                } else if (oneShot) {
                    stopPlayback();
                    stopSelf();
                } else if (loopMode == 2 || currentIndex < queue.size() - 1) {
                    playNext();
                } else {
                    stopPlayback();
                    stopSelf();
                }
            });
            player.prepare();
            player.start();
            updateState();
            startForeground(NOTIFICATION_ID, buildNotification());
        } catch (Exception exception) {
            stopPlayback();
            stopSelf();
        }
    }

    private void toggle() {
        if (player == null) {
            playIndex(currentIndex < 0 ? 0 : currentIndex);
            return;
        }
        if (player.isPlaying()) player.pause();
        else player.start();
        updateState();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    private void playNext() {
        if (queue.isEmpty()) queue.addAll(TrackStore.load(this));
        if (queue.isEmpty()) return;
        playIndex(currentIndex < 0 ? 0 : (currentIndex + 1) % queue.size());
    }

    private void playPrevious() {
        if (queue.isEmpty()) queue.addAll(TrackStore.load(this));
        if (queue.isEmpty()) return;
        playIndex(currentIndex <= 0 ? queue.size() - 1 : currentIndex - 1);
    }

    private void seekTo(int position) {
        if (player == null) return;
        try {
            player.seekTo(Math.max(0, Math.min(position, player.getDuration())));
            updateState();
            startForeground(NOTIFICATION_ID, buildNotification());
        } catch (Exception ignored) {}
    }

    private void stopPlayback() {
        releasePlayer();
        currentIndex = -1;
        lastIndex = -1;
        lastPlaying = false;
        lastDuration = 0;
        lastPosition = 0;
        lastUri = "";
        stopForeground(true);
    }

    private void releasePlayer() {
        if (player == null) return;
        try {
            player.stop();
        } catch (Exception ignored) {}
        player.release();
        player = null;
    }

    private void updateState() {
        lastIndex = currentIndex;
        lastPlaying = player != null && player.isPlaying();
        lastDuration = player == null ? 0 : player.getDuration();
        lastPosition = player == null ? 0 : player.getCurrentPosition();
        lastLoopMode = loopMode;
        lastUri = currentIndex >= 0 && currentIndex < queue.size() ? queue.get(currentIndex).uri : "";
    }

    public static void refreshSnapshot() {
        if (instance != null) instance.updateState();
    }

    private Notification buildNotification() {
        updateState();
        Track track = currentIndex >= 0 && currentIndex < queue.size()
                ? queue.get(currentIndex)
                : new Track("", "MP3 Player", "РњСѓР·С‹РєР° РіРѕС‚РѕРІР°");

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                1,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(this, CHANNEL_ID)
                : new Notification.Builder(this);

        builder.setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(track.title)
                .setContentText(track.artist)
                .setContentIntent(contentIntent)
                .setOngoing(player != null && player.isPlaying())
                .setCategory(Notification.CATEGORY_TRANSPORT)
                .setPriority(Notification.PRIORITY_LOW)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_media_previous, "РќР°Р·Р°Рґ", serviceIntent(ACTION_PREV, 2))
                .addAction(player != null && player.isPlaying() ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        player != null && player.isPlaying() ? "РџР°СѓР·Р°" : "РРіСЂР°С‚СЊ", serviceIntent(ACTION_TOGGLE, 3))
                .addAction(android.R.drawable.ic_media_next, "Р”Р°Р»СЊС€Рµ", serviceIntent(ACTION_NEXT, 4));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            updateMediaSession(track);
            builder.setStyle(new Notification.MediaStyle()
                    .setMediaSession(mediaSession.getSessionToken())
                    .setShowActionsInCompactView(0, 1, 2));
        }
        return builder.build();
    }

    private void updateMediaSession(Track track) {
        long position = player == null ? 0 : player.getCurrentPosition();
        long duration = player == null ? 0 : player.getDuration();
        int state = player != null && player.isPlaying() ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED;
        mediaSession.setMetadata(new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
                .build());
        mediaSession.setPlaybackState(new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY_PAUSE
                        | PlaybackState.ACTION_PLAY
                        | PlaybackState.ACTION_PAUSE
                        | PlaybackState.ACTION_SKIP_TO_NEXT
                        | PlaybackState.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackState.ACTION_SEEK_TO)
                .setState(state, position, 1f)
                .build());
    }

    private PendingIntent serviceIntent(String action, int requestCode) {
        Intent intent = new Intent(this, PlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "РњСѓР·С‹РєР°", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        if (mediaSession != null) mediaSession.release();
        instance = null;
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (player != null && player.isPlaying()) {
            startForeground(NOTIFICATION_ID, buildNotification());
        }
        super.onTaskRemoved(rootIntent);
    }
}

