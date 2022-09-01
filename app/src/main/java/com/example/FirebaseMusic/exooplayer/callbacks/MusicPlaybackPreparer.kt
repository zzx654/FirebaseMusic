package com.example.spotifyclone.exooplayer.callbacks

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
//특정 준비 이벤트를 위해 호출되는 기능을 제공하는 클래스
class MusicPlaybackPreparer (private val firebaseMusicSource:FirebaseMusicSource, private val playerPrepared:(MediaMetadataCompat?)->Unit): MediaSessionConnector.PlaybackPreparer
{//play준비된 mediaId를 찾아내어 그것을 콜백람다에 인자전달하여 호출
    //override fun onCommand(player: Player, controlDispatcher: ControlDispatcher, command: String, extras: Bundle?, cb: ResultReceiver?)=false
    override fun onCommand(
        player: Player,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    )=false

    override fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
        //준비 또는 play 상태의 mediaID를 얻는다
    }

    override fun onPrepare(playWhenReady: Boolean)=Unit

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        firebaseMusicSource.whenReady{
            val itemToPlay=firebaseMusicSource.songs.find{
                mediaId==it.description.mediaId//준비된 mediaId와 firebaseMusicSource에서 fetch된 Song의 mediaId 일치하는 metadata찾음

            }//find는 람다를 만족하는 하나를 찾는다
            playerPrepared(itemToPlay)
        }//람다콜백함수를 인자로 넘겨 whenReady 호출
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?)=Unit

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) =Unit

}
