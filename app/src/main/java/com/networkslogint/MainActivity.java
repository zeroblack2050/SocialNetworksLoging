package com.networkslogint;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity {

    private TwitterLoginButton twitterLoginButton;
    private LoginButton loginButtonFacebook;
    private CallbackManager callbackManagerFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(R.layout.activity_main);
        twitterLoginButton = findViewById(R.id.login_twitter);
        loginButtonFacebook = findViewById(R.id.login_button_facebook);
        callbackManagerFacebook = CallbackManager.Factory.create();
        loginTwitter();
        loginFacebook();
    }

    private void loginFacebook() {
        loginButtonFacebook.setReadPermissions("public_profile");
        loginButtonFacebook.registerCallback(callbackManagerFacebook, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest graphRequest = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String name = object.getString("name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Bundle bundle = new Bundle();
                bundle.putString("fields","name, email, birthday, picture.type(large)");
                graphRequest.setParameters(bundle);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, R.string.canceled, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode,resultCode,data);
        callbackManagerFacebook.onActivityResult(requestCode, resultCode, data);

    }

    private void loginTwitter() {
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {

                TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
                TwitterAuthToken twitterAuthToken = twitterSession.getAuthToken();
                Call<User> userCall = TwitterCore
                        .getInstance()
                        .getApiClient(twitterSession)
                        .getAccountService()
                        .verifyCredentials(true,true,true);

                userCall.enqueue (new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        String description = result.data.description;
                    }

                    @Override
                    public void failure(TwitterException exception) {
                        Toast.makeText(MainActivity.this, ""+exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(MainActivity.this, ""+exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
