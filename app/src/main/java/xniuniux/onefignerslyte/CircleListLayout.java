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
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created on 2016/5/10.
 */
public class CircleListLayout extends ViewGroup {

    String LOG_TAG = "CircleListLayout";
    private GestureDetector gestureDetector = null;

    /** constant **/
    public int mLayerSelected = 1;
    public final int mLayerTotal = 3;
    public int mAppsPerLayer = 8;

    /** for layout **/
    private double mDeltaAngle = Math.PI/mAppsPerLayer*2;
    private float mRadius, mRadiusSecond, mRadiusThird;
    private float mCurrentAngle = 0;
    private int mOriginVertical, mOriginHorizontal;
    private float mChildWidth, mChildWidthSecond, mChildWidthThird;

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

    public void setCurrentAngle(float angle){
        mClockwise = angle > mCurrentAngle;
        mCurrentAngle = angle;
        setListLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int length = Math.min(widthSize, heightSize);

        mRadius = length * 0.35f;
        mRadiusSecond = mRadius * 0.6f;
        mRadiusThird = mRadius * 0.4f;

        mChildWidth = Math.round( mRadius * 0.8 * Math.sin(mDeltaAngle) );
        mChildWidthSecond = mChildWidth * 0.7f;
        mChildWidthThird = mChildWidth * 0.5f;

        mOriginVertical = Math.round(length/2);
        mOriginHorizontal = Math.round(length/2);

        measureChildren(MeasureSpec.makeMeasureSpec(length, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(length, MeasureSpec.AT_MOST));

        int childCount = getChildCount();
        for (int i=0; i<childCount; i++) {
            View child = getChildAt((i+ mAppsPerLayer * mLayerSelected)%childCount);
            float thisChildWidth = mChildWidthSecond;
            if (i < mAppsPerLayer){
                thisChildWidth = mChildWidth;
            }
            if (i >= mAppsPerLayer*2){
                thisChildWidth = mChildWidthThird;
            }

            child.measure(MeasureSpec.makeMeasureSpec(Math.round(thisChildWidth), MeasureSpec.EXACTLY),
                          MeasureSpec.makeMeasureSpec(Math.round(thisChildWidth), MeasureSpec.EXACTLY));
        }

        setMeasuredDimension(length, length);
    }

    @Override
    protected void onLayout(boolean change, int l, int t, int r, int b){
        Log.d(LOG_TAG,"onLayout");
        setListLayout();
    }

    public void rotateListByAngleAnimator(float angle, long duration){
        if ( mRotateAnimator != null && mRotateAnimator.isRunning() ) {
            return;
        }
        mRotateAnimator = ObjectAnimator.ofFloat(this, "CurrentAngle", mCurrentAngle, mCurrentAngle + angle);
        mRotateAnimator.setInterpolator(new OvershootInterpolator());
        mRotateAnimator.setDuration(duration);
        mRotateAnimator.start();
    }

    private class gListener extends SimpleOnGestureListener{
        String LOG_TAG = "gesture";

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY){
            if (mSwitchLayer == null ){ return true; }
            if (mSwitchLayer){
                float x = e1.getX() - mOriginVertical;
                float y = e1.getY() - mOriginHorizontal;
                if (x*vX<0 || y*vY<0){
                    switchLayer(-1);
                } else {
                    switchLayer(1);
                }

            } else {
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
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dX, float dY) {
            float endY = e2.getY() - mOriginHorizontal;
            float endX = e2.getX() - mOriginVertical;
            if (mSwitchLayer == null){
                if (Math.abs(endY*dX - dY*endX)< (Math.abs(endX)+Math.abs(endY))*20){
                mSwitchLayer = true;
                } else { mSwitchLayer = false; }
            }
            if (!mSwitchLayer){
                float endAngle = (float) Math.atan2(endY, endX);
                setCurrentAngle(endAngle - mStartAngle + mCurrentAngle);
                mStartAngle = endAngle;
            }
            return true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        String LOG_TAG = "intercept touch event";
        boolean  intercept = false;

        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN){
            this.gestureDetector.onTouchEvent(ev);
        }
        if (action == MotionEvent.ACTION_MOVE && mRotateEnable){
            intercept = this.gestureDetector.onTouchEvent(ev);
        }

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
        return consume;
    }

    public void setListLayout(){

        int childCount = getChildCount();
        int childL, childT;

        for (int i=0; i<childCount; i++){
            View child = getChildAt((i+ mAppsPerLayer * mLayerSelected)%childCount);

            int thisChildWidth = child.getMeasuredWidth();
            int thisChildHeight = child.getMeasuredHeight();
            if (child.getVisibility() == View.GONE) { continue; }
            float radius = mRadiusSecond;
            if (i < mAppsPerLayer){
                ((ImageView) child).setImageAlpha(200);
                radius = mRadius;
            }
            if (i >= mAppsPerLayer*2){
                radius = mRadiusThird;
            }

            childL = mOriginVertical + (int) Math.round(radius * Math.cos(mCurrentAngle + mDeltaAngle * ( 1 + i % 8) ) - thisChildWidth/2);
            childT = mOriginHorizontal + (int) Math.round(radius * Math.sin(mCurrentAngle + mDeltaAngle * ( 1 + i % 8) ) - thisChildHeight/2);

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

    public void switchLayer(int i){
        mLayerSelected = (mLayerSelected + i + mLayerTotal) % mLayerTotal;
        Toast.makeText(getContext(), "Layer add " + i + " to " + mLayerSelected,
                Toast.LENGTH_SHORT).show();
        mSwitchLayer = null;
    }

    public void moveButton(View view, float dx, float dy){
        int childL = Math.round(view.getLeft() + dx);
        int childT = Math.round(view.getTop() + dy);
        view.layout(childL, childT, childL + view.getWidth(), childT + view.getHeight());
    }

    public void setRotateEnable(boolean isEnable){
        mRotateEnable = isEnable;
    }

    public int getAppShortcutsNum(){
        return mLayerTotal * mAppsPerLayer;
    }
}
