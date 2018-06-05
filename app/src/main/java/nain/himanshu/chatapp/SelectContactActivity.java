package nain.himanshu.chatapp;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nain.himanshu.chatapp.Adapters.SelectContactAdapter;
import nain.himanshu.chatapp.DataModels.ContactModel;

public class SelectContactActivity extends AppCompatActivity {

    private SelectContactAdapter mAdapter;
    private List<ContactModel> mDataList;
    private String USERID;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOAD_CONTACTS();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);

        USERID = getIntent().getStringExtra("user_id");

        mDataList = new ArrayList<>();
        mAdapter = new SelectContactAdapter(mDataList, this, USERID);

        ListView mListView = findViewById(R.id.listview);
        mListView.setAdapter(mAdapter);
    }

    private void LOAD_CONTACTS() {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading contacts..");
        dialog.setCancelable(false);
        dialog.show();
        mDataList.clear();

        String API = Config.BASE_URL+"/user/friends";

        JSONObject params = new JSONObject();
        try {
            params.put("user_id", USERID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                API,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if(response.getBoolean("success")){

                                int numUsers = response.getInt("numUsers");
                                if(numUsers == 0){
                                    Toast.makeText(getApplicationContext(),"No friends to chat. Make new friends", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_CANCELED);
                                    finish();
                                }else {

                                    JSONArray data = response.getJSONArray("data");
                                    JSONObject object;
                                    ContactModel model;

                                    for (int i=0; i<data.length();i++){

                                        object = data.getJSONObject(i);
                                        model = new ContactModel();
                                        model.setName(object.getString("name"));
                                        model.setProfilePic(object.getString("profilePic"));
                                        model.setUserId(object.getString("_id"));

                                        mDataList.add(model);
                                        model = null;

                                    }
                                    mAdapter.notifyDataSetChanged();
                                    dialog.dismiss();

                                }

                            }else {
                                Toast.makeText(getApplicationContext(),"Could not load contacts", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_CANCELED);
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
                        Toast.makeText(getApplicationContext(),"Could not load contacts. Network error", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
        );
        Volley.newRequestQueue(this).add(request);

    }
}
