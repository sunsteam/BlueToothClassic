package yomii.bluetoothclassic;

import android.widget.Toast;


/**
 * Toast工具类
 */
public class ToastUtils {

    private static Toast toast;

    public static void imitShowToast(String string) {
        if (toast == null) {
            toast = Toast.makeText(App.getContext(), string, Toast.LENGTH_SHORT);
        } else {
            toast.setText(string);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    public static void imitShowToast(int StringRes) {
        imitShowToast(App.getContext().getString(StringRes));
    }


    /**
     * 取消吐司显示
     */
    public static void cancelToast() {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
    }

}