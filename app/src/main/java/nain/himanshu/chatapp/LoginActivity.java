package nain.himanshu.chatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.internal.Util;

public class LoginActivity extends AppCompatActivity {

    private EditText mPhone, mName;
    private Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Utils.createNotificationChannels(this);

        mPhone = findViewById(R.id.phone);
        mName = findViewById(R.id.name);
        mLogin = findViewById(R.id.login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mPhone.getText().length()<10){
                    mPhone.setError("Not a valid phone or name");
                }else {
                    login_or_register(mPhone.getText().toString(), mName.getText().toString());
                }

            }
        });
    }

    private void login_or_register(String phone, String name) {

        JSONObject object = new JSONObject();
        try {
            object.put("phone", phone);
            object.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                Config.BASE_URL + "/user/login",
                object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            if(response.getBoolean("success")){

                                SharedPreferences preferences = getSharedPreferences(Config.LoginPrefs, MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("id", response.getString("id"));
                                editor.putString("name", response.getString("name"));
                                editor.apply();

                                finish();
                                startActivity(new Intent(
                                        LoginActivity.this, AllConversationsActivity.class
                                ));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"Network error", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        Volley.newRequestQueue(this).add(request);
    }
}
