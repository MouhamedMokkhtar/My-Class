package com.example.myclass1

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ClassesAdapter(val c:Fragment,val l:android.content.Context? ,val classeslist: ArrayList<Classes>) : RecyclerView.Adapter <ClassesAdapter.ClassesViewHolder> (){
    lateinit var auth: FirebaseAuth
    lateinit var DataBase : DatabaseReference
    lateinit var DataBase2 : DatabaseReference
    lateinit var DataBase3 : DatabaseReference
    lateinit var clipboardManager: ClipboardManager


    private lateinit var mListener :onItemClickListener

    interface onItemClickListener {

        fun onItemClick(position :Int)

    }

    /*fun setOnItemClickListener(listener : onItemClickListener){

       // mListener=listener

    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.class_items2,parent,false)
        //return ClassesViewHolder(itemView,mListener)
        return ClassesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ClassesViewHolder, position: Int) {
        val currentitem = classeslist[position]
        holder.classname.text = currentitem.name
        holder.classrole.text = currentitem.role

        holder.itemView.setOnClickListener {

            var classid=currentitem.class_id
            val moderaterid=currentitem.adminId
            var classname=currentitem.name
            val intent = Intent(l, DetailedClassActivity::class.java)
            intent.putExtra("classId", classid)
            intent.putExtra("moderaterId",moderaterid)
            intent.putExtra("classname",classname)
            l?.startActivity(intent)
            //Toast.makeText(l,"class id is: $classid", Toast.LENGTH_SHORT).show()

        }

    }



    override fun getItemCount(): Int {
        return classeslist.size
    }
    //, listener: onItemClickListener
    inner class ClassesViewHolder (val v: View): RecyclerView.ViewHolder(v) {
        var classname = v.findViewById<TextView>(R.id.class_name)
        var classrole = v.findViewById<TextView>(R.id.role_name)
        var classMenu = v.findViewById<ImageView>(R.id.class_menu)
        var classcodedialog = v.findViewById<ImageView>(R.id.bt_getcode)


        init {
            classMenu = v.findViewById<ImageView>(R.id.class_menu)
            classMenu.setOnClickListener { popMenus(it) }

            classcodedialog = v.findViewById<ImageView>(R.id.bt_getcode)
            classcodedialog.setOnClickListener { popCode(it) }

        }


        /* init {
            v.setOnClickListener {
                listener.onItemClick(adapterPosition)

            }
        }*/

        private fun popCode(it: View?) {
            val position = classeslist[adapterPosition]
            val view = LayoutInflater.from(l)
            val v = view.inflate(R.layout.layout_dialog_popcode, null)
            var classcode = v.findViewById<TextView>(R.id.codeoftheclass)
            val addDialog = AlertDialog.Builder(l)
            addDialog.setView(v)
            addDialog.create()
            addDialog.show()
            classcode.text = position.class_id
            val copycode = v.findViewById<Button>(R.id.copycode)
            copycode.setOnClickListener {
                var codeoftheclass = classcode.text.toString().trim()
                if (codeoftheclass.isNotEmpty()) {

                    //clipboardManager = getSystemService(activity,CLIPBOARD_SERVICE::class.java) as ClipboardManager
                    clipboardManager = l!!.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

                    val clipData = ClipData.newPlainText("key", codeoftheclass)
                    clipboardManager.setPrimaryClip(clipData)
                    Toast.makeText(l, "Copied", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(l, "No text to be copied", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun popMenus(v: View) {
            auth = FirebaseAuth.getInstance()
            val position = classeslist[adapterPosition]
            if (position.role.equals("Moderator")) {
                val popupMenus = PopupMenu(l, v)
                popupMenus.inflate(R.menu.class_menu)
                popupMenus.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.class_name_change -> {
                            val view = LayoutInflater.from(l)

                            /** << l >> hiya el activity :context*/
                            val v = view.inflate(R.layout.layout_dialog_update_class, null)
                            val addDialog = AlertDialog.Builder(l)
                            val btnUpdateclass: Button = v.findViewById(R.id.btn_update_class)
                            var classname = v.findViewById<EditText>(R.id.edt_classname_update)
                            var adminId = (auth.currentUser!!.uid).toString()
                            addDialog.setView(v)
                            addDialog.create()
                            addDialog.show()
                            btnUpdateclass.setOnClickListener {
                                if (position.role.equals("Moderator")) {
                                    position.name = classname.text.toString().trim()

                                    val key1: String = position.getId()
                                    updateData(key1, position.name, adminId)
                                    Toast.makeText(l, "Class changed", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(
                                        l,
                                        "You don't have permission to edit this Class",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }


                            }

                            true
                        }
                        R.id.delete_class -> {
                            AlertDialog.Builder(l)
                                .setTitle("Delete")
                                .setIcon(R.drawable.ic_warning)
                                .setMessage("Are you sure you want to delete this Class !!")
                                .setPositiveButton("Yes") { dialog, _ ->
                                    classeslist.removeAt(adapterPosition)
                                    notifyDataSetChanged()
                                    val key2: String = position.getId()
                                    if (position.role.equals("Moderator")) {
                                        deleteClassAdmin(key2)
                                        Toast.makeText(l, "Class deleted", Toast.LENGTH_SHORT)
                                            .show()
                                        dialog.dismiss()
                                    } else {
                                        deleteClassUser(key2)
                                    }

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
            } else {

                    val popupMenus = PopupMenu(l, v)
                    popupMenus.inflate(R.menu.class_menu_users)
                    popupMenus.setOnMenuItemClickListener {
                        when (it.itemId) {

                            R.id.leave_class -> {
                                AlertDialog.Builder(l)
                                    .setTitle("Leave Class")
                                    .setIcon(R.drawable.ic_warning)
                                    .setMessage("Are you sure you want to Leave this Class !!")
                                    .setPositiveButton("Yes") { dialog, _ ->
                                        classeslist.removeAt(adapterPosition)
                                        notifyDataSetChanged()
                                        val key2: String = position.getId()
                                        deleteClassUser(key2)


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


        }

        private fun updateData(key: String, nameoftheclass: String, adminId: String) {
            auth = FirebaseAuth.getInstance()
            DataBase =
                FirebaseDatabase.getInstance().getReference("Users").child(auth.currentUser!!.uid)
                    .child("classes")
            DataBase2 = FirebaseDatabase.getInstance().getReference("Classes")
            val classUpdate = Classes(key, nameoftheclass, "Moderator", adminId)
            DataBase.child(key).updateChildren(classUpdate.getMap())
            DataBase2.child(key).updateChildren(classUpdate.getMap())

        }


        private fun deleteClassAdmin(key: String) {
            auth = FirebaseAuth.getInstance()
            DataBase = FirebaseDatabase.getInstance().getReference("Users").child(auth.currentUser!!.uid).child("classes")
            DataBase.child(key).removeValue()

            DataBase2 = FirebaseDatabase.getInstance().getReference("Classes")
            DataBase2.child(key).removeValue()
        }

        private fun deleteClassUser(key: String) {
            auth = FirebaseAuth.getInstance()
            DataBase2 = FirebaseDatabase.getInstance().getReference("Users").child(auth.currentUser!!.uid)
                .child("classes")
            DataBase2.child(key).removeValue()

            //deleteUserFromClase(key)
            DataBase = FirebaseDatabase.getInstance().getReference("Classes")
            DataBase.child(key).child("ParticipantsID").child(auth.currentUser!!.uid).removeValue()




            Toast.makeText(l, " delete class User entred", Toast.LENGTH_LONG).show()

        }


    }
}