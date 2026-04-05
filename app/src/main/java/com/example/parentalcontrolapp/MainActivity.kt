package com.example.parentalcontrolapp

import com.google.firebase.messaging.FirebaseMessaging
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import android.content.Intent
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {

    private lateinit var deviceIdInput: EditText
    private lateinit var linkButton: Button
    private lateinit var statusView: TextView
    private lateinit var summaryView: TextView
    private lateinit var dashboardBtn: Button

    private var linkedDeviceId: String? = null
    private val handler = Handler(Looper.getMainLooper())

    private val backendBase =
        "https://parental-nsfw-cloud.onrender.com"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= 33) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val token = task.result
                    println("FCM TOKEN: $token")

                    val deviceId = "parent_device_2"

                    val call = RetrofitClient.instance.registerParent(deviceId, token)

                    call.enqueue(object : retrofit2.Callback<Map<String, String>> {
                        override fun onResponse(
                            call: retrofit2.Call<Map<String, String>>,
                            response: retrofit2.Response<Map<String, String>>
                        ) {}

                        override fun onFailure(
                            call: retrofit2.Call<Map<String, String>>,
                            t: Throwable
                        ) {}
                    })
                }
            }

        buildUI()
    }

    private fun buildUI() {

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 120, 40, 40)

        val title = TextView(this)
        title.textSize = 22f
        title.text = "Parent Monitoring Dashboard"
        title.gravity = Gravity.CENTER

        deviceIdInput = EditText(this)
        deviceIdInput.hint = "Enter Child Device ID"

        linkButton = Button(this)
        linkButton.text = "Link Device"

        dashboardBtn = Button(this)
        dashboardBtn.text = "Open Control Dashboard"

        statusView = TextView(this)
        summaryView = TextView(this)

        layout.addView(title)
        layout.addView(deviceIdInput)
        layout.addView(linkButton)
        layout.addView(dashboardBtn)
        layout.addView(statusView)
        layout.addView(summaryView)

        setContentView(layout)

        linkButton.setOnClickListener {
            val id = deviceIdInput.text.toString().trim()
            if (id.isNotEmpty()) {
                linkedDeviceId = id
                startMonitoring()
            }
        }

        dashboardBtn.setOnClickListener {

            val id = deviceIdInput.text.toString()

            if (id.isEmpty()) {
                Toast.makeText(this,"Link device first",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("device_id", id)
            startActivity(intent)
        }
    }

    private fun startMonitoring() {
        Toast.makeText(this, "Device Linked", Toast.LENGTH_SHORT).show()

        handler.post(object : Runnable {
            override fun run() {
                fetchActive()
                fetchSummary()
                handler.postDelayed(this, 5000)
            }
        })
    }

    private fun fetchActive() {

        val id = linkedDeviceId ?: return

        thread {
            try {
                val url = URL("$backendBase/active/$id")
                val conn = url.openConnection() as HttpURLConnection

                val response =
                    conn.inputStream.bufferedReader().readText()

                val json = JSONObject(response)
                val active = json.getBoolean("active")

                runOnUiThread {
                    if (active) {
                        statusView.text =
                            "⚠️ ALERT: Child watching inappropriate content"
                    } else {
                        statusView.text = "Child Safe"
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    statusView.text = "Error: ${e.message}"
                }
            }
        }
    }

    private fun fetchSummary() {

        val id = linkedDeviceId ?: return

        thread {
            try {
                val url = URL("$backendBase/summary/$id")
                val conn = url.openConnection() as HttpURLConnection

                val response =
                    conn.inputStream.bufferedReader().readText()

                val json = JSONObject(response)

                val frames =
                    json.getInt("total_bad_frames")

                val watchTime =
                    json.getInt("estimated_watch_time_seconds")

                runOnUiThread {
                    summaryView.text =
                        "\nBad Frames: $frames\nWatch Time: $watchTime sec"
                }

            } catch (e: Exception) {
                runOnUiThread {
                    summaryView.text = "Error: ${e.message}"
                }
            }
        }
    }
}