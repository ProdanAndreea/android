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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class AddFood_2 extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "MainActivity";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 6;

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;

    private Button mSendButton;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mFirebaseDatabaseReference;

    private Button button;
    private EditText mGramEditText;

    private Float carb;
    private Float prot;
    private Float fat;
    private Float cal;
    private ImageView imageView;
    private String foodName;

    private Float calcCal;
    private Float calcCarb;
    private Float calcProt;
    private Float calcFat;

    private Float quantity;
    private boolean flag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food_2);

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
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("foods");
        foodName = getIntent().getStringExtra("EXTRA_SESSION_ID");
        mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Food climate = postSnapshot.getValue(Food.class);
                    if (climate.getName().equals(foodName)) {
                        prot = climate.getProt();
                        cal = climate.getCal();
                        carb = climate.getCarb();
                        fat = climate.getFat();

                        TextView txt = (TextView) findViewById(R.id.textView);
                        txt.setText(foodName); //Float.toString(prot)



                        imageView = (ImageView) findViewById(R.id.foodImageView);
                        //imageview.setImageResource(climate.getPhoto());
                        Glide
                                .with(AddFood_2.this)
                                .load(climate.getPhoto()) // the uri you got from Firebase
                                .centerCrop()
                                .into(imageView); //Your imageView variable
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
                       /*
                        * You may print the error message.
                       **/
            }
        });

        mGramEditText = (EditText) findViewById(R.id.gramEditText);

        button = (Button) findViewById(R.id.add_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    quantity = Float.parseFloat(mGramEditText.getText().toString());

                    calculateData();
                    displayData();

                } catch(NumberFormatException e) {
                    EditText txt = (EditText) findViewById(R.id.gramEditText);
                    txt.setError("Wrong input !");
                }



            }
        });




        mGramEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
        mGramEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

    }


    public void calculateData() {
        calcCal = (quantity * cal) / 100;
        calcCarb = (quantity * carb) / 100;
        calcProt = (quantity * prot) / 100;
        calcFat = (quantity * fat) / 100;

        // reduce to 2 decimals
        calcCal = round(calcCal, 2);
        calcCarb = round(calcCarb, 2);
        calcProt = round(calcProt, 2);
        calcFat = round(calcFat, 2);
    }

    private void displayData() {
        TextView textCalView = findViewById(R.id.textCalView);
        TextView textCarbView = findViewById(R.id.textCarbView);
        TextView textProtView = findViewById(R.id.textProtView);
        TextView textFatView = findViewById(R.id.textFatView);

        textCalView.setText(Float.toString(cal));
        textCarbView.setText(Float.toString(carb));
        textProtView.setText(Float.toString(prot));
        textFatView.setText(Float.toString(fat));
    }




    /* round a float number to 2 decimal place*/
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


    // create right top the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_food_menu_2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.food_menu:
                startActivity(new Intent(this, Foods_2.class));
                return true;
            case R.id.chat_menu:
                startActivity(new Intent(this, MainActivity_2.class));
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
