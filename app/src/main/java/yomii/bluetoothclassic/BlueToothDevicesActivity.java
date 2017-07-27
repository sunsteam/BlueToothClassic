package yomii.bluetoothclassic;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Set;

/**
 * Created by Yomii on 2017/7/27.
 * <p>
 * 设备列表
 */

public class BlueToothDevicesActivity extends AppCompatActivity {

    /**
     * 申请打开蓝牙
     */
    private static final int REQUEST_ENABLE_BT = 919;
    /**
     * 进入扫描设备界面
     */
    private static final int REQUEST_OPEN_SCAN = 818;

    private ArrayAdapter<String> deviceAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devices_activity);
        ListView deviceListView = (ListView) findViewById(R.id.device_list);
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(deviceAdapter);

        initFunction();
    }

    private void initFunction() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            ToastUtils.imitShowToast(getString(R.string.bluetooth_not_support));
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else
            doOnBlueToothOpen();
    }

    BlueToothConnectCallback connectCallback = new BlueToothConnectCallback() {


        @Override
        public void onStartOpen() {
            Log.i("blue", "startOpen");
            doOnBlueToothOpen();
        }

        @Override
        public void onOpenFailed() {
            ToastUtils.imitShowToast(R.string.bluetooth_not_support);
            finish();
        }
    };


    private void doOnBlueToothOpen() {
        //获取已知配对设备
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        if (bondedDevices.size() > 0)
            for (BluetoothDevice device : bondedDevices) {
                deviceAdapter.add(device.getName() + "\n" + device.getAddress());
            }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                //蓝牙打开成功
                if (connectCallback != null) {
                    connectCallback.onStartOpen();
                }
            } else {
                // 蓝牙打开失败
                if (connectCallback != null) {
                    connectCallback.onOpenFailed();
                }
            }
        }

        if (requestCode == REQUEST_OPEN_SCAN && resultCode == Activity.RESULT_OK) {
            String deviceName = data.getStringExtra("deviceName");
            String deviceAddress = data.getStringExtra("deviceAddress");
            BluetoothDevice device = data.getParcelableExtra("device");

            Log.i("returnScan", "设备名: " + deviceName + " 地址: " + deviceAddress);

            Intent intent = new Intent(this, ConnectActivity.class);
            intent.putExtra("device", device);
            startActivity(intent);
        }
    }

    public void toScanActivity(View view) {
        startActivityForResult(new Intent(this, ScanActivity.class), REQUEST_OPEN_SCAN);
    }

    public void toServerActivity(View view) {
        startActivity(new Intent(this,ServerSocketActivity.class));
    }
}
