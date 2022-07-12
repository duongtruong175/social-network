package vn.hust.socialnetwork.ui.main.userprofile.edit.main;

import static vn.hust.socialnetwork.utils.StringExtension.checkValidValueString;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import vn.hust.socialnetwork.R;
import vn.hust.socialnetwork.common.style.CustomTypefaceSpan;
import vn.hust.socialnetwork.models.user.User;
import vn.hust.socialnetwork.ui.main.userprofile.edit.UserProfileViewModel;
import vn.hust.socialnetwork.utils.TimeExtension;

public class MainEditProfileFragment extends Fragment {

    private User user;
    private UserProfileViewModel userProfileViewModel;

    private AppCompatImageView ivToolbarBack;
    private AppCompatTextView tvToolbarTitle;
    private AppCompatTextView tvName, tvPhone, tvBirthday, tvGender, tvRelationship, tvWebsite, tvJoinIn, tvDescription, tvCurrentResidence, tvHometown, tvJob, tvEducation;
    private AppCompatImageView ivGender;
    private AppCompatImageView ivEditName, ivEditPhone, ivEditBirthday, ivEditGender, ivEditRelationship, ivEditWebsite;
    private AppCompatTextView tvEditDescription, tvEditAddress, tvEditJob, tvEditEducation;

    public MainEditProfileFragment() {
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
        View view = inflater.inflate(R.layout.fragment_main_edit_profile, container, false);

        // binding
        ivToolbarBack = view.findViewById(R.id.iv_toolbar_back);
        tvToolbarTitle = view.findViewById(R.id.tv_toolbar_title);
        tvName = view.findViewById(R.id.tv_name);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvBirthday = view.findViewById(R.id.tv_birthday);
        tvGender = view.findViewById(R.id.tv_gender);
        tvRelationship = view.findViewById(R.id.tv_relationship);
        tvWebsite =view.findViewById(R.id.tv_website);
        tvJoinIn = view.findViewById(R.id.tv_join_in);
        tvDescription = view.findViewById(R.id.tv_description);
        tvCurrentResidence = view.findViewById(R.id.tv_current_residence);
        tvHometown = view.findViewById(R.id.tv_hometown);
        tvJob = view.findViewById(R.id.tv_job);
        tvEducation = view.findViewById(R.id.tv_education);
        ivGender = view.findViewById(R.id.iv_gender);
        ivEditName = view.findViewById(R.id.iv_edit_name);
        ivEditPhone = view.findViewById(R.id.iv_edit_phone);
        ivEditBirthday = view.findViewById(R.id.iv_edit_birthday);
        ivEditGender = view.findViewById(R.id.iv_edit_gender);
        ivEditRelationship = view.findViewById(R.id.iv_edit_relationship);
        ivEditWebsite = view.findViewById(R.id.iv_edit_website);
        tvEditDescription= view.findViewById(R.id.tv_edit_description);
        tvEditAddress= view.findViewById(R.id.tv_edit_address);
        tvEditJob= view.findViewById(R.id.tv_edit_job);
        tvEditEducation= view.findViewById(R.id.tv_edit_education);

        // init view
        tvToolbarTitle.setText(R.string.toolbar_title_edit_main);

        // get value from view model
        userProfileViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        userProfileViewModel.getUser().observe(getViewLifecycleOwner(), newUser -> {
            // update UI when data change
            user = newUser;
            handleUserToView();
        });

        ivToolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        ivEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(MainEditProfileFragment.this);
                navController.navigate(R.id.action_edit_name_profile);
            }
        });
        ivEditPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(MainEditProfileFragment.this);
                navController.navigate(R.id.action_edit_phone_profile);
            }
        });
        ivEditBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(MainEditProfileFragment.this);
                navController.navigate(R.id.action_edit_birthday_profile);
            }
        });
        ivEditGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(MainEditProfileFragment.this);
                navController.navigate(R.id.action_edit_gender_profile);
            }
        });
        ivEditRelationship.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(MainEditProfileFragment.this);
                navController.navigate(R.id.action_edit_relationship_profile);
            }
        });
        ivEditWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(MainEditProfileFragment.this);
                navController.navigate(R.id.action_edit_website_profile);
            }
        });
        tvEditDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(MainEditProfileFragment.this);
                navController.navigate(R.id.action_edit_description_profile);
            }
        });
        tvDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(MainEditProfileFragment.this);
                navController.navigate(R.id.action_edit_description_profile);
            }
        });
        tvEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(MainEditProfileFragment.this);
                navController.navigate(R.id.action_edit_address_profile);
            }
        });
        tvEditJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(MainEditProfileFragment.this);
                navController.navigate(R.id.action_edit_job_profile);
            }
        });
        tvEditEducation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(MainEditProfileFragment.this);
                navController.navigate(R.id.action_edit_education_profile);
            }
        });

        return view;
    }

    private void handleUserToView() {
        // name
        tvName.setText(user.getName());
        // phone
        hideOrShowData(tvPhone, user.getPhone());
        // birthday
        tvBirthday.setText(TimeExtension.formatBirthday(user.getBirthday()));
        // gender
        String gender = user.getGender();
        tvGender.setText(gender);
        if (gender.equals(getString(R.string.male))) {
            ivGender.setImageResource(R.drawable.ic_male_gender);
        } else if (gender.equals(getString(R.string.female))) {
            ivGender.setImageResource(R.drawable.ic_female_gender);
        } else {
            ivGender.setImageResource(R.drawable.ic_other_gender);
        }
        // relationship
        hideOrShowData(tvRelationship, user.getRelationshipStatus());
        // website
        hideOrShowData(tvWebsite, user.getWebsite());
        // date join in
        tvJoinIn.setText(TimeExtension.formatTimeJoinIn(user.getCreatedAt()));
        // description
        hideOrShowData(tvDescription, user.getShortDescription());
        // current residence
        hideOrShowData(tvCurrentResidence, user.getCurrentResidence());
        // hometown
        hideOrShowData(tvHometown, user.getHometown());
        // job
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.f_roboto_medium);
        if (user.getJob() != null) {
            SpannableStringBuilder textJob = new SpannableStringBuilder();
            textJob.append(user.getJob().getPosition());
            textJob.append(" ");
            textJob.append(getString(R.string.at));
            textJob.append(" ");
            int i = textJob.length();
            textJob.append(user.getJob().getCompany());
            textJob.setSpan(new CustomTypefaceSpan(typeface), i, textJob.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvJob.setText(textJob);
        }
        // education
        if (user.getEducation() != null) {
            SpannableStringBuilder textEducation = new SpannableStringBuilder();
            textEducation.append(getString(R.string.study));
            textEducation.append(" ");
            textEducation.append(user.getEducation().getMajors());
            textEducation.append(" ");
            textEducation.append(getString(R.string.at));
            textEducation.append(" ");
            int i = textEducation.length();
            textEducation.append(user.getEducation().getSchool());
            textEducation.setSpan(new CustomTypefaceSpan(typeface), i, textEducation.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvEducation.setText(textEducation);
        }
    }

    private void hideOrShowData(AppCompatTextView tv, String value) {
        if (checkValidValueString(value)) {
            tv.setText(value);
        } else {
            tv.setText("");
        }
    }
}