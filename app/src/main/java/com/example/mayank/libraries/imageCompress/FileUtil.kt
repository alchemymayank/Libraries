package com.example.mayank.libraries.imageCompress

import android.content.Context
import android.net.Uri
import java.nio.file.Files.delete
import java.nio.file.Files.exists
import java.io.File.separator
import android.provider.OpenableColumns
import com.example.mayank.libraries.Constants.showLogDebug
import java.io.*


internal object FileUtil {
    private val EOF = -1
    private val DEFAULT_BUFFER_SIZE = 1024 * 4

    @Throws(IOException::class)
    fun from(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(context, uri)
        val splitName = splitFileName(fileName)
        var tempFile = File.createTempFile(splitName[0], splitName[1])
        tempFile = rename(tempFile, fileName)
        tempFile.deleteOnExit()
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(tempFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        if (inputStream != null) {
            copy(inputStream, out!!)
            inputStream.close()
        }

        if (out != null) {
            out.close()
        }
        return tempFile
    }

    private fun splitFileName(fileName: String): Array<String> {
        var name = fileName
        var extension = ""
        val i = fileName.lastIndexOf(".")
        if (i != -1) {
            name = fileName.substring(0, i)
            extension = fileName.substring(i)
        }

        return arrayOf(name, extension)
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.getScheme().equals("content")) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
        }
        if (result == null) {
            result = uri.getPath()
            val cut = result!!.lastIndexOf(File.separator)
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun rename(file: File, newName: String): File {
        val newFile = File(file.getParent(), newName)
        if (!newFile.equals(file)) {
            if (newFile.exists() && newFile.delete()) {
                showLogDebug("FileUtil", "Delete old $newName file")
            }
            if (file.renameTo(newFile)) {
                showLogDebug("FileUtil", "Rename file to $newName")
            }
        }
        return newFile
    }

    @Throws(IOException::class)
    private fun copy(input: InputStream, output: OutputStream): Long {
        var count: Long = 0
        var n: Int
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        n = input.read(buffer)
        while (EOF != n) {
            output.write(buffer, 0, n)
            count += n.toLong()
            n = input.read(buffer)
        }
        return count
    }
}