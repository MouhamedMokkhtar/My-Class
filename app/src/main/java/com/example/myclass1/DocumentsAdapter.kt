package com.example.myclass1

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class DocumentsAdapter (var context:Context,val pdflist: ArrayList<PdfData>) : RecyclerView.Adapter <DocumentsAdapter.DocumentsViewHolder> () {


    lateinit var auth: FirebaseAuth
    lateinit var DataBase : DatabaseReference
    lateinit var DataBase2 : DatabaseReference
    //private lateinit var mListener : CoursesAdapter.onItemClickListener



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.documents_items,parent,false)
        return DocumentsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DocumentsViewHolder, position: Int) {
        val currentitem = pdflist[position]
        holder.pdfName.text=currentitem.name
        holder.pdfDate.text=currentitem.date
        holder.pdfTime.text=currentitem.time
        holder.pdfUser.text=currentitem.user_uploaded_name

        holder.itemView.setOnClickListener {

            var pdfid=currentitem.pdf_id
            val intent = Intent(context, PdfViewActivity::class.java)
            intent.putExtra("pdfId", pdfid)
            context.startActivity(intent)

            //Toast.makeText(l,"class id is: $classid", Toast.LENGTH_SHORT).show()

        }
    }

    override fun getItemCount(): Int {
        return pdflist.size
    }

    inner class DocumentsViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
        var pdfName = v.findViewById<TextView>(R.id.pdf_name)
        var pdfDate = v.findViewById<TextView>(R.id.date_pdf)
        var pdfTime = v.findViewById<TextView>(R.id.time_pdf)
        var pdfUser = v.findViewById<TextView>(R.id.user_uploaded_pdf_name)
        var pdfDownload=v.findViewById<ImageButton>(R.id.pdf_download)

        var documentMenu = v.findViewById<ImageView>(R.id.document_menu)
        init {
            documentMenu = v.findViewById<ImageView>(R.id.document_menu)
            documentMenu.setOnClickListener { popMenus(it) }



        }

        private fun popMenus(it: View?) {
            auth = FirebaseAuth.getInstance()
            val position =pdflist[adapterPosition]

            if(position.userModeraterClassID.equals(auth.currentUser!!.uid)){
                val popupMenus = PopupMenu(context, v)
                popupMenus.inflate(R.menu.document_menu)
                popupMenus.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.document_name_change -> {
                            val view = LayoutInflater.from(context)

                            /** << l >> hiya el activity :context*/
                            val v = view.inflate(R.layout.layout_dialog_update_document, null)
                            val addDialog = AlertDialog.Builder(context)
                            val btnUpdatedocument: Button = v.findViewById(R.id.btn_update_document)
                            var documentname = v.findViewById<EditText>(R.id.edt_documentname_update)

                            addDialog.setView(v)
                            val dialog=addDialog.create()
                            dialog.show()
                            btnUpdatedocument.setOnClickListener {

                                position.name = documentname.text.toString().trim()
                                val userModeraterClassID: String = position.userModeraterClassID!!
                                var courseid=position.courseId
                                var documentid=position.pdf_id
                                var date=position.date
                                var time=position.time
                                var userUploadeduid=position.user_uploaded_uid
                                var userUploadedname=position.user_uploaded_name
                                var url=position.url
                                var pdfNameinStorage=position.pdfNameinStorage


                                updateDocument(documentid, position.name,date,time,userUploadeduid,userUploadedname,courseid,url,userModeraterClassID,pdfNameinStorage)

                                Toast.makeText(context, "Document changed", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()


                            }

                            true
                        }
                        R.id.delete_document -> {
                            AlertDialog.Builder(context)
                                .setTitle("Delete")
                                .setIcon(R.drawable.ic_warning)
                                .setMessage("Are you sure you want to delete this Document !!")
                                .setPositiveButton("Yes") { dialog, _ ->
                                    pdflist.removeAt(adapterPosition)
                                    notifyDataSetChanged()
                                    val documentkey: String = position.getpdfId()
                                    val courseid=position.courseId
                                    var pdfNameinStoragee=position.pdfNameinStorage

                                    deleteCourse(documentkey,courseid,pdfNameinStoragee)

                                    //Toast.makeText(context, "Document deleted", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()


                                }
                                .setNegativeButton("Cancel") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                                .show()

                            true
                        }
                        else -> true

                    }
                }
                popupMenus.show()
                val popup = PopupMenu::class.java.getDeclaredField("mPopup")
                popup.isAccessible = true
                val menu = popup.get(popupMenus)
                menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(menu, true)

            }
            else{
                val alertDialog = AlertDialog.Builder(context)
                alertDialog.apply {
                    setTitle("Access Denied")
                    setMessage("You don't have permissions to acces this document settings")

                    setNegativeButton("Dismiss") { _, _ ->
                        Toast.makeText(context, "Dismiss", Toast.LENGTH_SHORT).show()
                    }
                }.create().show()
            }

        }

        private fun deleteCourse(documentkey: String, courseid: String?, pdfNameinStoragee: String?) {

            auth = FirebaseAuth.getInstance()
            DataBase = FirebaseDatabase.getInstance().getReference("Courses").child(courseid!!).child("DocumentsID")
            DataBase.child(documentkey).removeValue()

            DataBase2 = FirebaseDatabase.getInstance().getReference("Documents")
            DataBase2.child(documentkey).removeValue()



            val ref= FirebaseStorage.getInstance().getReference("/PdfDocuments/$pdfNameinStoragee")

            ref.delete().addOnSuccessListener {
                Toast.makeText(context, "Document deleted from storage", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Document deleted failed due to ${it.message}", Toast.LENGTH_SHORT).show()

            }

        }

        private fun updateDocument(
            documentid: String?,
            name: String,
            date: String,
            time: String,
            userUploadeduid: String,
            userUploadedname: String?,
            courseid: String?,
            url: String?,
            userModeraterClassID: String,
            pdfNameinStorage: String?
        ) {

            auth = FirebaseAuth.getInstance()
            val documentUpdate = PdfData(documentid, name,date ,time,userUploadeduid,userUploadedname,courseid,url,userModeraterClassID,pdfNameinStorage)

            DataBase = FirebaseDatabase.getInstance().getReference("Courses").child(courseid!!).child("DocumentsID")
            DataBase.child(documentid!!).updateChildren(documentUpdate.getMap())

            DataBase2 = FirebaseDatabase.getInstance().getReference("Documents")
            DataBase2.child(documentid!!).updateChildren(documentUpdate.getMap())

        }


    }



}