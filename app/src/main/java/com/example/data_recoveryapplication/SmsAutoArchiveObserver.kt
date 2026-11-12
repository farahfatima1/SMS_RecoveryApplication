
package com.example.data_recoveryapplication

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Telephony

class SmsAutoArchiveObserver(
    private val context: Context
) {

    private val contentResolver: ContentResolver =
        context.applicationContext.contentResolver

    private val handler =
        Handler(Looper.getMainLooper())

    private var observer: ContentObserver? = null

    private var running = false

    /*
     * Prevent multiple delayed operations from running
     * at the same time.
     */
    private var processing = false


    fun start() {

        if (running) {
            return
        }

        running = true

        observer =
            object : ContentObserver(handler) {

                override fun onChange(
                    selfChange: Boolean
                ) {
                    super.onChange(selfChange)

                    scheduleProcessing()
                }
            }


        /*
         * Monitor SMS provider changes.
         */
        contentResolver.registerContentObserver(
            Telephony.Sms.CONTENT_URI,
            true,
            observer!!
        )


        /*
         * IMPORTANT:
         *
         * Archive all existing Inbox + Sent SMS
         * immediately when monitoring starts.
         */
        scheduleProcessing()
    }


    fun stop() {

        if (!running) {
            return
        }

        observer?.let {

            try {
                contentResolver.unregisterContentObserver(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        observer = null

        running = false

        handler.removeCallbacksAndMessages(null)
    }


    /*
     * Schedule processing after the SMS provider
     * has completed its database operation.
     */
    private fun scheduleProcessing() {

        handler.removeCallbacks(processRunnable)

        handler.postDelayed(
            processRunnable,
            1500L
        )
    }


    private val processRunnable =
        Runnable {

            if (!running) {
                return@Runnable
            }

            if (processing) {
                return@Runnable
            }

            processing = true

            try {

                processSmsChange()

            } catch (e: Exception) {

                e.printStackTrace()

            } finally {

                processing = false
            }
        }


    /*
     * Archive current Inbox + Sent SMS.
     *
     * Then compare the current provider against
     * the existing archive.
     */
    private fun processSmsChange() {

        try {

            /*
             * Read current SMS.
             */
            val recoveryEngine =
                SmsRecoveryEngine(
                    contentResolver
                )

            val currentMessages =
                recoveryEngine
                    .scanAvailableSms()
                    .filter { sms ->

                        /*
                         * Only Inbox and Sent are required
                         * by your project.
                         */
                        sms.type == "Inbox" ||
                                sms.type == "Sent"
                    }


            /*
             * Open the protected archive.
             */
            val archiveDatabase =
                SmsArchiveDatabase(
                    context.applicationContext
                )

            try {

                /*
                 * IMPORTANT ORDER:
                 *
                 * First compare the existing archive
                 * with the current provider.
                 *
                 * This allows a previously archived SMS
                 * that disappeared to become a deletion
                 * candidate.
                 */
                val deletedMessages =
                    archiveDatabase.findDeletedSms(
                        currentMessages
                    )


                /*
                 * Store deleted SMS.
                 */
                if (
                    deletedMessages.isNotEmpty()
                ) {

                    val recoveredStore =
                        RecoveredSmsStore(
                            context.applicationContext
                        )

                    recoveredStore.addRecoveredMessages(
                        deletedMessages
                    )
                }


                /*
                 * AFTER deletion comparison,
                 * archive the current SMS.
                 *
                 * This is critical.
                 */
                if (
                    currentMessages.isNotEmpty()
                ) {

                    archiveDatabase.archiveAll(
                        currentMessages
                    )
                }

            } finally {

                archiveDatabase.close()
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
}
