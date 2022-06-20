package com.example.myclass1

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myclass1.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class SignUpActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var DataBase :DatabaseReference
    private var name = ""
    private  var birthday=""
    private var email=""
    private var password=""
    private var cpassword=""
    private lateinit var binding: ActivitySignUpBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        binding.goSignin.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }


        binding.btnContinueSignup.setOnClickListener {
            validateData()
        }
        val myCalendar = Calendar.getInstance()
        val datePiker = DatePickerDialog.OnDateSetListener{view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR,year)
            myCalendar.set(Calendar.MONTH,month)
            myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateLable(myCalendar)
        }
        binding.edtBirthday.setOnClickListener {
            DatePickerDialog(this,datePiker,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        //button enable disable with email validation
        binding.edtEmailSignup.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if(Patterns.EMAIL_ADDRESS.matcher(binding.edtEmailSignup.text.toString()).matches())
                    binding.btnContinueSignup.isEnabled = true
                else{
                    binding.btnContinueSignup.isEnabled = false
                    binding.edtEmailSignup.error = "Invalid email"
                }
            }

        })


        dismissKeybord(binding.edtFullname)
        dismissKeybord(binding.edtBirthday)
        dismissKeybord(binding.edtEmailSignup)
        dismissKeybord(binding.edtPasswordSignup)
        dismissKeybord(binding.edtConfirmpasswordSignup)

    }
    //Dismissing keyboard
    private fun dismissKeybord(editText: EditText) {
        editText.onFocusChangeListener = object : View.OnFocusChangeListener {

            override
            fun onFocusChange(v: View, hasFocus: Boolean) {
                if (!hasFocus) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
    }
    //Fin

    @SuppressLint("NewApi")
    private fun updateLable(myCalendar: Calendar) {
        val myFormat ="dd-MM-YYYY"
        val sdf = SimpleDateFormat(myFormat,Locale.UK)
        binding.edtBirthday.setText(sdf.format(myCalendar.time))
    }


    // Test the email format and the password length
    private fun validateData() {
        name = binding.edtFullname.text.toString().trim()
        birthday = binding.edtBirthday.text.toString().trim()
        email = binding.edtEmailSignup.text.toString().trim()
        password = binding.edtPasswordSignup.text.toString().trim()
        cpassword=binding.edtConfirmpasswordSignup.text.toString().trim()
        if (TextUtils.isEmpty(name)){
            // No name added
            binding.edtFullname.error = "No name added !! Please enter your name"

        }else if (TextUtils.isEmpty(birthday)){
            // No birthday  added
            binding.edtBirthday.error = "No birthday added !! Please enter your birthday"

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //invalid email format
            binding.edtEmailSignup.error = "invalid email format"
        } else if (TextUtils.isEmpty(password)) {
            // No password added
            binding.edtPasswordSignup.error = "No password added !! Please enter Password"
        }
        else if (password.length <6) {
            // Password length is less than 6
            binding.edtPasswordSignup.error = " Password  must at least 6 characters long "
        }
        else if (TextUtils.isEmpty(cpassword)) {
            // No confirm password added
            binding.edtConfirmpasswordSignup.error = "Confirm password  !! Please Confirm  Password"
        }

        else if (password != cpassword) {
            // confirm password
            binding.edtConfirmpasswordSignup.error = " Password  doesn't match !!"
        }
        else{
            signUp()

        }
    }

    // methode for signUp
    private fun signUp() {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val firebaseUser: FirebaseUser = auth.currentUser!!
                val email = firebaseUser.email
                name = binding.edtFullname.text.toString().trim()
                birthday = binding.edtBirthday.text.toString().trim()
                val classe:String?=null

                firebaseUser.sendEmailVerification()

                // methode to add a user to DataBase
                addUserToDataBase(name,birthday,email,auth.currentUser?.uid!!)


                Toast.makeText(this, "Account created Successfully please verify your email", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {e->
                Toast.makeText(this,"Login failed ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun addUserToDataBase(name: String, birthday: String, email: String?, uid: String) {
        DataBase = FirebaseDatabase.getInstance().getReference("Users")
        DataBase.child(uid).setValue(User(name,birthday,email,uid,""))

    }

}