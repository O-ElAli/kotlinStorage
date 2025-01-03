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
        val mediaDetails = StringBuilder()
        mediaDetails.append("Pictures: ${getTotalSize(MediaStore.Images.Media.EXTERNAL_CONTENT_URI) / (1024 * 1024)} MB\n")
        mediaDetails.append("Videos: ${getTotalSize(MediaStore.Video.Media.EXTERNAL_CONTENT_URI) / (1024 * 1024)} MB\n")
        mediaDetails.append("Documents:\n")
        mediaDetails.append(getDocumentsDetails())

        outputTextView.text = mediaDetails.toString()
    }

    private fun getTotalSize(uri: android.net.Uri): Long {
        var totalSize: Long = 0
        val projection = arrayOf(MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DISPLAY_NAME)

        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val fileSize = cursor.getLong(sizeIndex)
                val fileName = cursor.getString(nameIndex)
                totalSize += fileSize

                // Log file details for debugging
                android.util.Log.d("MediaFile", "Name: $fileName, Size: $fileSize")
            }
        }
        return totalSize
    }

    private fun getDocumentsDetails(): String {
        val documentDetailsList = mutableListOf<Triple<String, Long, String>>() // List of (Name, Size, Type)
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        // Query for specific document MIME types
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?"
        val selectionArgs = arrayOf("application/pdf", "application/msword", "application/vnd.ms-excel")

        var totalSize: Long = 0

        val cursor: Cursor? = contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val typeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val size = cursor.getLong(sizeIndex)
                val type = cursor.getString(typeIndex)

                // Only include files with valid size
                if (size > 0) {
                    totalSize += size
                    documentDetailsList.add(Triple(name, size, type))
                }
            }
        }

        // Sort by size in descending order
        documentDetailsList.sortByDescending { it.second }

        // Build the output string
        val documentDetails = StringBuilder()

        // Add total size at the top
        documentDetails.append("Documents Total Size: ${totalSize / (1024 * 1024)} MB\n\n")

        // Add details for each file
        for ((name, size, type) in documentDetailsList) {
            documentDetails.append("Name: $name\nSize: ${size / (1024 * 1024)} MB\nType: $type\n\n")
        }

        return if (documentDetails.isNotEmpty()) {
            documentDetails.toString()
        } else {
            "No documents found.\n"
        }
    }


}
