package im.actor.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;

import im.actor.sdk.controllers.pickers.file.view.MaterialInterpolator;
import im.actor.sdk.util.Screen;

public class ViewUtils {
    public static void expand(final View v, int targetHeight) {

        v.measure(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);

        final int initialHeight = new Integer(v.getLayoutParams().height);

        v.getLayoutParams().height = initialHeight;
        Animation a = new ExpandAnimation(v, targetHeight, initialHeight);

        a.setDuration(((targetHeight > 0 ? targetHeight * 3 : initialHeight * 3) / Screen.dp(1)));
        a.setInterpolator(MaterialInterpolator.getInstance());
        v.startAnimation(a);
        if (targetHeight == 0) {
            v.setVisibility(View.INVISIBLE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    private static class ExpandAnimation extends Animation {
        private final View v;
        private final int targetHeight;
        private final int initialHeight;
        private int currentHeight;

        public ExpandAnimation(View v, int targetHeight, int initialHeight) {
            this.v = v;
            this.targetHeight = targetHeight;
            this.initialHeight = initialHeight;
            this.currentHeight = initialHeight;

        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (targetHeight > initialHeight) {
                currentHeight =
                        (int) ((targetHeight * interpolatedTime) - initialHeight * interpolatedTime + initialHeight);
            } else {
                currentHeight =
                        (int) (initialHeight - (initialHeight * interpolatedTime) - targetHeight * (1f - interpolatedTime) + targetHeight);
            }

            v.getLayoutParams().height = currentHeight;
            v.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }


    }
}
