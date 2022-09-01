package com.example.spotifyclone.exooplayer

import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING

inline val PlaybackStateCompat.isPrepared
    get() = state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_PLAYING ||
            state == PlaybackStateCompat.STATE_PAUSED
//버퍼링중이거나 플레이중이거나 멈춤상태에 있는지를 검사하여 boolean 리턴(true이면 준비된 상태)

inline val PlaybackStateCompat.isPlaying
    get() = state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_PLAYING

//버퍼링중이거나 플레이 중인지를 확인하여 boolean 리턴(true이면 플레이중인 상태)
inline val PlaybackStateCompat.isPlayEnabled
    get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L ||
            (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L &&
                    state == PlaybackStateCompat.STATE_PAUSED)//플레이중이거나 멈춤상태 이거나 둘중하나에 해당되는지 boolean리턴
//(세션이 play명령을 지원하거나) (재생/일시중지 토글 명령을 지원하고 멈춰있거나)하는지를 boolean리턴

inline val PlaybackStateCompat.currentPlaybackPosition: Long
    get() = if(state == STATE_PLAYING) {
        val timeDelta = SystemClock.elapsedRealtime() - lastPositionUpdateTime
        (position + (timeDelta * playbackSpeed)).toLong()
    } else position