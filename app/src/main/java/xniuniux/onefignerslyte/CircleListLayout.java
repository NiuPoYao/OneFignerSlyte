package xniuniux.onefignerslyte;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

/**
 * Created on 2016/5/10.
 */
public class CircleListLayout extends ViewGroup {

    String LOG_TAG = "CircleListLayout";
    private GestureDetector gestureDetector = null;

    /** constant **/
    public int layerSelected = 0;
    public final int layerTotal = 3;

    /** for layout **/
    private double mDeltaAngle = Math.PI/4;
    private float mRadius;
    private float mCurrentAngle = 0;
    private int mOriginVertical, mOriginHorizontal;
    private int maxChildWidth;

    /** for touch rotation event **/
    private boolean mRotateEnable = true;
    private Float mStartAngle = null;
    private Boolean mClockwise = null;
    private Boolean mSwitchLayer = null;

    /** animator **/
    private Animator mRotateAnimator;

    public CircleListLayout(Context context) {
        this(context, null);
    }

    public CircleListLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleListLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    protected void initialize(){
        gestureDetector = new GestureDetector(getContext(), new gListener());
        gestureDetector.setIsLongpressEnabled(false);
    }

    public void setMCurrentAngle(float angle){
        mClockwise = angle > mCurrentAngle;
        setListByAngle(angle);
        mCurrentAngle = angle;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int length = Math.min(widthSize, heightSize);
        Log.d(LOG_TAG, "length " + length);
        int childCount = getChildCount();
        for (int i=0; i<childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            measureChildren(MeasureSpec.makeMeasureSpec(length, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(length, MeasureSpec.AT_MOST));
        }

        mRadius = length * 0.35f;
        mOriginVertical = Math.round(length/2);
        mOriginHorizontal = Math.round(length/2);
        maxChildWidth = (int) Math.round( mRadius * Math.sin(mDeltaAngle) );
        setMeasuredDimension(length, length);
    }

    @Override
    protected void onLayout(boolean change, int l, int t, int r, int b){

        Log.d(LOG_TAG, "onLayout");
        int cx = (r - l)/2;
        int cy = (b - t)/2;
        int childL, childT;

        int childCount = getChildCount();
        for (int i=0; i<childCount; i++){
            View child = getChildAt(i);

            if (child.getVisibility() == View.GONE) { continue; }

            int thisChildWidth = child.getMeasuredWidth();
            int thisChildHeight = child.getMeasuredHeight();

            if (thisChildWidth > maxChildWidth) {
                float ratio = thisChildHeight / thisChildWidth;
                thisChildWidth = maxChildWidth;
                thisChildHeight = Math.round(maxChildWidth * ratio);
            }

            childL = cx + (int) Math.round(mRadius * Math.cos(mCurrentAngle + mDeltaAngle * ( 1 + i % 8) ) - thisChildWidth/2);
            childT = cy + (int) Math.round(mRadius * Math.sin(mCurrentAngle + mDeltaAngle * ( 1 + i % 8) ) - thisChildHeight/2);

            child.layout(childL, childT, childL + thisChildWidth, childT + thisChildHeight);
            child.setLongClickable(true);
        }
    }

    public void rotateListByAngleAnimator(float angle, long duration){
        if ( mRotateAnimator != null && mRotateAnimator.isRunning() ) {
            return;
        }
        mRotateAnimator = ObjectAnimator.ofFloat(this, "mCurrentAngle", mCurrentAngle, mCurrentAngle + angle);
        mRotateAnimator.setInterpolator(new OvershootInterpolator());
        mRotateAnimator.setDuration(duration);
        mRotateAnimator.start();
    }

    private class gListener extends SimpleOnGestureListener{
        String DEBUG_TAG = "gesture";

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY){
            if (mSwitchLayer == null ){ return true; }
            if (mSwitchLayer){
                float x = e1.getX() - mOriginVertical;
                float y = e1.getY() - mOriginHorizontal;
                if (x*vX<0 || y*vY<0){
                    layerSelected = switchLayer(-1);
                } else {
                    layerSelected = switchLayer(1);
                }

            } else {
                Log.d(DEBUG_TAG, "onFling: " + e1.toString() + e2.toString());
                float x = e2.getX() - mOriginVertical;
                float y = e2.getY() - mOriginHorizontal;
                float radius = (float) Math.sqrt(x * x + y * y);
                float v = (float) Math.sqrt(vX * vX + vY * vY);
                float flingAngle = 0;
                if (mClockwise) {
                        flingAngle = v / radius / 15;
                    }
                if (!mClockwise){
                        flingAngle = -v / radius / 15;
                    }
                rotateListByAngleAnimator(flingAngle, 750);
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent ev) {
            stopRotateAnimator();
            mStartAngle = (float) Math.atan2( ev.getY() - mOriginVertical, ev.getX() - mOriginVertical);
            Log.d(DEBUG_TAG,"onDown: " + ev.toString());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dX, float dY) {
            float endY = e2.getY() - mOriginHorizontal;
            float endX = e2.getX() - mOriginVertical;
            if (mSwitchLayer == null){
                if (Math.abs(endY*dX - dY*endX)< (Math.abs(endX)+Math.abs(endY))*20){
                mSwitchLayer = true;
                Log.d(DEBUG_TAG, "onScroll switch: " + e1.toString() +e2.toString());
                } else { mSwitchLayer = false; }
            }
            if (!mSwitchLayer){
                float endAngle = (float) Math.atan2(endY, endX);
                Log.d(DEBUG_TAG, "onScroll rotate: " + mStartAngle + " to " + endAngle + e2.toString());
                setMCurrentAngle(endAngle - mStartAngle + mCurrentAngle);
                mStartAngle = endAngle;
            }
            return true;
        }

         /*@Override
        public void onLongPress(MotionEvent event) {
            mDrag = false;
            Log.d(DEBUG_TAG, "onLongPress: " + mDrag);
        }*/

        /*@Override
        public boolean onSingleTapUp(MotionEvent event) {
            Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
            return false;
        }*/

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        String LOG_TAG = "intercept";
        boolean  intercept = false;

        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN){
            this.gestureDetector.onTouchEvent(ev);
        }
        if (action == MotionEvent.ACTION_MOVE && mRotateEnable){
            intercept = this.gestureDetector.onTouchEvent(ev);
        }

        Log.d(LOG_TAG, "return " + intercept + " " + ev.toString());
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        String LOG_TAG = "onTouchEvent";
        int action  = ev.getAction();
        boolean consume = true;
        if (action != MotionEvent.ACTION_DOWN){
            consume = this.gestureDetector.onTouchEvent(ev);
        }
        Log.d(LOG_TAG, "return " + consume + " " + ev.toString());
        return consume;
    }

    public void setListByAngle(float angle){

        int childCount = getChildCount();
        int childL;
        int childT;

        for (int i=0; i<childCount; i++){

            View child = getChildAt(i);
            int thisChildWidth = child.getWidth();
            int thisChildHeight = child.getHeight();

            if (child.getVisibility() == View.GONE) { continue; }

            childL = mOriginVertical + (int) Math.round(mRadius * Math.cos(angle + mDeltaAngle * ( 1 + i % 8) ) - thisChildWidth/2);
            childT = mOriginHorizontal + (int) Math.round(mRadius * Math.sin(angle + mDeltaAngle * ( 1 + i % 8) ) - thisChildHeight/2);

            child.layout(childL, childT, childL + thisChildWidth, childT + thisChildHeight);
        }
    }

    public void stopRotateAnimator(){
        if (mRotateAnimator != null && mRotateAnimator.isRunning()){
            mRotateAnimator.cancel();
        }
        mClockwise = null;
        mSwitchLayer = null;
        mStartAngle = null;
    }

    public int switchLayer(int i){
        int layer = (layerSelected + i + layerTotal) % layerTotal;
        Toast.makeText(getContext(), "Layer add " + i + " to " + layer,
                Toast.LENGTH_SHORT).show();
        mSwitchLayer = null;
        return layer;
    }

    public void moveButton(View view, float dx, float dy){
        int childL = Math.round(view.getLeft() + dx);
        int childT = Math.round(view.getTop() + dy);
        view.layout(childL, childT, childL + view.getWidth(), childT + view.getHeight());
    }


    public interface OnCenterClickListener {
        void onCenterClick();
    }

    public void setRotateEnable(boolean isEnable){
        mRotateEnable = isEnable;
    }

}
