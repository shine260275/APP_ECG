package com.ecgmonitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class BleManager {

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt ecgGatt, accGatt;
    private DataListener dataListener;
    private boolean ecgConnected = false, accConnected = false;

    public interface DataListener {
        void onEcgData(int adcValue, long timestamp);
        void onAccData(float x, float y, float z, long timestamp);
        void onConnectionStatusChanged(boolean connected);
    }

    public BleManager(Context context) {
        this.context = context;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        }
    }

    public void setDataListener(DataListener listener) {
        this.dataListener = listener;
    }

    public void startScan() {
        if (bluetoothLeScanner == null) return;

        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        bluetoothLeScanner.startScan(filters, settings, scanCallback);
    }

    public void stopScan() {
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    public void connectEcgDevice(BluetoothDevice device) {
        if (ecgGatt != null) {
            ecgGatt.close();
        }
        ecgGatt = device.connectGatt(context, false, ecgGattCallback);
    }

    public void connectAccDevice(BluetoothDevice device) {
        if (accGatt != null) {
            accGatt.close();
        }
        accGatt = device.connectGatt(context, false, accGattCallback);
    }

    public void disconnect() {
        if (ecgGatt != null) {
            ecgGatt.close();
            ecgGatt = null;
        }
        if (accGatt != null) {
            accGatt.close();
            accGatt = null;
        }
        ecgConnected = false;
        accConnected = false;
        if (dataListener != null) {
            dataListener.onConnectionStatusChanged(false);
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            // 这里可以处理扫描结果，例如显示设备列表
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    private BluetoothGattCallback ecgGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                ecgConnected = true;
                gatt.discoverServices();
                updateConnectionStatus();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                ecgConnected = false;
                updateConnectionStatus();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(java.util.UUID.fromString(Config.ECG_SERVICE_UUID));
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(java.util.UUID.fromString(Config.ECG_CHARACTERISTIC_UUID));
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (dataListener != null) {
                byte[] data = characteristic.getValue();
                // 解析心电数据
                int adcValue = ((data[1] & 0xFF) << 8) | (data[0] & 0xFF);
                long timestamp = System.currentTimeMillis();
                dataListener.onEcgData(adcValue, timestamp);
            }
        }
    };

    private BluetoothGattCallback accGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                accConnected = true;
                gatt.discoverServices();
                updateConnectionStatus();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                accConnected = false;
                updateConnectionStatus();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(java.util.UUID.fromString(Config.ACC_SERVICE_UUID));
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(java.util.UUID.fromString(Config.ACC_CHARACTERISTIC_UUID));
                    if (characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, true);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (dataListener != null) {
                byte[] data = characteristic.getValue();
                // 解析加速度数据
                float x = ((short) ((data[1] & 0xFF) << 8) | (data[0] & 0xFF)) / 16384.0f;
                float y = ((short) ((data[3] & 0xFF) << 8) | (data[2] & 0xFF)) / 16384.0f;
                float z = ((short) ((data[5] & 0xFF) << 8) | (data[4] & 0xFF)) / 16384.0f;
                long timestamp = System.currentTimeMillis();
                dataListener.onAccData(x, y, z, timestamp);
            }
        }
    };

    private void updateConnectionStatus() {
        if (dataListener != null) {
            dataListener.onConnectionStatusChanged(ecgConnected && accConnected);
        }
    }
}