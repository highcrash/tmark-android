package com.tmark.client

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * BroadcastReceiver for SMS Retriever API.
 * Registered dynamically in OtpStep composable. When an OTP SMS matching this app's
 * hash is received, the message is forwarded to [onOtpReceived] for auto-fill.
 *
 * NOTE: The SMS sent by the server must end with the app's hash code (11 characters).
 * Run `./gradlew signingReport` and use the app hash from the SHA-256 certificate.
 */
class SmsRetrieverReceiver(
    private val onOtpReceived: (String) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION != intent.action) return

        val extras = intent.extras ?: return
        val status = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelable(SmsRetriever.EXTRA_STATUS, Status::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(SmsRetriever.EXTRA_STATUS)
        } ?: return

        when (status.statusCode) {
            CommonStatusCodes.SUCCESS -> {
                val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE) ?: return
                // Extract 6-digit OTP from SMS message
                val otp = OTP_REGEX.find(message)?.value ?: return
                onOtpReceived(otp)
            }
            else -> { /* Timed out or failed — user enters manually */ }
        }
    }

    companion object {
        private val OTP_REGEX = Regex("\\b\\d{6}\\b")
    }
}
