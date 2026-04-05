package com.example.parentalcontrolapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class DashboardActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var deviceId:String

    private lateinit var screenTime:TextView
    private lateinit var deviceText:TextView
    private lateinit var topAppsList:TextView

    private lateinit var chart:BarChart

    private lateinit var appsRecycler:RecyclerView
    private lateinit var blockedRecycler:RecyclerView

    // ⭐ NEW
    private lateinit var riskRecycler:RecyclerView

    private lateinit var blockInput:EditText
    private lateinit var blockBtn:Button

    private lateinit var limitInput:EditText
    private lateinit var setLimitBtn:Button

    private lateinit var lockBtn:Button
    private lateinit var unlockBtn:Button


    private val refreshRunnable = object : Runnable {
        override fun run() {

            loadUsage()
            loadUsageChart()
            loadRiskTimeline()

            handler.postDelayed(this,8000)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        deviceId = intent.getStringExtra("device_id") ?: "child_device_1"

        deviceText = findViewById(R.id.deviceId)
        screenTime = findViewById(R.id.screenTime)
        topAppsList = findViewById(R.id.topAppsList)

        chart = findViewById(R.id.screenChart)

        lockBtn = findViewById(R.id.lockButton)
        unlockBtn = findViewById(R.id.unlockButton)

        blockInput = findViewById(R.id.blockAppInput)
        blockBtn = findViewById(R.id.blockButton)

        limitInput = findViewById(R.id.limitInput)
        setLimitBtn = findViewById(R.id.setLimitButton)

        appsRecycler = findViewById(R.id.appsRecycler)
        appsRecycler.layoutManager = LinearLayoutManager(this)

        blockedRecycler = findViewById(R.id.blockedRecycler)
        blockedRecycler.layoutManager = LinearLayoutManager(this)

        // ⭐ NEW
        riskRecycler = findViewById(R.id.riskRecycler)
        riskRecycler.layoutManager = LinearLayoutManager(this)

        deviceText.text = "Connected Child ID: $deviceId"

        loadUsage()
        loadUsageChart()
        loadApps()
        loadBlockedApps()
        loadRiskTimeline()

        handler.post(refreshRunnable)

        lockBtn.setOnClickListener { sendCommand("LOCK") }
        unlockBtn.setOnClickListener { sendCommand("UNLOCK") }

        blockBtn.setOnClickListener {

            val pkg = blockInput.text.toString()

            if(pkg.isEmpty()){
                Toast.makeText(this,"Enter package name",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            blockApp(pkg)
        }

        setLimitBtn.setOnClickListener {

            val limit = limitInput.text.toString().toIntOrNull()

            if(limit==null){
                Toast.makeText(this,"Enter minutes",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val req = SetLimitRequest(deviceId,limit)

            RetrofitClient.instance.setLimit(req)
                .enqueue(object:Callback<Map<String,String>>{

                    override fun onResponse(
                        call: Call<Map<String,String>>,
                        response: Response<Map<String,String>>
                    ) {
                        Toast.makeText(this@DashboardActivity,"Limit set",Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(
                        call: Call<Map<String,String>>,
                        t: Throwable
                    ) {
                        Toast.makeText(this@DashboardActivity,"Failed",Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable)
    }


    private fun loadUsage(){

        RetrofitClient.instance.getUsage(deviceId)
            .enqueue(object:Callback<UsageResponse>{

                override fun onResponse(
                    call: Call<UsageResponse>,
                    response: Response<UsageResponse>
                ) {

                    val total = response.body()?.totalScreenTime ?: 0

                    val hours = total / 3600
                    val minutes = (total % 3600) / 60

                    screenTime.text = "Screen Time Today: ${hours}h ${minutes}m"
                }

                override fun onFailure(
                    call: Call<UsageResponse>,
                    t: Throwable
                ) {
                    screenTime.text = "Unable to load"
                }

            })
    }


    private fun loadUsageChart(){

        RetrofitClient.instance.getUsageSummary(deviceId)
            .enqueue(object:Callback<Map<String,Any>>{

                override fun onResponse(
                    call: Call<Map<String,Any>>,
                    response: Response<Map<String,Any>>
                ) {

                    val apps = response.body()?.get("apps") as? List<Map<String,Any>> ?: listOf()

                    if(apps.isEmpty()){
                        chart.clear()
                        return
                    }

                    val entries = ArrayList<BarEntry>()
                    val labels = ArrayList<String>()

                    var index = 0f

                    apps.take(5).forEach {

                        val seconds = (it["total_seconds"] as Number).toFloat()
                        val minutes = seconds/60

                        entries.add(BarEntry(index,minutes))

                        val pkg = it["package_name"].toString()

                        val name = getAppName(pkg)

                        val shortName =
                            if(name.length>10) name.substring(0,10)
                            else name

                        labels.add(shortName)

                        index++
                    }

                    val dataSet = BarDataSet(entries,"Usage Minutes")

                    dataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
                    dataSet.valueTextSize = 12f

                    val data = BarData(dataSet)

                    chart.data = data

                    chart.description.isEnabled = false
                    chart.setFitBars(true)
                    chart.animateY(1000)

                    val xAxis = chart.xAxis

                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.granularity = 1f
                    xAxis.valueFormatter = IndexAxisValueFormatter(labels)

                    chart.invalidate()

                    val topText = StringBuilder()

                    apps.take(5).forEach{

                        val pkg = it["package_name"].toString()
                        val seconds = (it["total_seconds"] as Number).toInt()

                        val minutes = seconds/60

                        val name = getAppName(pkg)

                        topText.append("$name - ${minutes} min\n")
                    }

                    topAppsList.text = topText.toString()

                }

                override fun onFailure(
                    call: Call<Map<String,Any>>,
                    t: Throwable
                ) {}

            })
    }


    // ⭐ NEW RISK TIMELINE
    private fun loadRiskTimeline(){

        RetrofitClient.instance.getIncidents(deviceId)
            .enqueue(object: Callback<IncidentsResponse>{

                override fun onResponse(
                    call: Call<IncidentsResponse>,
                    response: Response<IncidentsResponse>
                ) {

                    val incidents = response.body()?.incidents ?: listOf()

                    val list = mutableListOf<String>()

                    incidents.forEach{

                        val riskLevel =
                            when{
                                it.peak_risk > 80 -> "CRITICAL"
                                it.peak_risk > 50 -> "HIGH"
                                it.peak_risk > 25 -> "MEDIUM"
                                else -> "LOW"
                            }

                        list.add(
                            "Risk: $riskLevel\nDuration: ${it.durationSeconds}s\nStarted: ${it.started_at}"
                        )
                    }

                    riskRecycler.adapter = RiskAdapter(list)
                }

                override fun onFailure(call: Call<IncidentsResponse>, t: Throwable) {}

            })
    }


    private fun loadApps(){

        RetrofitClient.instance.getInstalledApps(deviceId)
            .enqueue(object:Callback<Map<String,Any>>{

                override fun onResponse(
                    call: Call<Map<String,Any>>,
                    response: Response<Map<String,Any>>
                ) {

                    val apps = response.body()?.get("apps") as? List<Map<String,String>> ?: listOf()

                    appsRecycler.adapter =
                        InstalledAppsAdapter(
                            apps,
                            listOf(),
                            {pkg->blockApp(pkg)},
                            {pkg->unblockApp(pkg)}
                        )
                }

                override fun onFailure(
                    call: Call<Map<String,Any>>,
                    t: Throwable
                ) {}

            })
    }


    private fun loadBlockedApps(){

        RetrofitClient.instance.getBlockedApps(deviceId)
            .enqueue(object:Callback<Map<String,Any>>{

                override fun onResponse(
                    call: Call<Map<String,Any>>,
                    response: Response<Map<String,Any>>
                ) {

                    val apps = response.body()?.get("blocked_apps") as? List<String> ?: listOf()

                    blockedRecycler.adapter = CommandAdapter(apps)

                }

                override fun onFailure(
                    call: Call<Map<String,Any>>,
                    t: Throwable
                ) {}

            })
    }


    private fun blockApp(pkg:String){

        val data = mapOf(
            "device_id" to deviceId,
            "package_name" to pkg
        )

        RetrofitClient.instance.blockApp(data)
            .enqueue(object:Callback<Map<String,String>>{

                override fun onResponse(
                    call: Call<Map<String,String>>,
                    response: Response<Map<String,String>>
                ) {

                    loadBlockedApps()
                    loadApps()
                }

                override fun onFailure(
                    call: Call<Map<String,String>>,
                    t: Throwable
                ) {}

            })
    }


    private fun unblockApp(pkg:String){

        val data = mapOf(
            "device_id" to deviceId,
            "package_name" to pkg
        )

        RetrofitClient.instance.unblockApp(data)
            .enqueue(object:Callback<Map<String,String>>{

                override fun onResponse(
                    call: Call<Map<String,String>>,
                    response: Response<Map<String,String>>
                ) {

                    loadBlockedApps()
                    loadApps()
                }

                override fun onFailure(
                    call: Call<Map<String,String>>,
                    t: Throwable
                ) {}

            })
    }


    private fun sendCommand(type:String){

        val req = CommandRequest(deviceId,type)

        RetrofitClient.instance.sendCommand(req)
            .enqueue(object:Callback<Map<String,String>>{

                override fun onResponse(
                    call: Call<Map<String,String>>,
                    response: Response<Map<String,String>>
                ) {

                    Toast.makeText(this@DashboardActivity,"$type sent",Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(
                    call: Call<Map<String,String>>,
                    t: Throwable
                ) {

                    Toast.makeText(this@DashboardActivity,"Failed",Toast.LENGTH_SHORT).show()
                }

            })
    }


    private fun getAppName(pkg:String):String{

        return try{

            val pm = packageManager
            val info = pm.getApplicationInfo(pkg,0)

            pm.getApplicationLabel(info).toString()

        }catch(e:Exception){

            pkg
        }
    }
}