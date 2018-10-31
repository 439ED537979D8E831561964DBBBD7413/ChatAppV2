package nain.himanshu.chatapp;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import nain.himanshu.chatapp.Utility.VolleySingleton;

public class ProfileActivity extends AppCompatActivity {

    private String USERID;

    private String[] picOptions = {
            "Change Picture",
            "Take Picture",
            "Remove Picture"
    };

    private TextView mName, mStatus, mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences preferences = getSharedPreferences(Config.LoginPrefs, MODE_PRIVATE);
        USERID = preferences.getString("id","");

        mName = findViewById(R.id.name);
        mStatus = findViewById(R.id.status);
        mPhone = findViewById(R.id.phone);

        FloatingActionButton mChangePicture = findViewById(R.id.changePic);
        mChangePicture.setOnClickListener(mChangePictureClick);

        ImageView mChangeName = findViewById(R.id.changeName);
        mChangeName.setOnClickListener(mChangeNameClick);

        mStatus.setOnClickListener(mStatusClick);

        loadData();
    }

    private void update(String key, String value){


        JSONObject data = new JSONObject();
        try {
            data.put("_id", USERID);
            data.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    private void loadData() {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading Profile...");
        dialog.show();

        JSONObject data = new JSONObject();
        try {
            data.put("_id", USERID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                Config.USER_BASE_URL+"/getprofile",
                data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        dialog.dismiss();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        dialog.dismiss();

                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }


    private View.OnClickListener mStatusClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final EditText mStatusText = new EditText(ProfileActivity.this);
            mStatusText.setTextSize(18f);

            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setView(mStatusText);
            builder.setTitle("About Me");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    mStatus.setText(mStatusText.getText().toString().trim());
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.create().show();

        }
    };

    private View.OnClickListener mChangeNameClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final EditText mNameText = new EditText(ProfileActivity.this);
            mNameText.setTextSize(18f);

            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setView(mNameText);
            builder.setTitle("Name");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    mName.setText(mNameText.getText().toString().trim());
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.create().show();
        }
    };

    private View.OnClickListener mChangePictureClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setItems(picOptions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch (which){



                    }

                }
            });

            builder.create().show();
        }
    };


}
