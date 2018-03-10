package cn.edu.siso.androidanalog;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.newland.zigbeeanaloglibrary.ZigBeeAnalogServiceAPI;
import com.newland.zigbeeanaloglibrary.ZigBeeService;
import com.newland.zigbeeanaloglibrary.response.OnHumResponse;
import com.newland.zigbeeanaloglibrary.response.OnLightResponse;
import com.newland.zigbeeanaloglibrary.response.OnTemperatureResponse;

public class MainActivity extends AppCompatActivity {

    private Spinner comList = null;
    private Spinner rateList = null;
    private Spinner modeList = null;
    private Button comOpen = null;

    private TextView temperatureText = null;
    private TextView humidityText = null;
    private TextView illuminanceText = null;

    private Handler handler = null;

    private int comHandle = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        comList = (Spinner) findViewById(R.id.com_no);
        rateList = (Spinner) findViewById(R.id.com_rate);
        modeList = (Spinner) findViewById(R.id.com_mode);
        comOpen = (Button) findViewById(R.id.open_com);

        temperatureText = (TextView) findViewById(R.id.temperature);
        humidityText = (TextView) findViewById(R.id.humidity);
        illuminanceText = (TextView) findViewById(R.id.illuminance);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Bundle bundle = msg.getData();
                switch (msg.what) {
                    case 0x01:
                        temperatureText.setText(bundle.getString("DATA"));
                        break;

                    case 0x02:
                        humidityText.setText(bundle.getString("DATA"));
                        break;

                    case 0x03:
                        illuminanceText.setText(bundle.getString("DATA"));
                        break;
                }
            }
        };

        comOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (comOpen.getText().toString().equals("打开串口")) {
                    comHandle = ZigBeeAnalogServiceAPI.openPort(comList.getSelectedItemPosition(),
                            modeList.getSelectedItemPosition(),
                            rateList.getSelectedItemPosition());

                    if (comHandle < 0) {
                        Toast.makeText(getApplicationContext(), "打开串口失败", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "打开串口成功", Toast.LENGTH_LONG).show();

                        ZigBeeService service = new ZigBeeService();
                        service.start();
                        comOpen.setText("关闭串口");

                        // 一定要设置温度TAG，不然没有温度
                        ZigBeeAnalogServiceAPI.getTemperature("温度", new OnTemperatureResponse() {
                            @Override
                            public void onValue(double v) {

                            }

                            @Override
                            public void onValue(String s) {
                                Message msg = new Message();
                                msg.what = 0x01;
                                Bundle bundle = new Bundle();
                                bundle.putString("DATA", s);
                                msg.setData(bundle);

                                handler.sendMessage(msg);
                            }
                        });

                        // 一定要设置湿度TAG，不然没有湿度值
                        ZigBeeAnalogServiceAPI.getHum("湿度", new OnHumResponse() {
                            @Override
                            public void onValue(double v) {

                            }

                            @Override
                            public void onValue(String s) {
                                Message msg = new Message();
                                msg.what = 0x02;
                                Bundle bundle = new Bundle();
                                bundle.putString("DATA", s);
                                msg.setData(bundle);

                                handler.sendMessage(msg);
                            }
                        });

                        // 一定要设置光照TAG，不然没有光照值
                        ZigBeeAnalogServiceAPI.getLight("光照", new OnLightResponse() {
                            @Override
                            public void onValue(double v) {

                            }

                            @Override
                            public void onValue(String s) {
                                Message msg = new Message();
                                msg.what = 0x03;
                                Bundle bundle = new Bundle();
                                bundle.putString("DATA", s);
                                msg.setData(bundle);

                                handler.sendMessage(msg);
                            }
                        });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "关闭串口成功", Toast.LENGTH_LONG).show();

                    ZigBeeAnalogServiceAPI.closeUart();
                    comOpen.setText("打开串口");
                }
            }
        });
    }
}
