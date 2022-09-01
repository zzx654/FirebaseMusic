package com.example.spotifyclone.adapters

import android.app.LauncherActivity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingMethod
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.spotifyclone.R
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.databinding.ListItemBinding
import com.example.spotifyclone.databinding.SwipeItemBinding

 abstract class BaseSongAdapter(private val layoutId:Int,): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class SongFragViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song?) {
            binding.song=song
        }
    }

     class SongMainViewHolder(val binding:SwipeItemBinding):RecyclerView.ViewHolder(binding.root){

         fun bind(song:Song?){
            binding.song=song

         }
     }
//뷰모델의 song을 얻어야함
    protected val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }
    }

    protected abstract val differ:AsyncListDiffer<Song>

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        if (layoutId == R.layout.list_item) {
            return DataBindingUtil.inflate<ListItemBinding>(layoutInflater, layoutId, parent, false).let {
                SongFragViewHolder(it)
            }
        } else {
            return DataBindingUtil.inflate<SwipeItemBinding>(layoutInflater, layoutId, parent, false).let {
                SongMainViewHolder(it)
            }


        }


    }


    protected var onItemClickListener: ((Song) -> Unit)? = null

    fun setItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener//1.인자로 받아 할당한후에
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    /**fun PlaybackSong(song: Song) {
        onItemClickListener?.let {
            it(song)
        }

    }**/

    object BindingConversions {
        @BindingAdapter("songImage")
        @JvmStatic
        fun bindsongImage(view: ImageView, imageUrl: String?) {
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(view.context).load(imageUrl).into(view)
            }
        }

        @BindingAdapter("vpText")
        @JvmStatic
        fun bindvpText(view: TextView, curSong: Song) {
                view.text="${curSong.title}-${curSong.subtitle}"
        }
        @BindingAdapter("playpauseImg")
        @JvmStatic
        fun bindImg(view: ImageView, curSong: Int) {
            view.setImageResource(curSong)
        }

    }
}
