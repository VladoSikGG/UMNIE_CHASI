package com.example.testappbt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.example.testappbt.databinding.ContentStartBinding

class StartActivity : AppCompatActivity() {

    private lateinit var binding: ContentStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ContentStartBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


}