package com.example.myclass1

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class ParticipantsFragment : Fragment() {

    private lateinit var participantList : ArrayList<User>
    private lateinit var temporaryparticipantsList : java.util.ArrayList<User>
    private lateinit var participantsAdapter: ParticipantsAdapter
    private lateinit var participantRecycleview: RecyclerView

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
        // Inflate the layout for this fragment
        val v=inflater.inflate(R.layout.fragment_participants, container, false)

        val activity = activity as DetailedClassActivity?
        val classid: String? = activity?.getMyClassID()

        readDataFromDataBase(classid)

        initRecyclerView(v)

        setHasOptionsMenu(true)
        return v
    }


    private fun initRecyclerView(v: View) {
        /**set Liste */
        participantList = java.util.ArrayList()
        temporaryparticipantsList = java.util.ArrayList()

        /** set the  recycle view  */
        participantRecycleview = v.findViewById(R.id.participant_list_recycleview)
        participantRecycleview.layoutManager = LinearLayoutManager(activity)
        /**set adapter */
        participantsAdapter = ParticipantsAdapter(this, context, temporaryparticipantsList)
        participantRecycleview.adapter = participantsAdapter

        /** click in the class to go to next activity **/
        participantsAdapter.setOnItemClickListener(object :
            ParticipantsAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                //val intent = Intent(context, DetailedClassActivity::class.java)
                //startActivity(intent)

            }
        })
    }

    private fun readDataFromDataBase(classid: String?) {
        auth = FirebaseAuth.getInstance()
        DataBase = FirebaseDatabase.getInstance().getReference("Classes").child(classid!!).child("ParticipantsID")
        //DataBase2=FirebaseDatabase.getInstance().getReference("Classes")

        DataBase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                participantList.clear()

                for (postSnapshot in snapshot.children){
                    val participant =postSnapshot.getValue(User::class.java)
                    if (auth.currentUser!!.uid != participant!!.uid){
                        participantList.add(participant!!)
                    }
                }
                participantsAdapter.notifyDataSetChanged()
                temporaryparticipantsList.clear()
                temporaryparticipantsList.addAll(participantList) /**7atena list mta3 el classes bba3d mat3abet fel temprarylist ali chnnesta5dmoha **/
                participantList.clear()

            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
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