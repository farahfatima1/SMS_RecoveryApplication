package com.example.data_recoveryapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data_recoveryapplication.ui.theme.Data_RecoveryApplicationTheme

class DataRecoveryMainActivity : ComponentActivity() {

    /*
     * =========================================================
     * RECOVERED SMS RESULTS
     * =========================================================
     */
    private var recoveredCandidates by mutableStateOf<List<SmsRecoveryResult>>(
        emptyList()
    )

    /*
     * =========================================================
     * SCREEN NAVIGATION
     * =========================================================
     */
    private var showRecoveredScreen by mutableStateOf(false)

    private var showExportScreen by mutableStateOf(false)

    /*
     * =========================================================
     * SMS PERMISSION
     * =========================================================
     */
    private var permissionGranted by mutableStateOf(false)

    /*
     * =========================================================
     * SCAN STATE
     * =========================================================
     */
    private var isScanning by mutableStateOf(false)

    /*
     * =========================================================
     * STORAGE
     * =========================================================
     */
    private lateinit var recoveredSmsStore: RecoveredSmsStore

    private lateinit var archiveDatabase: SmsArchiveDatabase

    private lateinit var smsAutoArchiveObserver: SmsAutoArchiveObserver


    /*
     * =========================================================
     * SMS PERMISSION LAUNCHER
     * =========================================================
     */
    private val smsPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            permissionGranted = isGranted

            if (isGranted) {

                startAutomaticSmsArchive()

            } else {

                Toast.makeText(
                    this,
                    "SMS permission is required for recovery",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    /*
     * =========================================================
     * ON CREATE
     * =========================================================
     */
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)


        /*
         * Initialize recovered SMS storage.
         */
        recoveredSmsStore =
            RecoveredSmsStore(
                applicationContext
            )


        /*
         * Initialize archive database.
         */
        archiveDatabase =
            SmsArchiveDatabase(
                applicationContext
            )


        /*
         * Initialize automatic archive observer.
         */
        smsAutoArchiveObserver =
            SmsAutoArchiveObserver(
                applicationContext
            )


        /*
         * Check SMS permission.
         */
        permissionGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED


        /*
         * Start automatic archive if permission
         * is already available.
         */
        if (permissionGranted) {

            startAutomaticSmsArchive()

        } else {

            smsPermissionLauncher.launch(
                Manifest.permission.READ_SMS
            )
        }


        enableEdgeToEdge()


        /*
         * =========================================================
         * COMPOSE UI
         * =========================================================
         */
        setContent {

            Data_RecoveryApplicationTheme {


                /*
                 * =================================================
                 * SCREEN 3
                 * EXPORT
                 * =================================================
                 */
                if (showExportScreen) {

                    SmsExportScreen(

                        onExportCsv = {

                            val success =
                                SmsExportManager.exportCsv(
                                    this@DataRecoveryMainActivity,
                                    recoveredCandidates
                                )

                            Toast.makeText(
                                this@DataRecoveryMainActivity,

                                if (success) {
                                    "Download complete: recovered_sms.csv"
                                } else {
                                    "CSV export failed"
                                },

                                Toast.LENGTH_LONG
                            ).show()
                        },


                        onExportTxt = {

                            val success =
                                SmsExportManager.exportTxt(
                                    this@DataRecoveryMainActivity,
                                    recoveredCandidates
                                )

                            Toast.makeText(
                                this@DataRecoveryMainActivity,

                                if (success) {
                                    "Download complete: recovered_sms.txt"
                                } else {
                                    "TXT export failed"
                                },

                                Toast.LENGTH_LONG
                            ).show()
                        },


                        onExportPdf = {

                            val success =
                                SmsExportManager.exportPdf(
                                    this@DataRecoveryMainActivity,
                                    recoveredCandidates
                                )

                            Toast.makeText(
                                this@DataRecoveryMainActivity,

                                if (success) {
                                    "Download complete: recovered_sms.pdf"
                                } else {
                                    "PDF export failed"
                                },

                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }


                /*
                 * =================================================
                 * SCREEN 2
                 * RECOVERED SMS
                 * =================================================
                 */
                else if (showRecoveredScreen) {

                    RecoveredSmsScreen(

                        recoveredCandidates =
                            recoveredCandidates,

                        onOpenExport = {

                            showExportScreen = true
                        },

                        onBackToHome = {

                            showRecoveredScreen = false

                            showExportScreen = false

                            /*
                             * Reset displayed results.
                             *
                             * The database is NOT deleted.
                             */
                            recoveredCandidates =
                                emptyList()
                        }
                    )
                }


                /*
                 * =================================================
                 * SCREEN 1
                 * SMS RECOVERY HOME
                 * =================================================
                 */
                else {

                    Scaffold(

                        modifier =
                            Modifier.fillMaxSize()

                    ) { innerPadding ->

                        SmsRecoveryHomeScreen(

                            permissionGranted =
                                permissionGranted,

                            recoveredCount =
                                recoveredCandidates.size,

                            isScanning =
                                isScanning,

                            modifier =
                                Modifier.padding(
                                    innerPadding
                                ),


                            /*
                             * Scan button.
                             */
                            onScanSms = {

                                recoverDeletedSms()
                            },


                            /*
                             * View recovered SMS.
                             */
                            onOpenRecoveredSms = {

                                if (
                                    recoveredCandidates.isNotEmpty()
                                ) {

                                    showRecoveredScreen =
                                        true

                                } else {

                                    Toast.makeText(
                                        this@DataRecoveryMainActivity,
                                        "No recovered SMS available",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }


    /*
     * =============================================================
     * AUTOMATIC SMS ARCHIVING
     * =============================================================
     *
     * This keeps Inbox + Sent SMS archived while they
     * are still available.
     */
    private fun startAutomaticSmsArchive() {

        if (!permissionGranted) {
            return
        }

        if (
            ::smsAutoArchiveObserver.isInitialized
        ) {

            smsAutoArchiveObserver.start()
        }
    }


    /*
     * =============================================================
     * SCAN AND RECOVER DELETED SMS
     * =============================================================
     */
    private fun recoverDeletedSms() {

        if (!permissionGranted) {

            Toast.makeText(
                this,
                "SMS permission is required",
                Toast.LENGTH_LONG
            ).show()

            return
        }


        if (isScanning) {
            return
        }


        isScanning = true


        try {

            /*
             * Read SMS currently available
             * from Android SMS provider.
             */
            val recoveryEngine =
                SmsRecoveryEngine(
                    contentResolver
                )


            val currentMessages =
                recoveryEngine
                    .scanAvailableSms()
                    .filter { sms ->

                        sms.type == "Inbox" ||
                                sms.type == "Sent"
                    }


            /*
             * Compare current SMS with
             * archived SMS.
             */
            val deletedMessages =
                archiveDatabase.findDeletedSms(
                    currentMessages
                )


            /*
             * Save newly detected deleted SMS.
             */
            if (
                deletedMessages.isNotEmpty()
            ) {

                recoveredSmsStore.addRecoveredMessages(
                    deletedMessages
                )

                Toast.makeText(
                    this,
                    "${deletedMessages.size} deleted SMS detected",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Scan complete. No deleted SMS found.",
                    Toast.LENGTH_SHORT
                ).show()
            }


            /*
             * Load all saved recovery results.
             */
            loadRecoveredMessages()

        } catch (e: Exception) {

            e.printStackTrace()

            Toast.makeText(
                this,
                "SMS recovery scan failed",
                Toast.LENGTH_LONG
            ).show()

        } finally {

            isScanning = false
        }
    }


    /*
     * =============================================================
     * LOAD RECOVERED SMS
     * =============================================================
     */
    private fun loadRecoveredMessages() {

        try {

            recoveredCandidates =
                recoveredSmsStore
                    .loadRecoveredMessages()

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }


    /*
     * =============================================================
     * ACTIVITY DESTROY
     * =============================================================
     */
    override fun onDestroy() {

        if (
            ::smsAutoArchiveObserver.isInitialized
        ) {

            smsAutoArchiveObserver.stop()
        }


        if (
            ::archiveDatabase.isInitialized
        ) {

            archiveDatabase.close()
        }


        super.onDestroy()
    }
}


/*
 * =============================================================
 * SCREEN 1
 * SMS RECOVERY HOME
 * =============================================================
 */
@Composable
fun SmsRecoveryHomeScreen(

    permissionGranted: Boolean,

    recoveredCount: Int,

    isScanning: Boolean,

    modifier: Modifier = Modifier,

    onScanSms: () -> Unit,

    onOpenRecoveredSms: () -> Unit

) {

    Column(

        modifier =
            modifier
                .fillMaxSize()
                .padding(20.dp),

        verticalArrangement =
            Arrangement.spacedBy(20.dp)
    ) {


        Text(

            text =
                "SMS Recovery",

            style =
                MaterialTheme
                    .typography
                    .headlineMedium
        )


        Text(

            text =
                if (permissionGranted) {

                    "SMS protection active"

                } else {

                    "SMS permission required"
                },

            style =
                MaterialTheme
                    .typography
                    .bodyLarge
        )


        /*
         * =================================================
         * SCAN BUTTON
         * =================================================
         */
        Button(

            onClick =
                onScanSms,

            enabled =
                permissionGranted &&
                        !isScanning,

            modifier =
                Modifier.fillMaxWidth()

        ) {

            Text(

                text =
                    if (isScanning) {

                        "Scanning..."

                    } else {

                        "Scan SMS"
                    }
            )
        }


        /*
         * =================================================
         * RECOVERED COUNT
         * =================================================
         */
        Text(

            text =
                "Recovered SMS: $recoveredCount",

            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )


        /*
         * =================================================
         * VIEW RECOVERED SMS BUTTON
         * =================================================
         */
        Button(

            onClick =
                onOpenRecoveredSms,

            enabled =
                recoveredCount > 0,

            modifier =
                Modifier.fillMaxWidth()

        ) {

            Text(

                text =
                    "View Recovered SMS ($recoveredCount)"
            )
        }
    }
}


/*
 * =============================================================
 * SCREEN 2
 * RECOVERED SMS
 * =============================================================
 */
@Composable
fun RecoveredSmsScreen(

    recoveredCandidates:
    List<SmsRecoveryResult>,

    onOpenExport: () -> Unit,

    onBackToHome: () -> Unit

) {

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),

        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {


        /*
         * =================================================
         * BACK ARROW
         * =================================================
         */
        Text(

            text =
                "←",

            style =
                MaterialTheme
                    .typography
                    .headlineMedium,

            modifier =
                Modifier.clickable {

                    onBackToHome()
                }
        )


        Text(

            text =
                "Recovered Deleted SMS",

            style =
                MaterialTheme
                    .typography
                    .headlineMedium
        )


        /*
         * =================================================
         * SMS LIST
         * =================================================
         */
        if (
            recoveredCandidates.isNotEmpty()
        ) {

            LazyColumn(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),

                contentPadding =
                    PaddingValues(
                        bottom = 16.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                items(

                    items =
                        recoveredCandidates,

                    key = { sms ->

                        "${sms.sender}_" +
                                "${sms.dateTime}_" +
                                "${sms.message}_" +
                                "${sms.type}"
                    }

                ) { sms ->


                    Card(

                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Column(

                            modifier =
                                Modifier.padding(
                                    16.dp
                                ),

                            verticalArrangement =
                                Arrangement.spacedBy(
                                    6.dp
                                )
                        ) {


                            Text(

                                text =
                                    "From/To: ${sms.sender}",

                                style =
                                    MaterialTheme
                                        .typography
                                        .titleMedium
                            )


                            Text(

                                text =
                                    "Message: ${sms.message}",

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium
                            )


                            Text(

                                text =
                                    "Date & Time: ${sms.dateTime}",

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )


                            Text(

                                text =
                                    "Type: ${sms.type}",

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )


                            Text(

                                text =
                                    "Status: ${sms.recoveryStatus}",

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )


                            Text(

                                text =
                                    "Confidence: ${sms.confidence}",

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )


                            Text(

                                text =
                                    "Source: ${sms.source}",

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall
                            )
                        }
                    }
                }
            }


            /*
             * =================================================
             * EXPORT BUTTON
             * =================================================
             */
            Button(

                onClick =
                    onOpenExport,

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text(
                    text =
                        "Export Results"
                )
            }

        } else {

            Text(

                text =
                    "No deleted SMS recovered yet.",

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )
        }
    }
}