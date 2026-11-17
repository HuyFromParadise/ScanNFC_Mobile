import React, { useEffect, useState } from 'react';
import {
  StyleSheet,
  Text,
  View,
  NativeEventEmitter,
  Platform,
} from 'react-native';
import NativeNfcModule from './specs/NativeNfcModule';
import { SafeAreaView } from 'react-native-safe-area-context';

interface NfcData {
  id: string;
  techList: string;
  payload?: string; 
}

export default function App() {
  const [nfcData, setNfcData] = useState<NfcData | null>(null);

  useEffect(() => {
    if (Platform.OS === 'android') {
      NativeNfcModule.startListening();

      const eventEmitter = new NativeEventEmitter(NativeNfcModule as any);
      const subscription = eventEmitter.addListener('onNfcRead', (data: any) => {
        setNfcData(data);
      });

      return () => {
        NativeNfcModule.stopListening();
        subscription.remove();
      };
    }
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.card}>
        <Text style={styles.title}>NFC Tag Reader</Text>
        
        {nfcData ? (
          <View style={styles.dataContainer}>
            <Text style={styles.label}>Tag ID (HEX):</Text>
            <Text style={styles.value}>{nfcData.id}</Text>

            <Text style={styles.label}>Supported Technologies:</Text>
            <Text style={styles.value}>{nfcData.techList}</Text>

            {/* Bổ sung phần hiển thị Payload NDEF */}
            <Text style={styles.label}>NDEF Payload Content:</Text>
            <Text style={[styles.value, styles.payloadValue]}>
              {nfcData.payload || 'No Payload Content'}
            </Text>
          </View>
        ) : (
          <Text style={styles.hint}>
            Please place an NFC tag near the back of your phone...
          </Text>
        )}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
    justifyContent: 'center',
    alignItems: 'center',
  },
  card: {
    width: '85%',
    padding: 24,
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    elevation: 4,
  },
  title: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 16,
    textAlign: 'center',
    color: '#333',
  },
  hint: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
    fontStyle: 'italic',
  },
  dataContainer: {
    marginTop: 8,
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#444',
    marginTop: 12,
  },
  value: {
    fontSize: 14,
    color: '#007AFF',
    marginTop: 4,
    fontFamily: Platform.OS === 'android' ? 'monospace' : 'Courier',
  },
  payloadValue: {
    color: '#2E7D32', // Màu xanh lá nổi bật cho nội dung Payload
    fontWeight: '500',
  },
});