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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;


public class Diary extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    // class for Recycler table
    // this is the equivalent for a diary_item
    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView foodNameView;
        TextView foodQuantityView;
        TextView foodCarbView;
        TextView foodCalView;
        TextView foodProtView;
        TextView foodFatView;
        TextView foodDateView;


        public FoodViewHolder(View v) {
            super(v);
            foodNameView = (TextView) itemView.findViewById(R.id.foodNameView);
            foodQuantityView = (TextView) itemView.findViewById(R.id.foodQuantityView);
            foodCarbView = (TextView) itemView.findViewById(R.id.foodCarbView);
            foodCalView = (TextView) itemView.findViewById(R.id.foodCalView);
            foodProtView = (TextView) itemView.findViewById(R.id.foodProtView);
            foodFatView = (TextView) itemView.findViewById(R.id.foodFatView);
            foodDateView = (TextView) itemView.findViewById(R.id.foodDateView);
        }
    }


    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "MainActivity";

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<Food, Diary.FoodViewHolder> mAdapter;
    private ProgressBar mProgressBar;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mRecyclerView;

    private Float cal = 0f;
    private Float carb = 0f;
    private Float prot = 0f;
    private Float fat = 0f;

    private Float calT = 0f;
    private Float carbT = 0f;
    private Float protT = 0f;
    private Float fatT = 0f;


    private Float aux;

    private String date;
    String n;
    String[] data_keys = new String[20];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        setContentView(R.layout.activity_diary);

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
            // if signed, get its name
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRecyclerView = (RecyclerView) findViewById(R.id.diaryRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true); //show rows from bottom

        /*
             This code initially adds all existing foods then
            listens for new child entries under the diary path in Firebase Realtime Database.
             It adds a new element to the UI for each message
         */
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        // New child entries
        SnapshotParser<Food> parser = new SnapshotParser<Food>() {
            @Override
            public Food parseSnapshot(DataSnapshot dataSnapshot) {
                Food food = dataSnapshot.getValue(Food.class);
                if (food != null) {
                    food.setId(dataSnapshot.getKey());
                }
                return food;
            }
        };

        // get the date for which to get and display the data
        // from the previous action
        date = getIntent().getStringExtra("DATE");
        //date = "01-01-2018";

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child("diary")
                                                                    .child(mFirebaseUser.getDisplayName())
                                                                    .child(date);

        // create a recycler
        FirebaseRecyclerOptions<Food> options =
                new FirebaseRecyclerOptions.Builder<Food>()
                        .setQuery(messagesRef, parser)
                        .build();

        mAdapter = new FirebaseRecyclerAdapter<Food, Diary.FoodViewHolder>(options) {
            @Override
            public Diary.FoodViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new Diary.FoodViewHolder(inflater.inflate(R.layout.item_diary, viewGroup, false));
            }

            /*
                uses the View Holder constructed in the onCreateViewHolder() method
                to populate the current row of the RecyclerView with data
             */
            @Override
            protected void onBindViewHolder(final Diary.FoodViewHolder viewHolder,
                                            int position,
                                            Food food) {

                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                // display the data
                // first make sure the data is not null
                // if not, display it
                if (food.getCarb() != null) {
                    viewHolder.foodCarbView.setText(Float.toString(food.getCarb()));
                    viewHolder.foodCarbView.setVisibility(TextView.VISIBLE);
                }
                if (food.getName() != null) {
                    viewHolder.foodNameView.setText(food.getName());
                }
                if (food.getProt() != null) {
                    viewHolder.foodProtView.setText(Float.toString(food.getProt()));
                    viewHolder.foodProtView.setVisibility(TextView.VISIBLE);
                }
                if (food.getCal() != null) {
                    viewHolder.foodCalView.setText(Float.toString(food.getCal()));
                    viewHolder.foodCalView.setVisibility(TextView.VISIBLE);
                }
                if (food.getFat() != null) {
                    viewHolder.foodFatView.setText(Float.toString(food.getFat()));
                }
                if (food.getQuantity() != null) {
                    viewHolder.foodQuantityView.setText(Float.toString(food.getQuantity()));
                }

                // display the date
                viewHolder.foodDateView.setText(date);
            }
        };

        // for the scroll
        // allways send scroll to the bottom of the list to show the newly added data
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added data.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);


        // update Total table
        // go to 'diary', to this user, to date 'date'
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("diary")
                                                                                    .child(mFirebaseUser.getDisplayName())
                                                                                    .child(date);
        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // before, initialise the values which will be used
                // to get the total macro which was consumed on the date 'date'
                float cal = 0f;
                float carb = 0f;
                float prot = 0f;
                float fat = 0f;
                // get the data from 'diary' for the date 'date' for this user
                // and sum it to the total
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Food food = postSnapshot.getValue(Food.class);
                    cal += food.getCal();
                    carb += food.getCarb();
                    prot += food.getProt();
                    fat += food.getFat();
                }

                // display the calculated total
                TextView foodCalView_2 = findViewById(R.id.foodCalView_2);
                TextView foodCarbView_2 = findViewById(R.id.foodCarbView_2);
                TextView foodProtView_2 = findViewById(R.id.foodProtView_2);
                TextView foodFatView_2 = findViewById(R.id.foodFatView_2);

                foodCalView_2.setText(Float.toString(cal));
                foodCarbView_2.setText(Float.toString(carb));
                foodProtView_2.setText(Float.toString(prot));
                foodFatView_2.setText(Float.toString(fat));
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });


        // Remained table
        // get user's necessary macro
        /*
            addListenerForSingleValueEvent()
          executes onDataChange method immediately and after executing that method once,
          it stops listening to the reference location it is attached to.
         */
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("clients");
        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            // when the data is changed get the total cal and macro of the client from
            // the table 'clients' and update the Remained table with it
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Client client = postSnapshot.getValue(Client.class);
                    if (client.getName().equals(mFirebaseUser.getDisplayName())) {
                        cal = client.getCal();
                        carb = client.getCarb();
                        prot = client.getProt();
                        fat = client.getFat();

                        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference()
                                .child("diary")
                                .child(mFirebaseUser.getDisplayName())
                                .child(date);;
                        mFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    // get the total macros from diary, for today
                                    Food food = postSnapshot.getValue(Food.class);
                                    calT += food.getCal();
                                    carbT += food.getCarb();
                                    protT += food.getProt();
                                    fatT += food.getFat();
                                }

                                // update Remained table
                                TextView foodCalView_3 = findViewById(R.id.foodCalView_3);
                                TextView foodCarbView_3 = findViewById(R.id.foodCarbView_3);
                                TextView foodProtView_3 = findViewById(R.id.foodProtView_3);
                                TextView foodFatView_3 = findViewById(R.id.foodFatView_3);

                                aux = cal - calT;
                                foodCalView_3.setText(String.format("%.2f",aux));
                                // if the if daily calories are exceeded
                                // display the number with green
                                if (aux < 0) {
                                    foodCalView_3.setTextColor(0xFF00FF00); //this is green color
                                }

                                aux = carb - carbT;
                                foodCarbView_3.setText(String.format("%.2f",aux));
                                // if the if daily carbs are exceeded
                                // display the number with green
                                if (aux < 0) {
                                    foodCarbView_3.setTextColor(0xFF00FF00); //this is green color
                                }

                                aux = prot - protT;
                                foodProtView_3.setText(String.format("%.2f",aux));
                                if (aux < 0) {
                                    foodProtView_3.setTextColor(0xFF00FF00); //this is green color
                                }

                                aux = fat - fatT;
                                foodFatView_3.setText(String.format("%.2f",aux));
                                if (aux < 0) {
                                    foodFatView_3.setTextColor(0xFF00FF00); //this is green color
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError firebaseError) {
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }


    // if the connection fails show a message in logcat
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }


    // create right top the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.diary_menu, menu);
        return true;
    }

    // the actions to be taken for each selected item from the menu
    // each one will start a specific activity
    // which will lead to a specific page from the application
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.display_personal_data_menu:
                startActivity(new Intent(this, DisplayPersonalData.class));
                return true;
            case R.id.another_menu:
                startActivity(new Intent(this, ChooseDate.class));
                return true;
            case R.id.food_menu:
                startActivity(new Intent(this, Foods.class));
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

    // action to be taken on pause
    @Override
    public void onPause() {
        mAdapter.stopListening();
        super.onPause();
    }

    // action to be taken on resume
    @Override
    public void onResume() {
        super.onResume();
        mAdapter.startListening();
    }

    // action to be taken on destroy
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
