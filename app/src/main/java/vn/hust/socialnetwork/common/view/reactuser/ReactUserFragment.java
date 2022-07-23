package vn.hust.socialnetwork.common.view.reactuser;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.view.reactuser.adapters.OnReactUserListener;
import vn.hust.socialnetwork.common.view.reactuser.adapters.ReactUserAdapter;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.post.ReactCount;
import vn.hust.socialnetwork.models.post.ReactUser;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.CommentService;
import vn.hust.socialnetwork.network.PostService;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;

public class ReactUserFragment extends BottomSheetDialogFragment {

    private PostService postService;
    private CommentService commentService;

    private static final String OBJECT_ID = "object_id";
    private static final String OBJECT_TYPE = "object_type";

    // id and type: post or comment
    private int objectId;
    private String objectType;

    private ReactUserAdapter reactUserAdapter;
    private List<ReactUser> reactUsers, viewReactUsers;
    private ReactCount reactCount;

    private TabLayout tbReact;
    private RecyclerView rvReactUser;
    private ConstraintLayout lEmpty, lError;
    private ShimmerFrameLayout lShimmer;
    private AppCompatTextView tvEmptyTextData;

    public static ReactUserFragment newInstance(int objectId, String objectType) {
        ReactUserFragment fragment = new ReactUserFragment();
        Bundle args = new Bundle();
        args.putInt(OBJECT_ID, objectId);
        args.putString(OBJECT_TYPE, objectType);
        fragment.setArguments(args);
        return fragment;
    }

    public ReactUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            objectId = getArguments().getInt(OBJECT_ID);
            objectType = getArguments().getString(OBJECT_TYPE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                setupFullHeight(bottomSheetDialog);
            }
        });
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_react_user, container, false);

        // api
        postService = ApiClient.getClient().create(PostService.class);
        commentService = ApiClient.getClient().create(CommentService.class);

        // binding
        tbReact = view.findViewById(R.id.tb_react);
        rvReactUser = view.findViewById(R.id.rv_react_user);
        lEmpty = view.findViewById(R.id.l_empty);
        lError = view.findViewById(R.id.l_error);
        lShimmer = view.findViewById(R.id.l_shimmer);
        tvEmptyTextData = view.findViewById(R.id.tv_empty_text_data);

        // init
        reactUsers = new ArrayList<>();
        viewReactUsers = new ArrayList<>();
        reactUserAdapter = new ReactUserAdapter(getContext(), viewReactUsers, new OnReactUserListener() {
            @Override
            public void onItemClick(int position) {
                // open user detail
                Intent intent = new Intent(getActivity(), UserDetailActivity.class);
                intent.putExtra("user_id", viewReactUsers.get(position).getUser().getId());
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvReactUser.setLayoutManager(layoutManager);
        rvReactUser.setAdapter(reactUserAdapter);

        // get data
        getData();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View bottomSheet = (View) view.getParent();
        bottomSheet.setBackgroundTintMode(PorterDuff.Mode.CLEAR);
        bottomSheet.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        bottomSheet.setBackgroundColor(Color.TRANSPARENT);
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog) {
        // When using AndroidX the resource can be found at com.google.android.material.R.id.design_bottom_sheet
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = WindowManager.LayoutParams.MATCH_PARENT;
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        // if want dialog display fullscreen when opening, uncomment the line below
        //behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void getData() {
        if (objectType.equals("post")) {
            // call api for post
            Call<BaseResponse<ReactCount>> call = postService.getTotalReacts(objectId);
            call.enqueue(new Callback<BaseResponse<ReactCount>>() {
                @Override
                public void onResponse(Call<BaseResponse<ReactCount>> call, Response<BaseResponse<ReactCount>> response) {
                    if (response.isSuccessful()) {
                        BaseResponse<ReactCount> res = response.body();
                        reactCount = res.getData();
                        if (reactCount != null) {
                            // after loading react count success, init tab layout and load list react user
                            initReactTabs();
                            // get data for recycle view
                            getReactUserPost();
                        }
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<ReactCount>> call, Throwable t) {
                    // error network (no internet connection, socket timeout, unknown host, ...)
                    // error serializing/deserializing the data
                    call.cancel();
                    lError.setVisibility(View.VISIBLE);
                }
            });
        } else if (objectType.equals("comment")) {
            // call api for comment
            Call<BaseResponse<ReactCount>> call = commentService.getTotalReacts(objectId);
            call.enqueue(new Callback<BaseResponse<ReactCount>>() {
                @Override
                public void onResponse(Call<BaseResponse<ReactCount>> call, Response<BaseResponse<ReactCount>> response) {
                    if (response.isSuccessful()) {
                        BaseResponse<ReactCount> res = response.body();
                        reactCount = res.getData();
                        if (reactCount != null) {
                            // after loading react count success, init tab layout and load list react user
                            initReactTabs();
                            // get data for recycle view
                            getReactUserComment();
                        }
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<ReactCount>> call, Throwable t) {
                    // error network (no internet connection, socket timeout, unknown host, ...)
                    // error serializing/deserializing the data
                    call.cancel();
                    lError.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void getReactUserPost() {
        lError.setVisibility(View.GONE);
        lShimmer.setVisibility(View.VISIBLE);
        lShimmer.startShimmer();
        Call<BaseResponse<List<ReactUser>>> call = postService.getListReactUsers(objectId);
        call.enqueue(new Callback<BaseResponse<List<ReactUser>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<ReactUser>>> call, Response<BaseResponse<List<ReactUser>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<ReactUser>> res = response.body();
                    reactUsers.addAll(res.getData());
                    updateDataRecycleView(0);
                    if (reactUsers.size() == 0) {
                        lEmpty.setVisibility(View.VISIBLE);
                        tvEmptyTextData.setText(R.string.text_empty_react_user_in_post);
                    }
                }
                lShimmer.stopShimmer();
                lShimmer.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<ReactUser>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                lShimmer.stopShimmer();
                lShimmer.setVisibility(View.GONE);
                lError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getReactUserComment() {
        lError.setVisibility(View.GONE);
        lShimmer.setVisibility(View.VISIBLE);
        lShimmer.startShimmer();
        Call<BaseResponse<List<ReactUser>>> call = commentService.getListReactUsers(objectId);
        call.enqueue(new Callback<BaseResponse<List<ReactUser>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<ReactUser>>> call, Response<BaseResponse<List<ReactUser>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<ReactUser>> res = response.body();
                    reactUsers.addAll(res.getData());
                    updateDataRecycleView(0);
                    if (reactUsers.size() == 0) {
                        lEmpty.setVisibility(View.VISIBLE);
                        tvEmptyTextData.setText(R.string.text_empty_react_user_in_comment);
                    }
                }
                lShimmer.stopShimmer();
                lShimmer.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<ReactUser>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                lShimmer.stopShimmer();
                lShimmer.setVisibility(View.GONE);
                lError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initReactTabs() {
        int reactType1 = reactCount.getReactType1();
        int reactType2 = reactCount.getReactType2();
        int reactType3 = reactCount.getReactType3();
        int reactType4 = reactCount.getReactType4();
        int reactType5 = reactCount.getReactType5();
        int reactType6 = reactCount.getReactType6();
        int totalReact = reactType1 + reactType2 + reactType3 + reactType4 + reactType5 + reactType6;

        // init Tab Layout
        tbReact.addTab(tbReact.newTab().setTag(0).setCustomView(initTabView(false, totalReact, 0)));
        List<Pair<Integer, Integer>> reactList = new ArrayList<>();
        reactList.add(new Pair<>(reactType1, 1));
        reactList.add(new Pair<>(reactType2, 2));
        reactList.add(new Pair<>(reactType3, 3));
        reactList.add(new Pair<>(reactType4, 4));
        reactList.add(new Pair<>(reactType5, 5));
        reactList.add(new Pair<>(reactType6, 6));
        reactList.sort(new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                if (o2.first > o1.first) {
                    return 1;
                } else if (o2.first.equals(o1.first)) {
                    return 0;
                }
                return -1;
            }
        });
        for (int i = 0; i < reactList.size(); i++) {
            Pair<Integer, Integer> pair = reactList.get(i);
            if (pair.first > 0) {
                tbReact.addTab(tbReact.newTab().setTag(pair.second).setCustomView(initTabView(true, pair.first, pair.second)));
            }
        }

        tbReact.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (reactUsers.size() > 0) {
                    int type = (int) tab.getTag();
                    tbReact.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), getReactTabColor(type)));
                    updateDataRecycleView(type);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void updateDataRecycleView(int type) {
        if (type == 0) {
            // view all
            viewReactUsers.clear();
            viewReactUsers.addAll(reactUsers);
        } else {
            viewReactUsers.clear();
            for (int i = 0; i < reactUsers.size(); i++) {
                ReactUser reactUser = reactUsers.get(i);
                if (reactUser.getType() == type) {
                    viewReactUsers.add(reactUser);
                }
            }
        }
        reactUserAdapter.notifyDataSetChanged();
    }

    private View initTabView(boolean isViewIcon, int reactTypeCount, int type) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_tab, null);
        AppCompatImageView ivReactIcon = view.findViewById(R.id.iv_react_icon);
        AppCompatTextView tvReactCount = view.findViewById(R.id.tv_react_count);
        if (isViewIcon) {
            ivReactIcon.setVisibility(View.VISIBLE);
            ivReactIcon.setImageResource(getReactTabIcon(type));
            tvReactCount.setText(String.valueOf(reactTypeCount));
        } else {
            ivReactIcon.setVisibility(View.GONE);
            tvReactCount.setText(getString(R.string.all) + " " + reactTypeCount);
        }
        tvReactCount.setTextColor(ContextCompat.getColor(requireContext(), getReactTabColor(type)));
        return view;
    }

    private int getReactTabIcon(int type) {
        switch (type) {
            case 1:
                return R.drawable.ic_like;
            case 2:
                return R.drawable.ic_heart;
            case 3:
                return R.drawable.ic_haha;
            case 4:
                return R.drawable.ic_wow;
            case 5:
                return R.drawable.ic_sad;
            case 6:
                return R.drawable.ic_angry;
        }
        return R.drawable.ic_like;
    }

    private int getReactTabColor(int type) {
        switch (type) {
            case 1:
                return R.color.color_text_like;
            case 2:
                return R.color.color_text_heart;
            case 3:
                return R.color.color_text_haha;
            case 4:
                return R.color.color_text_wow;
            case 5:
                return R.color.color_text_sad;
            case 6:
                return R.color.color_text_angry;
        }
        return R.color.color_text_like;
    }
}