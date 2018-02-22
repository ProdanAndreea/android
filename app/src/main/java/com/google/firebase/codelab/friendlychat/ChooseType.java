package com.google.firebase.codelab.friendlychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Observable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.widget.Button;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseType extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "MainActivity";

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;

    private Button mSendButton;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;

    private Button button;
    private RadioGroup radioGroup;
    private RadioButton client;
    private RadioButton trainer;
    private DatabaseReference mFirebaseDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_type);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class)); // the user is not signed in => send him to sign in page
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        radioGroup = (RadioGroup) findViewById(R.id.radioChoice);
        client=(RadioButton)findViewById(R.id.radioClient);
        trainer=(RadioButton)findViewById(R.id.radioTrainer);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if(client.isChecked())
                {
                    Toast.makeText(ChooseType.this,"Client is selected", Toast.LENGTH_SHORT).show();
                }
                else if(trainer.isChecked())
                {
                    Toast.makeText(ChooseType.this,"Trainer is selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button = (Button) findViewById(R.id.nextButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(client.isChecked()) {
                    startActivity(new Intent(getBaseContext(), PersonalData.class));
                } else {
                    if (trainer.isChecked()) {
                        addToFirebase();
                        startActivity(new Intent(getBaseContext(), Foods_2.class));
                    }
                }
            }
        });

    }


    private void addToFirebase() {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Trainer t = new Trainer(mFirebaseUser.getDisplayName());
        mFirebaseDatabaseReference.child("trainers")
                //.child(mFirebaseUser.getDisplayName())
                .push().setValue(t);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }




}
