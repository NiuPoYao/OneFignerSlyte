package xniuniux.onefignerslyte;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import java.util.ArrayList;

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
    public int mAppShortcutTotal = mLayerTotal * mAppsPerLayer;

    /** for layout **/
    private double mDeltaAngle = Math.PI/mAppsPerLayer*2;
    private float mRadius, mRadiusSecond, mRadiusThird;
    private int mChildWidth, mChildWidthSecond, mChildWidthThird;
    private int mChildAlpha = 192, mChildAlphaSecond = 64, mChildAlphaThird = 64;
    private float mCurrentAngle = 0;
    private int mOriginVertical, mOriginHorizontal;

    /** for touch rotation event **/
    private boolean mRotateEnable = true;
    private Float mStartAngle = null;
    private Boolean mClockwise = null;
    private Boolean mSwitchLayer = null;

    /** animator **/
    private Animator mRotateAnimator;
    private AnimatorSet mSwitchAnimator = new AnimatorSet();

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
        Log.d(LOG_TAG, "on Measure");
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int length = Math.min(widthSize, heightSize);

        mRadius = length * 0.35f;
        mRadiusSecond = mRadius * 0.6f;
        mRadiusThird = mRadius * 0.4f;

        mChildWidth =(int) Math.round( mRadius * 0.8 * Math.sin(mDeltaAngle) );
        mChildWidthSecond = mChildWidth /2;
        mChildWidthThird = mChildWidth /2;

        mChildAlpha = 192;
        mChildAlphaSecond = 64;
        mChildAlphaThird = 64;

        mOriginVertical = Math.round(length/2);
        mOriginHorizontal = Math.round(length/2);

        measureChildren(MeasureSpec.makeMeasureSpec(length, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(length, MeasureSpec.AT_MOST));

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

    public void switchLayer(int switchInt){
        if ( mSwitchAnimator != null && mSwitchAnimator.isRunning() ) {
            return;
        }

        mLayerSelected = (mLayerSelected + switchInt + mLayerTotal) % mLayerTotal;
        Animator anima;

        ArrayList<Animator> animaList = new ArrayList<>();
        if (switchInt == 1) {
            anima = ObjectAnimator.ofFloat(this, "Radius", mRadiusSecond, mRadius);
            animaList.add(anima);
            anima = ObjectAnimator.ofFloat(this, "RadiusSecond", mRadiusThird, mRadiusSecond);
            animaList.add(anima);
            anima = ObjectAnimator.ofFloat(this, "RadiusThird", mRadius, mRadiusThird);
            animaList.add(anima);

            anima = ObjectAnimator.ofInt(this, "ChildWidth", mChildWidthSecond, mChildWidth);
            animaList.add(anima);
            anima = ObjectAnimator.ofInt(this, "ChildWidthSecond", mChildWidthThird, mChildWidthSecond);
            animaList.add(anima);
            anima = ObjectAnimator.ofInt(this, "ChildWidthThird", mChildWidth, mChildWidthThird);
            animaList.add(anima);

            anima = ObjectAnimator.ofInt(this, "ChildAlpha", mChildAlphaSecond, mChildAlpha);
            animaList.add(anima);
            anima = ObjectAnimator.ofInt(this, "ChildAlphaSecond", mChildAlphaThird, mChildAlphaSecond);
            animaList.add(anima);
            anima = ObjectAnimator.ofInt(this, "ChildAlphaThird", mChildAlpha, mChildAlphaThird);
            animaList.add(anima);

        }
        if (switchInt == -1) {
            anima = ObjectAnimator.ofFloat(this, "Radius", mRadiusThird, mRadius);
            animaList.add(anima);
            anima = ObjectAnimator.ofFloat(this, "RadiusSecond", mRadius, mRadiusSecond);
            animaList.add(anima);
            anima = ObjectAnimator.ofFloat(this, "RadiusThird", mRadiusSecond, mRadiusThird);
            animaList.add(anima);

            anima = ObjectAnimator.ofInt(this, "ChildWidth", mChildWidthThird, mChildWidth);
            animaList.add(anima);
            anima = ObjectAnimator.ofInt(this, "ChildWidthSecond", mChildWidth, mChildWidthSecond);
            animaList.add(anima);
            anima = ObjectAnimator.ofInt(this, "ChildWidthThird", mChildWidthSecond, mChildWidthThird);
            animaList.add(anima);

            anima = ObjectAnimator.ofInt(this, "ChildAlpha", mChildAlphaThird, mChildAlpha);
            animaList.add(anima);
            anima = ObjectAnimator.ofInt(this, "ChildAlphaSecond", mChildAlpha, mChildAlphaSecond);
            animaList.add(anima);
            anima = ObjectAnimator.ofInt(this, "ChildAlphaThird", mChildAlphaSecond, mChildAlphaThird);
            animaList.add(anima);

        }
        mSwitchAnimator.playTogether(animaList);
        mSwitchAnimator.setDuration(100).setInterpolator(new DecelerateInterpolator());
        mSwitchAnimator.start();


    }

    public void setRadius(float r){ this.mRadius = r; }
    public void setRadiusSecond(float r){ this.mRadiusSecond = r; }
    public void setRadiusThird(float r){ this.mRadiusThird = r; }
    public void setChildWidth(int w){ this.mChildWidth = w; }
    public void setChildWidthSecond(int w){ this.mChildWidthSecond = w; }
    public void setChildWidthThird(int w){ this.mChildWidthThird = w; }
    public void setChildAlpha(int a){ this.mChildAlpha = a; }
    public void setChildAlphaSecond(int a){ this.mChildAlphaSecond = a; }
    public void setChildAlphaThird(int a){ this.mChildAlphaThird = a; setListLayout();}

    /*public void moveButton(View view, float dx, float dy){
        int childL = Math.round(view.getLeft() + dx);
        int childT = Math.round(view.getTop() + dy);
        view.layout(childL, childT, childL + view.getWidth(), childT + view.getHeight());
    }*/


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
            if (child.getVisibility() == View.GONE) { continue; }
            int thisChildWidth;
            float radius;
            if (i < mAppsPerLayer){
                ((ImageView) child).setImageAlpha(mChildAlpha);
                radius = mRadius;
                thisChildWidth = mChildWidth;
            }else if (i < mAppsPerLayer*2){
                ((ImageView) child).setImageAlpha(mChildAlphaSecond);
                radius = mRadiusSecond;
                thisChildWidth = mChildWidthSecond;
            }else {
                ((ImageView) child).setImageAlpha(mChildAlphaThird);
                radius = mRadiusThird;
                thisChildWidth = mChildWidthThird;
            }
            child.measure(MeasureSpec.makeMeasureSpec(Math.round(thisChildWidth), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(Math.round(thisChildWidth), MeasureSpec.EXACTLY));

            childL = mOriginVertical + (int) Math.round(radius * Math.cos(mCurrentAngle + mDeltaAngle * ( 1 + i % 8) ) - thisChildWidth/2);
            childT = mOriginHorizontal + (int) Math.round(radius * Math.sin(mCurrentAngle + mDeltaAngle * ( 1 + i % 8) ) - thisChildWidth/2);

            child.layout(childL, childT, childL + thisChildWidth, childT + thisChildWidth);
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

    public void setRotateEnable(boolean isEnable){
        mRotateEnable = isEnable;
    }

    public int getAppShortcutsTotal(){
        return mAppShortcutTotal;
    }
}
