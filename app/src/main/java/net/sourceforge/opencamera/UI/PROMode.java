package net.sourceforge.opencamera.UI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.PreferenceKeys;
import net.sourceforge.opencamera.Preview.Preview;
import net.sourceforge.opencamera.R;

/**
 * Created by cti-pdd on 9/7/17.
 */

public class PROMode extends Fragment {

    private static final String TAG = "FirstFragment";
    private OrientationEventListener orientationEventListener;
    private Preview preview;

    private ImageButton btnWB;
    private static MainActivity mainActivity;
    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.pro_mode, container, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(PreferenceKeys.getProModePreferenceKey(), true);
        editor.apply();


        return v;
    }

    public static PROMode newInstance(MainActivity main_activity) {

        mainActivity = main_activity;

        PROMode pro = new PROMode();
        Bundle b = new Bundle();
        pro.setArguments(b);
        return pro;
    }

    public void setUIRotation(int rotation){
        View view = v.findViewById(R.id.wb);
        mainActivity.getMainUI().setViewRotation(view, rotation);

    }



}
