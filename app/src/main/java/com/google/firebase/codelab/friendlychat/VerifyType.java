package com.google.firebase.codelab.friendlychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import android.widget.RadioButton;
import android.widget.RadioGroup;


public class VerifyType extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "MainActivity";

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;

    private DatabaseReference mFirebaseDatabaseReference;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_type);

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


        // verify if the user has a type saved to firebase
        // look into trainers
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("trainers");
        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.getValue(Trainer.class).getName().equals(mFirebaseUser.getDisplayName()) == true) {
                            flag = true;
                        }
                    }
                }
                // if the user doesn't have a trainer type, so look into clients
                if (flag == false) {
                    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("clients");
                    mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    if (ds.getValue(Trainer.class).getName().equals(mFirebaseUser.getDisplayName()) == true) {
                                        flag = true;
                                    }
                                }
                            }
                            // if the user doesn't have either a client type, send him to choose a type
                            if (flag == false) {
                                startActivity(new Intent(VerifyType.this, ChooseType.class));
                            } else { // if the user has a client type
                                startActivity(new Intent(VerifyType.this, Foods.class));
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                        }
                    });
                } else { // if the user has a trainer type
                    startActivity(new Intent(VerifyType.this, Foods_2.class));
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });



    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }




}
