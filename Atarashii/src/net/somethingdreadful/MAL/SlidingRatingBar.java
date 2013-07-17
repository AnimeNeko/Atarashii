package net.somethingdreadful.MAL;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RatingBar;

public class SlidingRatingBar extends RatingBar {

    float prevX;
    Context c;

    IUpdateRatingText passer;

    public SlidingRatingBar(Context context) {
        super(context);

        c = context;
    }

    public SlidingRatingBar(Context context, AttributeSet aSet) {
        super(context, aSet);

        c = context;
    }

    public void setPasser(RatingPickerDialogFragment ratingPickerDialogFragment) {
        passer = ratingPickerDialogFragment;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (isPressed()) {
                    final float x = event.getX();
                    final float THRESHOLD = (float)0.5;
                    if (Math.abs(x - prevX) > THRESHOLD) {
                        passer.updateRatingText(getProgress());

                    }
                    prevX = x;
                }
                break;
             case MotionEvent.ACTION_DOWN:
            	passer.updateRatingText(getProgress());
            	break;
        }
        return true;
    }

    public interface IUpdateRatingText {
        void updateRatingText(int rating);
    }



}
