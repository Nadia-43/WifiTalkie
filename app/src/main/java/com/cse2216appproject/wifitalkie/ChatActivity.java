package com.cse2216appproject.wifitalkie;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ChatActivity extends Activity {

    Button btnSend;
    Button btnCall;
    Button btnEndCall;
    TextView read_msg_box;
    EditText writeMsg;

    SendReceive sendReceive;
    String sendData;

    static final int MESSAGE_READ=1;
    InputStream inputStream;
    OutputStream outputStream;
    AudioCall audioCall;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initialWork();
        exqtListener();
        btnEndCall.setEnabled(false);
        read_msg_box.setText("counter = "+MainActivity.counter);
        sendReceive=new SendReceive(Data.socket);
    }

    void exqtListener()
    {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData = writeMsg.getText().toString();
                sendReceive.writeto(sendData);
            }
        });
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startAudioCall();
            }
        });
        btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                endAudioCall();
            }
        });
    }

    void initialWork()
    {
        btnSend = (Button)findViewById(R.id.sendButton);
        btnCall=(Button)findViewById(R.id.callButton);
        btnEndCall=(Button)findViewById(R.id.endCall);
        read_msg_box = (TextView)findViewById(R.id.readMsg);
        writeMsg=(EditText)findViewById(R.id.writeMsg);
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte []readBuff = (byte[])msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);
                    read_msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private class SendReceive implements Runnable{
        private Thread thread;
        private Socket socket;

        SendReceive(Socket socket)
        {

            thread=new Thread(this,"Client");
            this.socket=socket;
            try {
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();
                //printStream = new PrintStream(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            thread.start();
        }

        @Override
        public void run() {

            byte []buffer = new byte[1024];
            int bytes;
            while (socket!=null)
            {
                try {
                    bytes =inputStream.read(buffer);
                    if(bytes>0)
                    {
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        public void writeto(String msg)
        {
            new SendReceiveTask().execute(msg);
        }
    }

    class SendReceiveTask extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... strings) {
            String msg=strings[0];
            try {
                outputStream.write(msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    void startAudioCall()
    {
        audioCall=new AudioCall(Data.socket.getInetAddress());
        audioCall.startCall();
        btnCall.setEnabled(false);
        btnEndCall.setEnabled(true);
    }
    void endAudioCall()
    {
        audioCall.endCall();
        btnEndCall.setEnabled(false);
        btnCall.setEnabled(true);
    }
}
