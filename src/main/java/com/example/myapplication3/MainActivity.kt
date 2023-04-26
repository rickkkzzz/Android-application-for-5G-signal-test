package com.example.myapplication3

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.telephony.CellInfo
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.concurrent.Executors
import java.io.*
import kotlin.coroutines.jvm.internal.*
import kotlin.math.*


class MainActivity : AppCompatActivity() {
    private lateinit var locationManager: LocationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 获取TelephonyManager
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val signalStrengthTextView = findViewById<TextView>(R.id.signal_strength_text_view)
        val locationStrengthTextView = findViewById<TextView>(R.id.location_strength_text_view)
        val buttonOn = findViewById<Button>(R.id.button)
        // 获取TextView对象
        var signalStrings = ""
        // 检查设备是否支持5G

        var locationStrings = ""

        val fileName = "infor.txt"
        val path: File
        path = File( getExternalFilesDir(null).toString() )
        val letDictionary = File(path,"TMP")
        letDictionary.mkdir()
        val file = File(letDictionary, fileName)
        file.appendText("\nBegin:\n")
        signalStrengthTextView.text = file.absolutePath.toString()

        buttonOn.setOnClickListener {
            @SuppressLint("MissingPermission")
            //5G信号
            if (telephonyManager.isDataEnabled) {
                //5G信号更新
                val callback = object : TelephonyCallback(), TelephonyCallback.CellInfoListener {
                    override fun onCellInfoChanged(p0: MutableList<CellInfo>){
                        val size = p0.size
                        signalStrings = "CellInfo总个数：$size\n"
                        //遍历整个CellInfo
                        for (i in 0 until p0.size) {
                            val cellInfo = p0[i]
                            // 获取5G信号强度
                            val nrSignalStrength = cellInfo.cellSignalStrength
                            // 更新TextView上的显示
                            signalStrings += "CellInfo[$i]:\n$nrSignalStrength \n信号强度：${nrSignalStrength.dbm}dBM\nasuLevel: ${nrSignalStrength.asuLevel}\n抽象信号强度：${nrSignalStrength.level}\n "
                            signalStrengthTextView.text = signalStrings
                            print(signalStrings)
                            if(i == 0) {
                                file.appendText("Location: $locationStrings \n $nrSignalStrength \n")
                            }
                        }

                    }
                }
                val executor = Executors.newSingleThreadScheduledExecutor()
                //5G位置更新Callback函数
                telephonyManager.registerTelephonyCallback(
                executor,
                callback
                )
            }
            else{
                // 如果没有找到5G网络，显示未找到5G网络
                signalStrengthTextView.text = "未找到5G网络"
            }



            //位置信息有关常量
            val MIN_TIME_BETWEEN_UPDATES: Long = 1000
            val MIN_DISTANCE_BETWEEN_UPDATES: Float = 0.01F // 位置更新最小距离间隔，单位米
            var jizhanlat = 31.028758
            var jizhanlon = 121.430368
            //位置信息获取并更新
            val locationListener= object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val button2 = findViewById<Button>(R.id.button2)
                    button2.setOnClickListener{
                        jizhanlat = latitude
                        jizhanlon = longitude
                    }
                    val distance = getDistance(jizhanlat, jizhanlon, latitude, longitude)
                    locationStrings = "Latitude: $latitude, Longitude: $longitude，距离：$distance"
                    locationStrengthTextView.text=locationStrings
                    print(latitude)
                }
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES,
                    MIN_DISTANCE_BETWEEN_UPDATES, locationListener)
            }

            //output
        }
    }
    fun Context.showToast( text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this,text,duration).show()
    }
}

fun getDistance(
    bsLat: Double, // 基站纬度
    bsLon: Double, // 基站经度
    phoneLat: Double, // 手机纬度
    phoneLon: Double // 手机经度
): Double {
    val earthRadius = 6371000 // 地球半径，单位米

    // 将经纬度转换为弧度
    val bsLatRad = bsLat.toRadians()
    val bsLonRad = bsLon.toRadians()
    val phoneLatRad = phoneLat.toRadians()
    val phoneLonRad = phoneLon.toRadians()

    // 计算经纬度差
    val dLat = phoneLatRad - bsLatRad
    val dLon = phoneLonRad - bsLonRad

    // 计算距离
    val a = sin(dLat / 2).pow(2) + cos(bsLatRad) * cos(phoneLatRad) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}

fun Double.toRadians(): Double {
    return this * PI / 180
}


