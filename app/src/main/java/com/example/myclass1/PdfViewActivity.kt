package com.example.myclass1

import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myclass1.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class PdfViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewBinding
    var pdf_id=""
    private lateinit var progressDialog:ProgressDialog

    private companion object{
        const val TAG="PDF_VIEW_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pdf_id= intent.getStringExtra("pdfId")!!
        loadPdfDetails()

        progressDialog=ProgressDialog(this)
        progressDialog.setTitle("Please Wait ...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnBackPdfLayout.setOnClickListener { onBackPressed() }

        binding.pdfDownload.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                Log.d(TAG,"STORAGE PERMISSION is already granted")
                Toast.makeText(this, "STORAGE PERMISSION is already granted", Toast.LENGTH_SHORT).show()
                downloadPdftoLocalStorage(pdf_id)
            }
            else{
                Log.d(TAG,"STORAGE PERMISSION was not Granted")
                Toast.makeText(this, "STORAGE PERMISSION was not Granted ", Toast.LENGTH_SHORT).show()
                requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }




    }
    private val requestStoragePermissionLauncher= registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted:Boolean->
        // testiw granted wale : ma3neha el utilisateur 7att allow permission lel storage wale
        if (isGranted){
            downloadPdftoLocalStorage(pdf_id)
        }
        else{
            Toast.makeText(this, "PERMISSION denied ", Toast.LENGTH_SHORT).show()
        }

    }

    private fun downloadPdftoLocalStorage(pdfId: String) {

        progressDialog.setMessage("Downloading PDF ...")
        progressDialog.show()
        val ref=FirebaseDatabase.getInstance().getReference("Documents")
        ref.child(pdfId)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get pdf url
                    val pdfURl=snapshot.child("url").value
                    val pdfName=snapshot.child("name").value.toString()
                    Log.d(TAG,"onDataChange : PDF_URL : $pdfURl")
                    val reference=FirebaseStorage.getInstance().getReferenceFromUrl(pdfURl.toString())
                    reference.getBytes(Constants.MAX_BYTES_PDF)
                        .addOnSuccessListener {bytes->
                            Log.d(TAG,"dowwnload pdf : Pdf downloaded ")
                            saveToDownloadsFolder(bytes,pdfName)
                        }
                        .addOnFailureListener { e->

                            Log.d(TAG,"dowwnload pdf : Failed to download pdf due to ${e.message} ")
                            Toast.makeText(applicationContext, "Failed to download pdf due to ${e.message} ", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun saveToDownloadsFolder(bytes: ByteArray?, pdfName: String) {
        //val nameWithExtention ="${System.currentTimeMillis()}.pdf"
        val nameWithExtention ="${pdfName}.pdf"
        try {
            val downloadsFolder=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs() // create folder if not exists

            val filePath=downloadsFolder.path +"/"+nameWithExtention

            val out =FileOutputStream(filePath)
            out.write(bytes)
            out.close()
            Toast.makeText(applicationContext, "Saved to Downloads Folder", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }
        catch (e:Exception){
            Log.d(TAG,"Save dowwnload pdf to folder : Failed to save pdf due to ${e.message} ")
            Toast.makeText(applicationContext, "Failed to save pdf due to ${e.message}", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()

        }
    }

    private fun loadPdfDetails() {
        Log.d(TAG,"loadBookDetails: Get pdf from database")

        val ref=FirebaseDatabase.getInstance().getReference("Documents")
        ref.child(pdf_id)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get pdf url
                    val pdfURl=snapshot.child("url").value
                    val pdfName=snapshot.child("name").value
                    binding.readPdfTitle.text=pdfName.toString()
                    Log.d(TAG,"onDataChange : PDF_URL : $pdfURl")

                    //load pdf using url from firebase storage
                    loadPdfFromUrl("$pdfURl")

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun loadPdfFromUrl(pdfURl: String) {
        Log.d(TAG,"LoadPdfFromUrl : Get Pdf from firebase storage using URL ")
        //val reference=FirebaseStorage.getInstance().getReferenceFromUrl("/PdfDocuments/$pdfURl")
        val reference=FirebaseStorage.getInstance().getReferenceFromUrl(pdfURl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.d(TAG,"LoadPdfFromUrl : Pdf got from URL ")

                //load pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)//sett false to scroll vertical , set true to scroll horizontal
                    .onPageChange{page,pageCount->
                        //set current and total pages in toolbar subtitle
                        val currentPage=page+1  //  page tabda mel 0 zedana +1 bech nnabdwe mel 1
                        binding.pdfNumbers.text="$currentPage/$pageCount" // exp: 3/232
                        Log.d(TAG,"LoadPdfFromUrl :$currentPage/$pageCount ")
                    }
                    .onError{ t->
                        Log.d(TAG,"LoadPdfFromUrl :${t.message} ")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG,"LoadPdfFromUrl :${t.message} ")
                    }
                    .load()
                binding.progressBar.visibility=View.GONE

            }
            .addOnFailureListener { e->
                Log.d(TAG,"LoadPdfFromUrl : Failed to get URL due to ${e.message} ")
                binding.progressBar.visibility=View.GONE


            }

    }
}