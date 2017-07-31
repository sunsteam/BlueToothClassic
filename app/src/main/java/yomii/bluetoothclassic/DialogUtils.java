package yomii.bluetoothclassic;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;


public class DialogUtils {

    private static ProgressDialog progressDialog;

    public static void createProgressDialog(@NonNull Activity activity, @NonNull String message,
                                            @NonNull String titleStr) {
        createProgressDialog(activity, message, titleStr, true);
    }

    public static void createProgressDialog(@NonNull Activity activity, @NonNull String message,
                                            @NonNull String titleStr, boolean isCancelable) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(titleStr);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(isCancelable);
        progressDialog.setIndeterminate(true);//设置滚动条的状态为滚动
        progressDialog.show();
    }

    public static void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.cancel();
            progressDialog = null;
        }
    }

    public static void setProgressMessage(String message) {
        if (progressDialog != null) {
            progressDialog.setMessage(message);
        }
    }
}
