package vn.hust.socialnetwork.ui.main.userprofile.edit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import vn.hust.socialnetwork.models.user.User;

public class UserProfileViewModel extends ViewModel {

    private final MutableLiveData<User> user = new MutableLiveData<User>();

    public void setUser(User newUser) {
       this.user.setValue(newUser);
    }

    public LiveData<User> getUser() {
        return this.user;
    }
}
