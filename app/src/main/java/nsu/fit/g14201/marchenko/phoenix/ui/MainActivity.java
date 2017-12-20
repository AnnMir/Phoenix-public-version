package nsu.fit.g14201.marchenko.phoenix.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.coordination.Coordinator;
import nsu.fit.g14201.marchenko.phoenix.coordination.SuperiorActivity;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingContract;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingFragment;
import nsu.fit.g14201.marchenko.phoenix.recording.RecordingPresenter;
import nsu.fit.g14201.marchenko.phoenix.utils.ActivityUtils;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, SuperiorActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    private Coordinator coordinator;
    private RecordingContract.Presenter recordingPresenter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureToolbarAndNavigationView();
        coordinator = getIntent().getParcelableExtra(App.getExtraCoordinator());
        configureRecordingBlock();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        toggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();

        recordingPresenter.start();
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
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
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
                    R.id.main_content);
        }

        recordingFragment.setSuperiorActivity(this);

        recordingPresenter = new RecordingPresenter(getApplicationContext(), recordingFragment);
    }

    @Override
    public void goToNextView() {

    }
}
