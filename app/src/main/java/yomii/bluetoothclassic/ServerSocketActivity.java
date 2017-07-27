package yomii.bluetoothclassic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Yomii on 2017/7/27.
 * <p>
 * 被连接界面
 */

public class ServerSocketActivity extends AppCompatActivity {

    /**
     * 请求打开设备可见性
     */
    private static final int REQUEST_VISIBLE = 135;
    /**
     * 蓝牙流数据处理
     */
    private static final int MESSAGE_READ = 1;

    private ArrayAdapter<String> connectAdapter;
    private TextView emptyTv;
    private CountDownTimer countDownTimer;
    private AcceptThread acceptThread;
    private ConnectedThread connectedThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_socket_activity);
        emptyTv = (TextView) findViewById(R.id.connect_list_empty_tv);
        ListView connectListView = (ListView) findViewById(R.id.connect_list);
        connectListView.setEmptyView(emptyTv);

        connectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        connectListView.setAdapter(connectAdapter);

        tryOpenVisible();
    }

    private void tryOpenVisible() {
        //尝试打开蓝牙设备可见性
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 150);//可见性持续150秒
        startActivityForResult(discoverableIntent, REQUEST_VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VISIBLE) {
            int countDownTime = 150;
            if (resultCode == RESULT_CANCELED)
                ToastUtils.imitShowToast("设备可见性未开启, 仍可通过已配对设备连接");
            else
                countDownTime = resultCode;

            initCountDown(countDownTime);
            initServerSocket();
        }
    }

    private void initCountDown(int countDownTime) {
        countDownTimer = new CountDownTimer(countDownTime * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                emptyTv.setText(getString(R.string.waiting_connect, millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                finish();
            }
        };
        countDownTimer.start();
    }

    private void initServerSocket() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();//确保连接前关闭扫描
        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE_READ){
                byte[] bytes = (byte[]) msg.obj;
                connectAdapter.add(new String(bytes,0,msg.arg1));
                connectedThread.write("收到".getBytes());
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null)
            countDownTimer.cancel();
        if (acceptThread != null)
            acceptThread.cancel();
        if (connectedThread != null)
            connectedThread.cancel();

    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        private boolean accepting = true;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord
                        ("server", UUID.nameUUIDFromBytes("test".getBytes()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            if (mmServerSocket == null)
                return;

            while (accepting) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    countDownTimer.cancel();
                    cancel();
                }
            }
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        void cancel() {
            accepting = false;
            try {
                if (mmServerSocket != null) {
                    mmServerSocket.close();
                }
            } catch (IOException ignored) {
            }
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


}
