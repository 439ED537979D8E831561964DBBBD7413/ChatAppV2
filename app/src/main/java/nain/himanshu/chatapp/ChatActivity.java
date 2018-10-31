package nain.himanshu.chatapp;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.internal.Util;

public class ChatActivity extends AppCompatActivity {

    private String USERID, USERNAME, CONVERSATIONID, OTHERID;
    private Boolean isConversationIdEmpty = true;
    private Bundle bundle;

    private TextView mTyping;

    private LinearLayout mChatLayout;
    private LayoutInflater mLayoutInflater;

    private EditText mMessage;
    private Button mSend;

    private RequestQueue mRequestQueue;

    /*
    TODO:extend socket to listen for online
     */
    private Socket mSocket;

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preferences = getSharedPreferences(Config.LoginPrefs, MODE_PRIVATE);
        USERID = preferences.getString("id","");
        USERNAME = preferences.getString("name","");
        mSocket = MyApplication.getSocket();
        if(!mSocket.connected()){
            mSocket.connect();
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        String sender = data.getString("author");
                        String sender_name = data.getString("name");
                        String sender_pic = data.getString("profilePic");
                        String conversationId = data.getString("conversationId");
                        String message = data.getString("message");

                        if(conversationId.equals(CONVERSATIONID)){

                            if(!sender.equals(USERID)){
                                addOtherMessage(message, null);
                            }
                        }else {

                            String title = sender_name + " send you a message";
                            Utils.sendNotification(title, message, Config.CHAT_NOTIF_CHANNEL,getApplicationContext());

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                }
            });

        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            String name = (String) args[0];

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTyping.setVisibility(View.VISIBLE);
                    mTyping.setText("typing...");
                }
            });

        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTyping.setText("");
                    mTyping.setVisibility(View.GONE);
                }
            });

        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        mSocket.on("new message", onNewMessage);
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSocket.off("new message", onNewMessage);
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mRequestQueue = Volley.newRequestQueue(this);
        if(getIntent().getExtras()!=null){

            bundle = getIntent().getExtras();
            CONVERSATIONID = bundle.getString("conversationId");
            if(CONVERSATIONID == null || CONVERSATIONID.isEmpty()){
                OTHERID = bundle.getString("other_id");
                isConversationIdEmpty = true;
            }else {
                isConversationIdEmpty = false;
            }

        }else {
            finish();
            Toast.makeText(this, "Not enough details to start conversation", Toast.LENGTH_SHORT).show();

        }

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTyping = findViewById(R.id.typing);
        mTyping.setVisibility(View.GONE);

        TextView mOtherName = findViewById(R.id.name);
        mOtherName.setText(bundle.getString("other_name"));
        CircleImageView mOtherProfilePic = findViewById(R.id.profilePic);

        if(!(Objects.requireNonNull(bundle.getString("other_pic")).isEmpty())){

            Glide.with(this)
                    .load(bundle.getString("other_pic"))
                    .apply(new RequestOptions()
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis()))))
                    .into(mOtherProfilePic);

        }else {
            Glide.with(this).clear(mOtherProfilePic);
            mOtherProfilePic.setImageResource(R.drawable.demo_photo);
        }

        mChatLayout = findViewById(R.id.chatLayout);
        mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        mMessage = findViewById(R.id.message);
        mMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(!isConversationIdEmpty){

                    if(!String.valueOf(s).isEmpty()){
                        JSONObject data = new JSONObject();
                        try {
                            data.put("name",USERNAME);
                            data.put("conversationId", CONVERSATIONID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mSocket.emit("typing", data);
                    }else {
                        mSocket.emit("stop typing", CONVERSATIONID);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSend = findViewById(R.id.send);

        mSend.setOnClickListener(onSendClickListener);

        if(!(CONVERSATIONID == null || CONVERSATIONID.isEmpty())) {
            LOAD_MESSAGES();
        }
    }

    private void LOAD_MESSAGES() {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading your chats..");
        dialog.setCancelable(false);
        dialog.show();

        JSONObject object = new JSONObject();
        try {
            object.put("conversationId", CONVERSATIONID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Config.GET_CONVERSATION,
                object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if(response.getBoolean("success")){

                                JSONArray array = response.getJSONArray("conversation");
                                JSONObject message;
                                String body, author, time;

                                for (int i=0; i<array.length();i++){

                                    message = array.getJSONObject(i);
                                    body = message.getString("body");
                                    author = message.getString("author");
                                    time = message.getString("createdAt");
                                    if(author.equals(USERID)){
                                        addSelfMessage(body, Utils.getTime(time));
                                    }else {
                                        addOtherMessage(body, Utils.getTime(time));
                                    }

                                }
                                dialog.dismiss();

                            }else {
                                Toast.makeText(getApplicationContext(), "Could not get messages", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );
        mRequestQueue.add(request);

    }

    private View.OnClickListener onSendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String message = mMessage.getText().toString();
            if(message.isEmpty()){
                mSend.setError("Message cannot be empty");
            }else {

                //SEND MESSAGE i.e check conversationId
                if(CONVERSATIONID == null || CONVERSATIONID.isEmpty()){

                    //start new conversation

                    attemptStartConversation(message);

                }else {

                    attemptSendMessage(message);
                    mSocket.emit("stop typing", CONVERSATIONID);

                }

            }

        }
    };

    private void attemptSendMessage(final String message) {

        mMessage.setText("");

        JSONObject params = new JSONObject();

        try {
            params.put("message", message);
            params.put("sender_id", USERID);
            params.put("conversationId", CONVERSATIONID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Config.SEND_MESSAGE,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if (response.getBoolean("success")) {

                                JSONObject object = new JSONObject();
                                object.put("sender_id", USERID);
                                object.put("conversationId", CONVERSATIONID);
                                object.put("message", message);

                                mSocket.emit("new message", object);

                                if (response.has("time")) {
                                    addSelfMessage(message, Utils.getTime(response.getString("time")));
                                }else {
                                    addSelfMessage(message, null);
                                }

                            }else {

                                mMessage.setText(message);
                                Toast.makeText(getApplicationContext(),"Could not send message", Toast.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mMessage.setText(message);
                        Toast.makeText(getApplicationContext(),"Network error", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        mRequestQueue.add(request);

    }

    private void attemptStartConversation(final String message) {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Starting conversation...");
        dialog.setCancelable(false);
        dialog.show();

        JSONObject params = new JSONObject();
        try {
            params.put("sender_id", USERID);
            params.put("recipient_id", OTHERID);
            params.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Config.START_CONVERSATION,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dialog.dismiss();

                        try {
                            if(response.getBoolean("success")){

                                mMessage.setText("");

                                CONVERSATIONID = response.getString("conversationId");

                                isConversationIdEmpty = false;

                                if (response.has("time")) {
                                    addSelfMessage(message, Utils.getTime(response.getString("time")));
                                }else {
                                    addSelfMessage(message, null);
                                }

                                //join room
                                mSocket.emit("join", CONVERSATIONID);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();

                        Toast.makeText(getApplicationContext(),"Failed sending message. Network error", Toast.LENGTH_SHORT).show();

                    }
                }
        );

        mRequestQueue.add(request);

    }

    private void addSelfMessage(@NonNull String message, @Nullable String time){

        View view = mLayoutInflater.inflate(R.layout.self_message, null);

        TextView mMessageText = view.findViewById(R.id.message);
        TextView mTime = view.findViewById(R.id.time);
        mMessageText.setText(message);
        if(time == null){
            mTime.setVisibility(View.GONE);
        }else {
            mTime.setText(time);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(350, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(5,5,5,5);
        params.gravity = Gravity.END;
        view.setPadding(5,5,5,5);
        mChatLayout.addView(view, params);
    }

    private void addOtherMessage(@NonNull String message, @Nullable String time){

        View view = mLayoutInflater.inflate(R.layout.other_message, null);
        TextView mMessageText = view.findViewById(R.id.message);
        TextView mTime = view.findViewById(R.id.time);
        mMessageText.setText(message);
        if(time == null){
            mTime.setVisibility(View.GONE);
        }else {
            mTime.setText(time);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(350, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(5,5,5,5);
        view.setPadding(5,5,5,5);
        mChatLayout.addView(view, params);

    }
}
