package com.example.spotifyclone.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.adapters.SwipeSongAdapter
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.databinding.ActivityMainBinding
import com.example.spotifyclone.exooplayer.isPlaying
import com.example.spotifyclone.exooplayer.toSong
import com.example.spotifyclone.other.Status
import com.example.spotifyclone.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class  MainActivity : AppCompatActivity() {

private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var curPlayingSong: Song?=null
    @Inject
    lateinit var glide: RequestManager

    lateinit var binding:ActivityMainBinding

    private var playbackState:PlaybackStateCompat?=null

    private var bottomVisibility: Boolean=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_main)
        subscribeToObservers()
        binding.playpauseimg=R.drawable.ic_pause
        val a=R.drawable.ic_pause
        binding.vpSong.adapter=swipeSongAdapter
        binding.vpSong.registerOnPageChangeCallback(object :ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackState?.isPlaying==true){
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                }else{
                    curPlayingSong=swipeSongAdapter.songs[position]
                    binding.song=curPlayingSong
                }
            }
        })

        binding.bottomvisible=bottomVisibility

        val navHostFragment = supportFragmentManager.findFragmentByTag("navHostFragment")
        navHostFragment?.let{
            //println("EXIST!Q!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            it.findNavController().addOnDestinationChangedListener { _, destination, _ ->
                when(destination.id){
                    R.id.songFragment->hideBottomBar()
                    R.id.homeFragment->showBottomBar()
                    else->showBottomBar()

                }
            }


        }
        swipeSongAdapter.setItemClickListener {
            navHostFragment?.let{
                it.findNavController().navigate(
                        R.id.globalActionToSongFragment)
            }

        }
        binding.activity=this
    }

    private fun hideBottomBar(){//******************
        bottomVisibility=false
       binding.bottomvisible=bottomVisibility

    }

    private fun showBottomBar(){//***************
        bottomVisibility=true
        binding.bottomvisible=bottomVisibility
    }

    private fun switchViewPagerToCurrentSong(song:Song){
        val newItemIndex=swipeSongAdapter.songs.indexOf(song)
        if(newItemIndex!=-1){
            binding.vpSong.currentItem=newItemIndex
            curPlayingSong=song
            binding.song=song
        }
    }
    fun playpause()
    {
        curPlayingSong?.let{
            mainViewModel.playOrToggleSong(it,true)
        }
    }
    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(this){
            it?.let{result->
                when(result.status){
                    Status.SUCCESS->{
                        result.data?.let{songs->
                            swipeSongAdapter.songs=songs
                            if(songs.isNotEmpty()){
                                //glide.load((curPlayingSong?:songs[0]).imageUrl).into(ivCurSongImage)
                                curPlayingSong=songs[0]


                            }
                            switchViewPagerToCurrentSong(curPlayingSong?:return@observe)
                        }

                    }
                    Status.ERROR->Unit
                    Status.LOADING->Unit
                }

            }
        }
        mainViewModel.curPlayingSong.observe(this){
            if(it==null)return@observe

            curPlayingSong=it.toSong()
            //glide.load(curPlayingSong?.imageUrl).into(ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong?:return@observe)//curPlayingSong이 nullable,null이 아닐경우 값을 가져감
        }
        mainViewModel.playbackState.observe(this){
            playbackState=it

            //ivCurSongImage.setImageResource(
              //      if(playbackState?.isPlaying==true)R.drawable.ic_pause else R.drawable.ic_play
            //)
            if(playbackState?.isPlaying==true)
                binding.playpauseimg=R.drawable.ic_pause
            else
                binding.playpauseimg=R.drawable.ic_play

        }

        mainViewModel.isConnected.observe(this){
            it?.getContentIfNotHandled()?.let{result->
                when(result.status){
                    Status.ERROR->Snackbar.make(binding.rootLayout,
                            result.message?:"An wnknown error occured",
                            Snackbar.LENGTH_LONG
                    ).show()
                    else->Unit

                }


            }
        }

        mainViewModel.networkError.observe(this){
            it?.getContentIfNotHandled()?.let{result->
                when(result.status){
                    Status.ERROR->Snackbar.make(binding.rootLayout,
                            result.message?:"An wnknown error occured",
                            Snackbar.LENGTH_LONG
                    ).show()
                    else->Unit

                }


            }
        }

    }
}