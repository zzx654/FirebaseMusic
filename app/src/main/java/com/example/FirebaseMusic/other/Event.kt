package com.example.spotifyclone.other

class Event<out T>(private val data: T) {


    var hasbeenhandled=false
    private set

    fun getContentIfNotHandled():T?{
        return if(hasbeenhandled){
            null
        }
        else{
            hasbeenhandled=true
            data
        }
    }

    fun peekContent()=data
}