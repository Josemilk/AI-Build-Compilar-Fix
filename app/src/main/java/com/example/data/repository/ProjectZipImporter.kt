package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.model.ProjectFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.util.zip.ZipInputStream

/**
 * Unzips a real Android project archive into ProjectFile entries
 * so the agent and GitHub push can work with the actual source tree.
 */
object ProjectZipImporter {

    private val TEXT_EXTENSIONS = setOf(
        "kt", "kts", "java", "xml", "gradle", "properties", "pro", "md", "txt",
        "json", "yml", "yaml", "toml", "cfg", "ini", "gitignore", "env", "example",
        "c", "cpp", "h", "hpp", "swift", "js", "ts", "html", "css", "sql"
    )

    private val SKIP_PREFIXES = listOf(
        "__MACOSX/", ".git/", "build/", ".gradle/", ".idea/",
        "captures/", ".cxx/", "local.properties"
    )

    private const val MAX_FILES = 200
    private const val MAX_FILE_BYTES = 512 * 1024 // 512 KB per text file

    data class ImportResult(
        val files: List<ProjectFile>,
        val skippedBinary: Int,
        val skippedLarge: Int,
        val rootHint: String?
    )

    suspend fun importFromUri(context: Context, uri: Uri): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val input = resolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("No se pudo abrir el ZIP"))

            val files = mutableListOf<ProjectFile>()
            var skippedBinary = 0
            var skippedLarge = 0
            var rootHint: String? = null

            ZipInputStream(BufferedInputStream(input)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null && files.size < MAX_FILES) {
                    val name = entry.name.replace('\\', '/')
                    val lower = name.lowercase()

                    val shouldSkip = entry.isDirectory ||
                        SKIP_PREFIXES.any { lower.contains(it) } ||
                        name.startsWith(".") && name.count { it == '/' } == 0

                    if (!shouldSkip) {
                        val simpleName = name.substringAfterLast('/')
                        val ext = simpleName.substringAfterLast('.', "").lowercase()
                        val isText = ext in TEXT_EXTENSIONS ||
                            simpleName == "gradlew" ||
                            simpleName.startsWith("Dockerfile") ||
                            !simpleName.contains('.')

                        if (!isText) {
                            skippedBinary++
                        } else {
                            val bytes = zis.readBytes()
                            if (bytes.size > MAX_FILE_BYTES) {
                                skippedLarge++
                            } else {
                                val content = bytes.toString(Charsets.UTF_8)
                                // Detect common Android project root (strip single top-level folder)
                                if (rootHint == null && name.contains('/')) {
                                    val first = name.substringBefore('/')
                                    if (first.isNotBlank() && !first.startsWith(".")) {
                                        rootHint = first
                                    }
                                }
                                files.add(
                                    ProjectFile(
                                        path = name,
                                        name = simpleName.ifBlank { name },
                                        extension = ext.ifBlank { "txt" },
                                        content = content,
                                        isModified = false
                                    )
                                )
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            // If every path shares the same top-level folder, strip it for cleaner repo layout
            val normalized = normalizeRoot(files, rootHint)

            if (normalized.isEmpty()) {
                Result.failure(Exception("El ZIP no contenía archivos de código legibles (kt/java/xml/gradle/...)."))
            } else {
                Result.success(
                    ImportResult(
                        files = normalized,
                        skippedBinary = skippedBinary,
                        skippedLarge = skippedLarge,
                        rootHint = rootHint
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun normalizeRoot(files: List<ProjectFile>, rootHint: String?): List<ProjectFile> {
        if (rootHint.isNullOrBlank()) return files
        val prefix = "$rootHint/"
        val allUnderRoot = files.all { it.path.startsWith(prefix) || it.path == rootHint }
        if (!allUnderRoot) return files
        return files.mapNotNull { f ->
            if (f.path == rootHint) null
            else {
                val newPath = f.path.removePrefix(prefix)
                f.copy(path = newPath, name = newPath.substringAfterLast('/'))
            }
        }
    }
}
