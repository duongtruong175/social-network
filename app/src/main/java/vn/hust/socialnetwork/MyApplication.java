package vn.hust.socialnetwork;

import android.app.Application;

import com.orhanobut.hawk.Hawk;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // init emoji
        EmojiManager.install(new GoogleEmojiProvider());

        // init library for simple key-value storage
        Hawk.init(getApplicationContext()).build();
    }
}
