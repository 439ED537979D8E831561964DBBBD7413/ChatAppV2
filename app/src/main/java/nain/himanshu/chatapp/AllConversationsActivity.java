package nain.himanshu.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import nain.himanshu.chatapp.Adapters.AllConversationsAdapter;
import nain.himanshu.chatapp.Adapters.SelectContactAdapter;
import nain.himanshu.chatapp.DataModels.ConversationData;
import okhttp3.internal.Util;

public class AllConversationsActivity extends AppCompatActivity {

    private static final int CHOOSE_USER = 293;
    private String USERID;

    //Service related

    private boolean mBounded;
    private NotificationService mService;

    private HashMap<String, String> mConversationLogs;

    //private HashSet<String> mConversationIds;
    private List<ConversationData> mDataList;
    private AllConversationsAdapter mAdapter;

    private ListView mListView;
    private FloatingActionButton mFab;
    private CustomSwipeRefresh mSwipeRefreshLayout;

    private RequestQueue mRequestQueue;
    /*
    TODO:this will listen to only new message for now, extend to typing and stop typing.
     */
    private Socket mSocket;
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            final JSONObject data = (JSONObject)args[0];

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*
                    TODO: ADD BLINK OPTION(MAYBE IN ADAPTER) TO HELP USER IDENTIFY WHICH CHAT SENT MESSAGE
                     */

                    try {

                        String conversationId = data.getString("conversationId");
                        String name = data.getString("name");
                        String userId = data.getString("author");
                        String message = data.getString("message");

                        //if(mConversationIds.contains(conversationId)){
                        if(mConversationLogs.containsValue(conversationId)){

                            /*
                               conversation is listed
                             */
                            int i=-1;
                            boolean found = false;
                            for (ConversationData aMDataList : mDataList) {
                                i++;
                                if (aMDataList.getConversationId().equals(conversationId)) {
                                    found = true;
                                    break;
                                }
                            }
                            if(found) {
                                mDataList.get(i).setLatestMessage(message);
                                mAdapter.notifyDataSetChanged();
                                String title = data.getString("name")+" sent you a message";

                                Utils.sendNotification(title, message, Config.CHAT_NOTIF_CHANNEL,getApplicationContext());
                            }

                        }else {
                            /*
                                conversations not listed
                             */
                            mConversationLogs.put(userId, conversationId);
                            ConversationData newConvo = new ConversationData();
                            newConvo.setConversationId(conversationId);
                            newConvo.setOtherName(name);
                            newConvo.setOtherProfilePic(data.getString("profilePic"));
                            newConvo.setLatestMessage(message);

                            mDataList.add(newConvo);
                            mAdapter.notifyDataSetChanged();

                            String title = name+" sent you a message";

                            Utils.sendNotification(title,  message, Config.CHAT_NOTIF_CHANNEL,getApplicationContext());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

        }
    };

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


    private View.OnClickListener onFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(AllConversationsActivity.this, SelectContactActivity.class);
            intent.putExtra("user_id", USERID);
            startActivityForResult(intent, CHOOSE_USER);

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == CHOOSE_USER){

            if(resultCode == RESULT_OK){

                final Bundle selected_user_data = intent.getExtras();
                assert selected_user_data != null;

                if(mConversationLogs.containsKey(selected_user_data.getString("other_id"))){

                    Bundle bundle = new Bundle();
                    bundle.putString("conversationId", mConversationLogs.get(selected_user_data.getString("other_id")));
                    bundle.putString("other_name", selected_user_data.getString("other_name"));
                    bundle.putString("other_pic", selected_user_data.getString("other_pic"));

                    Intent in = new Intent(getApplicationContext(), ChatActivity.class);
                    in.putExtras(bundle);
                    startActivity(in);

                }else {

                    final ProgressDialog dialog = new ProgressDialog(AllConversationsActivity.this);
                    dialog.setMessage("Starting Chat...");
                    dialog.setCancelable(false);
                    dialog.show();
                    JSONObject params = new JSONObject();
                    try {
                        params.put("current_user", USERID);
                        params.put("target_user", selected_user_data.getString("other_id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.POST,
                            Config.HAS_CONVERSATION,
                            params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    dialog.dismiss();

                                    try {
                                        if(response.getBoolean("success")){

                                            if(response.getBoolean("exists")){

                                                //conversation already there
                                                Bundle bundle = new Bundle();
                                                bundle.putString("conversationId", response.getString("id"));
                                                bundle.putString("other_name", selected_user_data.getString("other_name"));
                                                bundle.putString("other_pic", selected_user_data.getString("other_pic"));

                                                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                                intent.putExtras(bundle);
                                                startActivity(intent);

                                            }else {

                                                Bundle bundle = new Bundle();
                                                bundle.putString("conversationId", null);
                                                bundle.putString("other_name", selected_user_data.getString("other_name"));
                                                bundle.putString("other_pic", selected_user_data.getString("other_pic"));
                                                bundle.putString("other_id", selected_user_data.getString("other_id"));

                                                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                                intent.putExtras(bundle);
                                                startActivity(intent);

                                            }

                                        }else {
                                            Toast.makeText(getApplicationContext(), "Could not start conversation", Toast.LENGTH_SHORT).show();
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
                                    Log.e("START NEW CHAT ERROR", error.toString());
                                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                    mRequestQueue.add(request);

                }
            }

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_conversations);

        mRequestQueue = Volley.newRequestQueue(this);
        //mConversationIds = new HashSet<>();
        mConversationLogs = new HashMap<>();
        mDataList = new ArrayList<>();
        mAdapter = new AllConversationsAdapter(mDataList, this);

        mListView = findViewById(R.id.listview);
        mSwipeRefreshLayout = findViewById(R.id.parentLayout);
        mSwipeRefreshLayout.setTargetView(mListView);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LOAD_DATA();
            }
        });

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(onFabClickListener);

        mListView.setAdapter(mAdapter);

        //LOAD_DATA();
    }

    private void SUBSCRIBE() {

        for (String id : mConversationLogs.values()){
            mSocket.emit("join", id);
        }

        /*for (String id : mConversationIds){
            mSocket.emit("join", id);
            //Toast.makeText(getApplicationContext(),id,Toast.LENGTH_SHORT).show();
        }*/
    }

    private void LOAD_DATA() {

        //mConversationIds.clear();
        mConversationLogs.clear();
        mDataList.clear();
        //mAdapter.notifyDataSetChanged();
        if(!mSwipeRefreshLayout.isRefreshing()){
            mSwipeRefreshLayout.setRefreshing(true);
        }

        String API_URL = Config.BASE_URL+"/chat/allConversations";

        JSONObject params = new JSONObject();
        try {
            params.put("id",USERID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                API_URL,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mSwipeRefreshLayout.setRefreshing(false);

                        try{

                            if(response.getBoolean("success")){

                                //notify no. of conversations
                                Toast.makeText(getApplicationContext(), String.valueOf(response.getInt("numConversations") + " Conversations"), Toast.LENGTH_SHORT).show();

                                if(response.getInt("numConversations")==0){
                                    /*
                                    TODO: ADD NO conversations
                                     */
                                }else {
                                    JSONArray conversations = response.getJSONArray("conversations");

                                    ConversationData conversationData;
                                    JSONObject conversation, latest, user;

                                    for(int i=0; i<conversations.length(); i++){

                                        conversation = conversations.getJSONObject(i);
                                        latest = conversation.getJSONObject("message");
                                        user = conversation.getJSONObject("user");
                                        conversationData = new ConversationData();

                                        String id = latest.getString("conversationId");

                                        //add conversation
                                       // mConversationIds.add(id);

                                        conversationData.setConversationId(id);
                                        conversationData.setLatestMessage(latest.getString("body"));
                                        conversationData.setOtherName(user.getString("name"));
                                        conversationData.setOtherProfilePic(user.getString("profilePic"));

                                        //add userid:conversation
                                        mConversationLogs.put(user.getString("_id"),id);

                                        mDataList.add(conversationData);
                                        conversationData = null;
                                        latest = null;
                                        user = null;

                                    }

                                    mAdapter.notifyDataSetChanged();
                                    SUBSCRIBE();
                                }

                            }else {

                                Toast.makeText(getApplicationContext(), "Could not get conversations", Toast.LENGTH_SHORT).show();

                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(AllConversationsActivity.this, "Could not get conversations. Network error", Toast.LENGTH_SHORT).show();
                    }
                }
        );
       mRequestQueue.add(request);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSocket.off("new message", onNewMessage);
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOAD_DATA();
        mSocket.on("new message", onNewMessage);
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
    }

    @Override
    protected void onStart() {
        super.onStart();
        USERID = getSharedPreferences(Config.LoginPrefs, MODE_PRIVATE).getString("id","");
        mSocket = MyApplication.getSocket();
        if(!mSocket.connected()){
            mSocket.connect();
        }

    }
}
