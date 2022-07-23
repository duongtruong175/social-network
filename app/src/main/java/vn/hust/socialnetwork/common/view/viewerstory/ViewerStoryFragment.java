package vn.hust.socialnetwork.common.view.viewerstory;

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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.view.viewerstory.adapters.OnViewerStoryListener;
import vn.hust.socialnetwork.common.view.viewerstory.adapters.ViewerStoryAdapter;
import vn.hust.socialnetwork.models.BaseResponse;
import vn.hust.socialnetwork.models.story.ViewerStory;
import vn.hust.socialnetwork.network.ApiClient;
import vn.hust.socialnetwork.network.StoryService;
import vn.hust.socialnetwork.ui.userdetail.UserDetailActivity;

public class ViewerStoryFragment extends BottomSheetDialogFragment {

    // id and type: story
    private int storyId;

    private StoryService storyService;

    private ViewerStoryAdapter viewerStoryAdapter;
    private List<ViewerStory> viewers;

    private RecyclerView rvViewerStory;
    private ConstraintLayout lEmpty, lError;
    private ShimmerFrameLayout lShimmer;
    private AppCompatTextView tvViewerStory, tvClose;

    private OnBottomSheetDismiss onBottomSheetDismiss;

    public ViewerStoryFragment() {
        // Required empty public constructor
    }

    public ViewerStoryFragment(int storyId, OnBottomSheetDismiss onBottomSheetDismiss) {
        this.storyId = storyId;
        this.onBottomSheetDismiss = onBottomSheetDismiss;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        View view = inflater.inflate(R.layout.fragment_viewer_story, container, false);

        // api
        storyService = ApiClient.getClient().create(StoryService.class);

        // binding
        tvViewerStory = view.findViewById(R.id.tv_viewer_story);
        tvClose = view.findViewById(R.id.tv_close);
        rvViewerStory = view.findViewById(R.id.rv_viewer_story);
        lEmpty = view.findViewById(R.id.l_empty);
        lError = view.findViewById(R.id.l_error);
        lShimmer = view.findViewById(R.id.l_shimmer);

        // init
        viewers = new ArrayList<>();
        viewerStoryAdapter = new ViewerStoryAdapter(getContext(), viewers, new OnViewerStoryListener() {
            @Override
            public void onItemClick(int position) {
                // open user detail
                Intent intent = new Intent(getActivity(), UserDetailActivity.class);
                intent.putExtra("user_id", viewers.get(position).getUser().getId());
                startActivity(intent);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvViewerStory.setLayoutManager(layoutManager);
        rvViewerStory.setAdapter(viewerStoryAdapter);

        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

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

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // use interface to callback method
        onBottomSheetDismiss.onDialogDismiss();
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
        lError.setVisibility(View.GONE);
        lShimmer.setVisibility(View.VISIBLE);
        lShimmer.startShimmer();
        Call<BaseResponse<List<ViewerStory>>> call = storyService.getViewerStory(storyId);
        call.enqueue(new Callback<BaseResponse<List<ViewerStory>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<ViewerStory>>> call, Response<BaseResponse<List<ViewerStory>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<ViewerStory>> res = response.body();
                    viewers.clear();
                    viewers.addAll(res.getData());
                    viewerStoryAdapter.notifyDataSetChanged();
                    tvViewerStory.setText(viewers.size() + " người xem");
                    if (viewers.size() == 0) {
                        lEmpty.setVisibility(View.VISIBLE);
                    } else {
                        lEmpty.setVisibility(View.GONE);
                    }
                }
                lShimmer.stopShimmer();
                lShimmer.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<BaseResponse<List<ViewerStory>>> call, Throwable t) {
                // error network (no internet connection, socket timeout, unknown host, ...)
                // error serializing/deserializing the data
                call.cancel();
                lError.setVisibility(View.VISIBLE);
                lShimmer.stopShimmer();
                lShimmer.setVisibility(View.GONE);
            }
        });
    }
}