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

import java.util.ArrayList;
import java.util.Map;


public class ChooseDate extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView dateView;
        View v;

        public IMyViewHolderClicks mListener;

        public FoodViewHolder(View v, IMyViewHolderClicks listener) {
            super(v);
            dateView = (TextView) itemView.findViewById(R.id.dateView);

            mListener = listener;
            dateView.setOnClickListener(this);
            v.setOnClickListener(this);

            this.v = v;
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ImageView){
                mListener.onTomato((TextView)v);
            } else {
                mListener.onPotato(v);
            }
        }

        public static interface IMyViewHolderClicks {
            public void onPotato(View caller);
            public void onTomato(TextView callerImage);
        }
    }


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
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<Date, ChooseDate.FoodViewHolder> mAdapter;
    private ProgressBar mProgressBar;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_date);

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
        SnapshotParser<Date> parser = new SnapshotParser<Date>() {
            @Override
            public Date parseSnapshot(DataSnapshot dataSnapshot) {
                Date date = dataSnapshot.getValue(Date.class);
                if (date != null) {
                    date.setId(dataSnapshot.getKey());
                }
                return date;
            }
        };

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child("dates").child(mFirebaseUser.getDisplayName());
        FirebaseRecyclerOptions<Date> options =
                new FirebaseRecyclerOptions.Builder<Date>()
                        .setQuery(messagesRef, parser)
                        .build();

        mAdapter = new FirebaseRecyclerAdapter<Date, ChooseDate.FoodViewHolder>(options) {
            @Override
            public ChooseDate.FoodViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View vv = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_choose_date, viewGroup, false);

                ChooseDate.FoodViewHolder vh = new FoodViewHolder(vv, new ChooseDate.FoodViewHolder.IMyViewHolderClicks() {
                    public void onPotato(View caller) {
                        addFood(String.valueOf(caller.getTag()));
                    };
                    public void onTomato(TextView callerImage) {
                        addFood(String.valueOf(callerImage.getTag()));
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
            @Override
            protected void onBindViewHolder(final ChooseDate.FoodViewHolder viewHolder,
                                            int position,
                                            Date date) {

                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (date.getDate() != null) {
                    viewHolder.dateView.setText(date.getDate());
                    viewHolder.dateView.setVisibility(TextView.VISIBLE);

                    viewHolder.v.setTag(date.getDate());
                }
            }
        };

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

    public void addFood(String date){
        Intent intent = new Intent(getBaseContext(), Diary.class);
        intent.putExtra("DATE", date);
        startActivity(intent);

    };


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


    @Override
    public void onPause() {
        mAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
