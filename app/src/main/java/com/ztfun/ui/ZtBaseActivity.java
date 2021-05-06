package com.ztfun.ui;

import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.ztfun.util.Log;
import com.ztfun.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class ZtBaseActivity extends AppCompatActivity {
    private static final String TAG = ZtBaseActivity.class.getSimpleName();

    /**
     * Hide SystemUi (Navigation Bar & Status Bar)
     */
    protected void hideSystemUi() throws NullPointerException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // for API 30 or higher, use WindowInsetsController
            WindowInsetsController controller = getWindow().getInsetsController();
            controller.hide(WindowInsets.Type.navigationBars());
            controller.hide(WindowInsets.Type.statusBars());
        } else {
            hideSystemUi29();
        }
    }

    /**
     * Hide system ui for API level lower than 30
     */
    private void hideSystemUi29() {
        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i("Turning immersive mode mode off. ");
        } else {
            Log.i("Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }

    /**
     * View list for toggle hide/show status for one touch
     */
    protected List<View> hideList = new ArrayList<View>();
    protected void add2OneTouchHideList(View v) {
        hideList.add(v);
    }

    protected void hideAll() {
        if (hideList.size() <= 0) {
            return;
        }

        int status = hideList.get(0).getVisibility();
        int newStatus = status == View.GONE ? View.VISIBLE : View.GONE;
        for (View v : hideList) {
            v.setVisibility(newStatus);
            Log.d(TAG, "Setting new status=" + newStatus + " for view " + ViewUtils.getResourceName(v));
        }
    }

    /**
     * Request for permissions
     */
    protected void requestPermission(int reqCode, String[] accessPermissions) {
        boolean needRequire = false;
        for(String access : accessPermissions) {
            int curPermission = ActivityCompat.checkSelfPermission(this, access);
            if (curPermission != PackageManager.PERMISSION_GRANTED) {
                needRequire = true;
                break;
            }
        }

        if (needRequire) {
            ActivityCompat.requestPermissions(
                    this,
                    accessPermissions,
                    reqCode);
        }
    }
}
