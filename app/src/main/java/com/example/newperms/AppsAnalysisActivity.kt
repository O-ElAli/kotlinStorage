package com.example.newperms

import android.app.Activity
import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.widget.TextView

class AppsAnalysisActivity : Activity() {

    private lateinit var outputTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        outputTextView = findViewById(R.id.output_text)

        // Display Apps Analysis
        outputTextView.text = getInstalledAppsWithSizes()
    }

    private fun getInstalledAppsWithSizes(): String {
        val pm: PackageManager = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val appDetailsList = mutableListOf<Pair<String, Long>>()

        for (app in apps) {
            val appName = pm.getApplicationLabel(app).toString()
            val packageName = app.packageName
            val appSize = getAppSizeInBytes(packageName)
            appDetailsList.add(Pair("$appName ($packageName)", appSize))
        }

        // Sort by size in descending order
        appDetailsList.sortByDescending { it.second }

        val appDetails = StringBuilder()
        for ((app, size) in appDetailsList) {
            appDetails.append("$app: ${size / (1024 * 1024)} MB\n")
        }
        return appDetails.toString()
    }

    private fun getAppSizeInBytes(packageName: String): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val storageStatsManager =
                    getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
                val uuid = storageManager.getUuidForPath(filesDir)
                val storageStats: StorageStats =
                    storageStatsManager.queryStatsForPackage(uuid, packageName, android.os.Process.myUserHandle())
                storageStats.appBytes
            } else {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val sourceDir = packageInfo?.applicationInfo?.sourceDir
                if (sourceDir != null) {
                    val file = java.io.File(sourceDir)
                    file.length()
                } else {
                    0L
                }
            }
        } catch (e: Exception) {
            0L
        }
    }
}
