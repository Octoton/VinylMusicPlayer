package com.poupa.vinylmusicplayer.service.playback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.poupa.vinylmusicplayer.model.Song;


/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public interface Playback {

    String getPath(@NonNull Song song);

    boolean setDataSource(Song song);

    void setNextDataSource(@Nullable Song song);

    void setCallbacks(PlaybackCallbacks callbacks);

    boolean isInitialized();

    void start();

    void stop();

    void release();

    void pause();

    boolean isPlaying();

    int duration();

    int position();

    void seek(int whereto);

    boolean setAudioSessionId(int sessionId);

    int getAudioSessionId();

    void setReplayGain(float replaygain);

    void setDuckingFactor(float duckingFactor);

    interface PlaybackCallbacks {
        void onTrackWentToNext();

        void onTrackEnded();
    }
}
