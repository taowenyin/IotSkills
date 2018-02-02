package cn.edu.siso.iotskills;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private TextView socketResultText = null;
    private EditText socketSendText = null;
    private Button socketSendBtn = null;
    private Button socketConnectBtn = null;
    private EditText serverIpText = null;
    private EditText serverPortText = null;

    private Socket client = null;
    private Handler handler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        socketResultText = findViewById(R.id.socket_result_text);
        socketSendText = findViewById(R.id.socket_send_text);
        socketSendBtn = findViewById(R.id.socket_send_btn);
        socketConnectBtn = findViewById(R.id.socket_connect_btn);
        serverIpText = findViewById(R.id.server_ip_text);
        serverPortText = findViewById(R.id.server_port_text);

        socketSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SocketStringTask task = new SocketStringTask(client);
                task.execute(socketSendText.getText().toString()); // 执行一个发送字符串的异步任务
            }
        });

        socketConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverIp = serverIpText.getText().toString();
                int serverPort = Integer.valueOf(serverPortText.getText().toString());
                // 启动Socket连接和接收线程，传入服务器IP、端口和数据接收的Handler
                new Thread(new SocketRunnable(serverIp, serverPort, handler)).start();
            }
        });

        client = new Socket();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x01) { // 接收数据的消息
                    Bundle bundle = msg.getData();
                    byte[] data = bundle.getByteArray("DATA");
                    socketResultText.setText(new String(data, 0, data.length));
                }

                if (msg.what == 0x02) { // 服务器连接正常的消息
                    Toast.makeText(getApplicationContext(), "服务器连接成功", Toast.LENGTH_LONG).show();
                }

                if (msg.what == 0x03) { // 服务器连接断开的消息
                    Toast.makeText(getApplicationContext(), "服务器连接断开", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 退出应用时关闭所有的通道
        if (client.isConnected()) {
            try {
                client.getOutputStream().close();
                client.getInputStream().close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SocketRunnable implements Runnable {

        private String serverIp = "";
        private int serverPort = 0;
        private Handler handler = null;

        public SocketRunnable(String serverIp, int serverPort, Handler handler) {
            this.serverIp = serverIp;
            this.serverPort = serverPort;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                // 连接服务器
                client.connect(new InetSocketAddress(serverIp, serverPort));

                // 连接成功发送消息
                Message msg = new Message();
                msg.what = 0x02; // 表示连接成功
                handler.sendMessage(msg);
                msg = null; // 清空消息，不然下面使用会出错

                byte[] buffer = new byte[1024]; // 接收数据的缓冲
                int dataLength = 0; // 有效数据长度
                InputStream iStream = client.getInputStream(); // 接收数据的流
                while (client.isConnected() && (dataLength = iStream.read(buffer)) != -1) {
                    // 只要有数据就发送到主线程进行处理
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("DATA", Arrays.copyOfRange(buffer, 0, dataLength));
                    msg = new Message();
                    msg.what = 0x01;
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }

                // Socket断开后就关闭Socket
                iStream.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();

                // 出现异常就表示连接断开，发送消息
                Message msg = new Message();
                msg.what = 0x03;
                handler.sendMessage(msg);
            }
        }
    }

    // 发送字符串信息的异步任务
    private class SocketStringTask extends AsyncTask<String, Integer, Void> {

        private Socket client = null;

        public SocketStringTask(Socket client) {
            this.client = client;
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                // 发送消息
                OutputStream oStream = client.getOutputStream();
                oStream.write(strings[0].getBytes("UTF-8"));
                oStream.flush(); // 清空发送缓冲
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
