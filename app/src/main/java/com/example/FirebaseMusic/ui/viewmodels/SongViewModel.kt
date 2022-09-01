package com.example.spotifyclone.ui.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifyclone.exooplayer.MusicService
import com.example.spotifyclone.exooplayer.MusicServiceConnection
import com.example.spotifyclone.exooplayer.currentPlaybackPosition
import com.example.spotifyclone.other.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
        musicServiceConnection:MusicServiceConnection
): ViewModel(){

    private val playbackState=musicServiceConnection.playbackState

    private val _curSongDuration=MutableLiveData<Long>()
    val curSongDuration:LiveData<Long> = _curSongDuration

    private val _curPlayerPosition=MutableLiveData<Long?>()
    val curPlayerPosition: MutableLiveData<Long?> = _curPlayerPosition

    private val compositeDisposable= CompositeDisposable()
    init{
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition(){
        viewModelScope.launch{
            while(true){
                val pos=playbackState.value?.currentPlaybackPosition
                if(curPlayerPosition.value!=pos){
                    _curPlayerPosition.postValue(pos)
                    _curSongDuration.postValue(MusicService.curSongDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)

            }

        }


    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()

    }
}