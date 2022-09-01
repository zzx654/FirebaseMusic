 package com.example.spotifyclone.exooplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.spotifyclone.R
import com.example.spotifyclone.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.spotifyclone.other.Constants.NOTIFICATION_ID
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

 class MusicNotificationManager(private val context: Context, sessionToken: MediaSessionCompat.Token, notificationListener: PlayerNotificationManager.NotificationListener,
         private val newSongCallback: () -> Unit
 ) {//MusicService의 context, 세션토큰,notificationListener을 받음
     //notificationmanager를 생성하고 notification을 show하는 메소드 포함하는 클래스
     private val notificationManager: PlayerNotificationManager

     init {//PlayerNotificationManager 초기화
         val mediaController = MediaControllerCompat(context, sessionToken)//세션토큰, service의 context를 넘겨 mediaController 얻음
         notificationManager = PlayerNotificationManager.Builder(
             context,
             NOTIFICATION_ID,
             NOTIFICATION_CHANNEL_ID
         ).setChannelNameResourceId(R.string.notification_channel_name)
             .setChannelDescriptionResourceId( R.string.notification_channel_description)
             .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
             .setNotificationListener( notificationListener)
             .build().apply {
                 setSmallIcon(R.drawable.ic_music)
                 setMediaSessionToken(sessionToken)
             }
         /**notificationManager = PlayerNotificationManager.createWithNotificationChannel(
                 context,
                 NOTIFICATION_CHANNEL_ID,
                 R.string.notification_channel_name,
                 R.string.notification_channel_description,
                 NOTIFICATION_ID,
                 DescriptionAdapter(mediaController),
                 notificationListener
         ).apply {
             setSmallIcon(R.drawable.ic_music)
             setMediaSessionToken(sessionToken)
         }**/
     }

     fun showNotification(player: Player) {
         notificationManager.setPlayer(player)
     }

     private inner class DescriptionAdapter(
             private val mediaController: MediaControllerCompat
     ) : PlayerNotificationManager.MediaDescriptionAdapter {//앞서 만든 mediaController를 이용한 descriptionAdapter클래스, notificationmanager의 생성자로 전달할 객체의 클래스

         override fun getCurrentContentTitle(player: Player): CharSequence {
             newSongCallback()//&&&&&&&&&&&&&&&&&&&&&&&&&&&&
             return mediaController.metadata.description.title.toString()
         }

         override fun createCurrentContentIntent(player: Player): PendingIntent? {
             return mediaController.sessionActivity
         }

         override fun getCurrentContentText(player: Player): CharSequence? {
             return mediaController.metadata.description.subtitle.toString()
         }

         override fun getCurrentLargeIcon(
                 player: Player,
                 callback: PlayerNotificationManager.BitmapCallback
         ): Bitmap? {
             Glide.with(context).asBitmap()
                     .load(mediaController.metadata.description.iconUri)
                     .into(object : CustomTarget<Bitmap>() {
                         override fun onResourceReady(
                                 resource: Bitmap,
                                 transition: Transition<in Bitmap>?
                         ) {
                             callback.onBitmap(resource)
                         }

                         override fun onLoadCleared(placeholder: Drawable?) = Unit
                     })
             return null
         }
     }
 }