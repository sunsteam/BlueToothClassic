package yomii.bluetoothclassic;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private int hasBlueTooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    public void openBlueTooth(View view) {
        if (hasBlueToothFunction())
            startActivity(new Intent(this,BlueToothDevicesActivity.class));
        else
            ToastUtils.imitShowToast(getString(R.string.bluetooth_not_support));
    }

    private boolean hasBlueToothFunction(){
        if (hasBlueTooth == 0){
            BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
            hasBlueTooth = defaultAdapter == null ? -1 : 1;
        }
        return hasBlueTooth > 0;
    }
}
