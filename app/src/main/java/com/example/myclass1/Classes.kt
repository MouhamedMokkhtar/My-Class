package com.example.myclass1

import com.google.firebase.database.Exclude

data class Classes(var class_id:String?="" ,var name :String="" ,val role :String="", var adminId:String?=""){

    @Exclude
    fun getMap():Map<String,Any?>{
        return mapOf(
            "name" to name,
            //"code" to code,
            "role" to role
        )
    }
    @Exclude
    fun getId():String{
        return class_id!!
    }

}

