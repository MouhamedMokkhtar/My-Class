package com.example.myclass1

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myclass1.databinding.ActivityDocumentsBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class DocumentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocumentsBinding

   // private lateinit var uploadPDFBtn: FloatingActionButton
    private lateinit var pdfList: ArrayList<PdfData>
    private lateinit var temporarypdfList: java.util.ArrayList<PdfData>
    private lateinit var documensAdapter: DocumentsAdapter
    private lateinit var documentsRecycleview: RecyclerView

    lateinit var auth: FirebaseAuth
    lateinit var DataBase: DatabaseReference
    lateinit var DataBase2: DatabaseReference

    lateinit var DataBase3: DatabaseReference

    //lateinit var courrse_id:String
    private var pdf_id:String?=null
    private lateinit var progressDialog: ProgressDialog


    private var selectPDFUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        progressDialog=ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        val intent = getIntent()
        val courrse_id = intent.getStringExtra("courseId").toString()
        val userModerterClassID = intent.getStringExtra("userModerterClassID").toString()
        val courseName=intent.getStringExtra("coursename").toString()

        binding.courseNameActionbar.text=courseName
        //Toast.makeText(this, "course_id = $courrse_id ", Toast.LENGTH_SHORT).show()


        readDataFromDataBase(courrse_id)
        /**set Liste */
        pdfList = java.util.ArrayList()
        temporarypdfList = java.util.ArrayList()

        /** set the  recycle view  */
        documentsRecycleview = findViewById(R.id.pdf_list_recycleview)
        documentsRecycleview.layoutManager = LinearLayoutManager(this)
        /**set adapter */
        documensAdapter = DocumentsAdapter(this, temporarypdfList)
        documentsRecycleview.adapter = documensAdapter

        /** button add pdf **/
        binding.btncreatpdf.setOnClickListener { addDocumentToDataBase(courrse_id,userModerterClassID) }

        /** btn back **/
        binding.btnBackCoursesLayout.setOnClickListener {
            onBackPressed()
        }

    }

    private fun readDataFromDataBase(courrse_id: String) {

        auth = FirebaseAuth.getInstance()
        DataBase = FirebaseDatabase.getInstance().getReference("Courses").child(courrse_id!!)
            .child("DocumentsID")
        //DataBase2=FirebaseDatabase.getInstance().getReference("Classes")

        DataBase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfList.clear()

                for (postSnapshot in snapshot.children) {
                    val pdf = postSnapshot.getValue(PdfData::class.java)
                    pdfList.add(pdf!!)
                }
                documensAdapter.notifyDataSetChanged()
                temporarypdfList.clear()
                temporarypdfList.addAll(pdfList)



            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    private fun addDocumentToDataBase(courrse_id: String, userModerterClassID: String) {
        DataBase = FirebaseDatabase.getInstance().getReference("Documents")
        DataBase2=FirebaseDatabase.getInstance().getReference("Users")
        val view = LayoutInflater.from(this)
        val v = view.inflate(R.layout.layout_dialog_upload_pdf, null)
        var pdfName = v.findViewById<EditText>(R.id.edt_pdfname_create)
        val addDialog = AlertDialog.Builder(this)
        val btnAddPdf: Button = v.findViewById(R.id.btn_add_PDF)
        val btnpickPdf:ImageButton=v.findViewById(R.id.pick_pdf)
        addDialog.setView(v)
        val dialog=addDialog.create()
        dialog.show()
        btnpickPdf.setOnClickListener {
            pdfPickIntent()

        }
        btnAddPdf.setOnClickListener {


            var nameofthepdf = pdfName.text.toString().trim()

            pdf_id = DataBase.push().key!!



            val dateofthepdf: String = formatTimeStamp()

            val timeofthepdf: String = getCurrentTime()



            if (TextUtils.isEmpty(nameofthepdf)) {
                // No name added
                pdfName.error = "No name added !! Please enter PDF name"
            }else if(selectPDFUri==null){
                Toast.makeText(this, "Pick pdf ", Toast.LENGTH_SHORT).show()
            } else {

                DataBase2.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        for (postSnapshot in snapshot.children) {
                            val user = postSnapshot.getValue(User::class.java)
                            if (user != null) {
                                if (auth.currentUser!!.uid.equals(user.uid)) {
                                    var nameofUser = user.name
                                    var pdf_url=selectPDFUri.toString()
                                    pdfList.add(PdfData(pdf_id, nameofthepdf, dateofthepdf, timeofthepdf,auth.currentUser!!.uid,nameofUser ,courrse_id,"",userModerterClassID,""))
                                    documensAdapter.notifyDataSetChanged()
                                    Toast.makeText(applicationContext, "Adding pdf Success", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()


                                    if (courrse_id != null) {
                                        uploadPdfToFirebaseStorage(pdf_id!!, nameofthepdf, dateofthepdf, timeofthepdf,auth.currentUser!!.uid,nameofUser ,courrse_id,userModerterClassID)
                                        //addPDFToDataBase(pdf_id!!, nameofthepdf, dateofthepdf, timeofthepdf,auth.currentUser!!.uid,nameofUser ,courrse_id,pdf_url)

                                    } else {
                                        Toast.makeText(applicationContext, "courseID is null ", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })




            }
        }


    }



    private fun pdfPickIntent() {
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action=Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch((intent))

    }


    val pdfActivityResultLauncher =registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),ActivityResultCallback<ActivityResult>{result ->

            if (result.resultCode== RESULT_OK){
                Log.d(TAG,"PDF Picked")
                selectPDFUri=result.data!!.data


            }
            else{
                Log.d(TAG,"PDF Pick Cancelled")
                Toast.makeText(this,"Cancelled",Toast.LENGTH_SHORT).show()

            }

        }
    )

    private fun addPDFToDataBase(pdfId: String, nameofthepdf: String, dateofthepdf: String, timeofthepdf: String,uid:String,name:String?, courseId: String, pdf_url: String) {
        auth = FirebaseAuth.getInstance()
        DataBase = FirebaseDatabase.getInstance().getReference("Documents")
        DataBase.child(pdfId)
            .setValue(PdfData(pdf_id, nameofthepdf, dateofthepdf, timeofthepdf,uid,name ,courseId,pdf_url))

        DataBase2 = FirebaseDatabase.getInstance().getReference("Courses")
        DataBase2.child(courseId).child("DocumentsID").child(pdfId)
            .setValue(PdfData(pdfId, nameofthepdf, dateofthepdf, timeofthepdf,uid,name,courseId,pdf_url))
    }



    //var selectPDFUri: Uri? = null
    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectPDFUri = data.data

            uploadPdfToFirebaseStorage(selectPDFUri)

        }
    }*/

    private fun uploadPdfToFirebaseStorage(
        pdfId: String,
        nameofthepdf: String,
        dateofthepdf: String,
        timeofthepdf: String,
        uid: String,
        nameofUser: String?,
        courrse_id: String,
        userModerterClassID: String
    ) {

        progressDialog.setMessage("Uploading PDF ...\n This may take a moment depending on the size of the PDF and the speed of your network connection")
        progressDialog.show()
        if(this.selectPDFUri ==null)return
        val filename=UUID.randomUUID().toString()
        val ref= FirebaseStorage.getInstance().getReference("/PdfDocuments/$filename")

        ref.putFile(selectPDFUri!!).addOnSuccessListener {taskSnapshot->
            /*ref.downloadUrl.addOnSuccessListener {
                savePdfinfoToDataBase(it.toString(),filename)
            }*/
            Log.d(TAG,"upload Pdf to storage : PDF uploaded now getting url ...")
            val uritask: Task<Uri> = taskSnapshot.storage.downloadUrl
            while (!uritask.isSuccessful);
            val uploadPdfUrl ="${uritask.result}"
            savePdfinfoToDataBase(pdfId,nameofthepdf,dateofthepdf,timeofthepdf,uid,nameofUser,courrse_id,uploadPdfUrl,userModerterClassID,filename)
            progressDialog.dismiss()
        }
            .addOnFailureListener{e->
                Log.d(TAG, "uploadPdfToFireBaseStorage Failed due to ${e.message}")
                Toast.makeText(this, "uploadPdfToFireBaseStorage Failed due to ${e.message} ", Toast.LENGTH_LONG).show()
                progressDialog.dismiss()
            }
    }

    private fun savePdfinfoToDataBase(
        pdfId: String,
        nameofthepdf: String,
        dateofthepdf: String,
        timeofthepdf: String,
        uid: String,
        nameofUser: String?,
        courrse_id: String,
        uploadPdfUrl: String,
        userModerterClassID: String,
        filename: String
    ) {

        auth = FirebaseAuth.getInstance()
        DataBase = FirebaseDatabase.getInstance().getReference("Documents")
        DataBase.child(pdfId)
            .setValue(PdfData(pdf_id, nameofthepdf, dateofthepdf, timeofthepdf,uid,nameofUser ,courrse_id,uploadPdfUrl,userModerterClassID,filename))

        DataBase2 = FirebaseDatabase.getInstance().getReference("Courses")
        DataBase2.child(courrse_id).child("DocumentsID").child(pdfId)
            .setValue(PdfData(pdfId, nameofthepdf, dateofthepdf, timeofthepdf,uid,nameofUser,courrse_id,uploadPdfUrl,userModerterClassID,filename))

    }


    private fun getCurrentTime(): String {
        val cal = Calendar.getInstance(Locale.ENGLISH).time
        var timeFormat = SimpleDateFormat("hh:mm", Locale.ENGLISH)
        var time = timeFormat.format(cal).toString()
        return time
    }

    private fun formatTimeStamp(): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        val timestamp = cal.timeInMillis
        return DateFormat.format("dd/MM/yyyy", cal).toString()
    }

}

