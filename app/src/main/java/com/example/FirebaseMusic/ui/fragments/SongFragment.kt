package com.example.spotifyclone.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.databinding.FragmentHomeBinding
import com.example.spotifyclone.databinding.FragmentSongBinding
import com.example.spotifyclone.exooplayer.isPlaying
import com.example.spotifyclone.exooplayer.toSong
import com.example.spotifyclone.other.Status
import com.example.spotifyclone.ui.viewmodels.MainViewModel
import com.example.spotifyclone.ui.viewmodels.SongViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongFragment: Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var  glide:RequestManager

    lateinit var binding:FragmentSongBinding
    private lateinit var mainViewModel:MainViewModel
    private val songViewModel: SongViewModel by viewModels()

    private var curPlayingSong: Song?=null

    private var playbacState:PlaybackStateCompat?=null

    private var shouldUpdateSeekbar=true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate<FragmentSongBinding>(inflater, R.layout.fragment_song, container, false)//프래그먼트 데이터바인딩
        mainViewModel=ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()
        binding.playpauseImg=R.drawable.ic_pause
        binding.progress=0
        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                shouldUpdateSeekbar=false
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                binding.seekBar?.let{
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar=true
                }
            }
        })
        binding.fragment=this
        return binding.root
    }

    fun playpause(){
        curPlayingSong?.let{
            mainViewModel.playOrToggleSong(it,true)
        }

    }
    fun skipToPrevious(){
        mainViewModel.skipToPreviousSong()
    }
    fun skipToNext(){
        mainViewModel.skipToNextSong()
    }


    private fun updateTitleAndSongImage(song: Song){
        val title="${song.title}-${song.subtitle}"
        //binding.tvSongName.text=title//*********************
        binding.songtitle=title
        binding.song=song
        //glide.load(song.imageUrl).into(binding.ivSongImage)//********************
    }

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(viewLifecycleOwner){
            it?.let{result->
                when(result.status){
                    Status.SUCCESS->{
                        result.data?.let{songs->
                            if(curPlayingSong==null&&songs.isNotEmpty()){
                                curPlayingSong=songs[0]
                                updateTitleAndSongImage(songs[0])
                            }

                        }
                    }
                    else->Unit
                }

            }
        }
        mainViewModel.curPlayingSong.observe(viewLifecycleOwner){
            if(it==null)return@observe
            curPlayingSong=it.toSong()
            updateTitleAndSongImage(curPlayingSong!!)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner){
            playbacState=it
            if(playbacState?.isPlaying==true)
                binding.playpauseImg=R.drawable.ic_pause
            else
                binding.playpauseImg=R.drawable.ic_play

            //binding.Progress=it?.position?.toInt()?:0
            binding.progress=it?.position?.toInt()?:0

        }
        songViewModel.curPlayerPosition.observe(viewLifecycleOwner){

            if(shouldUpdateSeekbar){
                if (it != null) {
                    binding.progress=it.toInt()
                }
                println(binding.progress)
                if (it != null) {
                    setCurPlayerTimeToTextView(it)
                }
            }
        }
        songViewModel.curSongDuration.observe(viewLifecycleOwner){
            binding.max=it.toInt()
            val dateFormat=SimpleDateFormat("mm:ss", Locale.getDefault())
            binding.songduration=dateFormat.format(it)

        }
    }

    private fun setCurPlayerTimeToTextView(ms:Long){
        val dateFormat=SimpleDateFormat("mm:ss", Locale.getDefault())
        binding.curtime=dateFormat.format(ms)

    }
}