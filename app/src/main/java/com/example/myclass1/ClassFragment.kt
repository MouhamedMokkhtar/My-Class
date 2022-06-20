package com.example.myclass1

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*


class ClassFragment : Fragment()  {

    private lateinit var classesList :ArrayList<Classes>
    private lateinit var classesListforLooping :ArrayList<Classes>
    private lateinit var temporaryclassesList :ArrayList<Classes>
     private lateinit var classesAdapter: ClassesAdapter
     private lateinit var classRecycleview: RecyclerView
     private lateinit var createClassBtn :FloatingActionButton
     private lateinit var shimmerFrameLayout:ShimmerFrameLayout
     private lateinit var msgForEmptyRecview:TextView


    lateinit var auth: FirebaseAuth
    lateinit var DataBase : DatabaseReference
    lateinit var DataBase2 : DatabaseReference




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v=inflater.inflate(R.layout.fragment_class, container, false)
        initRecyclerView(v)
        shimmerFrameLayout.startShimmer()
        readDataFromDataBase()
        createClassBtn = v.findViewById(R.id.btncreatClass)


        createClassBtn.setOnClickListener { addInfo() }

       /* classesAdapter.setOnItemClickListener(object :  ClassesAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                //communicator.sendClassIdData(classesList[position].class_id.toString())

                val intent = Intent(context, DetailedClassActivity::class.java)
                intent.putExtra("classId",classesList[position].class_id)
                startActivity(intent)

            }
        })*/




        setHasOptionsMenu(true)

        return v
    }


    private fun readDataFromDataBase() {
        auth = FirebaseAuth.getInstance()
        DataBase = FirebaseDatabase.getInstance().getReference("Users").child(auth.currentUser!!.uid).child("classes")
        //DataBase2=FirebaseDatabase.getInstance().getReference("Classes")

        DataBase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                classesList.clear()

                    for (postSnapshot in snapshot.children){
                        val classe =postSnapshot.getValue(Classes::class.java)
                        classesList.add(classe!!)
                    }
                    if(classesList.isEmpty()){
                        msgForEmptyRecview.visibility=View.VISIBLE
                        shimmerFrameLayout.visibility = View.GONE
                        classRecycleview.visibility = View.GONE
                    }else{
                        msgForEmptyRecview.visibility=View.GONE
                        shimmerFrameLayout.stopShimmer()
                        shimmerFrameLayout.visibility = View.GONE
                        classRecycleview.visibility = View.VISIBLE
                    }

                    classesAdapter.notifyDataSetChanged()
                temporaryclassesList.clear()
                temporaryclassesList.addAll(classesList) /**7atena list mta3 el classes bba3d mat3abet fel temprarylist ali chnnesta5dmoha **/
                classesList.clear()
                /** test for the data fletchin **/


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }


    private fun addInfo() {
        auth = FirebaseAuth.getInstance()
        DataBase2= FirebaseDatabase.getInstance().getReference("Classes")
        /** jevna el view ali feha el button mta3 add class 5aterha mahich fel main layout mta3 fragment :mawjouda fel dialog layout*/
        val view = LayoutInflater.from(activity)
        val v= view.inflate(R.layout.layout_dialog,null)

        /**set view*/
        var classname = v.findViewById<EditText>(R.id.edt_classname_create)

        /**dismissKeybord(classname)**/
        /**dismissKeybord(classcode)**/
        val addDialog= AlertDialog.Builder(activity)
        val btnCreateclass :Button =v.findViewById(R.id.btn_add_class)
        addDialog.setView(v)
        val dialog=addDialog.create()
        dialog.show()
        btnCreateclass.setOnClickListener {
            var nameoftheclass =classname.text.toString().trim()
            var adminId = auth.currentUser!!.uid.toString()
            val roleoftheclass :String="Moderator"
           // var class_id :String?=DataBase.child(auth.currentUser!!.uid).push().key
            var class_id :String?=geneteCode()
            if (TextUtils.isEmpty(nameoftheclass)){
                // No name added
                classname.error = "No name added !! Please enter the name of the class"

            }else{
                classesList.add(Classes(nameoftheclass,roleoftheclass,adminId))
                classesAdapter.notifyDataSetChanged()
                Toast.makeText(activity,"Adding Class Success",Toast.LENGTH_SHORT).show()
                addClassToDataBase(class_id!!,nameoftheclass,"Moderator",adminId)
                dialog.dismiss()
            }


            }

        }




    private fun geneteCode(): String? {
        val r = Random()
        val length=6
        val str1 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        var sb = StringBuilder(length)
        for (j in 0 until length) {
            sb.append(str1[r.nextInt(str1.length)])
        }
        return sb.toString()
    }



    private fun initRecyclerView(v: View) {
        /**set Liste */
        classesList= ArrayList()
        temporaryclassesList=ArrayList()

        /** set the  recycle view  , shimmer for loading data and text view for emty recycleview*/
        msgForEmptyRecview=v.findViewById(R.id.msg_for_empty_recv)
        shimmerFrameLayout=v.findViewById(R.id.shimmerFrameLayout)
        classRecycleview=v.findViewById(R.id.classes_list_recycleview)
        classRecycleview.layoutManager = LinearLayoutManager(activity)
        /**set adapter */
        classesAdapter= ClassesAdapter(this,context,temporaryclassesList)
        classRecycleview.adapter= classesAdapter

        //communicator=activity as Communicator

        /** click in the class to go to next activity **/
        /*classesAdapter.setOnItemClickListener(object :  ClassesAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                //communicator.sendClassIdData(classesList[position].class_id.toString())

                val intent = Intent(context, DetailedClassActivity::class.java)
                intent.putExtra("classId",temporaryclassesList[position].class_id)
                startActivity(intent)

            }
        })*/

    }
    private fun addClassToDataBase(class_id :String,name: String, role: String, adminId: String) {
        auth = FirebaseAuth.getInstance()
        DataBase = FirebaseDatabase.getInstance().getReference("Classes")
        DataBase.child(class_id).setValue(Classes(class_id,name, role, adminId))

        /*/** 7atena admmin id fel class **/
        DataBase.child(class_id).child("adminId").setValue(auth.currentUser!!.uid)*/


        DataBase2=FirebaseDatabase.getInstance().getReference("Users").child(auth.currentUser!!.uid)
        DataBase2.child("classes").child(class_id).setValue(Classes(class_id,name, role, adminId))



    }

    // search class
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.app_bar_menu, menu)
        val item = menu.findItem(R.id.misearch)
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(newText: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                txtSearch(newText)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu, inflater)
    }

    private fun txtSearch(newText: String){
        msgForEmptyRecview.visibility=View.GONE
        shimmerFrameLayout.stopShimmer()
        shimmerFrameLayout.visibility = View.GONE
        classRecycleview.visibility = View.VISIBLE
        temporaryclassesList.clear()
        val searchText = newText.lowercase(Locale.getDefault())
        if (searchText.isNotEmpty()){
            classesList.forEach {
                if(it.name.lowercase(Locale.getDefault()).contains(searchText)){
                    temporaryclassesList.add(it)
                }

            }

            msgForEmptyRecview.visibility=View.GONE
            shimmerFrameLayout.stopShimmer()
            shimmerFrameLayout.visibility = View.GONE
            classRecycleview.visibility = View.VISIBLE

            classesAdapter.notifyDataSetChanged()


        }else{
            temporaryclassesList.clear()
            temporaryclassesList.addAll(classesList)


            }
            msgForEmptyRecview.visibility=View.GONE
            shimmerFrameLayout.stopShimmer()
            shimmerFrameLayout.visibility = View.GONE
            classRecycleview.visibility = View.VISIBLE
            classesAdapter.notifyDataSetChanged()

    }



}