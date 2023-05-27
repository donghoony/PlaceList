package org.konkuk.placelist

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import org.konkuk.placelist.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private val handler = Handler(Looper.myLooper()!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler.postDelayed(Runnable {
            run{
                val intent = Intent(this@IntroActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 1000)
    }
}