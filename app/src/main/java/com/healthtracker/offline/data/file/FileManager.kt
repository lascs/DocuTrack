package com.healthtracker.offline.data.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.security.MessageDigest

/**
 * File management utility for export/import operations.
 * 
 * Handles file creation, validation, sharing, and cleanup
 * with proper error handling and progress tracking.
 */
class FileManager(private val context: Context) {
    
    companion object {
        private const val EXPORT_DIR = "exports"
        private const val TEMP_DIR = "temp"
        private const val BACKUP_DIR = "backups"
        private const val MAX_FILE_SIZE = 100 * 1024 * 1024 // 100MB
        private const val FILE_PROVIDER_AUTHORITY = ".fileprovider"
    }
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    // ========== DIRECTORY MANAGEMENT ==========
    
    /**
     * Gets the default export directory
     */
    suspend fun getExportDirectory(): File = withContext(Dispatchers.IO) {
        val exportDir = File(context.getExternalFilesDir(null), EXPORT_DIR)
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        exportDir
    }
    
    /**
     * Gets the backup directory
     */
    suspend fun getBackupDirectory(): File = withContext(Dispatchers.IO) {
        val backupDir = File(context.getExternalFilesDir(null), BACKUP_DIR)
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        backupDir
    }
    
    /**
     * Gets the temporary directory
     */
    suspend fun getTempDirectory(): File = withContext(Dispatchers.IO) {
        val tempDir = File(context.cacheDir, TEMP_DIR)
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }
        tempDir
    }
    
    // ========== FILE CREATION ==========
    
    /**
     * Creates a new export file with timestamp
     */
    suspend fun createExportFile(
        fileName: String,
        extension: String,
        isBackup: Boolean = false
    ): File = withContext(Dispatchers.IO) {
        val directory = if (isBackup) getBackupDirectory() else getExportDirectory()
        val timestamp = dateFormat.format(Date())
        val fullFileName = "${fileName}_$timestamp.$extension"
        File(directory, fullFileName)
    }
    
    /**
     * Creates a temporary file
     */
    suspend fun createTempFile(fileName: String, extension: String): File = withContext(Dispatchers.IO) {
        val tempDir = getTempDirectory()
        val timestamp = System.currentTimeMillis()
        val fullFileName = "${fileName}_$timestamp.$extension"
        File(tempDir, fullFileName)
    }
    
    // ========== FILE OPERATIONS ==========
    
    /**
     * Writes content to file with progress tracking
     */
    suspend fun writeFile(
        file: File,
        content: String,
        progressCallback: ((Int) -> Unit)? = null
    ): FileOperationResult = withContext(Dispatchers.IO) {
        try {
            val contentBytes = content.toByteArray(Charsets.UTF_8)
            
            // Check file size limit
            if (contentBytes.size > MAX_FILE_SIZE) {
                return@withContext FileOperationResult.failure("File size exceeds limit (${MAX_FILE_SIZE / 1024 / 1024}MB)")
            }
            
            FileOutputStream(file).use { outputStream ->
                val chunkSize = 8192
                var bytesWritten = 0
                
                contentBytes.inputStream().use { inputStream ->
                    val buffer = ByteArray(chunkSize)
                    var bytesRead: Int
                    
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        bytesWritten += bytesRead
                        
                        // Report progress
                        progressCallback?.invoke((bytesWritten * 100) / contentBytes.size)
                    }
                }
            }
            
            FileOperationResult.success(
                filePath = file.absolutePath,
                fileSize = file.length(),
                checksum = calculateChecksum(file)
            )
        } catch (e: Exception) {
            FileOperationResult.failure("Failed to write file: ${e.message}")
        }
    }
    
    /**
     * Reads content from file with progress tracking
     */
    suspend fun readFile(
        file: File,
        progressCallback: ((Int) -> Unit)? = null
    ): FileReadResult = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                return@withContext FileReadResult.failure("File does not exist: ${file.absolutePath}")
            }
            
            if (file.length() > MAX_FILE_SIZE) {
                return@withContext FileReadResult.failure("File size exceeds limit (${MAX_FILE_SIZE / 1024 / 1024}MB)")
            }
            
            val content = StringBuilder()
            val fileSize = file.length()
            var bytesRead = 0L
            
            FileInputStream(file).use { inputStream ->
                val buffer = ByteArray(8192)
                var read: Int
                
                while (inputStream.read(buffer).also { read = it } != -1) {
                    content.append(String(buffer, 0, read, Charsets.UTF_8))
                    bytesRead += read
                    
                    // Report progress
                    progressCallback?.invoke(((bytesRead * 100) / fileSize).toInt())
                }
            }
            
            FileReadResult.success(
                content = content.toString(),
                fileSize = fileSize,
                checksum = calculateChecksum(file)
            )
        } catch (e: Exception) {
            FileReadResult.failure("Failed to read file: ${e.message}")
        }
    }
    
    /**
     * Copies file to another location
     */
    suspend fun copyFile(source: File, destination: File): FileOperationResult = withContext(Dispatchers.IO) {
        try {
            if (!source.exists()) {
                return@withContext FileOperationResult.failure("Source file does not exist")
            }
            
            // Ensure destination directory exists
            destination.parentFile?.mkdirs()
            
            source.copyTo(destination, overwrite = true)
            
            FileOperationResult.success(
                filePath = destination.absolutePath,
                fileSize = destination.length(),
                checksum = calculateChecksum(destination)
            )
        } catch (e: Exception) {
            FileOperationResult.failure("Failed to copy file: ${e.message}")
        }
    }
    
    // ========== FILE VALIDATION ==========
    
    /**
     * Validates file existence and accessibility
     */
    suspend fun validateFile(file: File): FileValidationResult = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        if (!file.exists()) {
            errors.add("File does not exist: ${file.absolutePath}")
        } else {
            if (!file.canRead()) {
                errors.add("File is not readable: ${file.absolutePath}")
            }
            
            if (file.length() == 0L) {
                warnings.add("File is empty: ${file.absolutePath}")
            }
            
            if (file.length() > MAX_FILE_SIZE) {
                errors.add("File size exceeds limit (${MAX_FILE_SIZE / 1024 / 1024}MB)")
            }
        }
        
        FileValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            fileSize = if (file.exists()) file.length() else 0,
            lastModified = if (file.exists()) file.lastModified() else 0
        )
    }
    
    /**
     * Validates file format based on extension
     */
    suspend fun validateFileFormat(file: File, expectedExtension: String): FileValidationResult = withContext(Dispatchers.IO) {
        val baseValidation = validateFile(file)
        if (!baseValidation.isValid) {
            return@withContext baseValidation
        }
        
        val errors = baseValidation.errors.toMutableList()
        val warnings = baseValidation.warnings.toMutableList()
        
        val actualExtension = file.extension.lowercase()
        val expected = expectedExtension.lowercase().removePrefix(".")
        
        if (actualExtension != expected) {
            errors.add("Expected .$expected file, found .$actualExtension")
        }
        
        baseValidation.copy(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
    
    // ========== FILE SHARING ==========
    
    /**
     * Creates a sharing intent for the file
     */
    suspend fun createSharingIntent(file: File, mimeType: String): Intent? = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                return@withContext null
            }
            
            val authority = context.packageName + FILE_PROVIDER_AUTHORITY
            val uri = FileProvider.getUriForFile(context, authority, file)
            
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Doctor Tracker Export")
                putExtra(Intent.EXTRA_TEXT, "Exported data from Offline Doctor Tracker")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Gets shareable URI for file
     */
    suspend fun getShareableUri(file: File): Uri? = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                return@withContext null
            }
            
            val authority = context.packageName + FILE_PROVIDER_AUTHORITY
            FileProvider.getUriForFile(context, authority, file)
        } catch (e: Exception) {
            null
        }
    }
    
    // ========== FILE CLEANUP ==========
    
    /**
     * Cleans up temporary files
     */
    suspend fun cleanupTempFiles(): FileCleanupResult = withContext(Dispatchers.IO) {
        try {
            val tempDir = getTempDirectory()
            var deletedCount = 0
            var deletedSize = 0L
            
            tempDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    deletedSize += file.length()
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
            
            FileCleanupResult.success(deletedCount, deletedSize)
        } catch (e: Exception) {
            FileCleanupResult.failure("Failed to cleanup temp files: ${e.message}")
        }
    }
    
    /**
     * Cleans up old export files
     */
    suspend fun cleanupOldExports(olderThanDays: Int = 30): FileCleanupResult = withContext(Dispatchers.IO) {
        try {
            val exportDir = getExportDirectory()
            val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
            var deletedCount = 0
            var deletedSize = 0L
            
            exportDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < cutoffTime) {
                    deletedSize += file.length()
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
            
            FileCleanupResult.success(deletedCount, deletedSize)
        } catch (e: Exception) {
            FileCleanupResult.failure("Failed to cleanup old exports: ${e.message}")
        }
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Calculates MD5 checksum of file
     */
    private suspend fun calculateChecksum(file: File): String = withContext(Dispatchers.IO) {
        try {
            val md = MessageDigest.getInstance("MD5")
            FileInputStream(file).use { inputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Gets human-readable file size
     */
    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.1f %s".format(size, units[unitIndex])
    }
    
    /**
     * Gets MIME type for file extension
     */
    fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "json" -> "application/json"
            "csv" -> "text/csv"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}

// ========== RESULT CLASSES ==========

/**
 * Result of file operation
 */
data class FileOperationResult(
    val success: Boolean,
    val filePath: String = "",
    val fileSize: Long = 0,
    val checksum: String = "",
    val error: String = ""
) {
    companion object {
        fun success(filePath: String, fileSize: Long, checksum: String) = 
            FileOperationResult(true, filePath, fileSize, checksum)
        
        fun failure(error: String) = 
            FileOperationResult(false, error = error)
    }
}

/**
 * Result of file read operation
 */
data class FileReadResult(
    val success: Boolean,
    val content: String = "",
    val fileSize: Long = 0,
    val checksum: String = "",
    val error: String = ""
) {
    companion object {
        fun success(content: String, fileSize: Long, checksum: String) = 
            FileReadResult(true, content, fileSize, checksum)
        
        fun failure(error: String) = 
            FileReadResult(false, error = error)
    }
}

/**
 * Result of file validation
 */
data class FileValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val fileSize: Long = 0,
    val lastModified: Long = 0
) {
    fun getSummary(): String {
        return when {
            !isValid -> "Validation failed: ${errors.joinToString(", ")}"
            warnings.isNotEmpty() -> "Validation passed with warnings: ${warnings.joinToString(", ")}"
            else -> "File is valid"
        }
    }
}

/**
 * Result of file cleanup operation
 */
data class FileCleanupResult(
    val success: Boolean,
    val deletedCount: Int = 0,
    val deletedSize: Long = 0,
    val error: String = ""
) {
    companion object {
        fun success(deletedCount: Int, deletedSize: Long) = 
            FileCleanupResult(true, deletedCount, deletedSize)
        
        fun failure(error: String) = 
            FileCleanupResult(false, error = error)
    }
    
    fun getSummary(): String {
        return if (success) {
            "Deleted $deletedCount files (${formatSize(deletedSize)})"
        } else {
            "Cleanup failed: $error"
        }
    }
    
    private fun formatSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.1f %s".format(size, units[unitIndex])
    }
}