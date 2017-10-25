package net.sourceforge.opencamera.UI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.sourceforge.opencamera.Preview.Preview;
import net.sourceforge.opencamera.R;

/**
 * Created by cti-pdd on 9/7/17.
 */

public class FourthFragment extends Fragment {

    private static final String TAG = "FourthFragment";
    private Preview preview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fourth_frag, container, false);
        LinearLayout layout = (LinearLayout)v.findViewById(R.id.fourfrag);
        Log.d(" first frag ", " frag 1 ");
        TextView tv = (TextView) v.findViewById(R.id.tvFragFourth);
        tv.setText(getArguments().getString("msg"));
//        layout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.d(TAG, " MotionEvent event 4th");
////                return preview.touchEvent(event);
//                return false;
//            }
//        });
        return v;
    }

    public static FourthFragment newInstance(String text) {

        FourthFragment f = new FourthFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

}
