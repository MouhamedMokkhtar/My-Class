package com.example.myclass1

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class ChatRoomFragment : Fragment() {

    lateinit var auth: FirebaseAuth
    lateinit var DataBase : DatabaseReference
    lateinit var DataBase2 : DatabaseReference

    private lateinit var messageList : ArrayList<Message>
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var chatroomRecycleview: RecyclerView

    private lateinit var messageBox:EditText
    private lateinit var sendButton: ImageButton

    private  var name:String=""
    private  var email:String=""
    private  var birthdat:String=""
    private  var uid:String=""
    private  var photoid:String=""

    var classRoom:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v=inflater.inflate(R.layout.fragment_chat_room, container, false)

        /** bring Current User Data */
        bringCurrentUser()

        /** bring class ID from the activity   */
        val activity = activity as DetailedClassActivity?
        val classID: String? = activity?.getMyClassID()

        /** set the classRoom Id  **/
        classRoom = classID+"room"

        setHasOptionsMenu(true)




        /** set the  recycle view + list */
        messageList= java.util.ArrayList()
        chatroomRecycleview=v.findViewById(R.id.chatroom_recyclerview)
        LinearLayoutManager(activity).stackFromEnd = false
        LinearLayoutManager(activity).reverseLayout = true
        chatroomRecycleview.layoutManager = LinearLayoutManager(activity)


        /**set adapter */
        messageAdapter= MessageAdapter(this,context,messageList)
        chatroomRecycleview.adapter= messageAdapter

       /**  read message from DataBase  */

        DataBase= FirebaseDatabase.getInstance().getReference("ChatRoom").child(classRoom!!).child("messages")
        DataBase.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (postSnapshot in snapshot.children){
                    val message = postSnapshot.getValue(Message::class.java)
                    messageList.add(message!!)
                }
                messageAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })



       /** send a message and put it to DataBase   */
        messageBox=v.findViewById(R.id.messagebox)
        sendButton=v.findViewById(R.id.btn_send_message)

        sendButton.setOnClickListener {

            val message = messageBox.text.toString()
            if (TextUtils.isEmpty(message)) {
                messageBox.error = "Please enter your message !!!"
            } else {
                val messageeObject = Message(message, uid, name)
                DataBase2 =
                    FirebaseDatabase.getInstance().getReference("ChatRoom").child(classRoom!!)
                        .child("messages").push()
                DataBase2.setValue(messageeObject)
                messageBox.setText("")
            }

        }

        return v
    }
    private fun bringCurrentUser() {
        auth = FirebaseAuth.getInstance()
        DataBase= FirebaseDatabase.getInstance().getReference("Users").child(auth.currentUser!!.uid)
        DataBase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUser =snapshot.getValue(User::class.java)
                if (currentUser!!.uid.equals(auth.currentUser!!.uid)){
                    putCurrentUserData(currentUser.name,currentUser.email,currentUser.birthday,currentUser.uid,currentUser.profileImageUrl)


                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun putCurrentUserData(name: String?, email: String?, birthday: String?, uid: String?, profileImageUrl: String?) {
        this.email=email!!
        this.birthdat=birthday!!
        this.uid=uid!!
        this.name=name!!
        this.photoid=profileImageUrl!!
    }


}