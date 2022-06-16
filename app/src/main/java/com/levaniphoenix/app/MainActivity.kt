package com.levaniphoenix.app

import android.R.attr
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private val REQUEST_SCREENSHOT=59706
    private lateinit var mgr : MediaProjectionManager
    private lateinit var button : Button

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            startService(REQUEST_SCREENSHOT,result.resultCode,data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkOverlayPermission();
        mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        button = findViewById(R.id.button)
        button.setOnClickListener {
//            startActivityForResult(mgr.createScreenCaptureIntent(), REQUEST_SCREENSHOT);
            resultLauncher.launch(mgr.createScreenCaptureIntent())
        }
        //startService();
    }

    override fun onResume() {
        super.onResume()
        //startService();
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this,ForegroundService::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SCREENSHOT) {
            if (resultCode == RESULT_OK) {

                startService(requestCode,resultCode,data)
            }
        }
    }

    // method for starting the service
    fun startService(requestCode: Int, resultCode: Int, data: Intent?) {
        val intent = Intent(this, ForegroundService::class.java)
            .putExtra("resultCode", resultCode)
            .putExtra("resultIntent", data)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // check if the user has already granted
            // the Draw over other apps permission
            if (Settings.canDrawOverlays(this)) {
                // start the service based on the android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        }
    }

    // method to ask user to grant the Overlay permission
    fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // send user to the device settings
                val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivity(myIntent)
            }
        }
    }
}