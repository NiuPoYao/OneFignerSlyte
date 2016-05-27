package xniuniux.circlelistlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by xniuniux on 2016/5/10.
 */
public class CircleListLayout extends ViewGroup {

    public int layerSelected = 1;
    public final int layerTotal = 3;

    private double dAngle = Math.PI/4;
    private double radius;

    private int maxChildWidth;

    public CircleListLayout(Context context) {
        this(context, null);
    }

    public CircleListLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleListLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init(attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int length = Math.min(widthSize, heightSize);

        int childCount = getChildCount();
        for (int i=0; i<childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            measureChildren(MeasureSpec.makeMeasureSpec(length, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(length, MeasureSpec.AT_MOST));
        }

        radius = length * 0.35;
        maxChildWidth = (int) Math.round( radius * Math.sin(dAngle) );
        setMeasuredDimension(length, length);
    }

    @Override
    protected void onLayout(boolean change, int l, int t, int r, int b){
        int cx = (r - l)/2;
        int cy = (b - t)/2;
        int ChildL, ChildT;

        int childCount = getChildCount();
        for (int i=0; i<childCount; i++){
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            int thisChildWidth = child.getMeasuredWidth();
            int thisChildHeight = child.getMeasuredHeight();
            if (thisChildWidth > maxChildWidth) {
                float ratio = thisChildHeight / thisChildWidth;
                thisChildWidth = maxChildWidth;
                thisChildHeight = Math.round(maxChildWidth * ratio);
            }
            ChildL = cx + (int) Math.round(radius * Math.sin(dAngle * ( 1 + i % 8) ) - thisChildWidth/2);
            ChildT = cy + (int) Math.round(radius * Math.cos(dAngle * ( 1 + i % 8) ) - thisChildHeight/2);
            //TODO: layout child

            child.layout(ChildL, ChildT, ChildL + thisChildWidth, ChildT + thisChildHeight);
        }
    }
}
