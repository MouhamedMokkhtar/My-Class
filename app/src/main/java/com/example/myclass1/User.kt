package com.example.myclass1

import com.google.firebase.database.Exclude

data class User(var name :String?="", var birthday: String?="", var email :String?="", var uid :String?="", var profileImageUrl:String?=""){
    @Exclude
    fun getMap():Map<String,Any?>{
        return mapOf(
            "name" to name,
            "profileImageUrl" to profileImageUrl
            )
    }
}



