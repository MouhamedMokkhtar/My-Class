package com.example.myclass1

import com.google.firebase.database.Exclude

data class Course(var coursee_id:String?="" ,var name :String="" ,val date :String="", var classesId:String?="",var userModerterClassID:String?="",var userCreatedCourseID:String?=""){

    @Exclude
    fun getMap():Map<String,Any?>{
        return mapOf(
            "name" to name,

        )
    }
    @Exclude
    fun getCourseId():String{
        return coursee_id!!
    }
}
