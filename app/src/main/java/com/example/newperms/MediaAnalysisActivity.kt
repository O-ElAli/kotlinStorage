package com.example.newperms

import android.app.Activity
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView

class MediaAnalysisActivity : Activity() {

    private lateinit var outputTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        outputTextView = findViewById(R.id.output_text)

        // Display Media Analysis
        outputTextView.text = getMediaFilesSize()
    }

    private fun getMediaFilesSize(): String {
        val mediaStats = StringBuilder()
        val totalPicsSize = getTotalSize(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val totalVideosSize = getTotalSize(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        val totalDocsSize = getTotalSize(MediaStore.Files.getContentUri("external"))

        mediaStats.append("Pictures: ${totalPicsSize / (1024 * 1024)} MB\n")
        mediaStats.append("Videos: ${totalVideosSize / (1024 * 1024)} MB\n")
        mediaStats.append("Documents: ${totalDocsSize / (1024 * 1024)} MB\n")

        return mediaStats.toString()
    }

    private fun getTotalSize(uri: android.net.Uri): Long {
        var totalSize: Long = 0
        val projection = arrayOf(MediaStore.MediaColumns.SIZE)

        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            while (cursor.moveToNext()) {
                totalSize += cursor.getLong(sizeIndex)
            }
        }
        return totalSize
    }
}
