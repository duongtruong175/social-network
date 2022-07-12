package vn.hust.socialnetwork.ui.main.userprofile.edit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.models.user.User;

public class EditUserProfileActivity extends AppCompatActivity {

    private User user;
    private UserProfileViewModel userProfileViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        // get value
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getParcelable("user");
        } else {
            user = null;
            Toast.makeText(EditUserProfileActivity.this, R.string.error_init_data_edit_user_profile, Toast.LENGTH_SHORT).show();
        }

        // set value to view model
        userProfileViewModel = new ViewModelProvider(EditUserProfileActivity.this).get(UserProfileViewModel.class);
        userProfileViewModel.setUser(user);
        userProfileViewModel.getUser().observe(EditUserProfileActivity.this, newUser -> {
            user = newUser;
        });
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("updated_user", user);
        setResult(Activity.RESULT_OK, returnIntent);
        super.finish();
    }
}