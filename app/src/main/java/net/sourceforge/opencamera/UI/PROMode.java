package net.sourceforge.opencamera.UI;

import android.animation.ValueAnimator;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class PROMode extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

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
    ImageButton left_pro;
    ImageButton right_pro;
    LinearLayout layout_iso_top;
    LinearLayout layout_shutter_top;
    TextView exposure_label;
    TextView saturation_label;
    TextView contrast_label;
    TextView wb_label;
    HorizontalScrollView horizontalscrollview;
    TextView focus_auto;
    ImageView macro;
    ImageView mountain;
    TextView shutter_auto;
    TextView shutter_1s;
    TextView shutter_2s;
    TextView shutter_4s;
    TextView shutter_8s;
    TextView iso_auto;
    TextView iso_100;
    TextView iso_200;
    TextView iso_400;
    TextView iso_800;
    boolean isArrownShown = true;

     @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         this.mainActivity = (MainActivity)getActivity();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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
        left_pro = (ImageButton)v.findViewById(R.id.left_pro);
        right_pro = (ImageButton)v.findViewById(R.id.right_pro);
        layout_iso_top = (LinearLayout)v.findViewById(R.id.layout_iso_top);
        layout_shutter_top = (LinearLayout)v.findViewById(R.id.layout_shutter_top);
        exposure_label = (TextView)v.findViewById(R.id.exposure_label);
        saturation_label = (TextView)v.findViewById(R.id.saturation_label);
        contrast_label = (TextView)v.findViewById(R.id.contrast_label);
        wb_label = (TextView)v.findViewById(R.id.wb_label);
        horizontalscrollview = (HorizontalScrollView) v.findViewById(R.id.horizontalscrollview);

        wb.setImageResource(R.drawable.whitebalance_pressed);

        wb.setOnClickListener(this);
        iso.setOnClickListener(this);
        exposure.setOnClickListener(this);
        shutter.setOnClickListener(this);
        focus.setOnClickListener(this);
        saturation.setOnClickListener(this);
        contrast.setOnClickListener(this);
        left_pro.setOnClickListener(this);
        right_pro.setOnClickListener(this);

        seekBar_wb.setOnSeekBarChangeListener(this);
        seekBar_exposure.setOnSeekBarChangeListener(this);
        seekBar_focus.setOnSeekBarChangeListener(this);
        seekBar_saturation.setOnSeekBarChangeListener(this);
        seekBar_contrast.setOnSeekBarChangeListener(this);
        horizontalscrollview.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.d(TAG, " FOCUS_RIGHT " + " scrollX " + scrollX);

                if (scrollX == 0)
                {
                    left_pro.setVisibility(View.INVISIBLE);
                }
                else if (scrollX == 450)
                {
                    right_pro.setVisibility(View.INVISIBLE);
                }
                else
                {
                    left_pro.setVisibility(View.VISIBLE);
                    right_pro.setVisibility(View.VISIBLE);
                }
            }
        });

        left_pro.setVisibility(View.INVISIBLE);

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


        view = v.findViewById(R.id.wb_label);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.contrast_label);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.saturation_label);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.exposure_label);
        setViewRotation(view, rotation);


        view = v.findViewById(R.id.focus_auto);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.macro);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.mountain);
        setViewRotation(view, rotation);


        view = v.findViewById(R.id.shutter_auto);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.shutter_1s);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.shutter_2s);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.shutter_4s);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.shutter_8s);
        setViewRotation(view, rotation);


        view = v.findViewById(R.id.iso_auto);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.iso_100);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.iso_200);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.iso_400);
        setViewRotation(view, rotation);
        view = v.findViewById(R.id.iso_800);
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

                wb_label.setVisibility(View.VISIBLE);
                layout_wb_top.setVisibility(View.VISIBLE);
                layout_iso_top.setVisibility(View.GONE);
                layout_shutter_top.setVisibility(View.GONE);
                exposure_label.setVisibility(View.GONE);
                saturation_label.setVisibility(View.GONE);
                contrast_label.setVisibility(View.GONE);
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

                wb_label.setVisibility(View.GONE);
                seekBar_wb.setVisibility(View.GONE);
                layout_wb_top.setVisibility(View.GONE);
                layout_iso_top.setVisibility(View.VISIBLE);
                layout_shutter_top.setVisibility(View.GONE);
                exposure_label.setVisibility(View.GONE);
                saturation_label.setVisibility(View.GONE);
                contrast_label.setVisibility(View.GONE);
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

                wb_label.setVisibility(View.GONE);
                seekBar_wb.setVisibility(View.GONE);
                layout_wb_top.setVisibility(View.GONE);
                layout_iso_top.setVisibility(View.GONE);
                layout_shutter_top.setVisibility(View.GONE);
                exposure_label.setVisibility(View.VISIBLE);
                saturation_label.setVisibility(View.GONE);
                contrast_label.setVisibility(View.GONE);
                seekBar_iso.setVisibility(View.GONE);
//                seekBar_iso.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                seekBar_shutter.setVisibility(View.GONE);
                seekBar_exposure.setVisibility(View.VISIBLE);
                seekBar_exposure.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                seekBar_saturation.setVisibility(View.GONE);
                seekBar_contrast.setVisibility(View.GONE);
                layout_focus_top.setVisibility(View.GONE);
                seekBar_focus.setVisibility(View.GONE);
                label_fadeout(exposure_label);
                break;

            case R.id.shutter:
                wb.setImageResource(R.drawable.whitebalance);
                iso.setImageResource(R.drawable.iso);
                exposure.setImageResource(R.drawable.exposure);
                shutter.setImageResource(R.drawable.shutter_pressed);
                focus.setImageResource(R.drawable.focus);
                saturation.setImageResource(R.drawable.saturation);
                contrast.setImageResource(R.drawable.contrast);

                wb_label.setVisibility(View.GONE);
                seekBar_wb.setVisibility(View.GONE);
                layout_wb_top.setVisibility(View.GONE);
                layout_iso_top.setVisibility(View.GONE);
                layout_shutter_top.setVisibility(View.VISIBLE);
                exposure_label.setVisibility(View.GONE);
                saturation_label.setVisibility(View.GONE);
                contrast_label.setVisibility(View.GONE);
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

                wb_label.setVisibility(View.GONE);
                seekBar_wb.setVisibility(View.GONE);
                layout_wb_top.setVisibility(View.GONE);
                layout_iso_top.setVisibility(View.GONE);
                layout_shutter_top.setVisibility(View.GONE);
                exposure_label.setVisibility(View.GONE);
                saturation_label.setVisibility(View.GONE);
                contrast_label.setVisibility(View.GONE);
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

                wb_label.setVisibility(View.GONE);
                seekBar_wb.setVisibility(View.GONE);
                layout_wb_top.setVisibility(View.GONE);
                layout_iso_top.setVisibility(View.GONE);
                layout_shutter_top.setVisibility(View.GONE);
                exposure_label.setVisibility(View.GONE);
                saturation_label.setVisibility(View.VISIBLE);
                contrast_label.setVisibility(View.GONE);
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

                wb_label.setVisibility(View.GONE);
                seekBar_wb.setVisibility(View.GONE);
                layout_wb_top.setVisibility(View.GONE);
                layout_iso_top.setVisibility(View.GONE);
                layout_shutter_top.setVisibility(View.GONE);
                exposure_label.setVisibility(View.GONE);
                saturation_label.setVisibility(View.GONE);
                contrast_label.setVisibility(View.VISIBLE);
                seekBar_iso.setVisibility(View.GONE);
                seekBar_shutter.setVisibility(View.GONE);
                seekBar_exposure.setVisibility(View.GONE);
                seekBar_saturation.setVisibility(View.GONE);
                seekBar_contrast.setVisibility(View.VISIBLE);
                seekBar_contrast.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.orange)));
                layout_focus_top.setVisibility(View.GONE);
                seekBar_focus.setVisibility(View.GONE);
                break;


            case R.id.left_pro :

                horizontalscrollview.fullScroll(HorizontalScrollView.FOCUS_LEFT);

                if(horizontalscrollview.getScrollX() == 0 && !isArrownShown){
                    Log.d(TAG,"horizontalscrollview left 1");
                    left_pro.setVisibility(View.VISIBLE);
                    right_pro.setVisibility(View.INVISIBLE);
                    isArrownShown = true;
                }
                else
                {
                    Log.d(TAG,"horizontalscrollview left 2");
                    right_pro.setVisibility(View.VISIBLE);
                    left_pro.setVisibility(View.INVISIBLE);
                }
                break;

            case R.id.right_pro :
                horizontalscrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                if(horizontalscrollview.getScrollX() == 0 && isArrownShown){
                    Log.d(TAG,"horizontalscrollview right 1");
                    right_pro.setVisibility(View.INVISIBLE);
                    left_pro.setVisibility(View.VISIBLE);
                    isArrownShown = false;
                }
                else
                {
                    Log.d(TAG,"horizontalscrollview right 2");
                    left_pro.setVisibility(View.VISIBLE);
                    right_pro.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }

    private void label_fadeout(final TextView label)
    {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0f);
        valueAnimator.setDuration(4000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = Float.parseFloat(animation.getAnimatedValue().toString());
                label.setAlpha(alpha);
            }
        });
        valueAnimator.start();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        switch (seekBar.getId())
        {
            case R.id.seekBar_wb :

                wb_label.setVisibility(View.VISIBLE);
                switch (progress)
                {
                    case 0 :
                        wb_label.setText("Auto");
                        label_fadeout(wb_label);
                        break;

                    case 1 :
                        wb_label.setText("Incandescence");
                        label_fadeout(wb_label);
                        break;

                    case 2 :
                        wb_label.setText("Daylight");
                        label_fadeout(wb_label);
                        break;

                    case 3 :
                        wb_label.setText("Flourescence");
                        label_fadeout(wb_label);
                        break;

                    case 4 :
                        wb_label.setText("Overcast");
                        label_fadeout(wb_label);
                        break;
                }
            break;

            case R.id.seekBar_exposure :
                Log.d(TAG, " onProgressChanged1 " + progress + " fromUser " + fromUser);

                int max_exposure = 12;

                if (progress >= 12 || progress <= 24)
                {
                    progress -= max_exposure;
                    Log.d(TAG, " onProgressChanged1 " + progress + " fromUser " + fromUser);

                } else if(progress >= 0 || progress < 12){
                    progress -= max_exposure;
                    Log.d(TAG, " onProgressChanged2 " + progress + " fromUser " + fromUser);
                }
                exposure_label.setText(""+progress);
                label_fadeout(exposure_label);
                break;

            case R.id.seekBar_focus :
                Log.d(TAG, " onProgressChanged1 " + progress + " fromUser " + fromUser);
                break;

            case R.id.seekBar_saturation :
                Log.d(TAG, " onProgressChanged1 " + progress + " fromUser " + fromUser);

                int max_saturation = 2;

                if (progress >= 2 || progress <= 4)
                {
                    progress -= max_saturation;
                    Log.d(TAG, " onProgressChanged1 " + progress + " fromUser " + fromUser);

                } else if(progress >= 0 || progress < 2){
                    progress -= max_saturation;
                    Log.d(TAG, " onProgressChanged2 " + progress + " fromUser " + fromUser);
                }
                saturation_label.setText(""+progress);
                label_fadeout(saturation_label);
                break;

            case R.id.seekBar_contrast :
                Log.d(TAG, " onProgressChanged1 " + progress + " fromUser " + fromUser);

                int max_contrast = 2;

                if (progress >= 2 || progress <= 4)
                {
                    progress -= max_contrast;
                    Log.d(TAG, " onProgressChanged1 " + progress + " fromUser " + fromUser);

                } else if(progress >= 0 || progress < 2){
                    progress -= max_contrast;
                    Log.d(TAG, " onProgressChanged2 " + progress + " fromUser " + fromUser);
                }
                contrast_label.setText(""+progress);
                label_fadeout(contrast_label);
                break;

            case R.id.horizontalscrollview :
                Log.d(TAG, " FOCUS_RIGHT ");
                horizontalscrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
