
package com.example.data_recoveryapplication

import java.security.MessageDigest

object SmsIntegrityHasher {

    /**
     * Creates a SHA-256 hash for one SMS.
     *
     * The same SMS data will produce the same hash.
     */
    fun sha256(
        sms: SmsRecoveryResult
    ): String {

        val evidenceText =
            buildString {

                append("sender=")
                append(sms.sender)
                append("|")

                append("message=")
                append(sms.message)
                append("|")

                append("dateTime=")
                append(sms.dateTime)
                append("|")

                append("type=")
                append(sms.type)
                append("|")

                append("recoveryStatus=")
                append(sms.recoveryStatus)
                append("|")

                append("confidence=")
                append(sms.confidence)
                append("|")

                append("source=")
                append(sms.source)
            }

        return sha256(evidenceText)
    }


    /**
     * Creates a SHA-256 hash from text.
     */
    fun sha256(
        text: String
    ): String {

        val digest =
            MessageDigest.getInstance(
                "SHA-256"
            )

        val hashBytes =
            digest.digest(
                text.toByteArray(
                    Charsets.UTF_8
                )
            )

        return hashBytes.joinToString("") { byte ->

            "%02x".format(
                byte.toInt() and 0xff
            )
        }
    }
}