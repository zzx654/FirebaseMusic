package com.example.spotifyclone.other

data class Resource<out T>(val status:Status,val data:T?,val message: String?) {
    //클래스는 이름다음에 <T>가 옴
    companion object{
        fun<T> success(data:T? )=Resource(Status.SUCCESS,data,null)//함수는 이름앞에 <T>가옴

        fun<T> error(message:String,data:T?)=Resource(Status.ERROR,data,message)

        fun<T> loading(data:T?)=Resource(Status.LOADING,data,null)


    }
}

enum class Status{
    SUCCESS,
    ERROR,
    LOADING,


}