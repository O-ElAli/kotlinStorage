package com.example.newperms

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionRequestActivity : Activity() {
    companion object {
        private const val REQUEST_USAGE_ACCESS = 1
        private const val REQUEST_EXTERNAL_STORAGE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isUsageAccessGranted()) {
            showUsageAccessDialog()
        } else if (!isExternalStorageAccessGranted()) {
            requestExternalStoragePermission()
        } else {
            proceedToNextActivity()
        }
    }

    private fun showUsageAccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Usage Access Required")
            .setMessage("This app requires access to usage stats to function properly.")
            .setPositiveButton("Grant") { _, _ ->
                startActivityForResult(
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                    REQUEST_USAGE_ACCESS
                )
            }
            .setNegativeButton("Cancel") { _, _ -> finish() }
            .create()
            .show()
    }

    private fun isUsageAccessGranted(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                proceedToNextActivity()
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                proceedToNextActivity()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun isExternalStorageAccessGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun proceedToNextActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_USAGE_ACCESS -> {
                if (isUsageAccessGranted()) {
                    requestExternalStoragePermission()
                } else {
                    Toast.makeText(this, "Usage access is required", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            REQUEST_EXTERNAL_STORAGE -> {
                if (isExternalStorageAccessGranted()) {
                    proceedToNextActivity()
                } else {
                    Toast.makeText(this, "External storage permission is required", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
