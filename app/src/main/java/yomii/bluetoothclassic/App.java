package yomii.bluetoothclassic;

import android.app.Application;

/**
 * Created by Yomii on 2017/7/27.
 *
 * Context
 */

public class App extends Application {

    private static App context;

    public static App getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
