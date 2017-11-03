package net.sourceforge.opencamera.UI;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
import android.widget.SeekBar;
import android.widget.TextView;

import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.PreferenceKeys;
import net.sourceforge.opencamera.Preview.Preview;
import net.sourceforge.opencamera.R;

/**
 * Created by cti-pdd on 9/7/17.
 */

public class PROMode extends Fragment implements View.OnClickListener {

    private static final String TAG = "FirstFragment";
    private OrientationEventListener orientationEventListener;
    private Preview preview;

    private ImageButton btnWB;
    private static MainActivity mainActivity;
    private View v;
    ImageButton wb;
    ImageButton iso;
    ImageButton exposure;
    ImageButton shutter;
    ImageButton focus;
    ImageButton saturation;
    ImageButton contrast;
    SeekBar seekBar_wb;
    SeekBar seekBar_iso;
    SeekBar seekBar_shutter;
    LinearLayout layout_wb_top;
    SeekBar seekBar_exposure;
    SeekBar seekBar_saturation;
    SeekBar seekBar_contrast;
    LinearLayout layout_focus_top;
    SeekBar seekBar_focus;


     @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         this.mainActivity = (MainActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.pro_mode, container, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(PreferenceKeys.getProModePreferenceKey(), true);
        editor.apply();

        wb = (ImageButton)v.findViewById(R.id.wb);
        iso = (ImageButton)v.findViewById(R.id.iso);
        exposure = (ImageButton)v.findViewById(R.id.exposure);
        shutter = (ImageButton)v.findViewById(R.id.shutter);
        focus = (ImageButton)v.findViewById(R.id.focus);
        saturation = (ImageButton)v.findViewById(R.id.saturation);
        contrast = (ImageButton)v.findViewById(R.id.contrast);
        seekBar_wb = (SeekBar)v.findViewById(R.id.seekBar_wb);
        seekBar_iso = (SeekBar)v.findViewById(R.id.seekBar_iso);
        seekBar_shutter = (SeekBar)v.findViewById(R.id.seekBar_shutter_time);
        layout_wb_top = (LinearLayout)v.findViewById(R.id.layout_wb_top);
        seekBar_exposure = (SeekBar)v.findViewById(R.id.seekBar_exposure);
        seekBar_saturation = (SeekBar)v.findViewById(R.id.seekBar_saturation);
        seekBar_contrast = (SeekBar)v.findViewById(R.id.seekBar_contrast);
        layout_focus_top = (LinearLayout)v.findViewById(R.id.layout_focus_top);
        seekBar_focus = (SeekBar)v.findViewById(R.id.seekBar_focus);

        wb.setImageResource(R.drawable.whitebalance_pressed);


        wb.setOnClickListener(this);
        iso.setOnClickListener(this);
        exposure.setOnClickListener(this);
        shutter.setOnClickListener(this);
        focus.setOnClickListener(this);
        saturation.setOnClickListener(this);
        contrast.setOnClickListener(this);

        return v;
    }

    public void orientationChanged(int rotation){

        View view = v.findViewById(R.id.layout_wb);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.layout_iso);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.layout_exposure);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.layout_shutter);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.layout_focus);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.layout_saturation);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.layout_contrast);
        setViewRotation(view, rotation);

        view = v.findViewById(R.id.auto);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.incandescence);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.daylight);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.flourescence);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.overcast);
        setViewRotation(view, rotation);


    }

    public void setViewRotation(View view, float ui_rotation) {
        //view.setRotation(ui_rotation);
        Log.d(TAG, " setViewRotation ");
        float rotate_by = ui_rotation - view.getRotation();
        if( rotate_by > 181.0f )
            rotate_by -= 360.0f;
        else if( rotate_by < -181.0f )
            rotate_by += 360.0f;
        // view.animate() modifies the view's rotation attribute, so it ends up equivalent to view.setRotation()
        // we use rotationBy() instead of rotation(), so we get the minimal rotation for clockwise vs anti-clockwise
        view.animate().rotationBy(rotate_by).setDuration(35).setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.wb:

                wb.setImageResource(R.drawable.whitebalance_pressed);
                iso.setImageResource(R.drawable.iso);
                exposure.setImageResource(R.drawable.exposure);
                shutter.setImageResource(R.drawable.shutter);
                focus.setImageResource(R.drawable.focus);
                saturation.setImageResource(R.drawable.saturation);
                contrast.setImageResource(R.drawable.contrast);

                layout_wb_top.setVisibility(View.VISIBLE);
                seekBar_wb.setVisibility(View.VISIBLE);
                seekBar_iso.setVisibility(View.GONE);
                seekBar_shutter.setVisibility(View.GONE);
                seekBar_exposure.setVisibility(View.GONE);
                seekBar_saturation.setVisibility(View.GONE);
                seekBar_contrast.setVisibility(View.GONE);
                layout_focus_top.setVisibility(View.GONE);
                seekBar_focus.setVisibility(View.GONE);
                break;
            case R.id.iso:

                wb.setImageResource(R.drawable.whitebalance);
                iso.setImageResource(R.drawable.iso_pressed);
                exposure.setImageResource(R.drawable.exposure);
                shutter.setImageResource(R.drawable.shutter);
                focus.setImageResource(R.drawable.focus);
                saturation.setImageResource(R.drawable.saturation);
                contrast.setImageResource(R.drawable.contrast);


                seekBar_wb.setVisibility(View.GONE);
                layout_wb_top.setVisibility(View.GONE);
                seekBar_iso.setVisibility(View.VISIBLE);
                seekBar_iso.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                seekBar_shutter.setVisibility(View.GONE);
                seekBar_exposure.setVisibility(View.GONE);
                seekBar_saturation.setVisibility(View.GONE);
                seekBar_contrast.setVisibility(View.GONE);
                layout_focus_top.setVisibility(View.GONE);
                seekBar_focus.setVisibility(View.GONE);
                break;

            case R.id.exposure:

                wb.setImageResource(R.drawable.whitebalance);
                iso.setImageResource(R.drawable.iso);
                exposure.setImageResource(R.drawable.exposure_pressed);
                shutter.setImageResource(R.drawable.shutter);
                focus.setImageResource(R.drawable.focus);
                saturation.setImageResource(R.drawable.saturation);
                contrast.setImageResource(R.drawable.contrast);

                seekBar_wb.setVisibility(View.GONE);
                layout_wb_top.setVisibility(View.GONE);
                seekBar_iso.setVisibility(View.GONE);
//                seekBar_iso.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                seekBar_shutter.setVisibility(View.GONE);
                seekBar_exposure.setVisibility(View.VISIBLE);
                seekBar_exposure.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                seekBar_saturation.setVisibility(View.GONE);
                seekBar_contrast.setVisibility(View.GONE);
                layout_focus_top.setVisibility(View.GONE);
                seekBar_focus.setVisibility(View.GONE);
                break;

            case R.id.shutter:
                wb.setImageResource(R.drawable.whitebalance);
                iso.setImageResource(R.drawable.iso);
                exposure.setImageResource(R.drawable.exposure);
                shutter.setImageResource(R.drawable.shutter_pressed);
                focus.setImageResource(R.drawable.focus);
                saturation.setImageResource(R.drawable.saturation);
                contrast.setImageResource(R.drawable.contrast);

                seekBar_wb.setVisibility(View.GONE);
                layout_wb_top.setVisibility(View.GONE);
                seekBar_iso.setVisibility(View.GONE);
                seekBar_shutter.setVisibility(View.VISIBLE);
                seekBar_shutter.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                seekBar_exposure.setVisibility(View.GONE);
                seekBar_saturation.setVisibility(View.GONE);
                seekBar_contrast.setVisibility(View.GONE);
                layout_focus_top.setVisibility(View.GONE);
                seekBar_focus.setVisibility(View.GONE);
                break;

            case R.id.focus:

                wb.setImageResource(R.drawable.whitebalance);
                iso.setImageResource(R.drawable.iso);
                exposure.setImageResource(R.drawable.exposure);
                shutter.setImageResource(R.drawable.shutter);
                focus.setImageResource(R.drawable.focus_pressed);
                saturation.setImageResource(R.drawable.saturation);
                contrast.setImageResource(R.drawable.contrast);

                seekBar_wb.setVisibility(View.GONE);
                layout_wb_top.setVisibility(View.GONE);
                seekBar_iso.setVisibility(View.GONE);
                seekBar_shutter.setVisibility(View.GONE);
                seekBar_exposure.setVisibility(View.GONE);
                seekBar_saturation.setVisibility(View.GONE);
                seekBar_contrast.setVisibility(View.GONE);
                layout_focus_top.setVisibility(View.VISIBLE);
                seekBar_focus.setVisibility(View.VISIBLE);
                break;

            case R.id.saturation:

                wb.setImageResource(R.drawable.whitebalance);
                iso.setImageResource(R.drawable.iso);
                exposure.setImageResource(R.drawable.exposure);
                shutter.setImageResource(R.drawable.shutter);
                focus.setImageResource(R.drawable.focus);
                saturation.setImageResource(R.drawable.saturation_pressed);
                contrast.setImageResource(R.drawable.contrast);


                seekBar_wb.setVisibility(View.GONE);
                layout_wb_top.setVisibility(View.GONE);
                seekBar_iso.setVisibility(View.GONE);
                seekBar_shutter.setVisibility(View.GONE);
                seekBar_exposure.setVisibility(View.GONE);
                seekBar_saturation.setVisibility(View.VISIBLE);
                seekBar_saturation.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                seekBar_contrast.setVisibility(View.GONE);
                layout_focus_top.setVisibility(View.GONE);
                seekBar_focus.setVisibility(View.GONE);
                break;

            case R.id.contrast:

                wb.setImageResource(R.drawable.whitebalance);
                iso.setImageResource(R.drawable.iso);
                exposure.setImageResource(R.drawable.exposure);
                shutter.setImageResource(R.drawable.shutter);
                focus.setImageResource(R.drawable.focus);
                saturation.setImageResource(R.drawable.saturation);
                contrast.setImageResource(R.drawable.contrast_pressed);

                seekBar_wb.setVisibility(View.GONE);
                layout_wb_top.setVisibility(View.GONE);
                seekBar_iso.setVisibility(View.GONE);
                seekBar_shutter.setVisibility(View.GONE);
                seekBar_exposure.setVisibility(View.GONE);
                seekBar_saturation.setVisibility(View.GONE);
                seekBar_contrast.setVisibility(View.VISIBLE);
                seekBar_contrast.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                layout_focus_top.setVisibility(View.GONE);
                seekBar_focus.setVisibility(View.GONE);
                break;
        }
    }
}
