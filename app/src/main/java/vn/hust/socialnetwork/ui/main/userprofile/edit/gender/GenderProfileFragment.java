package vn.hust.socialnetwork.ui.main.userprofile.edit.gender;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import vn.hust.socialnetwork.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GenderProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenderProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GenderProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GenderProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GenderProfileFragment newInstance(String param1, String param2) {
        GenderProfileFragment fragment = new GenderProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gender_profile, container, false);

        AppCompatSpinner spinner = view.findViewById(R.id.et_gender);
        String[] genders = getResources().getStringArray(R.array.gender);
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), R.layout.simple_spinner_item, genders);
        spinner.setAdapter(adapter);

        return view;
    }
}