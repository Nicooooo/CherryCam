package net.sourceforge.opencamera.UI;

import net.sourceforge.opencamera.CameraController.CameraController;
import net.sourceforge.opencamera.CameraController.CameraController1;
import net.sourceforge.opencamera.CameraController.CameraController2;
import net.sourceforge.opencamera.HDRProcessor;
import net.sourceforge.opencamera.MainActivity;
import net.sourceforge.opencamera.MyApplicationInterface;
import net.sourceforge.opencamera.MyDebug;
import net.sourceforge.opencamera.PreferenceKeys;
import net.sourceforge.opencamera.R;
import net.sourceforge.opencamera.RecordButton;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ZoomControls;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.sourceforge.opencamera.R.id.settings_bitrate_txt;

/** This contains functionality related to the main UI.
 */
public class MainUI {
	private static final String TAG = "MainUI";

	private final MainActivity main_activity;

	private volatile boolean popup_view_is_open; // must be volatile for test project reading the state
    private PopupView popup_view;

    private int current_orientation;
	private boolean ui_placement_right = true;

	private boolean immersive_mode;
    private boolean show_gui = true; // result of call to showGUI() - false means a "reduced" GUI is displayed, whilst taking photo or video

	private boolean keydown_volume_up;
	private boolean keydown_volume_down;

	private boolean isFlash;
	private boolean isTimer;
	public static final String HDR_STATE = "HDR_STATE";
	private boolean hdrstate;
	private Boolean isHdrState = false;
	private String flash = "";
	private CameraController camera_controller;
	private boolean save_expo;
	private List<byte []> images;
	private Date current_date;
	private String storage_path = "";
	private String camera_resolution = "";
	private String camera_brightness = "";
	private String bitrate = "";
    private String timer_picture = "";
	private String theme_color = "";
	RecordButton recordButton;

	public MainUI(MainActivity main_activity) {
		if( MyDebug.LOG )
			Log.d(TAG, "MainUI");
		this.main_activity = main_activity;

		this.setSeekbarColors();

		this.setIcon(R.id.gallery);
		this.setIcon(R.id.settings);
		this.setIcon(R.id.popup);
		this.setIcon(R.id.exposure_lock);
		this.setIcon(R.id.exposure);
		this.setIcon(R.id.switch_video);
		this.setIcon(R.id.switch_camera);
		this.setIcon(R.id.audio_control);
		this.setIcon(R.id.trash);
		this.setIcon(R.id.share);
	}

	private void setIcon(int id) {
		if( MyDebug.LOG )
			Log.d(TAG, "setIcon: " + id);
	    ImageButton button = (ImageButton)main_activity.findViewById(id);
	    button.setBackgroundColor(Color.argb(63, 63, 63, 63)); // n.b., rgb color seems to be ignored for Android 6 onwards, but still relevant for older versions
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void setSeekbarColors() {
		if( MyDebug.LOG )
			Log.d(TAG, "setSeekbarColors");
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
			ColorStateList progress_color = ColorStateList.valueOf( Color.argb(255, 240, 240, 240) );
			ColorStateList thumb_color = ColorStateList.valueOf( Color.argb(255, 255, 255, 255) );

			SeekBar seekBar = (SeekBar)main_activity.findViewById(R.id.zoom_seekbar);
			seekBar.setProgressTintList(progress_color);
			seekBar.setThumbTintList(thumb_color);

			seekBar = (SeekBar)main_activity.findViewById(R.id.focus_seekbar);
			seekBar.setProgressTintList(progress_color);
			seekBar.setThumbTintList(thumb_color);

			seekBar = (SeekBar)main_activity.findViewById(R.id.exposure_seekbar);
			seekBar.setProgressTintList(progress_color);
			seekBar.setThumbTintList(thumb_color);

			seekBar = (SeekBar)main_activity.findViewById(R.id.iso_seekbar);
			seekBar.setProgressTintList(progress_color);
			seekBar.setThumbTintList(thumb_color);

			seekBar = (SeekBar)main_activity.findViewById(R.id.exposure_time_seekbar);
			seekBar.setProgressTintList(progress_color);
			seekBar.setThumbTintList(thumb_color);
		}
	}

	/** Similar view.setRotation(ui_rotation), but achieves this via an animation.
	 */
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
		view.animate().rotationBy(rotate_by).setDuration(100).setInterpolator(new AccelerateDecelerateInterpolator()).start();
	}

    public void layoutUI() {
		long debug_time = 0;
		Log.d(TAG, " updateForSettings layoutUI");
		if( MyDebug.LOG ) {
			debug_time = System.currentTimeMillis();
		}
		//main_activity.getPreview().updateUIPlacement();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		String ui_placement = sharedPreferences.getString(PreferenceKeys.getUIPlacementPreferenceKey(), "ui_right");
    	// we cache the preference_ui_placement to save having to check it in the draw() method
		this.ui_placement_right = ui_placement.equals("ui_right");
		if( MyDebug.LOG )
			Log.d(TAG, "ui_placement: " + ui_placement);
		// new code for orientation fixed to landscape
		// the display orientation should be locked to landscape, but how many degrees is that?
	    int rotation = main_activity.getWindowManager().getDefaultDisplay().getRotation();
	    int degrees = 0;
	    switch (rotation) {
	    	case Surface.ROTATION_0: degrees = 0; break;
	        case Surface.ROTATION_90: degrees = 90; break;
	        case Surface.ROTATION_180: degrees = 180; break;
	        case Surface.ROTATION_270: degrees = 270; break;
    		default:
    			break;
	    }
	    // getRotation is anti-clockwise, but current_orientation is clockwise, so we add rather than subtract
	    // relative_orientation is clockwise from landscape-left
    	//int relative_orientation = (current_orientation + 360 - degrees) % 360;
    	int relative_orientation = (current_orientation + degrees) % 360;
		if( MyDebug.LOG ) {
			Log.d(TAG, "    current_orientation = " + current_orientation);
			Log.d(TAG, "    degrees = " + degrees);
			Log.d(TAG, "    relative_orientation = " + relative_orientation);
		}
		int ui_rotation = (360 - relative_orientation) % 360;
		Log.d(TAG, "    current_orientation = " + current_orientation);
		main_activity.getPreview().setUIRotation(ui_rotation);
		int align_left = RelativeLayout.ALIGN_LEFT;
		int align_right = RelativeLayout.ALIGN_RIGHT;
		//int align_top = RelativeLayout.ALIGN_TOP;
		//int align_bottom = RelativeLayout.ALIGN_BOTTOM;
		int left_of = RelativeLayout.LEFT_OF;
		int right_of = RelativeLayout.RIGHT_OF;
		int above = RelativeLayout.ABOVE;
		int below = RelativeLayout.BELOW;
		int align_parent_left = RelativeLayout.ALIGN_PARENT_LEFT;
		int align_parent_right = RelativeLayout.ALIGN_PARENT_RIGHT;
		int align_parent_top = RelativeLayout.ALIGN_PARENT_TOP;
		int align_parent_bottom = RelativeLayout.ALIGN_PARENT_BOTTOM;
		if( !ui_placement_right ) {
			//align_top = RelativeLayout.ALIGN_BOTTOM;
			//align_bottom = RelativeLayout.ALIGN_TOP;
			above = RelativeLayout.BELOW;
			below = RelativeLayout.ABOVE;
			align_parent_top = RelativeLayout.ALIGN_PARENT_BOTTOM;
			align_parent_bottom = RelativeLayout.ALIGN_PARENT_TOP;
		}
		{
			// we use a dummy button, so that the GUI buttons keep their positioning even if the Settings button is hidden (visibility set to View.GONE)
			View view = main_activity.findViewById(R.id.gui_anchor);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_parent_left, 0);
//			layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_bottom, 0);
//			layoutParams.addRule(left_of, 0);
//			layoutParams.addRule(right_of, 0);
//			view.setLayoutParams(layoutParams);
//			setViewRotation(view, ui_rotation);
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.gallery);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_left, RelativeLayout.TRUE);
			layoutParams.leftMargin= 50;
			layoutParams.bottomMargin=60;
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);

			view = main_activity.findViewById(R.id.settings);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
			layoutParams.addRule(align_parent_bottom, 0);
			layoutParams.addRule(left_of, R.id.gallery);
			layoutParams.addRule(right_of, 0);
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);

			view = main_activity.findViewById(R.id.popup);
//			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_bottom, 0);
//			layoutParams.addRule(left_of, R.id.settings);
//			layoutParams.addRule(right_of, 0);
//			view.setLayoutParams(layoutParams);
//			setViewRotation(view, ui_rotation);
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.flash_auto);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);

			view = main_activity.findViewById(R.id.flash_on);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);


			view = main_activity.findViewById(R.id.hdr);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);

			view = main_activity.findViewById(R.id.toggle_camera);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			layoutParams.bottomMargin = 60;
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);

			view = main_activity.findViewById(R.id.settings_dots);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);

			view = main_activity.findViewById(R.id.timer);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			view.setLayoutParams(layoutParams);
			layoutParams.rightMargin = 40;
			setViewRotation(view, ui_rotation);

			view = main_activity.findViewById(R.id.flash_off);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);



			view = main_activity.findViewById(R.id.exposure_lock);
//			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_bottom, 0);
//			layoutParams.addRule(left_of, R.id.popup);
//			layoutParams.addRule(right_of, 0);
//			view.setLayoutParams(layoutParams);
//			setViewRotation(view, ui_rotation);
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.exposure);
//			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_bottom, 0);
//			layoutParams.addRule(left_of, R.id.exposure_lock);
//			layoutParams.addRule(right_of, 0);
//			view.setLayoutParams(layoutParams);
//			setViewRotation(view, ui_rotation);
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.switch_video);
//			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_bottom, 0);
//			layoutParams.addRule(left_of, R.id.exposure);
//			layoutParams.addRule(right_of, 0);
//			view.setLayoutParams(layoutParams);
//			setViewRotation(view, ui_rotation);
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.switch_camera);
//			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_parent_left, 0);
//			layoutParams.addRule(align_parent_right, 0);
//			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_bottom, 0);
//			layoutParams.addRule(left_of, R.id.switch_video);
//			layoutParams.addRule(right_of, 0);
//			view.setLayoutParams(layoutParams);
//			setViewRotation(view, ui_rotation);
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.audio_control);
//			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_parent_left, 0);
//			layoutParams.addRule(align_parent_right, 0);
//			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_bottom, 0);
//			layoutParams.addRule(left_of, R.id.switch_camera);
//			layoutParams.addRule(right_of, 0);
//			view.setLayoutParams(layoutParams);
//			setViewRotation(view, ui_rotation);
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.trash);
//			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_bottom, 0);
//			layoutParams.addRule(left_of, R.id.audio_control);
//			layoutParams.addRule(right_of, 0);
//			view.setLayoutParams(layoutParams);
//			setViewRotation(view, ui_rotation);
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.share);
//			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_parent_top, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_bottom, 0);
//			layoutParams.addRule(left_of, R.id.trash);
//			layoutParams.addRule(right_of, 0);
//			view.setLayoutParams(layoutParams);
//			setViewRotation(view, ui_rotation);
			view.setVisibility(View.GONE);


			view = main_activity.findViewById(R.id.back);
			setViewRotation(view, ui_rotation);
			view = main_activity.findViewById(R.id.slowmo);
			setViewRotation(view, ui_rotation);
			view = main_activity.findViewById(R.id.square);
			setViewRotation(view, ui_rotation);
			view = main_activity.findViewById(R.id.longexposure);
			setViewRotation(view, ui_rotation);
			view = main_activity.findViewById(R.id.panorama);
			setViewRotation(view, ui_rotation);
			view = main_activity.findViewById(R.id.gif);
			setViewRotation(view, ui_rotation);
			view = main_activity.findViewById(R.id.timelapse);
			setViewRotation(view, ui_rotation);
			view = main_activity.findViewById(R.id.audioimage);
			setViewRotation(view, ui_rotation);
			view = main_activity.findViewById(R.id.watermark);
			setViewRotation(view, ui_rotation);
			view = main_activity.findViewById(R.id.settings_gear);
			setViewRotation(view, ui_rotation);

			view = main_activity.findViewById(R.id.take_photo);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			layoutParams.bottomMargin = 20;
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);

			view = main_activity.findViewById(R.id.pause_video);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			layoutParams.bottomMargin = 20;
			view.setLayoutParams(layoutParams);
			setViewRotation(view, ui_rotation);

			view = main_activity.findViewById(R.id.zoom);
//			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_parent_left, 0);
//			layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
//			layoutParams.addRule(align_parent_top, 0);
//			layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
//			view.setLayoutParams(layoutParams);
//			view.setRotation(180.0f); // should always match the zoom_seekbar, so that zoom in and out are in the same directions

			view = main_activity.findViewById(R.id.zoom_seekbar);
			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			 if we are showing the zoom control, the align next to that; otherwise have it aligned close to the edge of screen
			if( sharedPreferences.getBoolean(PreferenceKeys.getShowZoomControlsPreferenceKey(), false) ) {
				layoutParams.addRule(align_left, 0);
				layoutParams.addRule(align_right, R.id.zoom);
				layoutParams.addRule(above, R.id.zoom);
				layoutParams.addRule(below, 0);
				// need to clear the others, in case we turn zoom controls on/off
				layoutParams.addRule(align_parent_left, 0);
				layoutParams.addRule(align_parent_right, 0);
				layoutParams.addRule(align_parent_top, 0);
				layoutParams.addRule(align_parent_bottom, 0);
			}
			else {
				layoutParams.addRule(align_parent_left, 0);
				layoutParams.addRule(align_parent_right, RelativeLayout.TRUE);
				layoutParams.addRule(align_parent_top, 0);
				layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
				// need to clear the others, in case we turn zoom controls on/off
				layoutParams.addRule(align_left, 0);
				layoutParams.addRule(align_right, 0);
				layoutParams.addRule(above, 0);
				layoutParams.addRule(below, 0);
			}
//			view.setLayoutParams(layoutParams);
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.focus_seekbar);
//			layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			layoutParams.addRule(align_left, R.id.preview);
//			layoutParams.addRule(align_right, 0);
//			layoutParams.addRule(left_of, R.id.zoom_seekbar);
//			layoutParams.addRule(right_of, 0);
//			layoutParams.addRule(align_parent_top, 0);
//			layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
//			view.setLayoutParams(layoutParams);
		}

		{
			// set seekbar info
			int width_dp;
			if( ui_rotation == 0 || ui_rotation == 180 ) {
				width_dp = 300;
			}
			else {
				width_dp = 200;
			}
			int height_dp = 50;
			final float scale = main_activity.getResources().getDisplayMetrics().density;
			int width_pixels = (int) (width_dp * scale + 0.5f); // convert dps to pixels
			int height_pixels = (int) (height_dp * scale + 0.5f); // convert dps to pixels
			int exposure_zoom_gap = (int) (4 * scale + 0.5f); // convert dps to pixels

			View view = main_activity.findViewById(R.id.exposure_container);
			setViewRotation(view, ui_rotation);
			view = main_activity.findViewById(R.id.exposure_seekbar);
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
			lp.width = width_pixels;
			lp.height = height_pixels;
			view.setLayoutParams(lp);
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.exposure_seekbar_zoom);
//			setViewRotation(view, ui_rotation);
//			view.setAlpha(0.5f);

			// n.b., using left_of etc doesn't work properly when using rotation (as the amount of space reserved is based on the UI elements before being rotated)
//			if( ui_rotation == 0 ) {
//				view.setTranslationX(0);
//				view.setTranslationY(height_pixels+exposure_zoom_gap);
//			}
//			else if( ui_rotation == 90 ) {
//				view.setTranslationX(-height_pixels-exposure_zoom_gap);
//				view.setTranslationY(0);
//			}
//			else if( ui_rotation == 180 ) {
//				view.setTranslationX(0);
//				view.setTranslationY(-height_pixels-exposure_zoom_gap);
//			}
//			else if( ui_rotation == 270 ) {
//				view.setTranslationX(height_pixels+exposure_zoom_gap);
//				view.setTranslationY(0);
//			}
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.manual_exposure_container);
			setViewRotation(view, ui_rotation);
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.iso_seekbar);
//			lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			lp.width = width_pixels;
//			lp.height = height_pixels;
//			view.setLayoutParams(lp);
			view.setVisibility(View.GONE);

			view = main_activity.findViewById(R.id.exposure_time_seekbar);
//			lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
//			lp.width = width_pixels;
//			lp.height = height_pixels;
//			view.setLayoutParams(lp);
			view.setVisibility(View.GONE);
		}

		{
			View view = main_activity.findViewById(R.id.popup_container);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
			//layoutParams.addRule(left_of, R.id.popup);
			layoutParams.addRule(align_right, R.id.popup);
			layoutParams.addRule(below, R.id.popup);
			layoutParams.addRule(align_parent_bottom, RelativeLayout.TRUE);
			layoutParams.addRule(above, 0);
			layoutParams.addRule(align_parent_top, 0);
			view.setLayoutParams(layoutParams);

			setViewRotation(view, ui_rotation);
			// reset:
			view.setTranslationX(0.0f);
			view.setTranslationY(0.0f);
			if( MyDebug.LOG ) {
				Log.d(TAG, "popup view width: " + view.getWidth());
				Log.d(TAG, "popup view height: " + view.getHeight());
			}
			if( ui_rotation == 0 || ui_rotation == 180 ) {
				view.setPivotX(view.getWidth()/2.0f);
				view.setPivotY(view.getHeight()/2.0f);
			}
			else {
				view.setPivotX(view.getWidth());
				view.setPivotY(ui_placement_right ? 0.0f : view.getHeight());
				if( ui_placement_right ) {
					if( ui_rotation == 90 )
						view.setTranslationY( view.getWidth() );
					else if( ui_rotation == 270 )
						view.setTranslationX( - view.getHeight() );
				}
				else {
					if( ui_rotation == 90 )
						view.setTranslationX( - view.getHeight() );
					else if( ui_rotation == 270 )
						view.setTranslationY( - view.getWidth() );
				}
			}
		}
//		setTakePhotoIcon();
		// no need to call setSwitchCameraContentDescription()

		if( MyDebug.LOG ) {
			Log.d(TAG, "layoutUI: total time: " + (System.currentTimeMillis() - debug_time));
		}
    }

    public void disableTakePhoto()
    {
        View view = main_activity.findViewById(R.id.take_photo);
        RelativeLayout.LayoutParams layoutParams;
        layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
        view.setLayoutParams(layoutParams);
		view.setEnabled(false);

    }

	public void enableTakePhoto()
	{
		View view = main_activity.findViewById(R.id.take_photo);
		RelativeLayout.LayoutParams layoutParams;
		layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
		view.setLayoutParams(layoutParams);
		view.setEnabled(true);

	}

    /** Set icon for taking photos vs videos.
	 *  Also handles content descriptions for the take photo button and switch video button.
     */
    public void setTakePhotoIcon() {
		Log.d(TAG, "setTakePhotoIcon() " + main_activity.getPreview() );
//		if( MyDebug.LOG )

		if( main_activity.getPreview() != null ) {
			Log.d(TAG, "setTakePhotoIcon()2222");
			ImageButton view = (ImageButton)main_activity.findViewById(R.id.take_photo);
			int resource;
			int content_description;
			int switch_video_content_description;
			if( main_activity.getPreview().isVideo() ) {
//				if( MyDebug.LOG )
					Log.d(TAG, "set icon to video " );
				if (main_activity.getPreview().isVideoRecording())
//					resource = R.drawable.take_video_recording;
				resource = R.drawable.start_video_record_button;

//				else resource = R.drawable.take_video_selector;
				else resource = R.drawable.start_video_record_button;

				content_description = main_activity.getPreview().isVideoRecording() ? R.string.stop_video : R.string.start_video;
				switch_video_content_description = R.string.switch_to_photo;
			}
			else {
//				if( MyDebug.LOG )
					Log.d(TAG, "set icon to photo " +  main_activity.getPreview().isVideoRecording());
				resource = R.drawable.take_photo_button;
//				resource = R.drawable.take_photo_selector;
//				resource = R.drawable.take_video_selector;
				content_description = R.string.take_photo;
				switch_video_content_description = R.string.switch_to_video;
			}
			view.setImageResource(resource);
			view.setContentDescription( main_activity.getResources().getString(content_description) );
			view.setTag(resource); // for testing

			view = (ImageButton)main_activity.findViewById(R.id.switch_video);
			view.setContentDescription( main_activity.getResources().getString(switch_video_content_description) );
		}

		Log.d(TAG, "setTakePhotoIcon()1111");
    }

    /** Set content description for switch camera button.
     */
    public void setSwitchCameraContentDescription() {
		if( MyDebug.LOG )
			Log.d(TAG, "setSwitchCameraContentDescription()");
		if( main_activity.getPreview() != null && main_activity.getPreview().canSwitchCamera() ) {
			ImageButton view = (ImageButton)main_activity.findViewById(R.id.switch_camera);
			int content_description;
			int cameraId = main_activity.getNextCameraId();
		    if( main_activity.getPreview().getCameraControllerManager().isFrontFacing( cameraId ) ) {
				content_description = R.string.switch_to_front_camera;
		    }
		    else {
				content_description = R.string.switch_to_back_camera;
		    }
			if( MyDebug.LOG )
				Log.d(TAG, "content_description: " + main_activity.getResources().getString(content_description));
			view.setContentDescription( main_activity.getResources().getString(content_description) );
//			view.setVisibility(View.GONE);
		}
    }

	/** Set content description for pause video button.
	 */
	public void setPauseVideoContentDescription() {
		if (MyDebug.LOG)
			Log.d(TAG, "setPauseVideoContentDescription()");
		View pauseVideoButton = main_activity.findViewById(R.id.pause_video);
		int content_description;
		if( main_activity.getPreview().isVideoRecordingPaused() ) {
			content_description = R.string.resume_video;
		}
		else {
			content_description = R.string.pause_video;
		}
		if( MyDebug.LOG )
			Log.d(TAG, "content_description: " + main_activity.getResources().getString(content_description));
		pauseVideoButton.setContentDescription(main_activity.getResources().getString(content_description));
	}

    public boolean getUIPlacementRight() {
    	return this.ui_placement_right;
    }

    public void onOrientationChanged(int orientation) {
		/*if( MyDebug.LOG ) {
			Log.d(TAG, "onOrientationChanged()");
			Log.d(TAG, "orientation: " + orientation);
			Log.d(TAG, "current_orientation: " + current_orientation);
		}*/
		if( orientation == OrientationEventListener.ORIENTATION_UNKNOWN )
			return;
		int diff = Math.abs(orientation - current_orientation);
		if( diff > 180 )
			diff = 360 - diff;
		// only change orientation when sufficiently changed
		if( diff > 60 ) {
		    orientation = (orientation + 45) / 90 * 90;
		    orientation = orientation % 360;
		    if( orientation != current_orientation ) {
			    this.current_orientation = orientation;
				if( MyDebug.LOG ) {
					Log.d(TAG, "current_orientation is now: " + current_orientation);
				}
			    layoutUI();
			}
		}
	}

    public void setImmersiveMode(final boolean immersive_mode) {
		if( MyDebug.LOG )
			Log.d(TAG, "setImmersiveMode: " + immersive_mode);
    	this.immersive_mode = immersive_mode;
		main_activity.runOnUiThread(new Runnable() {
			public void run() {
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
				// if going into immersive mode, the we should set GONE the ones that are set GONE in showGUI(false)
		    	final int visibility_gone = immersive_mode ? View.GONE : View.VISIBLE;
		    	final int visibility = immersive_mode ? View.GONE : View.VISIBLE;
				Log.d(TAG," immersive ");
				if( MyDebug.LOG )
					Log.d(TAG, "setImmersiveMode: set visibility: " + visibility);
		    	// n.b., don't hide share and trash buttons, as they require immediate user input for us to continue
			    View switchCameraButton = main_activity.findViewById(R.id.switch_camera);
			    View switchVideoButton = main_activity.findViewById(R.id.switch_video);
			    View exposureButton = main_activity.findViewById(R.id.exposure);
			    View exposureLockButton = main_activity.findViewById(R.id.exposure_lock);
			    View audioControlButton = main_activity.findViewById(R.id.audio_control);
			    View popupButton = main_activity.findViewById(R.id.popup);
			    View galleryButton = main_activity.findViewById(R.id.gallery);
			    View settingsButton = main_activity.findViewById(R.id.settings);
			    View zoomControls = main_activity.findViewById(R.id.zoom);
			    View zoomSeekBar = main_activity.findViewById(R.id.zoom_seekbar);
			    if( main_activity.getPreview().getCameraControllerManager().getNumberOfCameras() > 1 )
			    	switchCameraButton.setVisibility(visibility);
		    		switchVideoButton.setVisibility(visibility);
			    if( main_activity.supportsExposureButton() )
			    	exposureButton.setVisibility(visibility);
			    if( main_activity.getPreview().supportsExposureLock() )
			    	exposureLockButton.setVisibility(visibility);
			    if( main_activity.hasAudioControl() )
			    	audioControlButton.setVisibility(visibility);
		    	popupButton.setVisibility(visibility);
			    galleryButton.setVisibility(visibility);
			    settingsButton.setVisibility(visibility);
				if( MyDebug.LOG ) {
					Log.d(TAG, "has_zoom: " + main_activity.getPreview().supportsZoom());
				}
				if( main_activity.getPreview().supportsZoom() && sharedPreferences.getBoolean(PreferenceKeys.getShowZoomControlsPreferenceKey(), false) ) {
					zoomControls.setVisibility(visibility);
				}
				if( main_activity.getPreview().supportsZoom() && sharedPreferences.getBoolean(PreferenceKeys.getShowZoomSliderControlsPreferenceKey(), true) ) {
					zoomSeekBar.setVisibility(visibility);
				}
        		String pref_immersive_mode = sharedPreferences.getString(PreferenceKeys.getImmersiveModePreferenceKey(), "immersive_mode_low_profile");
        		if( pref_immersive_mode.equals("immersive_mode_everything") ) {
					if( sharedPreferences.getBoolean(PreferenceKeys.getShowTakePhotoPreferenceKey(), true) ) {
						View takePhotoButton = main_activity.findViewById(R.id.take_photo);
						takePhotoButton.setVisibility(visibility);
					}
					if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && main_activity.getPreview().isVideoRecording() ) {
						View pauseVideoButton = main_activity.findViewById(R.id.pause_video);
						pauseVideoButton.setVisibility(visibility);
					}
        		}
				if( !immersive_mode ) {
					// make sure the GUI is set up as expected
					Log.d(TAG," !immersive ");
					showGUI(show_gui);
				}
			}
		});
    }

    public boolean inImmersiveMode() {
    	return immersive_mode;
    }

    public void showGUI(final boolean show) {
		if( MyDebug.LOG )
			Log.d(TAG, "showGUI: " + show);
		this.show_gui = show;
		if( inImmersiveMode() )
			return;
		if( show && main_activity.usingKitKatImmersiveMode() ) {
			// call to reset the timer
			main_activity.initImmersiveMode();
		}
		main_activity.runOnUiThread(new Runnable() {
			public void run() {
		    	final int visibility = show ? View.VISIBLE : View.GONE;
			    View switchCameraButton = main_activity.findViewById(R.id.switch_camera);
			    View switchVideoButton = main_activity.findViewById(R.id.switch_video);
			    View exposureButton = main_activity.findViewById(R.id.exposure);
			    View exposureLockButton = main_activity.findViewById(R.id.exposure_lock);
			    View audioControlButton = main_activity.findViewById(R.id.audio_control);
			    View popupButton = main_activity.findViewById(R.id.popup);

				View cameraSetting = main_activity.findViewById(R.id.settings_dots);
				View timer = main_activity.findViewById(R.id.timer);
				View hdr = main_activity.findViewById(R.id.hdr);
				View flash_auto = main_activity.findViewById(R.id.flash_auto);
			    if( main_activity.getPreview().getCameraControllerManager().getNumberOfCameras() > 1 )
//			    	switchCameraButton.setVisibility(visibility);
//                switchCameraButton.setVisibility(View.GONE);
			    if( !main_activity.getPreview().isVideo() )
//			    	switchVideoButton.setVisibility(visibility); // still allow switch video when recording video
//				switchVideoButton.setVisibility(View.GONE);
			    if( main_activity.supportsExposureButton() && !main_activity.getPreview().isVideo() ) // still allow exposure when recording video
//			    	exposureButton.setVisibility(visibility);
			    if( main_activity.getPreview().supportsExposureLock() && !main_activity.getPreview().isVideo() ) // still allow exposure lock when recording video
//			    	exposureLockButton.setVisibility(visibility);
			    if( main_activity.hasAudioControl() )
//			    	audioControlButton.setVisibility(visibility);
			    if( !show ) {
			    	closePopup(); // we still allow the popup when recording video, but need to update the UI (so it only shows flash options), so easiest to just close
			    }
			    if( !main_activity.getPreview().isVideo() || !main_activity.getPreview().supportsFlash() );
//			    	popupButton.setVisibility(visibility); // still allow popup in order to change flash mode when recording video
			}
		});
    }


	public void flashAuto(){

		Log.d(TAG, "flashauto");

		flash = "flash_auto";
		main_activity.getPreview().updateFlash(flash, true);
//        Camera.Parameters p = CameraController1.camera.getParameters();
//        p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
//        CameraController1.camera.setParameters(p);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();


		ImageButton flash_auto = (ImageButton)main_activity.findViewById(R.id.flash_auto);
		ImageButton flash_off = (ImageButton)main_activity.findViewById(R.id.flash_off);
		ImageButton flash_on = (ImageButton)main_activity.findViewById(R.id.flash_on);
		ImageButton hdr = (ImageButton)main_activity.findViewById(R.id.hdr);
		ImageButton settings_dots = (ImageButton)main_activity.findViewById(R.id.settings_dots);
		ImageButton timer = (ImageButton)main_activity.findViewById(R.id.timer);

		if (settings_dots.isShown() && timer.isShown()){
			Log.d(TAG," isShown flashAuto ");
			flash_auto.setImageResource(R.drawable.flash_auto);
			flash_off.setImageResource(R.drawable.flash_off);
			flash_on.setImageResource(R.drawable.flash_on);

			hdr.setVisibility(View.INVISIBLE);
			flash_off.setVisibility(View.VISIBLE);
			flash_on.setVisibility(View.VISIBLE);
			settings_dots.setVisibility(View.INVISIBLE);
			timer.setVisibility(View.INVISIBLE);

			editor = sharedPreferences.edit();
			editor.putString(PreferenceKeys.getFlashAuto(), flash);
			editor.apply();
		}
		else if (!settings_dots.isShown() && !timer.isShown() ){
			Log.d(TAG," !isShown flashAuto " );

			if (!hdrstate)
			{
				Log.d(TAG," !hdrstate1 flashAuto " );
//				flash_off.setImageResource(R.drawable.ic_hdr_off);

				editor = sharedPreferences.edit();
				editor.putBoolean(PreferenceKeys.getHdrState(), true);
				editor.apply();
			}
			else
			{
				Log.d(TAG," !hdrstate2 flashAuto " + hdrstate);
//				flash_off.setImageResource(R.drawable.ic_hdr_on);

				editor = sharedPreferences.edit();
				editor.putBoolean(PreferenceKeys.getHdrState(), false);
				editor.apply();
			}

			flash_auto.setImageResource(R.drawable.flash_auto);
			flash_on.setImageResource(R.drawable.flash_on);

			hdr.setVisibility(View.VISIBLE);
			flash_off.setVisibility(View.INVISIBLE);
			flash_on.setVisibility(View.INVISIBLE);
			settings_dots.setVisibility(View.VISIBLE);
			timer.setVisibility(View.VISIBLE);

			editor = sharedPreferences.edit();
			editor.putBoolean(PreferenceKeys.getHdrState(), true);
			editor.apply();
		}


//		if (!isTimer){
//			Log.d(TAG, " !isTimer ");
//
//			flash_auto.setImageResource(R.drawable.flash_auto);
//			flash_off.setImageResource(R.drawable.ic_hdr_on);
//			timer.setImageResource(R.drawable.sec_3);
//
//			flash_off.setVisibility(View.VISIBLE);
//			flash_auto.setVisibility(View.VISIBLE);
//			settings_dots.setVisibility(View.VISIBLE);
//			timer.setVisibility(View.VISIBLE);
//		}

		isTimer = true;
	}


	public void hdrState(){
			Log.d(TAG, "hdrState");
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		String photo_mode_pref = sharedPreferences.getString(PreferenceKeys.getPhotoModePreferenceKey(), "preference_photo_mode_std");
		ImageButton hdr = (ImageButton)main_activity.findViewById(R.id.hdr);



//		MyApplicationInterface.PhotoMode photo_mode = main_activity.getApplicationInterface().getPhotoMode().HDR;
//		final List<String> photo_modes = new ArrayList<>();
//		final List<MyApplicationInterface.PhotoMode> photo_mode_values = new ArrayList<>();

//        Camera.Parameters p = CameraController1.camera.getParameters();

		isHdrState = sharedPreferences.getBoolean(PreferenceKeys.getHdrState(), isHdrState);
//		hdrtest = sharedPreferences.getString(PreferenceKeys.getPhotoModePreferenceKey(), "");
		int cameraId = main_activity.getApplicationInterface().getCameraIdPref();

		CameraController.ErrorCallback previewErrorCallback = new CameraController.ErrorCallback() {
			public void onError() {
				if( MyDebug.LOG )
					Log.e(TAG, "error from CameraController: preview failed to start");
				main_activity.getApplicationInterface().onFailedStartPreview();
			}
		};

		CameraController.ErrorCallback cameraErrorCallback = new CameraController.ErrorCallback() {
			public void onError() {
				if( MyDebug.LOG )
					Log.e(TAG, "error from CameraController: camera device failed");
				if( camera_controller != null ) {
					camera_controller = null;
					main_activity.getApplicationInterface().onCameraError();
				}
			}
		};

		if (isHdrState){
			hdr.setImageResource(R.drawable.ic_hdr_on);
			Log.d(TAG,"updateForSettings1  ");
//            p.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
//            CameraController1.camera.setParameters(p);
//			photo_mode.add( getResources().getString(R.string.photo_mode_hdr) );


//			main_activity.updateForSettings();
//			Camera.Parameters p = CameraController1.camera.getParameters();
//			CameraController1.camera.setParameters(p);
//			p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

//				boolean done_hdr_info = sharedPreferences.contains(PreferenceKeys.getHDRInfoPreferenceKey());
//				if( !done_hdr_info ) {
//					popup_view = new PopupView(this.main_activity);
//					popup_view.showInfoDialog(R.string.photo_mode_hdr, R.string.hdr_info, PreferenceKeys.getHDRInfoPreferenceKey());
//				}
//			editor.putString(PreferenceKeys.getPhotoModePreferenceKey(), "preference_photo_mode_hdr");

//			boolean hdr1 = photo_mode_pref.equals("preference_photo_mode_hdr");
//			if( hdr1 && main_activity.supportsHDR() )
//				return MyApplicationInterface.PhotoMode.HDR;

//			photo_mode_values.add( MyApplicationInterface.PhotoMode.Standard );
//			main_activity.getApplicationInterface().getExpoBracketingNImagesPref();

//			main_activity.getApplicationInterface().onBurstPictureTaken(images, current_date);
			CameraController camera_controller = main_activity.getPreview().camera_controller;
//			camera_controller = new CameraController2(this.main_activity.getPreview(), cameraId, previewErrorCallback, cameraErrorCallback);



			editor = sharedPreferences.edit();
			editor.putBoolean(PreferenceKeys.getHdrState(), false);
//			editor.putString(PreferenceKeys.getPhotoModePreferenceKey(), "preference_photo_mode_std");
			editor.apply();
			Log.d(TAG, " hdrstate1 " + isHdrState + "  " + flash );
		}
		else{

			hdr.setImageResource(R.drawable.ic_hdr_off);
//            p.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
//            CameraController1.camera.setParameters(p);
//			MyApplicationInterface.PhotoMode photo_mode1 = main_activity.getApplicationInterface().getPhotoMode().Standard;


			editor = sharedPreferences.edit();
			editor.putBoolean(PreferenceKeys.getHdrState(), true);
//			editor.putString(PreferenceKeys.getPhotoModePreferenceKey(), "preference_photo_mode_hdr");
			editor.apply();
			Log.d(TAG, " hdrstate2 " + isHdrState  );
		}
	}

	public void flashOff(){
		Log.d(TAG," flashoff ");

		flash = "flash_off";

        Camera.Parameters p = CameraController1.camera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        CameraController1.camera.setParameters(p);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		ImageButton flash_auto = (ImageButton)main_activity.findViewById(R.id.flash_auto);
		ImageButton flash_off = (ImageButton)main_activity.findViewById(R.id.flash_off);
		ImageButton flash_on = (ImageButton)main_activity.findViewById(R.id.flash_on);
		ImageButton hdr = (ImageButton)main_activity.findViewById(R.id.hdr);
		ImageButton settings_dots = (ImageButton)main_activity.findViewById(R.id.settings_dots);
		ImageButton timer = (ImageButton)main_activity.findViewById(R.id.timer);

		isHdrState = sharedPreferences.getBoolean(PreferenceKeys.getHdrState(), isHdrState);

		if (flash_on.isShown() && isTimer ){
			Log.d(TAG, " isshown flashoff " + " hdrstate3 " + isHdrState + " flash " + flash);

			flash_auto.setImageResource(R.drawable.flash_off);
			flash_on.setImageResource(R.drawable.flash_on);

			flash_auto.setVisibility(View.VISIBLE);
			flash_off.setVisibility(View.INVISIBLE);
			settings_dots.setVisibility(View.VISIBLE);
			timer.setVisibility(View.VISIBLE);
			flash_on.setVisibility(View.INVISIBLE);
			hdr.setVisibility(View.VISIBLE);

			editor = sharedPreferences.edit();
			editor.putString(PreferenceKeys.getFlashAuto(), flash);
			editor.apply();
		}
		else if (!flash_on.isShown() && isTimer ) {
			Log.d(TAG, " !isshown flashoff " + isHdrState);

			if (isHdrState)
			{
				Log.d(TAG, " !isshown flashoff isFlash1 " + isHdrState);
//				hdr.setImageResource(R.drawable.ic_hdr_on);
				editor = sharedPreferences.edit();
				editor.putBoolean(PreferenceKeys.getHdrState(), false);
				editor.apply();
			}
			else
			{
				Log.d(TAG, " !isshown flashoff isFlash2 " + isHdrState);
//				hdr.setImageResource(R.drawable.ic_hdr_off);
				editor = sharedPreferences.edit();
				editor.putBoolean(PreferenceKeys.getHdrState(), true);
				editor.apply();
			}
		}

	}

	public void flashOn(){
		flash = "flash_on";
		main_activity.getPreview().updateFlash(flash, true);

//		Log.d(TAG," flashon " + " qwe " + qwe);
//        Camera.Parameters p = CameraController1.camera.getParameters();
//        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//        CameraController1.camera.setParameters(p);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		ImageButton flash_auto = (ImageButton)main_activity.findViewById(R.id.flash_auto);
		ImageButton flash_off = (ImageButton)main_activity.findViewById(R.id.flash_off);
		ImageButton flash_on = (ImageButton)main_activity.findViewById(R.id.flash_on);
		ImageButton hdr = (ImageButton)main_activity.findViewById(R.id.hdr);
		ImageButton settings_dots = (ImageButton)main_activity.findViewById(R.id.settings_dots);
		ImageButton timer = (ImageButton)main_activity.findViewById(R.id.timer);

		isHdrState = sharedPreferences.getBoolean(PreferenceKeys.getHdrState(), isHdrState);

		if (isHdrState)
		{
			Log.d(TAG, " isshown flashon isFlash1 " + isHdrState + " flash " + flash);
//			hdr.setImageResource(R.drawable.ic_hdr_on);



			editor = sharedPreferences.edit();
			editor.putBoolean(PreferenceKeys.getHdrState(), false);
			editor.putString(PreferenceKeys.getFlashAuto(), flash);
			editor.apply();
		}
		else
		{
			Log.d(TAG, " !isshown flashon isFlash2 " + isHdrState + " flash " + flash );
//			hdr.setImageResource(R.drawable.ic_hdr_off);
			editor = sharedPreferences.edit();
			editor.putBoolean(PreferenceKeys.getHdrState(), true);
			editor.putString(PreferenceKeys.getFlashAuto(), flash);
			editor.apply();
		}

//		hdr.setImageResource(R.drawable.ic_hdr_on);
		flash_on.setImageResource(R.drawable.flash_on);
		flash_auto.setImageResource(R.drawable.flash_on);

		flash_auto.setVisibility(View.VISIBLE);
		hdr.setVisibility(View.VISIBLE);
		settings_dots.setVisibility(View.VISIBLE);
		timer.setVisibility(View.VISIBLE);
		flash_on.setVisibility(View.INVISIBLE);
		flash_off.setVisibility(View.INVISIBLE);
	}

	public void timer(){
		Log.d(TAG, " timertest ");
		ImageButton timer = (ImageButton)main_activity.findViewById(R.id.timer);
		ImageButton flash_auto = (ImageButton)main_activity.findViewById(R.id.flash_auto);
		ImageButton hdr = (ImageButton)main_activity.findViewById(R.id.hdr);
		ImageButton flash_on = (ImageButton)main_activity.findViewById(R.id.flash_on);
		ImageButton settings_dots = (ImageButton)main_activity.findViewById(R.id.settings_dots);
		ImageButton flash_off = (ImageButton)main_activity.findViewById(R.id.flash_off);

		ImageButton sec_off = (ImageButton)main_activity.findViewById(R.id.timer_off);
		ImageButton sec_3 = (ImageButton)main_activity.findViewById(R.id.sec_3);
		ImageButton sec_5 = (ImageButton)main_activity.findViewById(R.id.sec_5);
		ImageButton sec_9 = (ImageButton)main_activity.findViewById(R.id.sec_9);

//		flash_auto.setImageResource(R.drawable.sec_3);
////		hdr.setImageResource(R.drawable.sec_5);
//		flash_on.setImageResource(R.drawable.sec_9);
//
		flash_auto.setVisibility(View.INVISIBLE);
		hdr.setVisibility(View.INVISIBLE);
//		flash_on.setVisibility(View.INVISIBLE);
		settings_dots.setVisibility(View.INVISIBLE);
		timer.setVisibility(View.INVISIBLE);
//
//		isTimer = false;

		sec_off.setVisibility(View.VISIBLE);
		sec_3.setVisibility(View.VISIBLE);
		sec_5.setVisibility(View.VISIBLE);
		sec_9.setVisibility(View.VISIBLE);

	}

	public void timer_off(){
		Log.d(TAG," timeroff ");

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		ImageButton timer = (ImageButton)main_activity.findViewById(R.id.timer);
		ImageButton flash_auto = (ImageButton)main_activity.findViewById(R.id.flash_auto);
		ImageButton hdr = (ImageButton)main_activity.findViewById(R.id.hdr);
		ImageButton flash_on = (ImageButton)main_activity.findViewById(R.id.flash_on);
		ImageButton settings_dots = (ImageButton)main_activity.findViewById(R.id.settings_dots);
		ImageButton flash_off = (ImageButton)main_activity.findViewById(R.id.flash_off);

		ImageButton sec_off = (ImageButton)main_activity.findViewById(R.id.timer_off);
		ImageButton sec_3 = (ImageButton)main_activity.findViewById(R.id.sec_3);
		ImageButton sec_5 = (ImageButton)main_activity.findViewById(R.id.sec_5);
		ImageButton sec_9 = (ImageButton)main_activity.findViewById(R.id.sec_9);

		isHdrState = sharedPreferences.getBoolean(PreferenceKeys.getHdrState(), isHdrState);

		if (isHdrState)
		{
			Log.d(TAG, " isshown flashon isFlash1 " + isHdrState);
//			hdr.setImageResource(R.drawable.ic_hdr_on);
			editor = sharedPreferences.edit();
			editor.putBoolean(PreferenceKeys.getHdrState(), false);
			editor.apply();
		}
		else
		{
			Log.d(TAG, " !isshown flashon isFlash2 " + isHdrState);
//			hdr.setImageResource(R.drawable.ic_hdr_off);
			editor = sharedPreferences.edit();
			editor.putBoolean(PreferenceKeys.getHdrState(), true);
			editor.apply();
		}

//		if (flash.equals("flashauto")){
//			Log.d(TAG, " flash1 1 " + flash);
//		}
//		else if (flash.equals("flashoff")){
//			Log.d(TAG, " flash1 2 " + flash);
//		}
//		else if (flash.equals("flashon")){
//			Log.d(TAG, " flash1 3 " + flash);
//		}

//		flash_auto.setImageResource(R.drawable.flash_on);
		timer.setImageResource(R.drawable.ic_timer);

		sec_off.setVisibility(View.INVISIBLE);
		sec_3.setVisibility(View.INVISIBLE);
		sec_5.setVisibility(View.INVISIBLE);
		sec_9.setVisibility(View.INVISIBLE);
		settings_dots.setVisibility(View.VISIBLE);
		timer.setVisibility(View.VISIBLE);
		flash_auto.setVisibility(View.VISIBLE);
		hdr.setVisibility(View.VISIBLE);
	}

	public void sec_three(){
		Log.d(TAG," sec3 " );

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		ImageButton timer = (ImageButton)main_activity.findViewById(R.id.timer);
		ImageButton flash_auto = (ImageButton)main_activity.findViewById(R.id.flash_auto);
		ImageButton hdr = (ImageButton)main_activity.findViewById(R.id.hdr);
		ImageButton flash_on = (ImageButton)main_activity.findViewById(R.id.flash_on);
		ImageButton settings_dots = (ImageButton)main_activity.findViewById(R.id.settings_dots);
		ImageButton flash_off = (ImageButton)main_activity.findViewById(R.id.flash_off);

		ImageButton sec_off = (ImageButton)main_activity.findViewById(R.id.timer_off);
		ImageButton sec_3 = (ImageButton)main_activity.findViewById(R.id.sec_3);
		ImageButton sec_5 = (ImageButton)main_activity.findViewById(R.id.sec_5);
		ImageButton sec_9 = (ImageButton)main_activity.findViewById(R.id.sec_9);

		isHdrState = sharedPreferences.getBoolean(PreferenceKeys.getHdrState(), isHdrState);

//		if (flash.equals("flashauto")){
//			Log.d(TAG, " flash1 1 " + flash);
//		}
//		else if (flash.equals("flashoff")){
//			Log.d(TAG, " flash1 2 " + flash);
//		}
//		else if (flash.equals("flashon")){
//			Log.d(TAG, " flash1 3 " + flash);
//		}

		timer.setImageResource(R.drawable.sec_3);

		sec_off.setVisibility(View.INVISIBLE);
		sec_3.setVisibility(View.INVISIBLE);
		sec_5.setVisibility(View.INVISIBLE);
		sec_9.setVisibility(View.INVISIBLE);
		settings_dots.setVisibility(View.VISIBLE);
		timer.setVisibility(View.VISIBLE);
		flash_auto.setVisibility(View.VISIBLE);
		hdr.setVisibility(View.VISIBLE);

		main_activity.getPreview().isTakingPhotoOrOnTimer();
	}


	public void sec_five(){
		Log.d(TAG," sec5 ");

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		ImageButton timer = (ImageButton)main_activity.findViewById(R.id.timer);
		ImageButton flash_auto = (ImageButton)main_activity.findViewById(R.id.flash_auto);
		ImageButton hdr = (ImageButton)main_activity.findViewById(R.id.hdr);
		ImageButton flash_on = (ImageButton)main_activity.findViewById(R.id.flash_on);
		ImageButton settings_dots = (ImageButton)main_activity.findViewById(R.id.settings_dots);
		ImageButton flash_off = (ImageButton)main_activity.findViewById(R.id.flash_off);

		ImageButton sec_off = (ImageButton)main_activity.findViewById(R.id.timer_off);
		ImageButton sec_3 = (ImageButton)main_activity.findViewById(R.id.sec_3);
		ImageButton sec_5 = (ImageButton)main_activity.findViewById(R.id.sec_5);
		ImageButton sec_9 = (ImageButton)main_activity.findViewById(R.id.sec_9);

		isHdrState = sharedPreferences.getBoolean(PreferenceKeys.getHdrState(), isHdrState);


//		if (flash.equals("flashauto")){
//			Log.d(TAG, " flash1 1 " + flash);
//		}
//		else if (flash.equals("flashoff")){
//			Log.d(TAG, " flash1 2 " + flash);
//		}
//		else if (flash.equals("flashon")){
//			Log.d(TAG, " flash1 3 " + flash);
//		}

//		flash_auto.setImageResource(R.drawable.flash_on);
		timer.setImageResource(R.drawable.sec_5);

		sec_off.setVisibility(View.INVISIBLE);
		sec_3.setVisibility(View.INVISIBLE);
		sec_5.setVisibility(View.INVISIBLE);
		sec_9.setVisibility(View.INVISIBLE);
		settings_dots.setVisibility(View.VISIBLE);
		timer.setVisibility(View.VISIBLE);
		flash_auto.setVisibility(View.VISIBLE);
		hdr.setVisibility(View.VISIBLE);
	}

	public void sec_nine(){
		Log.d(TAG," sec9 ");

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		ImageButton timer = (ImageButton)main_activity.findViewById(R.id.timer);
		ImageButton flash_auto = (ImageButton)main_activity.findViewById(R.id.flash_auto);
		ImageButton hdr = (ImageButton)main_activity.findViewById(R.id.hdr);
		ImageButton flash_on = (ImageButton)main_activity.findViewById(R.id.flash_on);
		ImageButton settings_dots = (ImageButton)main_activity.findViewById(R.id.settings_dots);
		ImageButton flash_off = (ImageButton)main_activity.findViewById(R.id.flash_off);

		ImageButton sec_off = (ImageButton)main_activity.findViewById(R.id.timer_off);
		ImageButton sec_3 = (ImageButton)main_activity.findViewById(R.id.sec_3);
		ImageButton sec_5 = (ImageButton)main_activity.findViewById(R.id.sec_5);
		ImageButton sec_9 = (ImageButton)main_activity.findViewById(R.id.sec_9);

		isHdrState = sharedPreferences.getBoolean(PreferenceKeys.getHdrState(), isHdrState);

		if (flash.equals("flashauto")){
			Log.d(TAG, " flash1 1 " + flash);
		}
		else if (flash.equals("flashoff")){
			Log.d(TAG, " flash1 2 " + flash);
		}
		else if (flash.equals("flashon")){
			Log.d(TAG, " flash1 3 " + flash);
		}

//		flash_auto.setImageResource(R.drawable.flash_on);
		timer.setImageResource(R.drawable.sec_9);

		sec_off.setVisibility(View.INVISIBLE);
		sec_3.setVisibility(View.INVISIBLE);
		sec_5.setVisibility(View.INVISIBLE);
		sec_9.setVisibility(View.INVISIBLE);
		settings_dots.setVisibility(View.VISIBLE);
		timer.setVisibility(View.VISIBLE);
		flash_auto.setVisibility(View.VISIBLE);
		hdr.setVisibility(View.VISIBLE);

	}


    public void settings_dots(){
		Log.d(TAG, " settings_dots ");

		closePopup();
        RelativeLayout settings_relative = (RelativeLayout)main_activity.findViewById(R.id.settings_relative);
		ImageButton settings_dots = (ImageButton)main_activity.findViewById(R.id.settings_dots);

		settings_dots.setVisibility(View.INVISIBLE);
		settings_relative.setVisibility(View.VISIBLE);
    }


    public void back_setting(){
		Log.d(TAG, " back_setting ");

		RelativeLayout settings_relative = (RelativeLayout)main_activity.findViewById(R.id.settings_relative);
		ImageButton settings_dots = (ImageButton)main_activity.findViewById(R.id.settings_dots);

		settings_relative.setVisibility(View.INVISIBLE);
		settings_dots.setVisibility(View.VISIBLE);

	}

	public void setting_gear(){
		Log.d(TAG, " setting_gear ");

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		RelativeLayout settings_gear1 = (RelativeLayout)main_activity.findViewById(R.id.settings_gear1);
		RelativeLayout settings_relative = (RelativeLayout)main_activity.findViewById(R.id.settings_relative);
		ImageButton settings_dots = (ImageButton)main_activity.findViewById(R.id.settings_dots);
		SwitchCompat shutter_switch_compat = (SwitchCompat)main_activity.findViewById(R.id.shutter_switch_compat);

		settings_relative.setVisibility(View.INVISIBLE);
		settings_dots.setVisibility(View.VISIBLE);
		settings_gear1.setVisibility(View.VISIBLE);

		final boolean enable_sound = main_activity.getApplicationInterface().getShutterSoundPref();

		shutter_switch_compat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
				{

					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor = sharedPreferences.edit();
					editor.putBoolean(PreferenceKeys.getShutterSoundPreferenceKey(), true);
					editor.apply();
					boolean enable_sound = main_activity.getApplicationInterface().getShutterSoundPref();

					Log.d(TAG, "ischecked " + " enable_sound " + enable_sound);
//					main_activity.getPreview().camera_controller.enableShutterSound(true);
				}
				else
				{
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor = sharedPreferences.edit();
					editor.putBoolean(PreferenceKeys.getShutterSoundPreferenceKey(), false);
					editor.apply();
					boolean enable_sound = main_activity.getApplicationInterface().getShutterSoundPref();

					Log.d(TAG, "ischecked " + " enable_sound " + enable_sound);
//					main_activity.getPreview().camera_controller.enableShutterSound(enable_sound);
				}
			}
		});
	}

	public void storage_path(){
		Log.d(TAG, " storagepath ");

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		final TextView settings_storagepath_text = (TextView)main_activity.findViewById(R.id.settings_storagepath_text);

		final Dialog d = new BottomSheetDialog(this.main_activity);
		d.setContentView(R.layout.dialog_storagepath);
		d.setCancelable(true);
		d.show();

		final RadioButton internal_radio = (RadioButton)d.findViewById(R.id.internal_radio);
		final RadioButton external_radio = (RadioButton)d.findViewById(R.id.external_radio);
		RelativeLayout internal_relative = (RelativeLayout)d.findViewById(R.id.internal_relative);
		RelativeLayout external_relative = (RelativeLayout)d.findViewById(R.id.external_relative);
		Button storagepath_cancel = (Button)d.findViewById(R.id.storagepath_cancel);

		storage_path = sharedPreferences.getString(PreferenceKeys.getStoragePathState(), storage_path);
		if (storage_path.equals("Internal"))
		{
			internal_radio.setChecked(true);
		}
		else if (storage_path.equals("External"))
		{
			external_radio.setChecked(true);
		}


		internal_relative.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				storage_path = "Internal";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getStoragePathState(), storage_path);
				editor.apply();
				external_radio.setChecked(true);
				d.dismiss();
				settings_storagepath_text.setText(storage_path);
			}
		});

		external_relative.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				storage_path = "External";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getStoragePathState(), storage_path);
				editor.apply();
				internal_radio.setChecked(true);
				d.dismiss();
				settings_storagepath_text.setText(storage_path);
			}
		});

		internal_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				storage_path = "Internal";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getStoragePathState(), storage_path);
				editor.apply();
				external_radio.setChecked(true);
				d.dismiss();
				settings_storagepath_text.setText(storage_path);
			}
		});

		external_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				storage_path = "External";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getStoragePathState(), storage_path);
				editor.apply();
				internal_radio.setChecked(true);
				d.dismiss();
				settings_storagepath_text.setText(storage_path);
			}
		});

		storagepath_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});


		Log.d(TAG, "storage_path " + storage_path);

	}

	public void camera_resolution(){

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		final TextView settings_cameraresolution_txt = (TextView)main_activity.findViewById(R.id.settings_cameraresolution_txt);

		final Dialog d = new BottomSheetDialog(this.main_activity);
		d.setContentView(R.layout.dialog_cameraresolution);
		d.setCancelable(true);
		d.show();

		final RadioButton fourthree_radio = (RadioButton)d.findViewById(R.id.fourthree_radio);
		final RadioButton onesixnine_radio = (RadioButton)d.findViewById(R.id.onesixnine_radio);
		final RadioButton oneone_radio = (RadioButton)d.findViewById(R.id.oneone_radio);
		RelativeLayout camera_reso_oneone = (RelativeLayout)d.findViewById(R.id.camera_reso_oneone);
		RelativeLayout camera_reso_fourthree = (RelativeLayout)d.findViewById(R.id.camera_reso_fourthree);
		RelativeLayout camera_reso_onesix = (RelativeLayout)d.findViewById(R.id.camera_reso_onesix);
		Button camera_reso_cancel = (Button)d.findViewById(R.id.camera_reso_cancel);

		camera_resolution = sharedPreferences.getString(PreferenceKeys.getCameraResolution(), camera_resolution);

		if (camera_resolution.equals("4:3"))
		{
			fourthree_radio.setChecked(true);
		}
		else if (camera_resolution.equals("16:9"))
		{
			onesixnine_radio.setChecked(true);
		}
		else if (camera_resolution.equals("1:1"))
		{
			oneone_radio.setChecked(true);
		}

		fourthree_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				camera_resolution = "4:3";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getCameraResolution(), camera_resolution);
				editor.apply();
				onesixnine_radio.setChecked(false);
				oneone_radio.setChecked(false);
				fourthree_radio.setChecked(true);
				d.dismiss();
				settings_cameraresolution_txt.setText(camera_resolution);
			}
		});

		onesixnine_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				camera_resolution = "16:9";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getCameraResolution(), camera_resolution);
				editor.apply();
				onesixnine_radio.setChecked(true);
				oneone_radio.setChecked(false);
				fourthree_radio.setChecked(false);
				d.dismiss();
				settings_cameraresolution_txt.setText(camera_resolution);
			}
		});

		oneone_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				camera_resolution = "1:1";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getCameraResolution(), camera_resolution);
				editor.apply();
				onesixnine_radio.setChecked(false);
				oneone_radio.setChecked(true);
				fourthree_radio.setChecked(false);
				d.dismiss();
				settings_cameraresolution_txt.setText(camera_resolution);
			}
		});

		camera_reso_oneone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				camera_resolution = "1:1";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getCameraResolution(), camera_resolution);
				editor.apply();
				onesixnine_radio.setChecked(false);
				oneone_radio.setChecked(true);
				fourthree_radio.setChecked(false);
				d.dismiss();
				settings_cameraresolution_txt.setText(camera_resolution);
			}
		});

		camera_reso_fourthree.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				camera_resolution = "4:3";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getCameraResolution(), camera_resolution);
				editor.apply();
				onesixnine_radio.setChecked(false);
				oneone_radio.setChecked(false);
				fourthree_radio.setChecked(true);
				d.dismiss();
				settings_cameraresolution_txt.setText(camera_resolution);
			}
		});

		camera_reso_onesix.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				camera_resolution = "16:9";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getCameraResolution(), camera_resolution);
				editor.apply();
				onesixnine_radio.setChecked(true);
				oneone_radio.setChecked(false);
				fourthree_radio.setChecked(false);
				d.dismiss();
				settings_cameraresolution_txt.setText(camera_resolution);
			}
		});

		camera_reso_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});

	}


	public void camera_screen_brightness(){

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		final TextView settings_camera_brightness_text = (TextView)main_activity.findViewById(R.id.settings_camera_brightness_text);

		final Dialog d = new BottomSheetDialog(this.main_activity);
		d.setContentView(R.layout.dialog_camerascreen_brightness);
		d.setCancelable(true);
		d.show();

		RelativeLayout camera_brightness_lowlight = (RelativeLayout)d.findViewById(R.id.camera_brightness_lowlight);
		RelativeLayout camera_brightness_normal = (RelativeLayout)d.findViewById(R.id.camera_brightness_normal);
		RelativeLayout camera_brightness_highlight = (RelativeLayout)d.findViewById(R.id.camera_brightness_highlight);
		final RadioButton lowlight_radio = (RadioButton)d.findViewById(R.id.lowlight_radio);
		final RadioButton normal_radio = (RadioButton)d.findViewById(R.id.normal_radio);
		final RadioButton highlight_radio = (RadioButton)d.findViewById(R.id.highlight_radio);
		Button camera_brightness_cancel = (Button)d.findViewById(R.id.camera_brightness_cancel);

		camera_resolution = sharedPreferences.getString(PreferenceKeys.getCameraResolution(), camera_resolution);

		if (camera_brightness.equals("Low light"))
		{
			lowlight_radio.setChecked(true);
		}
		else if (camera_brightness.equals("Normal"))
		{
			normal_radio.setChecked(true);
		}
		else if (camera_brightness.equals("High light"))
		{
			highlight_radio.setChecked(true);
		}

		camera_brightness_lowlight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				camera_brightness = "Low light";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getCameraBrightness(), camera_brightness);
				editor.apply();
				lowlight_radio.setChecked(true);
				normal_radio.setChecked(false);
				highlight_radio.setChecked(false);
				d.dismiss();
				settings_camera_brightness_text.setText(camera_brightness);
			}
		});

		camera_brightness_normal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				camera_brightness = "Normal";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getCameraBrightness(), camera_brightness);
				editor.apply();
				lowlight_radio.setChecked(false);
				normal_radio.setChecked(true);
				highlight_radio.setChecked(false);
				d.dismiss();
				settings_camera_brightness_text.setText(camera_brightness);
			}
		});

		camera_brightness_highlight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				camera_brightness = "High light";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getCameraBrightness(), camera_brightness);
				editor.apply();
				lowlight_radio.setChecked(false);
				normal_radio.setChecked(false);
				highlight_radio.setChecked(true);
				d.dismiss();
				settings_camera_brightness_text.setText(camera_brightness);
			}
		});

		lowlight_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				camera_brightness = "Low light";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getCameraBrightness(), camera_brightness);
				editor.apply();
				lowlight_radio.setChecked(true);
				normal_radio.setChecked(false);
				highlight_radio.setChecked(false);
				d.dismiss();
				settings_camera_brightness_text.setText(camera_brightness);
			}
		});

		normal_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				camera_brightness = "Normal";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getCameraBrightness(), camera_brightness);
				editor.apply();
				lowlight_radio.setChecked(false);
				normal_radio.setChecked(true);
				highlight_radio.setChecked(false);
				d.dismiss();
				settings_camera_brightness_text.setText(camera_brightness);
			}
		});

		highlight_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				camera_brightness = "High light";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getCameraBrightness(), camera_brightness);
				editor.apply();
				lowlight_radio.setChecked(false);
				normal_radio.setChecked(false);
				highlight_radio.setChecked(true);
				d.dismiss();
				settings_camera_brightness_text.setText(camera_brightness);
			}
		});

		camera_brightness_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});

	}

	public void bit_rate(){

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		final TextView settings_bitrate_txt = (TextView)main_activity.findViewById(R.id.settings_bitrate_txt);

		final Dialog d = new BottomSheetDialog(this.main_activity);
		d.setContentView(R.layout.dialog_bitrate);
		d.setCancelable(true);
		d.show();

		RelativeLayout bitrate_high = (RelativeLayout)d.findViewById(R.id.bitrate_high);
		RelativeLayout bitrate_mid = (RelativeLayout)d.findViewById(R.id.bitrate_mid);
		RelativeLayout bitrate_low = (RelativeLayout)d.findViewById(R.id.bitrate_low);
		final RadioButton high_radio = (RadioButton)d.findViewById(R.id.high_radio);
		final RadioButton mid_radio = (RadioButton)d.findViewById(R.id.mid_radio);
		final RadioButton low_radio = (RadioButton)d.findViewById(R.id.low_radio);
		Button bitrate_cancel = (Button)d.findViewById(R.id.bitrate_cancel);

		bitrate = sharedPreferences.getString(PreferenceKeys.getBitrate(), bitrate);
		if (bitrate.equals("High"))
		{
			high_radio.setChecked(true);
		}
		else if (bitrate.equals("Mid"))
		{
			mid_radio.setChecked(true);
		}
		else if (bitrate.equals("Low"))
		{
			low_radio.setChecked(true);
		}

		bitrate_high.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bitrate = "High";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getBitrate(), bitrate);
				editor.apply();
				low_radio.setChecked(false);
				mid_radio.setChecked(false);
				high_radio.setChecked(true);
				d.dismiss();
				settings_bitrate_txt.setText(bitrate);
			}
		});

		bitrate_mid.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bitrate = "Mid";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getBitrate(), bitrate);
				editor.apply();
				low_radio.setChecked(false);
				mid_radio.setChecked(true);
				high_radio.setChecked(false);
				d.dismiss();
				settings_bitrate_txt.setText(bitrate);
			}
		});

		bitrate_low.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bitrate = "Low";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getBitrate(), bitrate);
				editor.apply();
				low_radio.setChecked(true);
				mid_radio.setChecked(false);
				high_radio.setChecked(false);
				d.dismiss();
				settings_bitrate_txt.setText(bitrate);
			}
		});

		high_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bitrate = "High";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getBitrate(), bitrate);
				editor.apply();
				low_radio.setChecked(false);
				mid_radio.setChecked(false);
				high_radio.setChecked(true);
				d.dismiss();
				settings_bitrate_txt.setText(bitrate);
			}
		});

		mid_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bitrate = "Mid";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getBitrate(), bitrate);
				editor.apply();
				low_radio.setChecked(false);
				mid_radio.setChecked(true);
				high_radio.setChecked(false);
				d.dismiss();
				settings_bitrate_txt.setText(bitrate);
			}
		});

		low_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bitrate = "Low";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getBitrate(), bitrate);
				editor.apply();
				low_radio.setChecked(true);
				mid_radio.setChecked(false);
				high_radio.setChecked(false);
				d.dismiss();
				settings_bitrate_txt.setText(bitrate);
			}
		});

		bitrate_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
	}

	public void timer_picture(){

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        final TextView settings_timerpicture_txt = (TextView)main_activity.findViewById(R.id.settings_timerpicture_txt);

		final Dialog d = new BottomSheetDialog(this.main_activity);
		d.setContentView(R.layout.dialog_timer_picture);
		d.setCancelable(true);
		d.show();

		RelativeLayout close_relative = (RelativeLayout)d.findViewById(R.id.close_relative);
		RelativeLayout threesec_relative = (RelativeLayout)d.findViewById(R.id.threesec_relative);
		RelativeLayout fivesec_relative = (RelativeLayout)d.findViewById(R.id.fivesec_relative);
		RelativeLayout tensec_relative = (RelativeLayout)d.findViewById(R.id.tensec_relative);
		final RadioButton close_radio = (RadioButton)d.findViewById(R.id.close_radio);
		final RadioButton threesec_radio = (RadioButton)d.findViewById(R.id.threesec_radio);
		final RadioButton fivesec_radio = (RadioButton)d.findViewById(R.id.fivesec_radio);
		final RadioButton tensec_radio = (RadioButton)d.findViewById(R.id.tensec_radio);
		Button timerpicture_cancel = (Button)d.findViewById(R.id.timerpicture_cancel);

        timer_picture = sharedPreferences.getString(PreferenceKeys.getTimerPicture(), timer_picture);
        if (timer_picture.equals("Close"))
        {
            close_radio.setChecked(true);
        }
        else if (timer_picture.equals("3s"))
        {
            threesec_radio.setChecked(true);
        }
        else if (timer_picture.equals("5s"))
        {
            fivesec_radio.setChecked(true);
        }
        else if (timer_picture.equals("10s"))
        {
            tensec_radio.setChecked(true);
        }

        close_relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer_picture = "Close";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor = sharedPreferences.edit();
                editor.putString(PreferenceKeys.getBitrate(), timer_picture);
                editor.apply();
                close_radio.setChecked(true);
                threesec_radio.setChecked(false);
                fivesec_radio.setChecked(false);
                tensec_radio.setChecked(false);
                d.dismiss();
				settings_timerpicture_txt.setText(timer_picture);
            }
        });

        threesec_relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer_picture = "3s";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor = sharedPreferences.edit();
                editor.putString(PreferenceKeys.getBitrate(), timer_picture);
                editor.apply();
                close_radio.setChecked(false);
                threesec_radio.setChecked(true);
                fivesec_radio.setChecked(false);
                tensec_radio.setChecked(false);
                d.dismiss();
				settings_timerpicture_txt.setText(timer_picture);
            }
        });

        fivesec_relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer_picture = "5s";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor = sharedPreferences.edit();
                editor.putString(PreferenceKeys.getBitrate(), timer_picture);
                editor.apply();
                close_radio.setChecked(false);
                threesec_radio.setChecked(false);
                fivesec_radio.setChecked(true);
                tensec_radio.setChecked(false);
                d.dismiss();
				settings_timerpicture_txt.setText(timer_picture);
            }
        });

        tensec_relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer_picture = "10s";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor = sharedPreferences.edit();
                editor.putString(PreferenceKeys.getBitrate(), timer_picture);
                editor.apply();
                close_radio.setChecked(false);
                threesec_radio.setChecked(false);
                fivesec_radio.setChecked(true);
                tensec_radio.setChecked(false);
                d.dismiss();
				settings_timerpicture_txt.setText(timer_picture);
            }
        });


        close_radio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer_picture = "Close";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor = sharedPreferences.edit();
                editor.putString(PreferenceKeys.getBitrate(), timer_picture);
                editor.apply();
                close_radio.setChecked(true);
                threesec_radio.setChecked(false);
                fivesec_radio.setChecked(false);
                tensec_radio.setChecked(false);
                d.dismiss();
				settings_timerpicture_txt.setText(timer_picture);
            }
        });

        threesec_radio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer_picture = "3s";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor = sharedPreferences.edit();
                editor.putString(PreferenceKeys.getBitrate(), timer_picture);
                editor.apply();
                close_radio.setChecked(false);
                threesec_radio.setChecked(true);
                fivesec_radio.setChecked(false);
                tensec_radio.setChecked(false);
                d.dismiss();
				settings_timerpicture_txt.setText(timer_picture);
            }
        });

        fivesec_radio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer_picture = "5s";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor = sharedPreferences.edit();
                editor.putString(PreferenceKeys.getBitrate(), timer_picture);
                editor.apply();
                close_radio.setChecked(false);
                threesec_radio.setChecked(false);
                fivesec_radio.setChecked(true);
                tensec_radio.setChecked(false);
                d.dismiss();
				settings_timerpicture_txt.setText(timer_picture);
            }
        });

        tensec_radio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer_picture = "10s";
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor = sharedPreferences.edit();
                editor.putString(PreferenceKeys.getBitrate(), timer_picture);
                editor.apply();
                close_radio.setChecked(false);
                threesec_radio.setChecked(false);
                fivesec_radio.setChecked(true);
                tensec_radio.setChecked(false);
                d.dismiss();
				settings_timerpicture_txt.setText(timer_picture);
            }
        });

		timerpicture_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});

	}

	public void theme_color(){

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		SharedPreferences.Editor editor = sharedPreferences.edit();

		final TextView settings_themecolor_txt = (TextView)main_activity.findViewById(R.id.settings_themecolor_txt);

		final Dialog d = new BottomSheetDialog(this.main_activity);
		d.setContentView(R.layout.dialog_themecolor);
		d.setCancelable(true);
		d.show();

		RelativeLayout themecolor_red = (RelativeLayout)d.findViewById(R.id.themecolor_red);
		RelativeLayout themecolor_green = (RelativeLayout)d.findViewById(R.id.themecolor_green);
		RelativeLayout themecolor_blue = (RelativeLayout)d.findViewById(R.id.themecolor_blue);
		final RadioButton red_radio = (RadioButton)d.findViewById(R.id.red_radio);
		final RadioButton green_radio = (RadioButton)d.findViewById(R.id.green_radio);
		final RadioButton blue_radio = (RadioButton)d.findViewById(R.id.blue_radio);
		Button themecolor_cancel = (Button)d.findViewById(R.id.themecolor_cancel);

		theme_color = sharedPreferences.getString(PreferenceKeys.getThemeColor(), theme_color);
		if (theme_color.equals("Red"))
		{
			red_radio.setChecked(true);
		}
		else if (theme_color.equals("Green"))
		{
			green_radio.setChecked(true);
		}
		else if (theme_color.equals("Blue"))
		{
			blue_radio.setChecked(true);
		}

		themecolor_red.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				theme_color = "Red";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getBitrate(), theme_color);
				editor.apply();
				red_radio.setChecked(true);
				green_radio.setChecked(false);
				blue_radio.setChecked(false);
				d.dismiss();
				settings_themecolor_txt.setText(theme_color);
			}
		});

		themecolor_green.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				theme_color = "Green";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getBitrate(), theme_color);
				editor.apply();
				red_radio.setChecked(false);
				green_radio.setChecked(true);
				blue_radio.setChecked(false);
				d.dismiss();
				settings_themecolor_txt.setText(theme_color);
			}
		});

		themecolor_blue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				theme_color = "Blue";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getBitrate(), theme_color);
				editor.apply();
				red_radio.setChecked(false);
				green_radio.setChecked(false);
				blue_radio.setChecked(true);
				d.dismiss();
				settings_themecolor_txt.setText(theme_color);
			}
		});


		red_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				theme_color = "red";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getBitrate(), theme_color);
				editor.apply();
				red_radio.setChecked(true);
				green_radio.setChecked(false);
				blue_radio.setChecked(false);
				d.dismiss();
				settings_themecolor_txt.setText(theme_color);
			}
		});

		green_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				theme_color = "green";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getBitrate(), theme_color);
				editor.apply();
				red_radio.setChecked(false);
				green_radio.setChecked(true);
				blue_radio.setChecked(false);
				d.dismiss();
				settings_themecolor_txt.setText(theme_color);
			}
		});

		blue_radio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				theme_color = "blue";
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor = sharedPreferences.edit();
				editor.putString(PreferenceKeys.getBitrate(), theme_color);
				editor.apply();
				red_radio.setChecked(false);
				green_radio.setChecked(false);
				blue_radio.setChecked(true);
				d.dismiss();
				settings_themecolor_txt.setText(theme_color);
			}
		});

		themecolor_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				d.dismiss();
			}
		});
	}

    public void audioControlStarted() {
		ImageButton view = (ImageButton)main_activity.findViewById(R.id.audio_control);
		view.setImageResource(R.drawable.ic_mic_red_48dp);
		view.setContentDescription( main_activity.getResources().getString(R.string.audio_control_stop) );
    }

    public void audioControlStopped() {
		ImageButton view = (ImageButton)main_activity.findViewById(R.id.audio_control);
		view.setImageResource(R.drawable.ic_mic_white_48dp);
		view.setContentDescription( main_activity.getResources().getString(R.string.audio_control_start) );
    }

    public void toggleExposureUI() {
		if( MyDebug.LOG )
			Log.d(TAG, "toggleExposureUI");
		closePopup();
		View exposure_seek_bar = main_activity.findViewById(R.id.exposure_container);
		int exposure_visibility = exposure_seek_bar.getVisibility();
		View manual_exposure_seek_bar = main_activity.findViewById(R.id.manual_exposure_container);
		int manual_exposure_visibility = manual_exposure_seek_bar.getVisibility();
		boolean is_open = exposure_visibility == View.VISIBLE || manual_exposure_visibility == View.VISIBLE;
		if( is_open ) {
			clearSeekBar();
		}
		else if( main_activity.getPreview().getCameraController() != null ) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
			String value = sharedPreferences.getString(PreferenceKeys.getISOPreferenceKey(), main_activity.getPreview().getCameraController().getDefaultISO());
			if( main_activity.getPreview().usingCamera2API() && !value.equals("auto") ) {
				// with Camera2 API, when using manual ISO we instead show sliders for ISO range and exposure time
				if( main_activity.getPreview().supportsISORange()) {
					manual_exposure_seek_bar.setVisibility(View.VISIBLE);
					SeekBar exposure_time_seek_bar = ((SeekBar)main_activity.findViewById(R.id.exposure_time_seekbar));
					if( main_activity.getPreview().supportsExposureTime() ) {
						exposure_time_seek_bar.setVisibility(View.VISIBLE);
					}
					else {
						exposure_time_seek_bar.setVisibility(View.GONE);
					}
				}
			}
			else {
				if( main_activity.getPreview().supportsExposures() ) {
					exposure_seek_bar.setVisibility(View.VISIBLE);
					ZoomControls seek_bar_zoom = (ZoomControls)main_activity.findViewById(R.id.exposure_seekbar_zoom);
					seek_bar_zoom.setVisibility(View.VISIBLE);
				}
			}
		}
    }

	public void setSeekbarZoom() {
		if( MyDebug.LOG )
			Log.d(TAG, "setSeekbarZoom");
	    SeekBar zoomSeekBar = (SeekBar) main_activity.findViewById(R.id.zoom_seekbar);
		zoomSeekBar.setProgress(main_activity.getPreview().getMaxZoom()-main_activity.getPreview().getCameraController().getZoom());
		if( MyDebug.LOG )
			Log.d(TAG, "progress is now: " + zoomSeekBar.getProgress());
	}

	public void changeSeekbar(int seekBarId, int change) {
		if( MyDebug.LOG )
			Log.d(TAG, "changeSeekbar: " + change);
		SeekBar seekBar = (SeekBar)main_activity.findViewById(seekBarId);
	    int value = seekBar.getProgress();
	    int new_value = value + change;
	    if( new_value < 0 )
	    	new_value = 0;
	    else if( new_value > seekBar.getMax() )
	    	new_value = seekBar.getMax();
		if( MyDebug.LOG ) {
			Log.d(TAG, "value: " + value);
			Log.d(TAG, "new_value: " + new_value);
			Log.d(TAG, "max: " + seekBar.getMax());
		}
	    if( new_value != value ) {
		    seekBar.setProgress(new_value);
	    }
	}

    public void clearSeekBar() {
		View view = main_activity.findViewById(R.id.exposure_container);
		view.setVisibility(View.GONE);
		view = main_activity.findViewById(R.id.exposure_seekbar_zoom);
		view.setVisibility(View.GONE);
		view = main_activity.findViewById(R.id.manual_exposure_container);
		view.setVisibility(View.GONE);
    }

    public void setPopupIcon() {
		if( MyDebug.LOG )
			Log.d(TAG, "setPopupIcon");
		ImageButton popup = (ImageButton)main_activity.findViewById(R.id.popup);
		String flash_value = main_activity.getPreview().getCurrentFlashValue();
		if( MyDebug.LOG )
			Log.d(TAG, "flash_value: " + flash_value);
    	if( flash_value != null && flash_value.equals("flash_off") ) {
    		popup.setImageResource(R.drawable.popup_flash_off);
    	}
    	else if( flash_value != null && flash_value.equals("flash_torch") ) {
    		popup.setImageResource(R.drawable.popup_flash_torch);
    	}
		else if( flash_value != null && ( flash_value.equals("flash_auto") || flash_value.equals("flash_frontscreen_auto") ) ) {
    		popup.setImageResource(R.drawable.popup_flash_auto);
    	}
		else if( flash_value != null && ( flash_value.equals("flash_on") || flash_value.equals("flash_frontscreen_on") ) ) {
    		popup.setImageResource(R.drawable.popup_flash_on);
    	}
    	else if( flash_value != null && flash_value.equals("flash_red_eye") ) {
    		popup.setImageResource(R.drawable.popup_flash_red_eye);
    	}
    	else {
    		popup.setImageResource(R.drawable.popup);
    	}
    }

    public void closePopup() {
		if( MyDebug.LOG )
			Log.d(TAG, "close popup");
		if( popupIsOpen() ) {
			ViewGroup popup_container = (ViewGroup)main_activity.findViewById(R.id.popup_container);
			popup_container.removeAllViews();
			popup_view_is_open = false;
			/* Not destroying the popup doesn't really gain any performance.
			 * Also there are still outstanding bugs to fix if we wanted to do this:
			 *   - Not resetting the popup menu when switching between photo and video mode. See test testVideoPopup().
			 *   - When changing options like flash/focus, the new option isn't selected when reopening the popup menu. See test
			 *     testPopup().
			 *   - Changing settings potentially means we have to recreate the popup, so the natural place to do this is in
			 *     MainActivity.updateForSettings(), but doing so makes the popup close when checking photo or video resolutions!
			 *     See test testSwitchResolution().
			 */
			destroyPopup();
			main_activity.initImmersiveMode(); // to reset the timer when closing the popup
		}
    }

    public boolean popupIsOpen() {
    	return popup_view_is_open;
    }

    public void destroyPopup() {
		if( popupIsOpen() ) {
			closePopup();
		}
		popup_view = null;
    }

    public void togglePopupSettings() {
		final ViewGroup popup_container = (ViewGroup)main_activity.findViewById(R.id.popup_container);
		if( popupIsOpen() ) {
			closePopup();
			return;
		}
		if( main_activity.getPreview().getCameraController() == null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "camera not opened!");
			return;
		}

		if( MyDebug.LOG )
			Log.d(TAG, "open popup");

		clearSeekBar();
		main_activity.getPreview().cancelTimer(); // best to cancel any timer, in case we take a photo while settings window is open, or when changing settings
		main_activity.stopAudioListeners();

    	final long time_s = System.currentTimeMillis();

    	{
			// prevent popup being transparent
			popup_container.setBackgroundColor(Color.BLACK);
			popup_container.setAlpha(0.9f);
		}

    	if( popup_view == null ) {
			if( MyDebug.LOG )
				Log.d(TAG, "create new popup_view");
    		popup_view = new PopupView(main_activity);
    	}
    	else {
			if( MyDebug.LOG )
				Log.d(TAG, "use cached popup_view");
    	}
		popup_container.addView(popup_view);
		popup_view_is_open = true;

        // need to call layoutUI to make sure the new popup is oriented correctly
		// but need to do after the layout has been done, so we have a valid width/height to use
		// n.b., even though we only need the portion of layoutUI for the popup container, there
		// doesn't seem to be any performance benefit in only calling that part
		popup_container.getViewTreeObserver().addOnGlobalLayoutListener(
			new OnGlobalLayoutListener() {
				@SuppressWarnings("deprecation")
				@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
				@Override
			    public void onGlobalLayout() {
					if( MyDebug.LOG )
						Log.d(TAG, "onGlobalLayout()");
					if( MyDebug.LOG )
						Log.d(TAG, "time after global layout: " + (System.currentTimeMillis() - time_s));
					layoutUI();
					if( MyDebug.LOG )
						Log.d(TAG, "time after layoutUI: " + (System.currentTimeMillis() - time_s));
		    		// stop listening - only want to call this once!
		            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
		            	popup_container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		            } else {
		            	popup_container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
		            }

		    		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
		    		String ui_placement = sharedPreferences.getString(PreferenceKeys.getUIPlacementPreferenceKey(), "ui_right");
		    		boolean ui_placement_right = ui_placement.equals("ui_right");
		            ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, ui_placement_right ? 0.0f : 1.0f);
		    		animation.setDuration(100);
		    		popup_container.setAnimation(animation);
		        }
			}
		);

		if( MyDebug.LOG )
			Log.d(TAG, "time to create popup: " + (System.currentTimeMillis() - time_s));
    }

	@SuppressWarnings("deprecation")
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if( MyDebug.LOG )
			Log.d(TAG, "onKeyDown: " + keyCode);
		switch( keyCode ) {
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS: // media codes are for "selfie sticks" buttons
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			case KeyEvent.KEYCODE_MEDIA_STOP:
			{
				if( keyCode == KeyEvent.KEYCODE_VOLUME_UP )

					keydown_volume_up = true;
				else if( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN )
					keydown_volume_down = true;

				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(main_activity);
				String volume_keys = sharedPreferences.getString(PreferenceKeys.getVolumeKeysPreferenceKey(), "volume_take_photo");

				if((keyCode==KeyEvent.KEYCODE_MEDIA_PREVIOUS
						||keyCode==KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
						||keyCode==KeyEvent.KEYCODE_MEDIA_STOP)
						&&!(volume_keys.equals("volume_take_photo"))) {
					AudioManager audioManager = (AudioManager) main_activity.getSystemService(Context.AUDIO_SERVICE);
					if(audioManager==null) break;
					if(!audioManager.isWiredHeadsetOn()) break; // isWiredHeadsetOn() is deprecated, but comment says "Use only to check is a headset is connected or not."
				}

				if( volume_keys.equals("volume_take_photo") ) {
					main_activity.takePicture();
					return true;
				}
				else if( volume_keys.equals("volume_focus") ) {
					if( keydown_volume_up && keydown_volume_down ) {
						if( MyDebug.LOG )
							Log.d(TAG, "take photo rather than focus, as both volume keys are down");
						main_activity.takePicture();
					}
					else if( main_activity.getPreview().getCurrentFocusValue() != null && main_activity.getPreview().getCurrentFocusValue().equals("focus_mode_manual2") ) {
						if( keyCode == KeyEvent.KEYCODE_VOLUME_UP )
							main_activity.changeFocusDistance(-1);
						else
							main_activity.changeFocusDistance(1);
					}
					else {
						// important not to repeatedly request focus, even though main_activity.getPreview().requestAutoFocus() will cancel, as causes problem if key is held down (e.g., flash gets stuck on)
						// also check DownTime vs EventTime to prevent repeated focusing whilst the key is held down
						if( event.getDownTime() == event.getEventTime() && !main_activity.getPreview().isFocusWaiting() ) {
							if( MyDebug.LOG )
								Log.d(TAG, "request focus due to volume key");
							main_activity.getPreview().requestAutoFocus();
						}
					}
					return true;
				}
				else if( volume_keys.equals("volume_zoom") ) {
					if( keyCode == KeyEvent.KEYCODE_VOLUME_UP )
						main_activity.zoomIn();
					else
						main_activity.zoomOut();
					return true;
				}
				else if( volume_keys.equals("volume_exposure") ) {
					if( main_activity.getPreview().getCameraController() != null ) {
						String value = sharedPreferences.getString(PreferenceKeys.getISOPreferenceKey(), main_activity.getPreview().getCameraController().getDefaultISO());
						boolean manual_iso = !value.equals("auto");
						if( keyCode == KeyEvent.KEYCODE_VOLUME_UP ) {
							if( manual_iso ) {
								if( main_activity.getPreview().supportsISORange() )
									main_activity.changeISO(1);
							}
							else
								main_activity.changeExposure(1);
						}
						else {
							if( manual_iso ) {
								if( main_activity.getPreview().supportsISORange() )
									main_activity.changeISO(-1);
							}
							else
								main_activity.changeExposure(-1);
						}
					}
					return true;
				}
				else if( volume_keys.equals("volume_auto_stabilise") ) {
					if( main_activity.supportsAutoStabilise() ) {
						boolean auto_stabilise = sharedPreferences.getBoolean(PreferenceKeys.getAutoStabilisePreferenceKey(), false);
						auto_stabilise = !auto_stabilise;
						SharedPreferences.Editor editor = sharedPreferences.edit();
						editor.putBoolean(PreferenceKeys.getAutoStabilisePreferenceKey(), auto_stabilise);
						editor.apply();
						String message = main_activity.getResources().getString(R.string.preference_auto_stabilise) + ": " + main_activity.getResources().getString(auto_stabilise ? R.string.on : R.string.off);
						main_activity.getPreview().showToast(main_activity.getChangedAutoStabiliseToastBoxer(), message);
					}
					else {
						main_activity.getPreview().showToast(main_activity.getChangedAutoStabiliseToastBoxer(), R.string.auto_stabilise_not_supported);
					}
					return true;
				}
				else if( volume_keys.equals("volume_really_nothing") ) {
					// do nothing, but still return true so we don't change volume either
					return true;
				}
				// else do nothing here, but still allow changing of volume (i.e., the default behaviour)
				break;
			}
			case KeyEvent.KEYCODE_MENU:
			{
				// needed to support hardware menu button
				// tested successfully on Samsung S3 (via RTL)
				// see http://stackoverflow.com/questions/8264611/how-to-detect-when-user-presses-menu-key-on-their-android-device
				main_activity.openSettings();
				return true;
			}
			case KeyEvent.KEYCODE_CAMERA:
			{
				if( event.getRepeatCount() == 0 ) {
					main_activity.takePicture();
					return true;
				}
			}
			case KeyEvent.KEYCODE_FOCUS:
			{
				// important not to repeatedly request focus, even though main_activity.getPreview().requestAutoFocus() will cancel - causes problem with hardware camera key where a half-press means to focus
				// also check DownTime vs EventTime to prevent repeated focusing whilst the key is held down - see https://sourceforge.net/p/opencamera/tickets/174/ ,
				// or same issue above for volume key focus
				if( event.getDownTime() == event.getEventTime() && !main_activity.getPreview().isFocusWaiting() ) {
					if( MyDebug.LOG )
						Log.d(TAG, "request focus due to focus key");
					main_activity.getPreview().requestAutoFocus();
				}
				return true;
			}
			case KeyEvent.KEYCODE_ZOOM_IN:
			{
				main_activity.zoomIn();
				return true;
			}
			case KeyEvent.KEYCODE_ZOOM_OUT:
			{
				main_activity.zoomOut();
				return true;
			}

			case KeyEvent.KEYCODE_BACK:
			{
				RelativeLayout settings_include = (RelativeLayout)main_activity.findViewById(R.id.settings_gear1);
				RelativeLayout settings_relative = (RelativeLayout)main_activity.findViewById(R.id.settings_relative);
				ImageButton settings_dots = (ImageButton)main_activity.findViewById(R.id.settings_dots);

				Log.d(TAG, " KEYCODE_BACK " + settings_relative.isShown() );

				if (settings_include.isShown())
				{
					settings_include.setVisibility(View.INVISIBLE);
				}

				if (settings_relative.isShown())
				{
					settings_dots.setVisibility(View.VISIBLE);
					settings_relative.setVisibility(View.INVISIBLE);
				}

				return true;
			}
		}
		return false;
	}

	public void onKeyUp(int keyCode, KeyEvent event) {
//		if( MyDebug.LOG )
			Log.d(TAG, "onKeyUp: " + keyCode);
		if( keyCode == KeyEvent.KEYCODE_VOLUME_UP )
		{
			keydown_volume_up = false;
			Log.d(TAG, " KEYCODE_VOLUME_UP ");
		}
		else if( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN )
		{
			Log.d(TAG, " KEYCODE_VOLUME_DOWN ");
			keydown_volume_down = false;
		}


	}

    // for testing
    public View getPopupButton(String key) {
    	return popup_view.getPopupButton(key);
    }


}
