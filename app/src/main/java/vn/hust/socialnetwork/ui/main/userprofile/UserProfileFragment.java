package vn.hust.socialnetwork.ui.main.userprofile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.google.android.material.appbar.AppBarLayout;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.ui.main.userprofile.edit.EditUserProfileActivity;

public class UserProfileFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private AppBarLayout appBarLayout;
    private AppCompatTextView tvUserNameToolbar;
    private CircleImageView civImageAvatarToolbar;
    private AppCompatImageView ivMenuToolbar, ivImageCover;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        swipeRefreshLayout = view.findViewById(R.id.l_swipe_refresh);
        appBarLayout = view.findViewById(R.id.ab_main_user_profile);

        tvUserNameToolbar = view.findViewById(R.id.tv_user_name_toolbar);
        civImageAvatarToolbar = view.findViewById(R.id.civ_image_avatar_toolbar);
        ivMenuToolbar = view.findViewById(R.id.iv_menu_toolbar);

        ivImageCover = view.findViewById(R.id.iv_image_cover);

        // show/hide toolbar when scroll
        // only refresh when at top screen (when appbar full)
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    // when show toolbar
                    ivMenuToolbar.setColorFilter(Color.BLACK);
                    civImageAvatarToolbar.setVisibility(View.VISIBLE);
                    tvUserNameToolbar.setVisibility(View.VISIBLE);
                } else {
                    // hide toolbar
                    ivMenuToolbar.setColorFilter(Color.WHITE);
                    civImageAvatarToolbar.setVisibility(View.GONE);
                    tvUserNameToolbar.setVisibility(View.GONE);
                }
                if (!swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setEnabled(verticalOffset == 0);
                }
            }
        });

        // navigate to edit user profile
        view.findViewById(R.id.l_edit_user_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditUserProfileActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}