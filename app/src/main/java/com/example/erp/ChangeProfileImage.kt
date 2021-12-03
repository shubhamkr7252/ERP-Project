package com.example.erp

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.erp.databinding.ActivityChangeProfileImageBinding
import com.example.erp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ChangeProfileImage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    lateinit var binding : ActivityChangeProfileImageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeProfileImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageName = auth.currentUser?.uid
        val storageRef = FirebaseStorage.getInstance().reference.child("profileImages/$imageName.jpg")
        val localFile = File.createTempFile("tempImage","jpg")
        storageRef.getFile(localFile).addOnSuccessListener {

            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            binding.tempProfileImg.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Toast.makeText(this,"Failed", Toast.LENGTH_SHORT).show()
        }
    }
}