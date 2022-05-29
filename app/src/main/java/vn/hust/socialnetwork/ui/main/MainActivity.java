package vn.hust.socialnetwork.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import vn.hust.socialnetwork.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavController navController = Navigation.findNavController(this, R.id.main_nav_host_fragment);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);

        // disable auto change color of BottomNavigationView
        bottomNavigation.setItemIconTintList(null);

        // set navigation graph with bottom navigation
        NavigationUI.setupWithNavController(bottomNavigation, navController);
    }
}