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
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import org.w3c.dom.Comment;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;


public class AddFood extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

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
    private String formattedDate;
    private boolean flag = false;

    private ImageView imageWarnView;
    private TextView textWarnView;
    private String user_allergies = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

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

                        final String all = climate.getAllergy();
                        // search for user's allergies
                        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("clients");
                        mFirebaseDatabaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    Client client = postSnapshot.getValue(Client.class);
                                    if (client.getName().equals(mFirebaseUser.getDisplayName())) {
                                        String user_allergies = client.getAllergies();
                                        String food_allergies = all;

                                        // check if the user is allergic to this food
                                        if (food_allergies.contains(user_allergies) && user_allergies.equals("")==false) {
                                            imageWarnView = findViewById(R.id.foodAllergyImageView);
                                            textWarnView = findViewById(R.id.foodAllergyView);

                                            imageWarnView.setImageResource(R.drawable.icon_warning);
                                            textWarnView.setText("You are allergic to this");
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError firebaseError) {
                            }
                        });



                        TextView txt = (TextView) findViewById(R.id.textView);
                        txt.setText(foodName); //Float.toString(prot)


                        imageView = (ImageView) findViewById(R.id.foodImageView);
                        //imageview.setImageResource(climate.getPhoto());
                        Glide
                                .with(AddFood.this)
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

        // get current date
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        formattedDate = df.format(c.getTime());

        button = (Button) findViewById(R.id.add_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    quantity = Float.parseFloat(mGramEditText.getText().toString());

                    calculateData();
                    saveData();

                    // String strDouble = String.format("%.2f", 1.9999); System.out.println(strDouble); // print 2.00
                    Intent intent = new Intent(getBaseContext(), Diary.class);
                    intent.putExtra("DATE", formattedDate);
                    startActivity(intent);

                } catch(NumberFormatException e) {
                    EditText txt = (EditText) findViewById(R.id.gramEditText);
                    txt.setError("Wrong input !");
                }

            }
        });

        mGramEditText = (EditText) findViewById(R.id.gramEditText);
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

    /* round a float number to 2 decimal place*/
    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }


    public void saveData() {
        /* TO DO
            check if the child (date) exists in firebase
            if it does, add the data there otherwise create the child
        */

        // save the food's data to 'diary'
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Food food = new Food(foodName, quantity, calcCal, calcCarb, calcProt, calcFat);
        mFirebaseDatabaseReference.child("diary")
                                .child(mFirebaseUser.getDisplayName())
                                .child(formattedDate)
                                .push().setValue(food);
        mGramEditText.setText("");

        // save the date
        // first check if the date already exists
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("dates")
                .child(mFirebaseUser.getDisplayName());
        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.getValue(Date.class).getDate().equals(formattedDate) == true) {
                            flag = true;
                        }
                    }
                }
                // if the date doesn't exists, add it
                if (flag == false) {
                    mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
                    Date date = new Date(formattedDate);
                    mFirebaseDatabaseReference.child("dates")
                            .child(mFirebaseUser.getDisplayName())
                            .push().setValue(date);
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });

        //ref.child("Victor").setValue("setting custom key when pushing new data to firebase database");



    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


    // create right top the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_food_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.display_personal_data_menu:
                startActivity(new Intent(this, DisplayPersonalData.class));
                return true;
            case R.id.diary_menu:
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
