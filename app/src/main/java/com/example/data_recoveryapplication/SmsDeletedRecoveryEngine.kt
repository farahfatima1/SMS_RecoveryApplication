package com.example.data_recoveryapplication

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsDeletedRecoveryEngine {

    fun recoverFromDatabase(
        databaseFile: File
    ): List<SmsRecoveryResult> {

        val results =
            mutableListOf<SmsRecoveryResult>()

        if (!databaseFile.exists()) {
            return results
        }

        val database = try {
            SQLiteDatabase.openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
        } catch (exception: Exception) {
            return results
        }

        database.use { db ->

            val tableNames =
                getTableNames(db)

            val smsTable =
                findSmsTable(tableNames)
                    ?: return@use

            val columns =
                getColumns(
                    db,
                    smsTable
                )

            val addressColumn =
                findColumn(
                    columns,
                    listOf(
                        "address",
                        "sender",
                        "recipient"
                    )
                )

            val bodyColumn =
                findColumn(
                    columns,
                    listOf(
                        "body",
                        "message"
                    )
                )

            val dateColumn =
                findColumn(
                    columns,
                    listOf(
                        "date",
                        "timestamp",
                        "date_sent"
                    )
                )

            val typeColumn =
                findColumn(
                    columns,
                    listOf("type")
                )

            if (bodyColumn == null) {
                return@use
            }

            val projection =
                buildList {

                    addressColumn?.let {
                        add(it)
                    }

                    add(bodyColumn)

                    dateColumn?.let {
                        add(it)
                    }

                    typeColumn?.let {
                        add(it)
                    }
                }.toTypedArray()

            val cursor =
                db.query(
                    smsTable,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
                )

            cursor.use {

                while (it.moveToNext()) {

                    val address =
                        readString(
                            it,
                            addressColumn
                        )

                    val body =
                        readString(
                            it,
                            bodyColumn
                        )

                    if (body.isNullOrBlank()) {
                        continue
                    }

                    val timestamp =
                        readLong(
                            it,
                            dateColumn
                        )

                    val rawType =
                        readInt(
                            it,
                            typeColumn
                        )

                    val type =
                        mapSmsType(rawType)

                    val dateTime =
                        formatDate(timestamp)

                    results.add(
                        SmsRecoveryResult(
                            sender =
                                address ?: "Unknown",

                            message =
                                body,

                            dateTime =
                                dateTime,

                            type =
                                type,

                            recoveryStatus =
                                "Forensic Database Record",

                            confidence =
                                "High",

                            source =
                                SmsRecoverySource.FORENSIC_DATABASE
                        )
                    )
                }
            }
        }

        return results.distinctBy {
            it.sender +
                    "|" +
                    it.message +
                    "|" +
                    it.dateTime +
                    "|" +
                    it.type
        }
    }

    private fun getTableNames(
        db: SQLiteDatabase
    ): List<String> {

        val tables =
            mutableListOf<String>()

        val cursor =
            db.rawQuery(
                """
                SELECT name
                FROM sqlite_master
                WHERE type = 'table'
                """.trimIndent(),
                null
            )

        cursor.use {

            while (it.moveToNext()) {

                tables.add(
                    it.getString(0)
                )
            }
        }

        return tables
    }

    private fun findSmsTable(
        tables: List<String>
    ): String? {

        val preferred =
            listOf(
                "sms",
                "messages",
                "mms"
            )

        return preferred.firstOrNull { preferredName ->

            tables.any { actualName ->

                actualName.equals(
                    preferredName,
                    ignoreCase = true
                )
            }
        }
    }

    private fun getColumns(
        db: SQLiteDatabase,
        tableName: String
    ): List<String> {

        val columns =
            mutableListOf<String>()

        val cursor =
            db.rawQuery(
                "PRAGMA table_info(`$tableName`)",
                null
            )

        cursor.use {

            while (it.moveToNext()) {

                columns.add(
                    it.getString(1)
                )
            }
        }

        return columns
    }

    private fun findColumn(
        columns: List<String>,
        possibleNames: List<String>
    ): String? {

        for (possibleName in possibleNames) {

            val actualColumn =
                columns.firstOrNull { column ->

                    column.equals(
                        possibleName,
                        ignoreCase = true
                    )
                }

            if (actualColumn != null) {
                return actualColumn
            }
        }

        return null
    }

    private fun readString(
        cursor: Cursor,
        column: String?
    ): String? {

        if (column == null) {
            return null
        }

        val index =
            cursor.getColumnIndex(column)

        if (index < 0) {
            return null
        }

        return try {
            cursor.getString(index)
        } catch (exception: Exception) {
            null
        }
    }

    private fun readLong(
        cursor: Cursor,
        column: String?
    ): Long {

        if (column == null) {
            return 0L
        }

        val index =
            cursor.getColumnIndex(column)

        if (index < 0) {
            return 0L
        }

        return try {
            cursor.getLong(index)
        } catch (exception: Exception) {
            0L
        }
    }

    private fun readInt(
        cursor: Cursor,
        column: String?
    ): Int {

        if (column == null) {
            return 0
        }

        val index =
            cursor.getColumnIndex(column)

        if (index < 0) {
            return 0
        }

        return try {
            cursor.getInt(index)
        } catch (exception: Exception) {
            0
        }
    }

    private fun mapSmsType(
        type: Int
    ): String {

        return when (type) {

            1 -> "Inbox"
            2 -> "Sent"
            3 -> "Draft"
            4 -> "Outbox"
            5 -> "Failed"
            6 -> "Queued"

            else -> "Unknown"
        }
    }

    private fun formatDate(
        timestamp: Long
    ): String {

        if (timestamp <= 0L) {
            return "Unknown date"
        }

        return try {

            SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            ).format(
                Date(timestamp)
            )

        } catch (exception: Exception) {

            "Unknown date"
        }
    }
}