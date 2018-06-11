package nain.himanshu.chatapp;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.internal.Util;

public class NotificationService extends Service {

    public static NotificationService instance = null;
    private static String USERID;
    private Socket mSocket;

    private final IBinder myBinder = new LocalBinder();

    public static boolean isInstanceCreated() {
        return instance != null;
    }

    public class LocalBinder extends Binder{
        public NotificationService getService(){
            return NotificationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(isInstanceCreated()){
            return;
        }
        super.onCreate();
        USERID = getApplicationContext().getSharedPreferences(Config.LoginPrefs, MODE_PRIVATE).getString("id","");
        mSocket = MyApplication.getSocket();
        mSocket.on("new message", onNewMessage);
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(isInstanceCreated()){
            return 0;
        }
        super.onStartCommand(intent, flags, startId);
        connectConnection();
        return START_STICKY;
    }

    private void connectConnection(){
        instance = this;
        if(!mSocket.connected()){
            mSocket.connect();
        }
    }
    private void disconnectConnection(){
        instance = null;
        if(mSocket.connected()){
            mSocket.disconnect();
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mSocket.emit("log me", USERID);
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject data = (JSONObject) args[0];

            new Handler(getMainLooper()).post(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Message recieved", Toast.LENGTH_SHORT).show();

                            try {
                                /*
                                TODO: IMPROVE NOTIFICATION
                                 */
                                String conversationId = data.getString("conversationId");
                                String name = data.getString("name");
                                String userId = data.getString("author");
                                String message = data.getString("message");

                                if(!userId.equals(USERID)){
                                    Utils.sendNotification(name + " sent you a message", message, Config.CHAT_NOTIF_CHANNEL, getApplicationContext());
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
            );

        }
    };
    public void listenToChat(boolean listen){
        if(listen){
            mSocket.on("new message", onNewMessage);
        }else {
            mSocket.off("new message", onNewMessage);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Off all events

        mSocket.off("new message", onNewMessage);
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        disconnectConnection();
    }
}
