package yomii.bluetoothclassic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yomii on 2017/7/27.
 * <p>
 * 扫描界面
 */

public class ScanActivity extends AppCompatActivity {

    private List<BluetoothDevice> devices = new ArrayList<>();

    private ArrayAdapter<String> deviceAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_activity);
        TextView emptyTv = (TextView) findViewById(R.id.scan_list_empty_tv);
        ListView scanListView = (ListView) findViewById(R.id.scan_list);
        scanListView.setEmptyView(emptyTv);

        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        scanListView.setAdapter(deviceAdapter);

        scanListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                //确认传递设备信息返回连接界面并关闭扫描Activity
                final String device = deviceAdapter.getItem(position);
                if (device != null) {
                    final String[] split = device.split("\n");
                    AlertDialog.Builder builder = new AlertDialog.Builder(ScanActivity.this);
                    builder.setTitle("发起连接")
                            .setMessage(String.format("是否对%s发起连接", split[0]))
                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = getIntent();
                                    intent.putExtra("deviceName", split[0]);
                                    intent.putExtra("deviceAddress", split[1]);
                                    intent.putExtra("device", devices.get(position));
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .create().show();
                }
            }
        });

        registerScanReceiver();
        BluetoothAdapter.getDefaultAdapter().startDiscovery();

    }

    private void registerScanReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        devices.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        unregisterReceiver(mReceiver);
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BluetoothClass deviceClass = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
                // Add the name and address to an array adapter to show in a ListView
                deviceAdapter.add(device.getName() + "\n" + device.getAddress() + "\n" + "type: "
                        + deviceClass.getDeviceClass());
                devices.add(device);
            }
        }
    };
}
