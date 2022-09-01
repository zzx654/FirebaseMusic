 package com.example.spotifyclone.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spotifyclone.R
import com.example.spotifyclone.adapters.SongAdapter
import com.example.spotifyclone.databinding.FragmentHomeBinding
import com.example.spotifyclone.other.Status
import com.example.spotifyclone.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

 @AndroidEntryPoint
class HomeFragment: Fragment() {

    lateinit var mainViewModel : MainViewModel

    lateinit var binding:FragmentHomeBinding


            @Inject
            lateinit var songAdapter:SongAdapter

     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
     {
         binding= DataBindingUtil.inflate<FragmentHomeBinding>(inflater, R.layout.fragment_home, container, false)//프래그먼트 데이터바인딩
         mainViewModel=ViewModelProvider(requireActivity()).get(MainViewModel::class.java)//lifecycle에 bind하게 하지 않기위함
         setupRecyclerView()
         subscribeToObservers()
         songAdapter.setItemClickListener {song->
             mainViewModel.playOrToggleSong(song)
         }//람다 함수를 인자로 넘겨 setOnItemClickListener호출(리스너 변수 할당),리사이클러뷰 원소 클릭시 mainviewmodel에서 생성자로 받는 musicserviceConnection의 요소들을 이용한 playorToggleSong 호출하기 위함
         return binding.root
     }


     private fun setupRecyclerView()=binding.rvAllSongs.apply{
         adapter=songAdapter
         layoutManager=LinearLayoutManager(requireContext())//fragment가 호스트에 붙어있을때 사용 하는 requireContext

     }
     private fun subscribeToObservers(){
         //뷰모델의 mediaItem(status와 songdata를 가진 resource를 관찰
         mainViewModel.mediaItems.observe(viewLifecycleOwner){result->
             when(result.status){
                 Status.SUCCESS->{
                    binding.allSongsProgressBar.isVisible=false
                     result.data.let{songs->
                         songAdapter.songs=songs!!

                     }
                 }
                 Status.ERROR->Unit
                 Status.LOADING->binding.allSongsProgressBar.isVisible=true
             }
         }
     }



}