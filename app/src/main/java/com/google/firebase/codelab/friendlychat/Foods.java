package com.google.firebase.codelab.friendlychat;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Map;


public class Foods extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    // class for Recycler table
    // this is the equivalent for a diary_item
    // on which is added a listener (View.OnClickListener)
    public static class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView foodNameView;
        ImageView foodImageView;
        TextView foodCarbView;
        TextView foodCalView;
        TextView foodProtView;
        TextView foodFatView;

        View v;

        public IMyViewHolderClicks mListener;

        public FoodViewHolder(View v, IMyViewHolderClicks listener) {
            super(v);
            foodNameView = (TextView) itemView.findViewById(R.id.foodNameView);
            foodImageView = (ImageView) itemView.findViewById(R.id.foodImageView);
            foodCarbView = (TextView) itemView.findViewById(R.id.foodCarbView);
            foodCalView = (TextView) itemView.findViewById(R.id.foodCalView);
            foodProtView = (TextView) itemView.findViewById(R.id.foodProtView);
            foodFatView = (TextView) itemView.findViewById(R.id.foodFatView);

            // get the listener, set a listener on the image and on the view v
            mListener = listener;
            foodImageView.setOnClickListener(this);
            v.setOnClickListener(this);

            this.v = v;
        }

        // actions to be taken on a click event on the view v or the image ImageView
        @Override
        public void onClick(View v) {
            if (v instanceof ImageView){
                mListener.onTomato((ImageView)v);
            } else {
                mListener.onPotato(v);
            }
        }

        public static interface IMyViewHolderClicks {
            public void onPotato(View caller);
            public void onTomato(ImageView callerImage);
        }
    }


    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "Foods";
    public static final String MESSAGES_CHILD = "foods";

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<Food, Foods.FoodViewHolder> mAdapter;
    private ProgressBar mProgressBar;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foods);

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
        mRecyclerView = (RecyclerView) findViewById(R.id.foodRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true); //show rows from bottom

        /*
             This code initially adds all existing messages then
            listens for new child entries under the messages path in your Firebase Realtime Database.
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

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD);

        FirebaseRecyclerOptions<Food> options =
                new FirebaseRecyclerOptions.Builder<Food>()
                        .setQuery(messagesRef, parser)
                        .build();

        /*
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
           View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_layout, parent, false);

           MyAdapter.ViewHolder vh = new ViewHolder(v, new MyAdapter.ViewHolder.IMyViewHolderClicks() {
               public void onPotato(View caller) { Log.d("VEGETABLES", "Poh-tah-tos"); };
               public void onTomato(ImageView callerImage) { Log.d("VEGETABLES", "To-m8-tohs"); }
            });
            return vh;
        }
         */

        // display the Recycler table
        // with the actions to be taken on an event
        mAdapter = new FirebaseRecyclerAdapter<Food, Foods.FoodViewHolder>(options) {
            @Override
            public Foods.FoodViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
               View vv = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_food, viewGroup, false);

                Foods.FoodViewHolder vh = new FoodViewHolder(vv, new Foods.FoodViewHolder.IMyViewHolderClicks() {
                    // if the view is clicked
                    // call teh method addFood which
                    // will take care of the starting of a new action
                    // which will lead to the AddFood page
                    public void onPotato(View caller) {
                         addFood(String.valueOf(caller.getTag()));
                    };
                    public void onTomato(ImageView callerImage) {
                        Log.d("VEGETABLES", "To-m8-tohs");
                    }
                });
                return vh;

                /*LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new Foods.FoodViewHolder(inflater.inflate(R.layout.item_food, viewGroup, false));*/
            }

            /*
                uses the View Holder constructed in the onCreateViewHolder() method
                to populate the current row of the RecyclerView with data
             */
            /*
             foodNameView   foodCalView     foodProtView        foodFatView
            foodImageView
            foodCarbView
             */
            @Override
            protected void onBindViewHolder(final Foods.FoodViewHolder viewHolder,
                                            int position,
                                            Food food) {

                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                // first check if the data is different from nil
                // if it is display it
                if (food.getCarb() != null) {
                    viewHolder.foodCarbView.setText(Float.toString(food.getCarb()));
                    viewHolder.foodCarbView.setVisibility(TextView.VISIBLE);
                    //viewHolder.foodImageView.setVisibility(ImageView.GONE); makes the image to disapear
                }
                if (food.getName() != null) {
                    viewHolder.foodNameView.setText(food.getName());

                    viewHolder.v.setTag(food.getName());

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

                // get the Google account photo of the user
                // and display it
/*
                viewHolder.foodImageView.setImageDrawable(ContextCompat.getDrawable(Foods.this,
                        R.drawable.ic_account_circle_black_36dp)); */
                /* if ther eisn't a photo put the default photo */
                if (food.getPhoto() == null) {
                    viewHolder.foodImageView.setImageDrawable(ContextCompat.getDrawable(Foods.this,
                            R.drawable.ic_account_circle_black_36dp));
                } else { // if the user doesn't have a picture display the default one
                    Glide.with(Foods.this)
                            .load(food.getPhoto())
                            .into(viewHolder.foodImageView);
                }
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
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);


    }

    // starting of a new action
    // which will lead to the AddFood page
    // and send as data the name of the food which has to be added
    public void addFood(String foodName){
        Intent intent = new Intent(getBaseContext(), AddFood.class);
        intent.putExtra("EXTRA_SESSION_ID", foodName);
        startActivity(intent);

    };

    // if the connection fails show a message in logcat
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

    // the actions to be taken for each selected item from the menu
    // each one will start a specific activity
    // which will lead to a specific page from the application
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_food:
                startActivity(new Intent(this, DeleteFood.class));
                return true;
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
