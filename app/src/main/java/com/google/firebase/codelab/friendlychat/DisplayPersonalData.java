package com.google.firebase.codelab.friendlychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DisplayPersonalData extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "MainActivity";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 6;

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mFirebaseDatabaseReference;

    private Button button;
    private String formattedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_personal_data);

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

        // search for the specific food and get its data
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("clients");
        mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Client client = postSnapshot.getValue(Client.class);
                    if (client.getName().equals(mFirebaseUser.getDisplayName())) {
                        TextView weight = findViewById(R.id.weightTextView);
                        TextView activity = findViewById(R.id.activityTextView);
                        TextView sex = findViewById(R.id.sexTextView);
                        TextView goal = findViewById(R.id.scopeTextView);
                        TextView allergies = findViewById(R.id.allergiesTextView);
                        TextView cal = findViewById(R.id.calTextView);
                        TextView carb = findViewById(R.id.carbTextView);
                        TextView prot = findViewById(R.id.protTextView);
                        TextView fat = findViewById(R.id.fatTextView);

                        weight.setText(client.getWeight());
                        activity.setText(client.getActivity());
                        sex.setText(client.getSex());
                        goal.setText(client.getScope());
                        allergies.setText(client.getAllergies());
                        cal.setText(Float.toString(client.getCal()));
                        carb.setText(Float.toString(client.getCarb()));
                        prot.setText(Float.toString(client.getProt()));
                        fat.setText(Float.toString(client.getFat()));
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {}
        });

        button = (Button) findViewById(R.id.add_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {


                } catch(NumberFormatException e) {
                }

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
        inflater.inflate(R.menu.display_personal_data_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.diary_menu:
                // get current date
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                formattedDate = df.format(c.getTime());

                Intent intent = new Intent(this, Diary.class);
                intent.putExtra("DATE", formattedDate);
                startActivity(intent);
                return true;
            case R.id.food_menu:
                startActivity(new Intent(this, Foods.class));
                return true;
            case R.id.chat_menu:
                startActivity(new Intent(this, MainActivity.class));
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

}
