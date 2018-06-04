package nain.himanshu.chatapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MyApplication extends Application {

    public static Socket mSocket;
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;

        try {
            mSocket = IO.socket(Config.BASE_URL, options);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e("SOCKET IO", e.getMessage());
        }

    }

    public static Socket getSocket() {
        return mSocket;
    }

    public static Context getContext() {
        return mContext;
    }
}
