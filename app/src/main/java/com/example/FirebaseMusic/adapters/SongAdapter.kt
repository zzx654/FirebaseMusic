package com.example.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.databinding.ListItemBinding
import javax.inject.Inject

class SongAdapter @Inject constructor(
         private val glide: RequestManager
):BaseSongAdapter(R.layout.list_item){

    override val differ=AsyncListDiffer(this,diffCallback)


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        val song=songs[position]

        (holder as SongFragViewHolder).bind(song)
        holder.binding.root.setOnClickListener {
            onItemClickListener?.let{click->
                click(song)

            }
        }
        //glide.load(song.imageUrl).into(Iv_ItemImage)
         /** holder.itemView.apply {
            tvPrimary.text=song.title
            tvSecondary.text=song.subtitle

            setOnClickListener{
                onItemClickListener?.let{
                    it(song)//2.1null이 아닐시에 실행하는 문장이므로 가능한 현상(초기화가 되지않으면 실행x)
                }
            }//아이템뷰 의 클릭 리스너를 설정하는부분

        }**/
    }




}