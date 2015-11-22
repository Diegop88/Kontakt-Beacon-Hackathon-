package com.example.it.beaconhack;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.it.beaconhack.APIs.LoginAPI;
import com.example.it.beaconhack.Models.Base;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.util.Arrays;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getClass().getSimpleName();
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                LoginAPI api = MyApp.getRetrofit().create(LoginAPI.class);
                Call<Base> call = api.login("");
                call.enqueue(new Callback<Base>() {
                    @Override
                    public void onResponse(Response<Base> response, Retrofit retrofit) {
                        startActivity(new Intent(MainActivity.this, Home.class));
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Toast.makeText(MainActivity.this, "Ya valiste", Toast.LENGTH_SHORT).show();
                    }
                });

                GraphRequest graphRequest = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject me, GraphResponse response) {
                                if (response.getError() != null) {
                                    Log.i(TAG, response.getError().getErrorMessage());
                                } else {
                                    String email = me.optString("email");
                                    String id = me.optString("id");
                                    String gender = me.optString("gender");
                                    String first_name = me.optString("first_name");
                                    String last_name = me.optString("last_name");
                                    String age_range = me.optString("age_range");


                                    Log.i("email", "-->" + email);
                                    Log.i("id", "-->" + id);
                                    Log.i("gender", "-->" + gender);
                                    Log.i("first_name", "-->" + first_name);
                                    Log.i("last_name", "-->" + last_name);
                                    Log.i("age_range", "-->" + age_range);

                                    ImageView imageView = (ImageView) findViewById(R.id.facebook_profile);

                                    Glide.with(MainActivity.this).load("https://graph.facebook.com/"+id+"/picture?type=large").into(imageView);


                                    // send email and id to your web server
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email,gender,age_range");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
                exception.printStackTrace();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    public void servicio(View view) {
        startActivity(new Intent(this, Home.class));
    }
}
