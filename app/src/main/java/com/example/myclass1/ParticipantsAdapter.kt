package com.example.myclass1

import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import de.hdodenhof.circleimageview.CircleImageView

class ParticipantsAdapter (val c: Fragment, val l:android.content.Context?, val Participantlist: ArrayList<User>) : RecyclerView.Adapter <ParticipantsAdapter.ParticipantsViewHolder> () {


    lateinit var auth: FirebaseAuth
    lateinit var DataBase : DatabaseReference
    private lateinit var mListener : ParticipantsAdapter.onItemClickListener


    interface onItemClickListener {

        fun onItemClick(position: Int)

    }
    fun setOnItemClickListener(listener: ParticipantsAdapter.onItemClickListener) {

        mListener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantsAdapter.ParticipantsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.participants_items,parent,false)
        return ParticipantsViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: ParticipantsAdapter.ParticipantsViewHolder, position: Int) {
        val currentitem = Participantlist[position]
        holder.participantname.text=currentitem.name
        holder.participantemail.text=currentitem.email
        if(currentitem.profileImageUrl!=""){
            var Uri=currentitem.profileImageUrl!!.toUri()
            var bitmap = MediaStore.Images.Media.getBitmap(l!!.contentResolver,Uri)
            holder.participantimage.setImageBitmap(bitmap)
        }



    }

    override fun getItemCount(): Int {
        return Participantlist.size
    }

    inner class ParticipantsViewHolder(val v: View, listener: onItemClickListener) : RecyclerView.ViewHolder(v) {

        var participantname = v.findViewById<TextView>(R.id.participant_name)
        var participantemail = v.findViewById<TextView>(R.id.participant_email)
        var participantimage=v.findViewById<CircleImageView>(R.id.participant_image)


        init {
            v.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }


    }




}