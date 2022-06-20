package com.example.myclass1

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.example.myclass1.databinding.ActivityDrawerBaseBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

open class DrawerBaseActivity : AppCompatActivity() {

    private lateinit var binding:ActivityDrawerBaseBinding
    open lateinit var auth: FirebaseAuth
    lateinit var drawerLayout: DrawerLayout
    lateinit var toggle: ActionBarDrawerToggle

    open lateinit var DataBase : DatabaseReference
    open lateinit var DataBase2 : DatabaseReference
    open lateinit var DataBase3 : DatabaseReference


    private lateinit var classesUserJoin :ArrayList<Classes>
    private  var name:String=""
    private  var email:String=""
    private  var birthdat:String=""
    private  var uid:String=""
    private  var photoid:String=""

    override fun setContentView(view: View) {

        auth = FirebaseAuth.getInstance()

        drawerLayout = getLayoutInflater().inflate(R.layout.activity_drawer_base, null) as DrawerLayout
        val container = drawerLayout.findViewById(R.id.activityContainer) as FrameLayout
        container.addView(view)
        super.setContentView(drawerLayout)

        if (auth.currentUser?.uid != null){
            bringParticipant()
        }

        val toolbar:Toolbar=drawerLayout.findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        val navView : NavigationView =drawerLayout.findViewById(R.id.navview)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home-> {
                    Toast.makeText(applicationContext,"clicked Home", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                R.id.nav_calendar-> {
                    Toast.makeText(applicationContext,"clicked Calendar", Toast.LENGTH_SHORT).show()
                    val calIntent = Intent(Intent.ACTION_INSERT)
                    calIntent.data = CalendarContract.Events.CONTENT_URI
                    startActivity(calIntent)
                    //startActivityForResult(intent,0)
                }
                R.id.nav_notification-> {
                    Toast.makeText(applicationContext,"clicked Notification", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_logout-> {
                    auth.signOut()
                    checkUser()
                }

            }
            true
        }

        toggle= ActionBarDrawerToggle(this,drawerLayout ,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bringuserinfo(navView)


        val header :View= navView.getHeaderView(0)
        val profilimagebutton=header.findViewById<Button>(R.id.select_profil_photo)

        profilimagebutton.setOnClickListener {
            val intent=Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)


        }
    }



    var selectPhotoUri: Uri?=null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==0 && resultCode== Activity.RESULT_OK && data != null){
            selectPhotoUri =data.data
            val bitmap =MediaStore.Images.Media.getBitmap(contentResolver,selectPhotoUri)

            val navView : NavigationView =drawerLayout.findViewById(R.id.navview)
            val header :View= navView.getHeaderView(0)
            val profilimageview=header.findViewById<CircleImageView>(R.id.profil_image)
            val profilimagebutton=header.findViewById<Button>(R.id.select_profil_photo)

            profilimageview.setImageBitmap(bitmap)
            profilimagebutton.alpha=0f


                uploadImageToFirebaseStorage()






        }

    }

    private fun uploadImageToFirebaseStorage() {
        if(selectPhotoUri==null)return
        val filename=UUID.randomUUID().toString()
        val ref=FirebaseStorage.getInstance().getReference("/ProfileImages/$filename")

        ref.putFile(selectPhotoUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                saveUserProfilePhotoData(it.toString())
            }
        }


    }
    private fun saveUserProfilePhotoData(uri: String) {
        DataBase=FirebaseDatabase.getInstance().getReference("Users").child((auth.currentUser!!.uid))
        DataBase.child("profileImageUrl").setValue(uri)

        /** from here **/
        classesUserJoin=ArrayList()
        DataBase2=FirebaseDatabase.getInstance().getReference("Users").child((auth.currentUser!!.uid)).child("classes")
        DataBase2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                classesUserJoin.clear()
                for (postSnapshot in snapshot.children){
                    val classe =postSnapshot.getValue(Classes::class.java)
                    if(classe!!.role.equals("User")){
                        classesUserJoin.add(classe!!)
                    }

                }
                findClasses(classesUserJoin,uri)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        /** to here **/


    }

    private fun findClasses(classesUserJoin: ArrayList<Classes>, uri: String) {
        DataBase3=FirebaseDatabase.getInstance().getReference("Classes")
        DataBase3.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    val classe =postSnapshot.getValue(Classes::class.java)
                    for(classefromlist in classesUserJoin){
                        if(classe!!.equals(classefromlist)){
                            updateUserprrofileImage(classe,uri)
                        }
                    }



                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun updateUserprrofileImage(classe: Classes, uri: String) {
        auth = FirebaseAuth.getInstance()

        val userParticipant = User(name,birthdat,email,uid,uri)
        DataBase=FirebaseDatabase.getInstance().getReference("Classes").child(classe.getId()).child("ParticipantsID")
        DataBase.child(auth.currentUser!!.uid).updateChildren(userParticipant.getMap())
    }


    private fun bringuserinfo(navView: NavigationView) {
       DataBase= FirebaseDatabase.getInstance().getReference("Users")

        val header :View= navView.getHeaderView(0)

        val username = header.findViewById<TextView>(R.id.user_name)
       val useremail = header.findViewById<TextView>(R.id.user_email)
        val profilimageview=header.findViewById<CircleImageView>(R.id.profil_image)
        val profilimagebutton=header.findViewById<Button>(R.id.select_profil_photo)
        //DataBase2=FirebaseDatabase.getInstance().getReference("Users").child(auth.currentUser!!.uid)

        DataBase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (postSnapshot in snapshot.children){
                    val user =postSnapshot.getValue(User::class.java)
                    if (user != null) {
                        if(auth.currentUser!!.uid.equals(user.uid)){
                            username.setText(user!!.name)
                            useremail.text=user!!.email
                            //var Uri=user.profileImageUrl!!.toUri()
                            //var bitmap =MediaStore.Images.Media.getBitmap(contentResolver,Uri)
                            //profilimageview.setImageBitmap(bitmap)
                            if(user.profileImageUrl != ""){
                                profilimagebutton.alpha=0f
                            }
                           // profilimagebutton.alpha=0f
                            Glide.with(this@DrawerBaseActivity).load(user.profileImageUrl).into(profilimageview)




                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

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





    // navigate the navigation view
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

   /* override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_menu,menu)
        return true
    }*/

    // if user is alredy  logged in go to Home activity
    private fun checkUser() {

        // get current user
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            null
        }
        else{

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }



}


