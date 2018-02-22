package com.google.firebase.codelab.friendlychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.images.WebImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


public class PersonalData extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "MainActivity";

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;

    private DatabaseReference mFirebaseDatabaseReference;

    private Button button;
    private RadioGroup radioActivityChoice;
    private RadioButton sedentary;
    private RadioButton recreational;
    private RadioButton athlete;
    private RadioButton powerlifter;
    private RadioGroup radioScopeChoise;
    private RadioButton mantain;
    private RadioButton lose;
    private RadioButton gain;
    private RadioGroup radioSexChoise;
    private RadioButton m;
    private RadioButton f;
    private EditText weightEditText;

    private String activity;
    private String scope;
    private String sex;
    private String weight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data);

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

        radioActivityChoice = (RadioGroup) findViewById(R.id.radioActivityChoice);
        sedentary = (RadioButton)findViewById(R.id.radioSedentary);
        recreational = (RadioButton)findViewById(R.id.radioRecreational);
        athlete = (RadioButton)findViewById(R.id.radioAthlete);
        powerlifter = (RadioButton)findViewById(R.id.radioPowerlifter);

        radioScopeChoise = (RadioGroup) findViewById((R.id.radioScopeChoice));
        mantain = (RadioButton)findViewById(R.id.radioMantain);
        lose = (RadioButton)findViewById(R.id.radioLose);
        gain = (RadioButton)findViewById(R.id.radioGain);

        radioSexChoise = (RadioGroup) findViewById(R.id.radioScopeChoice);
        f = (RadioButton) findViewById(R.id.radioF);
        m = (RadioButton) findViewById(R.id.radioM);

        weightEditText = (EditText) findViewById(R.id.weightEditText);

        button = (Button) findViewById(R.id.nextButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sedentary.isChecked()) {
                    activity = "sedentary";
                } else if (recreational.isChecked()) {
                        activity = "recreational";
                } else if (athlete.isChecked()) {
                    activity = "athlete";
                } else if (powerlifter.isChecked()) {
                                activity = "powerlifter";
                }

                if (mantain.isChecked()) {
                    scope = "maintain";
                } else if (lose.isChecked()) {
                    scope = "lose";
                } else if (gain.isChecked()) {
                    scope = "gain";
                }

                if (f.isChecked()) {
                    sex = "F";
                } else if (m.isChecked()) {
                    sex = "M";
                }

                weight = weightEditText.getText().toString();


                // send to main page
                Intent intent = new Intent(PersonalData.this, PersonalData2.class);
                intent.putExtra("ACTIVITY", activity);
                intent.putExtra("SCOPE", scope);
                intent.putExtra("SEX", sex);
                intent.putExtra("WEIGHT", weight);
                startActivity(intent);
            }
        });


    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

}
