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


public class PersonalData2 extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "MainActivity";

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;

    private DatabaseReference mFirebaseDatabaseReference;

    private String activity;
    private String scope;
    private String sex;
    private String weight;

    private String allergies = "";
    private Button button;
    private RadioGroup radioDairyChoice;
    private RadioButton radioDairy;
    private RadioGroup radioWheatChoice;
    private RadioButton radioWheat;
    private RadioGroup radioSoyChoice;
    private RadioButton radioSoy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data2);

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

        radioDairyChoice = (RadioGroup) findViewById((R.id.radioDairyChoice));
        radioDairy = (RadioButton)findViewById(R.id.radioDairy);

        radioWheatChoice = (RadioGroup) findViewById((R.id.radioWheatChoice));
        radioWheat = (RadioButton)findViewById(R.id.radioWheat);

        radioSoyChoice = (RadioGroup) findViewById((R.id.radioSoyChoice));
        radioSoy = (RadioButton)findViewById(R.id.radioSoy);

        button = (Button) findViewById(R.id.nextButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (radioDairy.isChecked()) {
                    allergies = allergies + " dairy";
                }
                if (radioWheat.isChecked()) {
                    allergies = allergies + " wheat";
                }
                if (radioSoy.isChecked()) {
                    allergies = allergies + " soy";
                }

                activity = getIntent().getStringExtra("ACTIVITY");
                scope = getIntent().getStringExtra("SCOPE");
                sex = getIntent().getStringExtra("SEX");
                weight = getIntent().getStringExtra("WEIGHT");

                Client client = new Client(mFirebaseUser.getDisplayName(), activity, scope, sex, weight, allergies);

                // calculate data
                client.calculateData();
                // add the datea to firebase
                addToFirebase(client);

                // send to main page
                startActivity(new Intent(PersonalData2.this, Foods.class));
            }
        });


    }


    private void addToFirebase(Client client) {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child("clients")
                .push().setValue(client);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

}
