package com.example.parentalcontrolapp

import com.google.firebase.messaging.FirebaseMessaging
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.parentalcontrolapp.ui.theme.DashboardScreen
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import kotlin.concurrent.thread
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    data class Child(
        val deviceId: String,
        val alias: String
    )
    private val childList = mutableListOf<Child>()
    private lateinit var spinner: Spinner
    private lateinit var addButton: Button
    private lateinit var riskView: TextView
    private lateinit var framesView: TextView
    private lateinit var watchTimeView: TextView

    private var selectedChild: Child? = null
    private lateinit var childrenContainer: LinearLayout
    private lateinit var riskCard: androidx.cardview.widget.CardView

    private val backendBase =
        "https://parental-nsfw-cloud.onrender.com"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // 🔥 REQUEST NOTIFICATION PERMISSION (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }

        // 🔥 GET FCM TOKEN AND REGISTER PARENT
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
                        ) {
                            println("REGISTER RESPONSE: ${response.body()}")
                        }

                        override fun onFailure(
                            call: retrofit2.Call<Map<String, String>>,
                            t: Throwable
                        ) {
                            println("REGISTER FAILED: ${t.message}")
                        }
                    })
                }
            }

        buildUI()

    }
    private fun fetchChildMetrics(child: Child) {

        thread {
            try {
                val url = URL("$backendBase/summary/${child.deviceId}")
                val conn = url.openConnection() as HttpURLConnection

                val response = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                val frames = json.getInt("total_bad_frames")
                val watchTime = json.getInt("estimated_watch_time_seconds")

                runOnUiThread {

                    riskCard.visibility = View.VISIBLE   // 👈 show card only when child selected

                    when {
                        frames > 100 -> {
                            riskView.text = "🔴 HIGH RISK"
                            riskView.setTextColor(android.graphics.Color.BLACK)
                            riskCard.setCardBackgroundColor(
                                android.graphics.Color.parseColor("#FFCDD2") // pastel red
                            )
                        }
                        frames > 20 -> {
                            riskView.text = "🟡 MODERATE RISK"
                            riskView.setTextColor(android.graphics.Color.BLACK)
                            riskCard.setCardBackgroundColor(
                                android.graphics.Color.parseColor("#FFF9C4") // pastel yellow
                            )
                        }
                        else -> {
                            riskView.text = "🟢 LOW RISK"
                            riskView.setTextColor(android.graphics.Color.BLACK)
                            riskCard.setCardBackgroundColor(
                                android.graphics.Color.parseColor("#C8E6C9") // pastel green
                            )
                        }
                    }

                    framesView.text = "Unusual Frames: $frames"
                    watchTimeView.text = "Total screen Time: $watchTime sec"
                }

            } catch (e: Exception) {
                runOnUiThread {
                    riskView.text = "Error loading data"
                }
            }
        }
    }
    private fun startMonitoringSelectedChild() {

        handler.removeCallbacksAndMessages(null)

        handler.post(object : Runnable {
            override fun run() {

                selectedChild?.let {
                    fetchChildMetrics(it)
                }

                handler.postDelayed(this, 5000)
            }
        })
    }

    private fun showAddChildDialog() {

        val dialogLayout = LinearLayout(this)
        dialogLayout.orientation = LinearLayout.VERTICAL
        dialogLayout.setPadding(40, 40, 40, 40)

        val aliasInput = EditText(this)
        aliasInput.hint = "Child Name"

        val deviceIdInput = EditText(this)
        deviceIdInput.hint = "Child Device ID"

        dialogLayout.addView(aliasInput)
        dialogLayout.addView(deviceIdInput)

        AlertDialog.Builder(this)
            .setTitle("Add Child")
            .setView(dialogLayout)
            .setPositiveButton("Add") { _, _ ->

                val alias = aliasInput.text.toString().trim()
                val deviceId = deviceIdInput.text.toString().trim()

                if (alias.isNotEmpty() && deviceId.isNotEmpty()) {

                    val child = Child(deviceId, alias)
                    childList.add(child)

                    updateSpinner()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun updateSpinner() {

        val aliasList = childList.map { it.alias }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            aliasList
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        if (childList.isNotEmpty()) {
            spinner.visibility = View.VISIBLE   // 👈 show it
            spinner.setSelection(childList.size - 1)
        } else {
            spinner.visibility = View.GONE
            riskCard.visibility = View.GONE // 👈 hide if empty
        }

    }

    private fun buildUI() {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(40, 120, 40, 40)
        root.setBackgroundColor(android.graphics.Color.parseColor("#F8F9FA")) // soft light grey

        val title = TextView(this)
        title.text = "Parent Dashboard"
        title.textSize = 24f
        title.setTextColor(android.graphics.Color.parseColor("#34495E"))
        title.gravity = Gravity.CENTER

        val titleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        titleParams.bottomMargin = 60
        title.layoutParams = titleParams

        // 🔵 Add Button (Pastel Blue)
        addButton = Button(this)
        addButton.text = "➕ Add Child"
        addButton.setBackgroundColor(android.graphics.Color.parseColor("#A8DADC"))
        addButton.setTextColor(android.graphics.Color.BLACK)

        val addParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        addParams.bottomMargin = 40
        addButton.layoutParams = addParams

        // 🔽 Spinner
        spinner = Spinner(this)
        spinner.visibility = View.GONE

        val spinnerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        spinnerParams.bottomMargin = 60
        spinner.layoutParams = spinnerParams

        // 🟣 Risk Card
        riskCard = androidx.cardview.widget.CardView(this)
        riskCard.radius = 25f
        riskCard.cardElevation = 12f
        riskCard.visibility = View.GONE   // 👈 Hidden initially

        val riskLayout = LinearLayout(this)
        riskLayout.orientation = LinearLayout.VERTICAL
        riskLayout.setPadding(40, 40, 40, 40)

        riskView = TextView(this)
        riskView.textSize = 22f

        framesView = TextView(this)
        framesView.textSize = 18f

        watchTimeView = TextView(this)
        watchTimeView.textSize = 18f

        riskLayout.addView(riskView)
        riskLayout.addView(framesView)
        riskLayout.addView(watchTimeView)

        riskCard.addView(riskLayout)

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.bottomMargin = 40
        riskCard.layoutParams = cardParams

        // Add views in order
        root.addView(title)
        root.addView(addButton)
        root.addView(spinner)
        root.addView(riskCard)

        setContentView(root)

        addButton.setOnClickListener {
            showAddChildDialog()
        }

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    if (position < childList.size) {
                        selectedChild = childList[position]
                        startMonitoringSelectedChild()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }




}