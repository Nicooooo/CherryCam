package net.sourceforge.opencamera.UI;

import android.view.View;
import android.view.Window;

/**
 * Created by cti-pdd on 9/26/17.
 */

public class Immersive {
    private static Immersive instance;

    private Immersive() {

    }
    static public Immersive CreateInstance() {
        if (instance == null)
        {
            instance = new Immersive();
        }
        return instance;
    }
    public void HideNavigationBar(Window w)
    {
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE ;
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//         | View.GONE;w.getDecorView().setSystemUiVisibility(uiOptions);}
    }
    public void UiChangeListener(Window w) {
        final View decorView = w.getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
        {
            @Override
            public void onSystemUiVisibilityChange(int visibility)
            {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|

                            View.SYSTEM_UI_FLAG_FULLSCREEN|
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                }
            }});
    }
}
