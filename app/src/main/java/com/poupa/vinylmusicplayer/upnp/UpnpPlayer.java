package com.poupa.vinylmusicplayer.upnp;


import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.upnp.remoterenderer.RendererState.State;
import com.poupa.vinylmusicplayer.util.PreferenceUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.Context;
import android.widget.Toast;

import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.playback.Playback;
import com.poupa.vinylmusicplayer.upnp.localserver.MediaServer;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_TOTAL;


public class UpnpPlayer implements Playback, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private final Context context;
    private Song song; //need to be a song
    private Song nextSong; //need to be a song

    @Nullable
    private Playback.PlaybackCallbacks callbacks;

    private boolean mIsInitialized = false;
    private final UpnpManager upnpManager;

    private boolean playerCannotBePlaying = true;

    public UpnpPlayer(final Context context) {
        upnpManager = UpnpManager.getInstance();
        this.context = context;
    }

    // TODO: put in utility class
    public static String getPathExtern(@NonNull Song song, @NonNull UpnpManager upnpManager) {
        return "http://"+upnpManager.getAddress()+"/"+ MediaServer.AUDIO_PREFIX + song.id;
    }

    @Override
    public String getPath(@NonNull Song song) {
        if (upnpManager.getAddress() == null)
            return null;

        return "http://"+upnpManager.getAddress()+"/"+ MediaServer.AUDIO_PREFIX + song.id;

        //Log.d("TOTO_player", "uri: "+song.id);
        //return uri;
    }

    /**
     * @param path The path of the file, or the http/rtsp URL of the stream
     *             you want to play
     * @return True if the <code>player</code> has been prepared and is
     * ready to play, false otherwise
     */
    @Override
    public boolean setDataSource(@NonNull final Song song) {
        Log.d("TOTO_player", "current data source: "+song);
        upnpManager.setOnCompletion(this, getPath(song));

        this.song = song;

        mIsInitialized = false;
        mIsInitialized = true; //setDataSourceImpl(path);
        playerCannotBePlaying = true;
        if (mIsInitialized) {
            setNextDataSource(null);
        }
        return mIsInitialized;
    }

    /**
     * @param path   The path of the file, or the http/rtsp URL of the stream
     *               you want to play
     * @return True if the <code>player</code> has been prepared and is
     * ready to play, false otherwise
     */
    /*private boolean setDataSourceImpl(@NonNull final String path) {
        if (context == null) {
            return false;
        }
        try {
            player.reset();
            player.setOnPreparedListener(null);
            if (path.startsWith("content://")) {
                player.setDataSource(context, Uri.parse(path));
            } else {
                player.setDataSource(path);
            }
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.prepare();
        } catch (Exception e) {
            return false;
        }
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.getPackageName());
        intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
        context.sendBroadcast(intent);
        return true;
    }*/

    /**
     * Set the MediaPlayer to start when this MediaPlayer finishes playback.
     *
     * @param path The path of the file, or the http/rtsp URL of the stream
     *             you want to play
     */
    @Override
    public void setNextDataSource(@Nullable final Song song) {
        Log.d("TOTO_player", "next data source: "+song);
        this.nextSong = song;
        /*if (context == null) {
            return;
        }
        try {
            mCurrentMediaPlayer.setNextMediaPlayer(null);
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Next media player is current one, continuing");
        } catch (IllegalStateException e) {
            Log.e(TAG, "Media player not initialized!");
            return;
        }
        if (mNextMediaPlayer != null) {
            mNextMediaPlayer.release();
            mNextMediaPlayer = null;
        }
        if (path == null) {
            return;
        }
        if (PreferenceUtil.getInstance().gaplessPlayback()) {
            mNextMediaPlayer = new MediaPlayer();
            mNextMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
            if (setDataSourceImpl(mNextMediaPlayer, path)) {
                try {
                    mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
                } catch (@NonNull IllegalArgumentException | IllegalStateException e) {
                    Log.e(TAG, "setNextDataSource: setNextMediaPlayer()", e);
                    if (mNextMediaPlayer != null) {
                        mNextMediaPlayer.release();
                        mNextMediaPlayer = null;
                    }
                }
            } else {
                if (mNextMediaPlayer != null) {
                    mNextMediaPlayer.release();
                    mNextMediaPlayer = null;
                }
            }
        }*/
    }

    /**
     * Sets the callbacks
     *
     * @param callbacks The callbacks to use
     */
    @Override
    public void setCallbacks(@Nullable Playback.PlaybackCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    /**
     * @return True if the player is ready to go, false otherwise
     */
    @Override
    public boolean isInitialized() {
        return mIsInitialized;
    }

    /**
     * Starts or resumes playback.
     */
    @Override
    public void start() {
        Log.d("TOTO_Player", "START");
        upnpManager.sendSong(song);
        playerCannotBePlaying = false;
        /*try {
            mCurrentMediaPlayer.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * Resets the MediaPlayer to its uninitialized state.
     */
    @Override
    public void stop() {
        //mCurrentMediaPlayer.reset();
        mIsInitialized = false;
        upnpManager.stop();
    }

    /**
     * Releases resources associated with this MediaPlayer object.
     */
    @Override
    public void release() {
        stop();
        /*mCurrentMediaPlayer.release();
        if (mNextMediaPlayer != null) {
            mNextMediaPlayer.release();
        }*/
    }

    /**
     * Pauses playback. Call start() to resume.
     */
    @Override
    public void pause() {
        /*try {
            mCurrentMediaPlayer.pause();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }*/
        if (upnpManager.getRendererCommand() != null)
            upnpManager.getRendererCommand().commandPause();
    }

    /**
     * Checks whether the MultiPlayer is playing.
     */
    @Override
    public boolean isPlaying() {
        if (playerCannotBePlaying || upnpManager.getRendererCommand() == null || upnpManager.getRendererCommand().getRendererState() == null)
            return false;

        return mIsInitialized && (upnpManager.getRendererCommand().getRendererState().getState() == State.PLAY); //&& mCurrentMediaPlayer.isPlaying();
    }

    /**
     * Gets the duration of the file.
     *
     * @return The duration in milliseconds
     */
    @Override
    public int duration() {
        if (upnpManager.getRendererCommand() == null || upnpManager.getRendererCommand().getRendererState() == null)
            return -1;

        if (!mIsInitialized) {
            return -1;
        }
        /*try {
            return mCurrentMediaPlayer.getDuration();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return -1;
        }*/
        return (int)(upnpManager.getRendererCommand().getRendererState().getDurationSeconds() * 1000);
    }

    /**
     * Gets the current playback position.
     *
     * @return The current position in milliseconds
     */
    @Override
    public int position() {
        if (upnpManager.getRendererCommand() == null || upnpManager.getRendererCommand().getRendererState() == null)
            return -1;

        if (!mIsInitialized) {
            return -1;
        }
        /*try {
            return mCurrentMediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return -1;
        }*/
        return (int)(upnpManager.getRendererCommand().getRendererState().getPositionSeconds() * 1000);
    }

    /**
     * Gets the current playback position.
     *
     * @param whereto The offset in milliseconds from the start to seek to
     */
    @Override
    public void seek(final int whereto) {
        if (upnpManager.getRendererCommand() == null)
            return;
        /*try {
            mCurrentMediaPlayer.seekTo(whereto);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }*/
        // TODO: put in utility class
        long h = whereto / (3600 * 1000);
        long m = ((whereto / 1000) - h * 3600) / 60;
        long s = whereto - h * 3600 * 1000 - m * 60 * 1000;
        String seek = formatTime(h, m, s);

        Log.d("TOTO_player", "Seek to " + seek);

        upnpManager.getRendererCommand().commandSeek(seek);
    }
    private String formatTime(long h, long m, long s)
    {
        return ((h >= 10) ? "" + h : "0" + h) + ":" + ((m >= 10) ? "" + m : "0" + m) + ":"
                + ((s >= 10) ? "" + s : "0" + s);
    }

    /*private void setVolume(final float vol) {
        try {
            mCurrentMediaPlayer.setVolume(vol, vol);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * Sets the audio session ID.
     *
     * @param sessionId The audio session ID
     */
    @Override
    public boolean setAudioSessionId(final int sessionId) {
        /*try {
            mCurrentMediaPlayer.setAudioSessionId(sessionId);
            return true;
        } catch (@NonNull IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
            return false;
        }*/
        return true;
    }

    /**
     * Returns the audio session ID.
     *
     * @return The current audio session ID.
     */
    @Override
    public int getAudioSessionId() {
        return -1; //mCurrentMediaPlayer.getAudioSessionId();
    }

    public void setReplayGain(float replaygain) {
        /*this.replaygain = replaygain;
        updateVolume();*/
    }

    public void setDuckingFactor(float duckingFactor) {
        /*this.duckingFactor = duckingFactor;
        updateVolume();*/
    }

    /*private void updateVolume() {
        float volume = 1.0f;
        if (!Float.isNaN(replaygain)) {
            volume = replaygain;
        }

        volume *= duckingFactor;

        setVolume(volume);
    }*/

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onError(final MediaPlayer mp, final int what, final int extra) {
        Log.d("TOTO_player", "ERROR");
        /*if (mp == mCurrentMediaPlayer) {
            if (context != null) {
                Toast.makeText(context, context.getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
            }
            mIsInitialized = false;
            mCurrentMediaPlayer.release();
            if (mNextMediaPlayer != null) {
                mCurrentMediaPlayer = mNextMediaPlayer;
                mIsInitialized = true;
                mNextMediaPlayer = null;
                if (callbacks != null) {
                    callbacks.onTrackWentToNext();
                }
            } else {
                mCurrentMediaPlayer = new MediaPlayer();
                mCurrentMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            }
        } else {
            mIsInitialized = false;
            mCurrentMediaPlayer.release();
            mCurrentMediaPlayer = new MediaPlayer();
            mCurrentMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            if (context != null) {
                Toast.makeText(context, context.getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
            }
        }
        return false;*/
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCompletion(final MediaPlayer mp) {
        Log.d("TOTO_player", "COMPLETION test");
        if (nextSong != null) {
            mIsInitialized = false;
            setDataSource(nextSong);
            mIsInitialized = true;
            nextSong = null;
            start();
            if (callbacks != null)
                callbacks.onTrackWentToNext();
        } else {
            if (callbacks != null)
                callbacks.onTrackEnded();
        }
        /*if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
            mIsInitialized = false;
            mCurrentMediaPlayer.release();
            mCurrentMediaPlayer = mNextMediaPlayer;
            mIsInitialized = true;
            mNextMediaPlayer = null;
            if (callbacks != null)
                callbacks.onTrackWentToNext();
        } else {
            if (callbacks != null)
                callbacks.onTrackEnded();
        }*/
    }
}
