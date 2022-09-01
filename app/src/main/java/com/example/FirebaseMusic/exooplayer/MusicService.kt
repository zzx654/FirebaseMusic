 package com.example.spotifyclone.exooplayer

import android.app.PendingIntent
import android.content.Intent
import android.media.session.MediaSession
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.spotifyclone.exooplayer.callbacks.FirebaseMusicSource
import com.example.spotifyclone.exooplayer.callbacks.MusicPlaybackPreparer
import com.example.spotifyclone.exooplayer.callbacks.MusicPlayerEventListener
import com.example.spotifyclone.exooplayer.callbacks.MusicPlayerNotificationListener
import com.example.spotifyclone.other.Constants.MEDIA_ROOT_ID
import com.example.spotifyclone.other.Constants.NETWORK_ERROR
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject


 private const val SERVICE_TAG = "MusicService"

 @AndroidEntryPoint
 class MusicService : MediaBrowserServiceCompat() {

     @Inject
     lateinit var dataSourceFactory: DefaultDataSourceFactory

     @Inject
     lateinit var exoPlayer: SimpleExoPlayer

     @Inject
     lateinit var firebaseMusicSource: FirebaseMusicSource

     private lateinit var musicNotificationManager: MusicNotificationManager

     //private val serviceJob = Job()
     //private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

     private lateinit var mediaSession: MediaSessionCompat
     private lateinit var mediaSessionConnector: MediaSessionConnector

     var isForegroundService = false

     private var curPlayingSong: MediaMetadataCompat? = null

     private var isPlayerInitialized = false

     private lateinit var musicPlayerEventListener: MusicPlayerEventListener

     private val compositeDisposable= CompositeDisposable()

     companion object {//다른 액티비티나 클래스 어디서든 접근가능
         var curSongDuration = 0L
             private set
     }

     override fun onCreate() {
         super.onCreate()
         /**serviceScope.launch {
             firebaseMusicSource.fetchMediaData()
         }//백그라운드 스레드(코루틴)에서 firebase 뮤직 fetch 작업 수행
         **/
         firebaseMusicSource.fetchMediaData(compositeDisposable)//rxjava single객체 이용해서 백그라운드 스레드에서 데이터 수신

         val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
             PendingIntent.getActivity(this, 0, it, 0)
         }//pendingIntent를 이용하여 notification으로 작업 수행시 인텐트 실행할수 있도록 함

         mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
             setSessionActivity(activityIntent)
             isActive = true
         }//미디어 세션(MediaSessionCompat)을 얻고 세션 액티비티 설정, isActive값을 줌

         sessionToken = mediaSession.sessionToken//MusicBrowserServiceCompat의 setSessionToekn 함수 호출(setSessionToken) ,변수 아님

         musicNotificationManager = MusicNotificationManager(
                 this,
                 mediaSession.sessionToken,
                 MusicPlayerNotificationListener(this)
         ) {
             curSongDuration = exoPlayer.duration
         }//람다콜백 생성자 인자로 넘김

         val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource) {
             curPlayingSong = it//MusicPlaybackpreparer에서 찾아준 준비된 재생곡을 할당
             preparePlayer(
                     firebaseMusicSource.songs,
                     it,
                     true
             )
         }//람다함수를 인자로 넘김

         mediaSessionConnector = MediaSessionConnector(mediaSession).apply{
             setPlaybackPreparer(musicPlaybackPreparer)
             setQueueNavigator(MusicQueueNavigator())
             setPlayer(exoPlayer)
         }
         //mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)//재생준비자 설정
         //mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
         //mediaSessionConnector.setPlayer(exoPlayer)

         musicPlayerEventListener = MusicPlayerEventListener(this)
         exoPlayer.addListener(musicPlayerEventListener)
         musicNotificationManager.showNotification(exoPlayer)
     }

     private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
         override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
             return firebaseMusicSource.songs[windowIndex].description
         }
     }

     private fun preparePlayer(
             songs: List<MediaMetadataCompat>,
             itemToPlay: MediaMetadataCompat?,
             playNow: Boolean
     ) {//exoplayer 재생곡 설정
         val curSongIndex = if(curPlayingSong == null) 0 else songs.indexOf(itemToPlay)//현재 재생곡의 인덱스반환
         exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))//인자로 concatenatingMedia를 가져다줌
         exoPlayer.seekTo(curSongIndex, 0L)
         exoPlayer.playWhenReady = playNow
     }

     override fun onTaskRemoved(rootIntent: Intent?) {
         super.onTaskRemoved(rootIntent)
         exoPlayer.stop()
     }

     override fun onDestroy() {
         super.onDestroy()
         //serviceScope.cancel()
         compositeDisposable.dispose()

         exoPlayer.removeListener(musicPlayerEventListener)
         exoPlayer.release()
     }

     override fun onGetRoot(
             clientPackageName: String,
             clientUid: Int,
             rootHints: Bundle?
     ): BrowserRoot? {
         return BrowserRoot(MEDIA_ROOT_ID, null)
     }

     override fun onLoadChildren(
             parentId: String,
             result: Result<MutableList<MediaBrowserCompat.MediaItem>>
     ) {
         when(parentId) {
             MEDIA_ROOT_ID -> {
                 val resultsSent = firebaseMusicSource.whenReady { isInitialized ->
                     if (isInitialized) {
                         result.sendResult(firebaseMusicSource.asMediaItems())//mediaBrowsercompat을 매개로 프래그먼트에도 initial되었음을 알림
                         if (!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()) {
                             preparePlayer(firebaseMusicSource.songs, firebaseMusicSource.songs[0], false)
                             isPlayerInitialized = true
                         }
                     } else {
                         mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                         result.sendResult(null)
                     }
                 }//람다 콜백 인자로 넘김
                 if(!resultsSent) {//whenready반환값 resultSent가 false이면
                     result.detach()
                 }
             }
         }
     }
 }
