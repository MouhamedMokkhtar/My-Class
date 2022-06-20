package com.example.myclass1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class MessageAdapter (val c: Fragment, val l:android.content.Context?, val messageList: ArrayList<Message>) : RecyclerView.Adapter <RecyclerView.ViewHolder> () {


    lateinit var auth: FirebaseAuth
    lateinit var DataBase : DatabaseReference
    lateinit var DataBase2 : DatabaseReference
    val ITEM_RECEIVE =1
    val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == 1){
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.receive,parent,false)
            return ReceiveMessageViewHolder(itemView)
        }
        else{
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.sent,parent,false)
            return SentMessageViewHolder(itemView)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if (holder.javaClass == SentMessageViewHolder::class.java){
            // do stuff for SentViewHolder
            val viewHolder= holder as SentMessageViewHolder

            holder.sentMassege.text = currentMessage.message

        }
        else{
            // do stuff for SentViewHolder
            val viewHolder= holder as ReceiveMessageViewHolder

            holder.receiveMassege.text = currentMessage.message
            holder.sendermessageName.text=currentMessage.senderName
        }
    }

    override fun getItemViewType(position: Int): Int {
        auth = FirebaseAuth.getInstance()
        val currentMessage = messageList[position]
        if(auth.currentUser?.uid.equals(currentMessage.senderId)){
            return ITEM_SENT
        }
        else{
            return ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    inner class SentMessageViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
        val  sentMassege =v.findViewById<TextView>(R.id.txt_sent_message)
        init {

        }
    }
    inner class ReceiveMessageViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
        val receiveMassege =v.findViewById<TextView>(R.id.txt_receive_message)
        val sendermessageName=v.findViewById<TextView>(R.id.txt_senderMessage_name)
        init {

        }
    }


}