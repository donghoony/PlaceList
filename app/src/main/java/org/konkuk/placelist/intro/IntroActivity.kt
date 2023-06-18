package org.konkuk.placelist.intro

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import org.konkuk.placelist.databinding.ActivityIntroBinding
import org.konkuk.placelist.main.MainActivity

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private val handler = Handler(Looper.myLooper()!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState != null) {
            return
        }
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler.postDelayed({
            run{
                val intent = Intent(this@IntroActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 1000)
    }
}