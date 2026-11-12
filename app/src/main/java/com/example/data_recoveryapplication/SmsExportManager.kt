package com.example.data_recoveryapplication

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object SmsExportManager {

    private const val FOLDER_NAME = "DataRecovery"

    fun exportCsv(
        context: Context,
        messages: List<SmsRecoveryResult>
    ): Boolean {

        return exportFile(
            context = context,
            fileName = "recovered_sms.csv",
            mimeType = "text/csv"
        ) { outputStream ->

            outputStream.writer(Charsets.UTF_8).use { writer ->

                writer.appendLine(
                    "Sender/Recipient,Message,Date & Time,Type,Recovery Status,Confidence,Source,SHA-256"
                )

                messages.forEach { sms ->

                    val hash =
                        SmsIntegrityHasher.sha256(sms)

                    writer.appendLine(
                        listOf(
                            csvEscape(sms.sender),
                            csvEscape(sms.message),
                            csvEscape(sms.dateTime),
                            csvEscape(sms.type),
                            csvEscape(sms.recoveryStatus),
                            csvEscape(sms.confidence),
                            csvEscape(sms.source.toString()),
                            hash
                        ).joinToString(",")
                    )
                }
            }
        }
    }


    fun exportTxt(
        context: Context,
        messages: List<SmsRecoveryResult>
    ): Boolean {

        return exportFile(
            context = context,
            fileName = "recovered_sms.txt",
            mimeType = "text/plain"
        ) { outputStream ->

            outputStream.writer(Charsets.UTF_8).use { writer ->

                writer.appendLine("SMS RECOVERY REPORT")
                writer.appendLine("========================================")
                writer.appendLine(
                    "Total Recovered SMS: ${messages.size}"
                )
                writer.appendLine()

                messages.forEachIndexed { index, sms ->

                    val hash =
                        SmsIntegrityHasher.sha256(sms)

                    writer.appendLine(
                        "Recovered SMS #${index + 1}"
                    )

                    writer.appendLine(
                        "----------------------------------------"
                    )

                    writer.appendLine(
                        "Sender/Recipient: ${sms.sender}"
                    )

                    writer.appendLine(
                        "Message: ${sms.message}"
                    )

                    writer.appendLine(
                        "Date & Time: ${sms.dateTime}"
                    )

                    writer.appendLine(
                        "Type: ${sms.type}"
                    )

                    writer.appendLine(
                        "Recovery Status: ${sms.recoveryStatus}"
                    )

                    writer.appendLine(
                        "Confidence: ${sms.confidence}"
                    )

                    writer.appendLine(
                        "Source: ${sms.source}"
                    )

                    writer.appendLine(
                        "SHA-256: $hash"
                    )

                    writer.appendLine()
                }
            }
        }
    }


    fun exportPdf(
        context: Context,
        messages: List<SmsRecoveryResult>
    ): Boolean {

        return exportFile(
            context = context,
            fileName = "recovered_sms.pdf",
            mimeType = "application/pdf"
        ) { outputStream ->

            createPdf(
                outputStream,
                messages
            )
        }
    }


    /*
     * ============================================================
     * EXPORT FILE
     * ============================================================
     *
     * Android 10+:
     * Uses MediaStore Downloads.
     *
     * Android 9 and below:
     * Uses Downloads/DataRecovery.
     */
    private fun exportFile(
        context: Context,
        fileName: String,
        mimeType: String,
        writeContent: (OutputStream) -> Unit
    ): Boolean {

        return try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                /*
                 * =================================================
                 * ANDROID 10+
                 * =================================================
                 */

                val resolver =
                    context.contentResolver

                val values =
                    ContentValues().apply {

                        put(
                            MediaStore.Downloads.DISPLAY_NAME,
                            fileName
                        )

                        put(
                            MediaStore.Downloads.MIME_TYPE,
                            mimeType
                        )

                        put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS +
                                    "/" +
                                    FOLDER_NAME
                        )

                        put(
                            MediaStore.Downloads.IS_PENDING,
                            1
                        )
                    }

                val collection =
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI

                val uri =
                    resolver.insert(
                        collection,
                        values
                    ) ?: return false

                try {

                    resolver.openOutputStream(uri)?.use { outputStream ->

                        writeContent(
                            outputStream
                        )

                    } ?: throw Exception(
                        "Unable to open output stream"
                    )

                    val completedValues =
                        ContentValues().apply {

                            put(
                                MediaStore.Downloads.IS_PENDING,
                                0
                            )
                        }

                    resolver.update(
                        uri,
                        completedValues,
                        null,
                        null
                    )

                    true

                } catch (e: Exception) {

                    resolver.delete(
                        uri,
                        null,
                        null
                    )

                    false
                }

            } else {

                /*
                 * =================================================
                 * ANDROID 9 AND BELOW
                 * =================================================
                 */

                val downloadsDirectory =
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )

                val recoveryFolder =
                    File(
                        downloadsDirectory,
                        FOLDER_NAME
                    )

                if (!recoveryFolder.exists()) {

                    if (!recoveryFolder.mkdirs() &&
                        !recoveryFolder.exists()
                    ) {

                        return false
                    }
                }

                val outputFile =
                    File(
                        recoveryFolder,
                        fileName
                    )

                FileOutputStream(
                    outputFile
                ).use { outputStream ->

                    writeContent(
                        outputStream
                    )
                }

                true
            }

        } catch (e: Exception) {

            e.printStackTrace()

            false
        }
    }


    /*
     * ============================================================
     * CSV ESCAPE
     * ============================================================
     */
    private fun csvEscape(
        value: String
    ): String {

        val escaped =
            value.replace(
                "\"",
                "\"\""
            )

        return "\"$escaped\""
    }


    /*
     * ============================================================
     * PDF CREATION
     * ============================================================
     */
    private fun createPdf(
        outputStream: OutputStream,
        messages: List<SmsRecoveryResult>
    ) {

        val document =
            android.graphics.pdf.PdfDocument()

        val paint =
            android.graphics.Paint().apply {

                textSize = 10f
            }

        val titlePaint =
            android.graphics.Paint().apply {

                textSize = 16f

                isFakeBoldText = true
            }

        var pageNumber = 1

        var page =
            document.startPage(
                android.graphics.pdf.PdfDocument.PageInfo.Builder(
                    595,
                    842,
                    pageNumber
                ).create()
            )

        var canvas =
            page.canvas

        var y = 40f

        canvas.drawText(
            "SMS RECOVERY REPORT",
            40f,
            y,
            titlePaint
        )

        y += 25f

        canvas.drawText(
            "Total Recovered SMS: ${messages.size}",
            40f,
            y,
            paint
        )

        y += 30f


        fun newPage() {

            document.finishPage(page)

            pageNumber++

            page =
                document.startPage(
                    android.graphics.pdf.PdfDocument.PageInfo.Builder(
                        595,
                        842,
                        pageNumber
                    ).create()
                )

            canvas =
                page.canvas

            y = 40f
        }


        messages.forEachIndexed { index, sms ->

            val hash =
                SmsIntegrityHasher.sha256(sms)

            val lines =
                listOf(
                    "Recovered SMS #${index + 1}",
                    "Sender/Recipient: ${sms.sender}",
                    "Message: ${sms.message}",
                    "Date & Time: ${sms.dateTime}",
                    "Type: ${sms.type}",
                    "Recovery Status: ${sms.recoveryStatus}",
                    "Confidence: ${sms.confidence}",
                    "Source: ${sms.source}",
                    "SHA-256: $hash"
                )

            lines.forEach { line ->

                if (y > 800f) {

                    newPage()
                }

                canvas.drawText(
                    line.take(95),
                    40f,
                    y,
                    paint
                )

                y += 18f
            }

            y += 15f
        }

        document.finishPage(page)

        document.writeTo(
            outputStream
        )

        document.close()
    }
}