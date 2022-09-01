package com.example.spotifyclone.data.remote

import android.util.Log
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.other.Constants.SONG_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.tasks.await

class MusicDatabase {

    private val firestore=FirebaseFirestore.getInstance()
    private val songCollection=firestore.collection(SONG_COLLECTION)
    //private val compositeDisposable=CompositeDisposable()

    /**suspend fun getAllSongs():List<Song>{
        return try{

            songCollection.get().await().toObjects(Song::class.java)

        }catch(e:Exception){
            e.printStackTrace()
            throw e
            emptyList()
        }

    }**/

    fun getAllSongs(disposable: CompositeDisposable,returnData:(List<Song>)->Unit){
        val fetchedData=io.reactivex.Single.create<List<Song>> { emitter->
            songCollection
                    .get()
                    .addOnSuccessListener { querySnapshot->
                        val SongList: MutableList<Song> = mutableListOf<Song>()
                        for(item in querySnapshot.documents)
                        {
                            var song=item.toObject(Song::class.java)
                            SongList.add(song!!)
                        }
                        emitter.onSuccess(SongList)
                    }
                    .addOnFailureListener { exception->
                        emitter.onError(exception)
                    }
        }
            disposable.add(
                    fetchedData.
                            subscribeOn(io.reactivex.schedulers.Schedulers.io())
                            .subscribe(
                                    {fetchedData->
                                        returnData(fetchedData)
                                    }
                            ,
                                    {
                                        Log.e("Fetching Error",it.message!!)
                                    }
                            )
            )

    }


}