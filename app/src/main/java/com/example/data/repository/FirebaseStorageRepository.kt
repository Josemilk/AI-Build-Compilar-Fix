package com.example.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class UploadEvent {
    data class Progress(val fraction: Float) : UploadEvent()
    data class Success(val downloadUrl: String) : UploadEvent()
    data class Error(val message: String) : UploadEvent()
}

class FirebaseStorageRepository {
    private val storage = FirebaseStorage.getInstance()

    /**
     * Uploads a file to Firebase Storage and emits real progress events.
     * Path: uploads/{uuid}.{extension}
     */
    fun uploadFileWithProgress(uri: Uri, extension: String): Flow<UploadEvent> = callbackFlow {
        val fileName = "uploads/${UUID.randomUUID()}.$extension"
        val ref = storage.reference.child(fileName)

        val uploadTask = ref.putFile(uri)

        val progressListener = uploadTask.addOnProgressListener { snapshot ->
            val total = snapshot.totalByteCount
            val transferred = snapshot.bytesTransferred
            val fraction = if (total > 0) (transferred.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
            trySend(UploadEvent.Progress(fraction))
        }

        uploadTask
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Upload failed")
                }
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                trySend(UploadEvent.Success(downloadUri.toString()))
                close()
            }
            .addOnFailureListener { e ->
                val msg = when (e) {
                    is StorageException -> e.message ?: "Firebase Storage error (${e.errorCode})"
                    else -> e.message ?: "Upload failed"
                }
                trySend(UploadEvent.Error(msg))
                close()
            }

        awaitClose {
            // Task continues; we just stop listening if collector is cancelled
        }
    }

    /** Simple suspend version (no progress) kept for compatibility */
    suspend fun uploadFile(uri: Uri, extension: String): Result<String> {
        return try {
            val fileName = "uploads/${UUID.randomUUID()}.$extension"
            val ref = storage.reference.child(fileName)
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
