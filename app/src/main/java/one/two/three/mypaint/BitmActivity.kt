package one.two.three.mypaint

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BitmActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
               setTheme(R.style.Theme_BitMapMyPaint)
        setContentView(R.layout.activity_bitm)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bitmap?.let {
            findViewById<ImageView>(R.id.bitmap_image_view).setImageBitmap(it)
            findViewById<ImageView>(R.id.bitmap_image_view)
        }
    }
}