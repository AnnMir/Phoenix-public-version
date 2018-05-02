package nsu.fit.g14201.marchenko.phoenix.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.connection.GoogleUserConnection;
import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.connection.UserConnection;
import nsu.fit.g14201.marchenko.phoenix.context.Context;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingContract;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingFragment;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingPresenter;
import nsu.fit.g14201.marchenko.phoenix.registration.RegistrationActivity;
import nsu.fit.g14201.marchenko.phoenix.utils.ActivityUtils;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnCompleteListener<Void> {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Context context;

    private RecordingContract.Presenter recordingPresenter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureToolbarAndNavigationView();
        try {
            context = Context.createContext(this);
            configureRecordingBlock();
        } catch (SignInException e) {
            showSnack(getString(R.string.some_error));
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    protected void onStart() {
        recordingPresenter.start();
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.nav_records_management:
                break;
            case R.id.nav_settings:
                break;
            case R.id.nav_logout:
                signOut();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            Log.d(App.getTag(), "Signed out");
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        } else {
            showSnack(getString(R.string.sign_out_failure));
            Exception exception = task.getException();
            if (exception != null) {
                Log.e(App.getTag(), exception.getMessage());
            }
        }
    }

    private void configureToolbarAndNavigationView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void configureRecordingBlock() {
        RecordingFragment recordingFragment =
                (RecordingFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.main_content);

        if (recordingFragment == null) {
            recordingFragment = RecordingFragment.newInstance();

            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(),
                    recordingFragment,
                    R.id.main_content,
                    null
            );
        }
        recordingPresenter = new RecordingPresenter(getApplicationContext(), recordingFragment);
        recordingPresenter.setContext(context);
    }

    private void signOut() {
        UserConnection userConnection = GoogleUserConnection.getInstance();
        if (!userConnection.isSignedIn(this)) {
            showSnack(getString(R.string.already_signed_out));
            return;
        }
        try {
            userConnection.signOut(this, this);
        } catch (SignInException e) {
            showSnack(getString(R.string.sign_out_failure));
            Log.e(App.getTag(), e.getMessage());
        }
    }
}

// TODO: Configure backstack: https://developer.android.com/guide/components/activities/tasks-and-back-stack.html
