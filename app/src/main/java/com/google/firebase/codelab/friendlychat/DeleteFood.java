package com.google.firebase.codelab.friendlychat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import android.widget.ProgressBar;
import android.widget.TextView;


public class DeleteFood extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "DeleteFood";

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;

    private DatabaseReference mFirebaseDatabaseReference;
    private Button button;
    EditText txt;
    private boolean found = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

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

        button = (Button) findViewById(R.id.add_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the name of the food from the input
                txt = (EditText) findViewById(R.id.nameEditText);

                // try to delete it
                // and if it succeeds set found on true
                mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("foods");
                mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Food food = postSnapshot.getValue(Food.class);
                            if (food.getName().equals(txt.getText())) {
                                found = true;
                                // delete it
                            }
                        }
                        // if it is not found, set an error
                        if (found == false) {
                            txt = (EditText) findViewById(R.id.nameEditText);
                            txt.setError("The food doesn't exist !");
                        } else {
                            // delete the food from the fire base
                            // go back to foods table
                            startActivity(new Intent(DeleteFood.this, Add.class));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                    }
                });
            }
        });
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    // create right top the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.foods_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_food:
                startActivity(new Intent(this, AddNewFood.class));
                return true;
            case R.id.display_personal_data_menu:
                startActivity(new Intent(this, DisplayPersonalData.class));
                return true;
            case R.id.diary_menu:
                startActivity(new Intent(this, ChooseDate.class));
                return true;
            case R.id.chat_menu:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mFirebaseUser = null;
                mUsername = ANONYMOUS;
                mPhotoUrl = null;
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
