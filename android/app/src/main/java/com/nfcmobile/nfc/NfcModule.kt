package com.mynfcapp.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

class NfcModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private var nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(reactContext)

    companion object {
        const val NAME = "NativeNfcModule"
        var instance: NfcModule? = null
            private set
    }

    init {
        instance = this
    }

    override fun getName(): String = NAME

    @ReactMethod
    fun startListening() {
        // Sử dụng reactContext.currentActivity thay vì currentActivity trực tiếp
        val activity: Activity = reactContext.currentActivity ?: return
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
    fun stopListening() {
        // Sử dụng reactContext.currentActivity
        val activity: Activity = reactContext.currentActivity ?: return
        val adapter = nfcAdapter ?: return

        activity.runOnUiThread {
            adapter.disableForegroundDispatch(activity)
        }
    }

    fun handleIntent(intent: Intent) {
        val action = intent.action
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == action
        ) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let {
                val tagId = bytesToHex(it.id)
                val techList = it.techList.joinToString(prefix = "[", postfix = "]")

                val payload: WritableMap = Arguments.createMap().apply {
                    putString("id", tagId)
                    putString("techList", techList)
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
    fun addListener(eventName: String) {}

    @ReactMethod
    fun removeListeners(count: Double) {}
}