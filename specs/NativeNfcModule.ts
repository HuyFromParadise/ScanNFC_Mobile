import { TurboModule, TurboModuleRegistry } from "react-native";

export interface Spec extends TurboModule{
    startListening(): void
    stopListening(): void
    addListener(eventName: string): void
    removeListeners(count: number): void
}

export default TurboModuleRegistry.getEnforcing<Spec>('NativeNfcModule')