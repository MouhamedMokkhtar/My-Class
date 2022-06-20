package com.example.myclass1

import android.app.AlertDialog
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

class CoursesAdapter (val c: Fragment, val l:android.content.Context?, val courselist: ArrayList<Course>) :
    RecyclerView.Adapter <CoursesAdapter.CoursesViewHolder> () {


    lateinit var auth: FirebaseAuth
    lateinit var DataBase : DatabaseReference
    lateinit var DataBase2 : DatabaseReference
   //private lateinit var mListener : CoursesAdapter.onItemClickListener


    interface onItemClickListener {

        fun onItemClick(position: Int)

    }
    /*fun setOnItemClickListener(listener: CoursesAdapter.onItemClickListener) {

        mListener = listener

    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoursesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.courses_items,parent,false)
        //return CoursesViewHolder(itemView, mListener)
        return CoursesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CoursesViewHolder, position: Int) {
        val currentitem = courselist[position]
        holder.courseName.text=currentitem.name
        holder.courseDate.text=currentitem.date

        holder.itemView.setOnClickListener {

            var courseid=currentitem.coursee_id
            var userModerterClassID=currentitem.userModerterClassID
            var coursename=currentitem.name
            val intent = Intent(l, DocumentsActivity::class.java)
            intent.putExtra("courseId", courseid)
            intent.putExtra("userModerterClassID", userModerterClassID)
            intent.putExtra("coursename",coursename)
            l?.startActivity(intent)
            //Toast.makeText(l,"class id is: $classid", Toast.LENGTH_SHORT).show()

        }
    }

    override fun getItemCount(): Int {
        return courselist.size
    }

    inner class CoursesViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
        var courseDate = v.findViewById<TextView>(R.id.date_course)
        var courseName = v.findViewById<TextView>(R.id.course_name)
        var courseMenu = v.findViewById<ImageView>(R.id.course_menu)

        init {
            courseMenu = v.findViewById<ImageView>(R.id.course_menu)
            courseMenu.setOnClickListener { popMenus(it) }



        }

        private fun popMenus(it: View?) {
            auth = FirebaseAuth.getInstance()
            val position =courselist[adapterPosition]

            if(position.userModerterClassID.equals(auth.currentUser!!.uid)){
                val popupMenus = PopupMenu(l, v)
                popupMenus.inflate(R.menu.course_menu)
                popupMenus.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.course_name_change -> {
                            val view = LayoutInflater.from(l)

                            /** << l >> hiya el activity :context*/
                            val v = view.inflate(R.layout.layout_dialog_update_course, null)
                            val addDialog = AlertDialog.Builder(l)
                            val btnUpdatecourse: Button = v.findViewById(R.id.btn_update_course)
                            var coursename = v.findViewById<EditText>(R.id.edt_coursename_update)

                            addDialog.setView(v)
                            val dialog=addDialog.create()
                            dialog.show()
                            btnUpdatecourse.setOnClickListener {

                                position.name = coursename.text.toString().trim()
                                val coursekey: String = position.coursee_id!!
                                var classidd=position.classesId
                                var date=position.date
                                var moderaterid=position.userModerterClassID
                                var usercrreatecourseid=position.userCreatedCourseID

                                updateCourse(coursekey, position.name,classidd,date,moderaterid,usercrreatecourseid)

                                Toast.makeText(l, "Course changed", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()


                            }

                            true
                        }
                        R.id.delete_course -> {
                            AlertDialog.Builder(l)
                                .setTitle("Delete")
                                .setIcon(R.drawable.ic_warning)
                                .setMessage("Are you sure you want to delete this Course !!")
                                .setPositiveButton("Yes") { dialog, _ ->
                                    courselist.removeAt(adapterPosition)
                                    notifyDataSetChanged()
                                    val coursekey: String = position.getCourseId()
                                    val classid=position.classesId

                                    deleteCourse(coursekey,classid)

                                    Toast.makeText(l, "Course deleted", Toast.LENGTH_SHORT).show()
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
                val alertDialog = AlertDialog.Builder(l)
                alertDialog.apply {
                    setTitle("Access Denied")
                    setMessage("You don't have permissions to acces this course settings")

                    setNegativeButton("Dismiss") { _, _ ->
                        Toast.makeText(context, "Dismiss", Toast.LENGTH_SHORT).show()
                    }
                }.create().show()
            }

        }

        private fun deleteCourse(coursekey: String, classid: String?) {
            auth = FirebaseAuth.getInstance()
            DataBase = FirebaseDatabase.getInstance().getReference("Classes").child(classid!!).child("CoursesID")
            DataBase.child(coursekey).removeValue()

            DataBase2 = FirebaseDatabase.getInstance().getReference("Courses")
            DataBase2.child(coursekey).removeValue()
        }

        private fun updateCourse(
            coursekey: String,
            name: String,
            classid: String?,
            date: String,
            moderaterid: String?,
            usercrreatecourseid: String?,) {

            auth = FirebaseAuth.getInstance()
            val courseUpdate = Course(coursekey, name,date ,classid,moderaterid,usercrreatecourseid)

            DataBase = FirebaseDatabase.getInstance().getReference("Classes").child(classid!!).child("CoursesID")
            DataBase.child(coursekey).updateChildren(courseUpdate.getMap())

            DataBase2 = FirebaseDatabase.getInstance().getReference("Courses")
            DataBase2.child(coursekey).updateChildren(courseUpdate.getMap())
        }


    }


}