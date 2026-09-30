package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

/**
 * PRODUCTION USER AVATAR STORAGE
 * ARTHROSCAN-NER | Persistent Local User Profile Photo Vault
 */
object UserAvatarStorage {

    fun saveAvatar(context: Context, username: String, bitmap: Bitmap) {
        try {
            val safeName = username.lowercase().replace("[^a-z0-9_]".toRegex(), "_")
            val file = File(context.filesDir, "${safeName}_avatar.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAvatar(context: Context, username: String): Bitmap? {
        return try {
            val safeName = username.lowercase().replace("[^a-z0-9_]".toRegex(), "_")
            val file = File(context.filesDir, "${safeName}_avatar.png")
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                // Backward compatibility check for asha_avatar.png
                val fallbackFile = File(context.filesDir, "asha_avatar.png")
                if (fallbackFile.exists() && (username.contains("asha", ignoreCase = true) || username == "asha.cadre")) {
                    BitmapFactory.decodeFile(fallbackFile.absolutePath)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun removeAvatar(context: Context, username: String) {
        try {
            val safeName = username.lowercase().replace("[^a-z0-9_]".toRegex(), "_")
            val file = File(context.filesDir, "${safeName}_avatar.png")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
