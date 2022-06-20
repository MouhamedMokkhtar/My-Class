package com.example.myclass1

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*


class CoursesFragment : Fragment() {

    private lateinit var createCourseBtn : FloatingActionButton
    private lateinit var courseList : ArrayList<Course>
    private lateinit var temporarycoursesList : java.util.ArrayList<Course>
    private lateinit var coursesAdapter: CoursesAdapter
    private lateinit var courseRecycleview: RecyclerView

    lateinit var auth: FirebaseAuth
    lateinit var DataBase : DatabaseReference
    lateinit var DataBase2 : DatabaseReference

    lateinit var DataBase3 : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v=inflater.inflate(R.layout.fragment_courses, container, false)
        /** i brin the class id with the function i made in activity: DetailledClassActivity of this fragment **/
        val activity = activity as DetailedClassActivity?
        val classID: String? = activity?.getMyClassID()
        val moderaterID: String? =activity?.getMyModeraterID()
        //Toast.makeText(activity,"class: $data", Toast.LENGTH_SHORT).show()

        /** menu : seach and notification display**/
        setHasOptionsMenu(true)

        /** jina data bech inajmou yattl3ouli les courses **/
        readDataFromDataBase(classID)

        /** initialisation  of recycleView and Adapters **/
        initRecyclerView(v)



        createCourseBtn = v.findViewById(R.id. btncreatcourse)
        createCourseBtn.setOnClickListener { addCoursesInfo(classID,moderaterID) }



        return v
    }

    private fun readDataFromDataBase(data: String?) {
        auth = FirebaseAuth.getInstance()
        DataBase = FirebaseDatabase.getInstance().getReference("Classes").child(data!!).child("CoursesID")
        //DataBase2=FirebaseDatabase.getInstance().getReference("Classes")

        DataBase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                courseList.clear()

                for (postSnapshot in snapshot.children){
                    val course =postSnapshot.getValue(Course::class.java)
                    courseList.add(course!!)
                }
                coursesAdapter.notifyDataSetChanged()
                temporarycoursesList.clear()
                temporarycoursesList.addAll(courseList) /**7atena list mta3 el classes bba3d mat3abet fel temprarylist ali chnnesta5dmoha **/


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun addCoursesInfo(data: String?, moderaterID: String?) {
        // add a new course
        auth = FirebaseAuth.getInstance()
        DataBase3 = FirebaseDatabase.getInstance().getReference("Courses")
        val view = LayoutInflater.from(activity)
        val v= view.inflate(R.layout.layout_dialog_create_course,null)
        var Coursename = v.findViewById<EditText>(R.id.edt_Coursename_create)
        val btnAddcourse : Button =v.findViewById(R.id.btn_add_Course)

        val addDialog= AlertDialog.Builder(context)
        addDialog.setView(v)
        val dialog=addDialog.create()
        dialog.show()

        btnAddcourse.setOnClickListener {


            var nameofthecourse = Coursename.text.toString().trim()

            var course_id :String?=DataBase3.push().key

            var classId = data
            var moderatedId=moderaterID
            var userCreatedcourseID:String=auth.currentUser!!.uid

            val dateofthecourse: String = formatTimeStamp()

            if (TextUtils.isEmpty(nameofthecourse)) {
                // No name added
                Coursename.error = "No name added !! Please enter the name of the class"
            }
            else{
                courseList.add(Course(course_id,nameofthecourse,dateofthecourse,classId,moderatedId,userCreatedcourseID))
                coursesAdapter.notifyDataSetChanged()
                Toast.makeText(activity,"Adding Course Success", Toast.LENGTH_SHORT).show()
                //Toast.makeText(activity,"class_id is $classId ", Toast.LENGTH_SHORT).show()

                if (classId != null) {

                    addCourseToDataBase(course_id!!,nameofthecourse,dateofthecourse,classId,moderatedId,userCreatedcourseID)

                    dialog.dismiss()




                }else{
                    Toast.makeText(activity,"class_id is null ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun addCourseToDataBase(
        courseId: String,
        nameofthecourse: String,
        dateofthecourse: String,
        classId: String,
        moderatedId: String?,
        userCreatedcourseID: String
    ) {
        auth = FirebaseAuth.getInstance()
        DataBase = FirebaseDatabase.getInstance().getReference("Courses")
        DataBase.child(courseId).setValue(Course(courseId,nameofthecourse, dateofthecourse, classId,moderatedId,userCreatedcourseID))

        DataBase2=FirebaseDatabase.getInstance().getReference("Classes")
        DataBase2.child(classId).child("CoursesID").child(courseId).setValue(Course(courseId,nameofthecourse, dateofthecourse, classId,moderatedId,userCreatedcourseID))
    }

    private fun formatTimeStamp(): String {
        val cal=Calendar.getInstance(Locale.ENGLISH)
        val timestamp=cal.timeInMillis
        return DateFormat.format("dd/MM/yyyy",cal).toString()
    }

    private fun initRecyclerView(v: View) {
        /**set Liste */
        courseList= java.util.ArrayList()
        temporarycoursesList= java.util.ArrayList()

        /** set the  recycle view  */
        courseRecycleview=v.findViewById(R.id.course_list_recycleview)
        courseRecycleview.layoutManager = LinearLayoutManager(activity)
        /**set adapter */
        coursesAdapter= CoursesAdapter(this,context,temporarycoursesList)
        courseRecycleview.adapter= coursesAdapter

        /** click in the course to go to next activity but we are not using it **/
        /*coursesAdapter.setOnItemClickListener(object :  CoursesAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                //val intent = Intent(context, DetailedClassActivity::class.java)
                //startActivity(intent)

            }
        })*/

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.app_bar_menu, menu)
        val item = menu.findItem(R.id.misearch)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(newText: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                //txtSearch(newText)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu, inflater)
    }


}