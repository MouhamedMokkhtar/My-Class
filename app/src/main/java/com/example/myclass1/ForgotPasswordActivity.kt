package com.example.myclass1

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myclass1.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //button send enable disable with email validation
        /*binding.edtEmailForgatpassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if(Patterns.EMAIL_ADDRESS.matcher(binding.edtEmailForgatpassword.text.toString()).matches())
                    binding.btnsendemail.isEnabled = true
                else{
                    binding.btnsendemail.isEnabled = false
                    binding.edtEmailForgatpassword.error = "Invalid email"
                }
            }

        })*/

        binding.btnsendemail.setOnClickListener {
            validateData()
        }
        binding.btnBackLogin.setOnClickListener {
           onBackPressed()
        }





    }
    private var email=""
    private fun validateData() {
        email=binding.edtEmailForgatpassword.text.toString().trim()
        if (email.isEmpty()){
            Toast.makeText(this,"Enter email ...",Toast.LENGTH_SHORT).show()
            binding.edtEmailForgatpassword.error="No email added !! Please enter your email"
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Invalid email",Toast.LENGTH_SHORT).show()
            binding.edtEmailForgatpassword.error="Invalid email"
        }
        else{
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this,"Instruction sent to \n$email",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e->
                Toast.makeText(this,"Failed to sent due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }
}