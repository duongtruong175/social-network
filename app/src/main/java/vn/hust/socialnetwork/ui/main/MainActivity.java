package vn.hust.socialnetwork.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.hawk.Hawk;

import java.util.HashMap;
import java.util.Map;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.ui.main.chat.ChatFragment;
import vn.hust.socialnetwork.ui.main.feed.FeedFragment;
import vn.hust.socialnetwork.ui.main.group.GroupFragment;
import vn.hust.socialnetwork.ui.main.notification.NotificationFragment;
import vn.hust.socialnetwork.ui.main.userprofile.UserProfileFragment;
import vn.hust.socialnetwork.utils.AppSharedPreferences;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private FeedFragment feedFragment;
    private GroupFragment groupFragment;
    private ChatFragment chatFragment;
    private NotificationFragment notificationFragment;
    private UserProfileFragment userProfileFragment;
    private FragmentManager fragmentManager;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_main);

        // binding
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // disable auto change color of BottomNavigationView
        bottomNavigation.setItemIconTintList(null);

        String fcmToken = Hawk.get(AppSharedPreferences.FCM_TOKEN, "");
        if (!fcmToken.isEmpty()) {
            String userId = String.valueOf(Hawk.get(AppSharedPreferences.LOGGED_IN_USER_ID_KEY, 0));
            Map<String, String> values = new HashMap<>();
            values.put("token", fcmToken);
            FirebaseDatabase.getInstance().getReference("fcmTokens").child(userId).setValue(values);
        }

        // set navigation graph with bottom navigation
        // NavController navController = Navigation.findNavController(this, R.id.main_nav_host_fragment);
        // NavigationUI.setupWithNavController(bottomNavigation, navController);

        bottomNavigation.setOnItemSelectedListener(onItemSelectedListener);
        bottomNavigation.setOnItemReselectedListener(onItemReselectedListener);

        // init
        fragmentManager = getSupportFragmentManager();
        feedFragment = new FeedFragment();
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, feedFragment, "feedFragment")
                .commit();
        activeFragment = feedFragment;
    }

    @Override
    public void finish() {
        if (!bottomNavigation.getMenu().findItem(R.id.feedFragment).isChecked()) {
            bottomNavigation.setSelectedItemId(R.id.feedFragment);
        } else {
            super.finish();
        }
    }

    private NavigationBarView.OnItemSelectedListener onItemSelectedListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.feedFragment) {
                if (feedFragment == null) {
                    feedFragment = new FeedFragment();
                    fragmentManager.beginTransaction()
                            .hide(activeFragment)
                            .add(R.id.fragment_container, feedFragment, "feedFragment")
                            .commit();
                } else {
                    fragmentManager.beginTransaction()
                            .hide(activeFragment)
                            .show(feedFragment)
                            .commit();
                }
                activeFragment = feedFragment;
            } else if (item.getItemId() == R.id.groupFragment) {
                if (groupFragment == null) {
                    groupFragment = new GroupFragment();
                    fragmentManager.beginTransaction()
                            .hide(activeFragment)
                            .add(R.id.fragment_container, groupFragment, "groupFragment")
                            .commit();
                } else {
                    fragmentManager.beginTransaction()
                            .hide(activeFragment)
                            .show(groupFragment)
                            .commit();
                }
                activeFragment = groupFragment;
            } else if (item.getItemId() == R.id.chatFragment) {
                if (chatFragment == null) {
                    chatFragment = new ChatFragment();
                    fragmentManager.beginTransaction()
                            .hide(activeFragment)
                            .add(R.id.fragment_container, chatFragment, "chatFragment")
                            .commit();
                } else {
                    fragmentManager.beginTransaction()
                            .hide(activeFragment)
                            .show(chatFragment)
                            .commit();
                }
                activeFragment = chatFragment;
            } else if (item.getItemId() == R.id.notificationFragment) {
                if (notificationFragment == null) {
                    notificationFragment = new NotificationFragment();
                    fragmentManager.beginTransaction()
                            .hide(activeFragment)
                            .add(R.id.fragment_container, notificationFragment, "notificationFragment")
                            .commit();
                } else {
                    fragmentManager.beginTransaction()
                            .hide(activeFragment)
                            .show(notificationFragment)
                            .commit();
                }
                activeFragment = notificationFragment;
            } else if (item.getItemId() == R.id.userProfileFragment) {
                if (userProfileFragment == null) {
                    userProfileFragment = new UserProfileFragment();
                    fragmentManager.beginTransaction()
                            .hide(activeFragment)
                            .add(R.id.fragment_container, userProfileFragment, "userProfileFragment")
                            .commit();
                } else {
                    fragmentManager.beginTransaction()
                            .hide(activeFragment)
                            .show(userProfileFragment)
                            .commit();
                }
                activeFragment = userProfileFragment;
            }
            return true;
        }
    };

    // reselected
    private NavigationBarView.OnItemReselectedListener onItemReselectedListener = new NavigationBarView.OnItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.feedFragment) {
                // reload
            } else if (item.getItemId() == R.id.groupFragment) {
                // reload
            } else if (item.getItemId() == R.id.chatFragment) {
                // nothing
            } else if (item.getItemId() == R.id.notificationFragment) {
                // nothing
            } else if (item.getItemId() == R.id.userProfileFragment) {
                // scroll to top
            }
        }
    };
}