package com.levaniphoenix.app

import android.R
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SHUTDOWN
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class ForegroundService : Service(), ImageReader.OnImageAvailableListener {

    private val TAG: String = ForegroundService::class.java.getName()

    private lateinit var mediaProjection: MediaProjection
    private lateinit var mediaProjectionManager : MediaProjectionManager
    private lateinit var vDisplay :VirtualDisplay
    private lateinit var mImageReader :ImageReader
    private var latestBitmap: Bitmap? = null
    private var width = -1
    private var height = -1
    private var windowBorderSize = -1

    private lateinit var window:Window

    private val handlerThread = HandlerThread(
        javaClass.simpleName,
        Process.THREAD_PRIORITY_BACKGROUND
    )
    private var handler: Handler? = null

    private var resultCode = -1
    private lateinit var resultData:Intent

    val VIRT_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC

    override fun onBind(intent: Intent?): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        // create the custom or default notification
        // based on the android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(
            1,
            Notification()
        )
        // create an instance of Window class
        // and display the content on screen
        window = Window(this)
        window.open()
    }

    override fun onDestroy() {
        super.onDestroy()
        //todo clean up after service is done
        Log.d(TAG, "destroy foreground service")
        window.close()
        //System.exit(0)
    }
    @SuppressLint("WrongConstant")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(ACTION_SHUTDOWN == intent?.action){
            stopForeground(true)
            stopSelf()
        }else {
            if(intent !=null) {
                val screenshotRequest = intent.getIntExtra("screenshotRequest", -1)!!
                if (screenshotRequest == 1) {

                    val width = intent.getIntExtra("width", -1)
                    val height = intent.getIntExtra("height", -1)
                    val windowX = intent.getIntExtra("windowX", -1)
                    val windowY = intent.getIntExtra("windowY", -1)
                    windowBorderSize = intent.getIntExtra("borderSize", -1)

                    Log.d(TAG, "got screen shot request")
                    val date: String =
                        SimpleDateFormat("yyyy-MM-dd-hh-mm-ss", Locale.getDefault()).format(Date())
                    val file = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            .toString(), date + ".png"
                    )
                    saveScreenshot(file, width, height, windowX, windowY)
                    return super.onStartCommand(intent, flags, startId)
                }

                resultCode = intent.getIntExtra("resultCode", -1)
                resultData = intent.getParcelableExtra<Intent>("resultIntent")!!

                mediaProjectionManager =
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData)

                val cb: MediaProjection.Callback = object : MediaProjection.Callback() {
                    override fun onStop() {
                        vDisplay.release()
                    }
                }
                //todo make it api 14 complaint
                val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val metrics = windowManager.maximumWindowMetrics

                width = metrics.bounds.width()
                height = metrics.bounds.height()
                val MAX_IMAGES = 10

                mImageReader =
                    ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, MAX_IMAGES);


                vDisplay = mediaProjection.createVirtualDisplay(
                    "screenshot",
                    width,
                    height,
                    resources.displayMetrics.densityDpi,
                    VIRT_DISPLAY_FLAGS,
                    mImageReader.getSurface(),
                    null,
                    null
                )

                handlerThread.start()
                handler = Handler(handlerThread.looper)
                mImageReader.setOnImageAvailableListener(this, handler)
                mediaProjection.registerCallback(cb, null)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    // for android version >=O we need to create
    // custom notification stating
    // foreground service is running
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "example.permanence"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )
        val manager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

        notificationBuilder.addAction(
            R.drawable.ic_dialog_info,
            "ShutDown",
            buildPendingIntent(ACTION_SHUTDOWN)
        )

        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("ScreenShotTool running")
            .setContentText("double click on the botton right corner to take a screenshot")
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    override fun onImageAvailable(reader: ImageReader?) {
        Log.d(TAG, "got image!")
        var image =mImageReader.acquireLatestImage()
        proccessImage(image)
    }
    fun proccessImage(image: Image?) {
        if (image != null) {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding: Int = rowStride - pixelStride * width
            val bitmapWidth: Int = width + rowPadding / pixelStride
            if (latestBitmap == null || latestBitmap!!.getWidth() != bitmapWidth || latestBitmap!!.getHeight() != height) {
                if (latestBitmap != null) {
                    latestBitmap!!.recycle()
                }
                latestBitmap = Bitmap.createBitmap(
                    bitmapWidth,
                    height, Bitmap.Config.ARGB_8888
                )
            }
            latestBitmap!!.copyPixelsFromBuffer(buffer)
            image.close()
        }
    }
    fun saveScreenshot(file: File,width:Int,height:Int,windowX:Int,windowY:Int){
        if (file.exists()) {
            return
        }
        val baos = ByteArrayOutputStream()
        val cropped: Bitmap = Bitmap.createBitmap(
            latestBitmap!!, windowX+windowBorderSize, windowY+statusBarHeight()+windowBorderSize,
            width-2*windowBorderSize, height -2*windowBorderSize
        )
        cropped.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val newPng = baos.toByteArray()
        val output = FileOutputStream(file)
        output.write(newPng)
        output.flush();
        output.getFD().sync();
        output.close()
    }
    fun statusBarHeight():Int{
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
    private fun buildPendingIntent(action: String): PendingIntent? {
        val i = Intent(this, javaClass)
        i.action = action
        return PendingIntent.getService(this, 0, i, 0)
    }
}