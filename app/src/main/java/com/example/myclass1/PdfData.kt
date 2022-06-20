package com.example.myclass1

import com.google.firebase.database.Exclude

data class PdfData(var pdf_id:String?="",
                   var name :String="" ,
                   val date :String="",
                   val time:String="" ,
                   val user_uploaded_uid:String="",
                   val user_uploaded_name:String?="",
                   var courseId:String?="",
                   val url:String?="",
                   var userModeraterClassID:String?="",
                   var pdfNameinStorage:String?=""){

    @Exclude
    fun getMap():Map<String,Any?>{
        return mapOf(
            "name" to name,

            )
    }
    @Exclude
    fun getpdfId():String{
        return pdf_id!!
    }
}
