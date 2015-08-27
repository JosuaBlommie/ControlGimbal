package josuablom.controlgimbal;

import android.app.Activity;
import android.os.Bundle;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Blommie on 2015-08-26.
 */
public class HomeActivity extends Activity
{
    public static final String SERVERIP = "192.168.43.82";
    public static final int SERVERPORT = 2390;
    public String message = "0.00,0.00,0.0000";


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        new Thread(new Client()).start();
    }


    public class Client implements Runnable {
        @Override
        public void run() {
            try {

                // send message to Arduino
                InetAddress serverAddr = InetAddress.getByName(SERVERIP);
                DatagramSocket clientSocket = new DatagramSocket();
                byte[] sendData = new byte[1024];
                String sentence = message;
                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddr, SERVERPORT);
                clientSocket.send(sendPacket);

                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
