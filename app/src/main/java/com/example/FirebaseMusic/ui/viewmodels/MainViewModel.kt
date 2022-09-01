 package com.example.spotifyclone.ui.viewmodels

import android.media.browse.MediaBrowser
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.exooplayer.*
import com.example.spotifyclone.other.Constants.MEDIA_ROOT_ID
import com.example.spotifyclone.other.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

 @HiltViewModel
class MainViewModel @Inject constructor(private val musicServiceConnection: MusicServiceConnection): ViewModel()
{//musicServiceConnection을 받아서 subscribe함으로써 music서비스에서 불러온곡에 대한 정보를 받게 됨
    private val _mediaItems=MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError=musicServiceConnection.networkError
    val curPlayingSong=musicServiceConnection.curPlayingSong
    val playbackState=musicServiceConnection.playbackState

    init{
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID,object:MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {//목록 불러와 졌을때 호출됨
                super.onChildrenLoaded(parentId, children)
                val items=children.map{
                    Song(
                            it.mediaId!!,
                            it.description.title.toString(),
                            it.description.subtitle.toString(),
                            it.description.mediaUri.toString(),
                            it.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }

    fun skipToNextSong(){
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong(){
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos:Long){
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem:Song,toggle:Boolean = false){//인자로 받는것(songAdapter의 원소인 Song을 받음,리사이클러뷰 아이템 클릭시호출되는 메소드
        val isPrepared=playbackState.value?.isPrepared?:false//디폴트값 null이면  false
        //val isPrepared=playbackState.isPrepared?:false
        if(isPrepared &&mediaItem.mediaId==curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)){//인자로 받은 mediaId와 livedata인 curPlayingSong(현재재생곡)의 mediaId가 일치하면
            playbackState.value?.let{playbackState->
                when{
                    playbackState.isPlaying->if(toggle) musicServiceConnection.transportControls.pause()//플레이 되고있다면 pause
                    playbackState.isPlayEnabled->musicServiceConnection.transportControls.play()//플레이
                    else->Unit
                }

            }

        }
        else{//인자로 받은(사용자가 누른 음악) media가 현재 재생곡이 아니면
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId,null)
        }
    }
    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID,object: MediaBrowserCompat.SubscriptionCallback(){

        })
    }

}