package org.example.rufusmobile

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var fileText: TextView

    companion object {
        private const val PICK_ISO_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(24))
        }
        scroll.addView(root)

        val title = TextView(this).apply {
            text = "Rufus Mobile"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "Herramienta Android para trabajar con imágenes ISO."
            textSize = 16f
            setPadding(0, dp(8), 0, dp(20))
        }
        root.addView(subtitle)

        val chooseButton = Button(this).apply {
            text = "Seleccionar imagen ISO"
            setOnClickListener { chooseIso() }
        }
        root.addView(chooseButton, matchWidth())

        fileText = TextView(this).apply {
            text = "Todavía no has seleccionado una ISO."
            textSize = 16f
            setPadding(0, dp(16), 0, dp(16))
        }
        root.addView(fileText)

        statusText = TextView(this).apply {
            text = "Estado: listo"
            textSize = 15f
            setPadding(0, dp(8), 0, dp(16))
        }
        root.addView(statusText)

        val warning = TextView(this).apply {
            text = "Esta versión solo selecciona archivos y muestra información. Todavía no escribe ni formatea memorias USB."
            textSize = 14f
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(0xFFECEFF1.toInt())
        }
        root.addView(warning, matchWidth())

        setContentView(scroll)
    }

    private fun chooseIso() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf("application/x-iso9660-image", "application/octet-stream")
            )
        }
        try {
            startActivityForResult(intent, PICK_ISO_REQUEST)
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir el selector de archivos.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Deprecated("Se actualizará en una versión posterior")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != PICK_ISO_REQUEST || resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return

        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        }

        fileText.text = queryFile(uri)
        statusText.text =
            "Estado: archivo seleccionado. Todavía no se ha escrito en ninguna USB."
    }

    private fun queryFile(uri: Uri): String {
        var name = uri.lastPathSegment ?: "Archivo seleccionado"
        var size: Long? = null

        contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex =
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex =
                    cursor.getColumnIndex(OpenableColumns.SIZE)

                if (nameIndex >= 0) {
                    name = cursor.getString(nameIndex) ?: name
                }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }

        val sizeText = size?.let { formatBytes(it) } ?: "No disponible"
        val isIso = name.lowercase(Locale.ROOT).endsWith(".iso")

        return "Archivo: $name\nTamaño: $sizeText\nExtensión ISO: " +
            if (isIso) "sí" else "no confirmada"
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes bytes"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.getDefault(), "%.2f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(Locale.getDefault(), "%.2f MB", mb)
        val gb = mb / 1024.0
        return String.format(Locale.getDefault(), "%.2f GB", gb)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun matchWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}
