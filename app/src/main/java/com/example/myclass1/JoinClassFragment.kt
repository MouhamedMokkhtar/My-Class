package com.example.myclass1

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class JoinClassFragment : Fragment() {
    lateinit var auth: FirebaseAuth
    lateinit var DataBase : DatabaseReference
    lateinit var DataBase2 : DatabaseReference
    lateinit var DataBase3 : DatabaseReference

    private lateinit var classesList : ArrayList<Classes>
    private lateinit var classesListforLooping : java.util.ArrayList<Classes>


    private lateinit var classesAdapter: ClassesAdapter
    private lateinit var classRecycleview: RecyclerView

    private  var name:String=""
    private  var email:String=""
    private  var birthdat:String=""
    private  var uid:String=""
    private  var photoid:String=""






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
        val v=inflater.inflate(R.layout.fragment_join_class, container, false)
        val v2=inflater.inflate(R.layout.fragment_class, container, false)





        val joinClass:Button=v.findViewById(R.id.btn_join)
        initRecycleView(v2)
        bringParticipant()
        joinClass.setOnClickListener {
            importClassFromDataBase(v)

        }


        return v
    }


    private fun initRecycleView(v2: View?) {
        /**set Liste */
        classesList= java.util.ArrayList()
        //temporaryclassesList= java.util.ArrayList()

        /** set the  recycle view  */
        classRecycleview=v2!!.findViewById(R.id.classes_list_recycleview)
        classRecycleview.layoutManager = LinearLayoutManager(activity)
        /**set adapter */
        classesAdapter= ClassesAdapter(this,context,classesList)
        classRecycleview.adapter= classesAdapter
    }


    private fun importClassFromDataBase(view: View) {
        auth = FirebaseAuth.getInstance()
        DataBase2= FirebaseDatabase.getInstance().getReference("Classes")

        var classIdd = view.findViewById<EditText>(R.id.edt_classCodeId2)
        var classCodeIdd =classIdd.text.toString().trim()

        classesListforLooping= java.util.ArrayList()

        DataBase2.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                classesList.clear()
                classesListforLooping.clear()

                for (postSnapshot in snapshot.children) {
                    val classe = postSnapshot.getValue(Classes::class.java)
                    classesList.add(classe!!)

                }

                classesListforLooping.addAll(classesList)
                classesList.clear()
                for (i in classesListforLooping){
                    if (i.getId().equals(classCodeIdd)){
                        if(i.adminId.equals(auth.currentUser!!.uid.toString())){
                            val alertDialog = AlertDialog.Builder(context)
                            alertDialog.apply {
                                setTitle("Class Exist")
                                setMessage("You Alredy Moderator in this Class")

                                setNegativeButton("Dismiss") { _, _ ->
                                    Toast.makeText(context, "Dismiss", Toast.LENGTH_SHORT).show()
                                }
                            }.create().show()

                        }else{
                            classesList.clear()
                            classesList.add(i)
                            classesAdapter.notifyDataSetChanged()
                            Toast.makeText(activity,"Join Class Success", Toast.LENGTH_SHORT).show()
                            addClassJoinToDataBase(i)
                            classCodeIdd=""
                           // val intent = Intent(context, HomeActivity::class.java)
                            //startActivity(intent)
                            classIdd.setText("")

                        }

                    }

                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }



    private fun addClassJoinToDataBase(i: Classes) {
        auth = FirebaseAuth.getInstance()
        DataBase2=FirebaseDatabase.getInstance().getReference("Users")
        val classUpdate = Classes(i.class_id,i.name,"User",i.adminId)
        DataBase2.child(auth.currentUser!!.uid).child("classes").child(i.getId()).setValue(classUpdate)





        val UserParticipant=User(name,birthdat,email, uid,photoid)
        DataBase = FirebaseDatabase.getInstance().getReference("Classes")
        DataBase.child(i.getId()).child("ParticipantsID").child(auth.currentUser!!.uid).setValue(UserParticipant)

    }

    private fun bringParticipant() {
        auth = FirebaseAuth.getInstance()
        DataBase3=FirebaseDatabase.getInstance().getReference("Users").child(auth.currentUser!!.uid)
        DataBase3.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val participant =snapshot.getValue(User::class.java)
                if (participant!!.uid.equals(auth.currentUser!!.uid)){
                    putParticipantData(participant.name,participant.email,participant.birthday,participant.uid,participant.profileImageUrl)


                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun putParticipantData(name: String?, email: String?, birthday: String?, uid: String?, profileImageUrl: String?) {
        this.email=email!!
        this.birthdat=birthday!!
        this.uid=uid!!
        this.name=name!!
        this.photoid=profileImageUrl!!

    }


}


