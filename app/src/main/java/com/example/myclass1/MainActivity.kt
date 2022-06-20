package com.example.myclass1

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.myclass1.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MainActivity : AppCompatActivity() {


    lateinit var auth: FirebaseAuth
    private var email = ""
    private var password = ""
    private lateinit var binding: ActivityMainBinding

    private val viewModel:MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
           /*setKeepOnScreenCondition{
               viewModel.isLoading.value
           }*/
        }


        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        checkUser()

        // btnSignup open signup activity
        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.forgetPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }


        // btnLogin connect with Firebase

        binding.btncontinue.setOnClickListener {
            //email = binding.edtEmail.text.toString()
            //password =binding.edtPassword.text.toString()
            //login(email,password)
            validateData()

        }


        //button enable disable with email validation
        binding.edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if(Patterns.EMAIL_ADDRESS.matcher(binding.edtEmail.text.toString()).matches())
                    binding.btncontinue.isEnabled = true
                else{
                    binding.btncontinue.isEnabled = false
                    binding.edtEmail.error = "Invalid email"
                }
            }

        })
        //Fin
        dismissKeybord(binding.edtEmail)
        dismissKeybord(binding.edtPassword)

    }
    //Dismissing keyboard
    private fun dismissKeybord(edtText: TextInputEditText) {
        edtText.onFocusChangeListener = object : View.OnFocusChangeListener {

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



    private fun checkUser() {
        // if user is alredy  logged in go to Home activity
        // get current user
        val firebaseUser = auth.currentUser
        if (firebaseUser != null && firebaseUser.isEmailVerified) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            /**
            jebbna el view mtal nav header bech najmou naxcidiwlhaa
            val view = LayoutInflater.from(this)
            val v= view.inflate(R.layout.nav_header,null)

            var username=v.findViewById<TextView>(R.id.user_name)
            var useremail=v.findViewById<TextView>(R.id.user_email)
            username.text=firebaseUser?.displayName.toString()
            useremail.text=firebaseUser?.email
             **/
        }

    }

    private fun validateData() {
        email = binding.edtEmail.text.toString()
        password = binding.edtPassword.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //invalid email format
            binding.edtEmail.error = "invalid email format"
        } else if (TextUtils.isEmpty(password)) {
            // No password added
            binding.edtPassword.error = "No password added !! Please enter Password"
        } else {
            // data is validate begin login
            login()
        }
    }

    private fun login() {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val firebaseUser: FirebaseUser = auth.currentUser!!
                val email = firebaseUser.email
                if (firebaseUser.isEmailVerified){
                    Toast.makeText(this, "Logged In ", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this, "Please verify your email  ", Toast.LENGTH_SHORT).show()
                }

            }
            .addOnFailureListener {e->
                Toast.makeText(this,"Login failed ${e.message}",Toast.LENGTH_SHORT).show()
            }


    }
}