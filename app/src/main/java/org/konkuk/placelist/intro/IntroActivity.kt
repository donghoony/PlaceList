package org.konkuk.placelist.intro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.konkuk.placelist.R
import org.konkuk.placelist.databinding.ActivityIntroBinding
import org.konkuk.placelist.main.MainActivity

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

        val backgroundImg : ImageView = findViewById(R.id.flag)
        val sideAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_translate)
        backgroundImg.startAnimation(sideAnimation)

        handler.postDelayed({
            run{
                val intent = Intent(this@IntroActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 2000)
    }
}