package com.mynfcapp.nfc

import com.facebook.react.TurboReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider

class NfcPackage : TurboReactPackage() {

    override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
        return if (name == NfcModule.NAME) {
            NfcModule(reactContext)
        } else {
            null
        }
    }

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
        return ReactModuleInfoProvider {
            mapOf(
                NfcModule.NAME to ReactModuleInfo(
                    NfcModule.NAME,
                    NfcModule.NAME,
                    false, // isCxxModule
                    false, // canOverrideExistingModule
                    true,  // needsEagerInit
                    false, // hasConstants
                    true   // isTurboModule
                )
            )
        }
    }
}