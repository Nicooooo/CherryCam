package net.sourceforge.opencamera;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import net.sourceforge.opencamera.MainActivity;
/**
 * Created by FM-JMK on 25/10/2017.
 */

public class RecordButton extends ImageButton {

    public interface RecordButtonListener {
        void onRecordButtonClicked();
    }
    private Drawable takePhotoDrawable;
    private Drawable startRecordDrawable;
    private Drawable stopRecordDrawable;
    private int iconPadding = 8;
    private int iconPaddingStop = 18;
    private MainActivity main_activity;

    @Nullable
    private RecordButtonListener listener;

//    public RecordButton(MainActivity main_activity){
//        this.main_activity = main_activity;  main_activity.takePicture();
//    }

    public RecordButton(@NonNull MainActivity main_activity) {
        this(main_activity, null, 0);
        this.main_activity = main_activity;
    }

    public RecordButton(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordButton(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        takePhotoDrawable = ContextCompat.getDrawable(context, R.drawable.take_photo_button);
        startRecordDrawable = ContextCompat.getDrawable(context, R.drawable.start_video_record_button);
        stopRecordDrawable = ContextCompat.getDrawable(context, R.drawable.stop_button_background);

        if (Build.VERSION.SDK_INT > 15)
            setBackground(ContextCompat.getDrawable(context, R.drawable.circle_frame_background));
        else
            setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.circle_frame_background));

        setOnClickListener(new OnClickListener() {
            private final static int CLICK_DELAY = 1000;

            private long lastClickTime = 0;

            @Override
            public void onClick(View view) {
                Log.d("RecordButton", "onRecordButtonClicked " );
main_activity.takePicture();
                if (System.currentTimeMillis() - lastClickTime < CLICK_DELAY) {
                    return;
                } else lastClickTime = System.currentTimeMillis();

                if(listener != null) {
                    listener.onRecordButtonClicked();
                }

            }
        });
        setSoundEffectsEnabled(false);
        setIconPadding(iconPadding);

        displayPhotoState();
    }

    //public void setup(@Configuration.MediaAction int mediaAction) {
    //    setMediaAction(mediaAction);
    //}

    private void setIconPadding(int paddingDP) {
//        int padding = Utils.convertDipToPixels(getContext(), paddingDP);
//        setPadding(padding, padding, padding, padding);
    }

    //public void setMediaAction(@Configuration.MediaAction int mediaAction) {
    //    this.mediaAction = mediaAction;
    //    if (listener != null) {
    //        if (Configuration.MEDIA_ACTION_PHOTO == mediaAction) {
    //            listener.setRecordState(Record.TAKE_PHOTO_STATE);
    //        } else {
    //            listener.setRecordState(Record.READY_FOR_RECORD_STATE);
    //        }
    //    }
    //}

    public void setRecordButtonListener(@NonNull RecordButtonListener listener) {
        this.listener = listener;
    }

    public void displayVideoRecordStateReady(){
        setImageDrawable(startRecordDrawable);
        setIconPadding(iconPadding);
    }

    public void displayVideoRecordStateInProgress(){
        setImageDrawable(stopRecordDrawable);
        setIconPadding(iconPaddingStop);
    }

    public void displayPhotoState(){
        setImageDrawable(takePhotoDrawable);
        setIconPadding(iconPadding);
    }

}
