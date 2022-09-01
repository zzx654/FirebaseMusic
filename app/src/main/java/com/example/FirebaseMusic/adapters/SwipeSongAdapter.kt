package com.example.spotifyclone.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyclone.R
import javax.inject.Inject

class SwipeSongAdapter :BaseSongAdapter(R.layout.swipe_item){

    override val differ=AsyncListDiffer(this,diffCallback)


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val song = songs[position]
        val text = "${song.title}-${song.subtitle}"
        (holder as SongMainViewHolder).bind(song)
        holder.binding.root.setOnClickListener {
            onItemClickListener?.let { click ->
                click(song)

            }
        }
    }
}