package vn.hust.socialnetwork.ui.main.group;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.ui.groupcreator.GroupCreatorActivity;

public class GroupFragment extends Fragment {

    AppCompatTextView tvAddGroup;

    public GroupFragment() {
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
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        tvAddGroup = view.findViewById(R.id.tv_add_group);

        tvAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GroupCreatorActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}