package yomii.bluetoothclassic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Yomii on 2017/7/27.
 *
 * 连接界面
 */

public class ConnectActivity extends AppCompatActivity {

    private static final int MESSAGE_READ = 3;

    private ArrayAdapter<String> connectAdapter;
    private TextView editText;
    private tryConnectThread tryConnectThread;
    private ConnectedThread connectedThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_activity);
        editText = (EditText) findViewById(R.id.connect_ed);
        ListView connectListView = (ListView) findViewById(R.id.connect_list);

        connectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        connectListView.setAdapter(connectAdapter);

        initData();
    }

    private void initData() {
        BluetoothDevice device = getIntent().getParcelableExtra("device");
        tryConnectThread = new tryConnectThread(device);
        tryConnectThread.start();
    }

    public void sendMessage(View view) {
        String s = editText.getText().toString();
        connectAdapter.add(s);
        connectedThread.write(s.getBytes());
    }

    private void manageConnectedSocket(BluetoothSocket mmSocket) {
        connectedThread = new ConnectedThread(mmSocket);
        connectedThread.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE_READ){
                byte[] bytes = (byte[]) msg.obj;
                connectAdapter.add(new String(bytes,0,msg.arg1));
            }
        }
    };


    private class tryConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public tryConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(UUID.nameUUIDFromBytes("test".getBytes()));
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {}
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException ignored) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024 * 3];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            //关闭流触发异常Break
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tryConnectThread != null) {
            tryConnectThread.cancel();
        }
        if (connectedThread != null) {
            connectedThread.cancel();
        }
    }
}
