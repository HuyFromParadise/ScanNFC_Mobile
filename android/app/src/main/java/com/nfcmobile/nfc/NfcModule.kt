package com.nfcmobile.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import android.nfc.Tag
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.nfcmobile.specs.NativeNfcModuleSpec

class NfcModule(reactContext: ReactApplicationContext) : NativeNfcModuleSpec(reactContext) {

    private var nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(reactContext)

    companion object {
        const val NAME = "NativeNfcModule"
        var instance: NfcModule? = null
            private set
    }

    init {
        instance = this
    }

    @ReactMethod
    override fun startListening() {
        val activity: Activity = reactApplicationContext.currentActivity ?: return
        val adapter = nfcAdapter ?: return

        val intent = Intent(activity, activity.javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            activity, 
            0, 
            intent, 
            PendingIntent.FLAG_MUTABLE
        )

        activity.runOnUiThread {
            adapter.enableForegroundDispatch(activity, pendingIntent, null, null)
        }
    }

    @ReactMethod
    override fun stopListening() {
        val activity: Activity = reactApplicationContext.currentActivity ?: return
        val adapter = nfcAdapter ?: return

        activity.runOnUiThread {
            adapter.disableForegroundDispatch(activity)
        }
    }

    fun handleIntent(intent: Intent) {
        val action = intent.action
        
        // 1. Kiem tra va xu ly khi truyen du lieu gia lap tu ADB Command
        val adbMockText = intent.getStringExtra("android.nfc.extra.TEXT")
        if (!adbMockText.isNullOrEmpty()) {
            val payload: WritableMap = Arguments.createMap().apply {
                putString("id", "MOCK-ADB-TAG-1234")
                putString("techList", "[android.nfc.tech.Ndef (Simulated)]")
                putString("payload", adbMockText)
            }
            sendEvent("onNfcRead", payload)
            return
        }

        // 2. Xu ly khi quet the NFC that (hoac Intent NDEF chuan)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == action
        ) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            var payloadContent = ""

            // Doc NDEF Message tu Intent neu co
            val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMsgs != null) {
                for (msg in rawMsgs) {
                    val ndefMessage = msg as NdefMessage
                    for (record in ndefMessage.records) {
                        payloadContent += String(record.payload, Charsets.UTF_8) + "\n"
                    }
                }
            }

            tag?.let {
                val tagId = bytesToHex(it.id)
                val techList = it.techList.joinToString(prefix = "[", postfix = "]")

                val payload: WritableMap = Arguments.createMap().apply {
                    putString("id", tagId)
                    putString("techList", techList)
                    putString("payload", payloadContent.trim())
                }

                sendEvent("onNfcRead", payload)
            }
        }
    }

    private fun sendEvent(eventName: String, params: WritableMap) {
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02X".format(it) }
    }

    @ReactMethod
    override fun addListener(eventName: String) {}

    @ReactMethod
    override fun removeListeners(count: Double) {}
}