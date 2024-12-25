package com.example.newperms

import android.app.Activity
import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.widget.TextView
import androidx.annotation.RequiresApi

class MainActivity : Activity() {

    private lateinit var outputTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the TextView from XML layout
        outputTextView = findViewById(R.id.textView)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!hasUsageAccessPermission()) {
                startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
            } else {
                displayInstalledAppsWithSizes()
            }
        } else {
            displayInstalledAppsWithSizes()
        }
    }

    private fun hasUsageAccessPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun displayInstalledAppsWithSizes() {
        val pm: PackageManager = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val appDetails = StringBuilder()

        for (app in apps) {
            val appName = pm.getApplicationLabel(app).toString()
            val packageName = app.packageName
            val appSize = getAppSize(packageName)
            appDetails.append("App: $appName\nPackage: $packageName\nSize: $appSize MB\n\n")
        }

        outputTextView.text = appDetails.toString()
    }

    private fun getAppSize(packageName: String): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val storageStatsManager =
                    getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
                val uuid = storageManager.getUuidForPath(filesDir)
                val storageStats: StorageStats =
                    storageStatsManager.queryStatsForPackage(uuid, packageName, android.os.Process.myUserHandle())
                val sizeInMB = storageStats.appBytes / (1024 * 1024)
                sizeInMB.toString()
            } else {
                val packageInfo = packageManager.getPackageInfo(packageName, 0)
                val sourceDir = packageInfo?.applicationInfo?.sourceDir
                if (sourceDir != null) {
                    val file = java.io.File(sourceDir)
                    val sizeInBytes = file.length()
                    (sizeInBytes / (1024 * 1024)).toString()
                } else {
                    "Unknown"
                }
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
