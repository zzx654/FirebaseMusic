package com.example.spotifyclone.exooplayer.callbacks

import android.widget.Toast
import com.example.spotifyclone.exooplayer.MusicService
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player

class MusicPlayerEventListener(
        private val musicService: MusicService
): Player.Listener {
    //음악을 멈췄을떄
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if(playbackState==Player.STATE_READY&&!playWhenReady){//플레이어가 준비되었지만 자동적으로 플레이안되어야할시
            musicService.stopForeground(false)
        }
    }

    /**override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService,"An unknown error occured",Toast.LENGTH_LONG).show()

    }**/
}