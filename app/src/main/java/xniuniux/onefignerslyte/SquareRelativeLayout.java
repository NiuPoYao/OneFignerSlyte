package xniuniux.onefignerslyte;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class SquareRelativeLayout extends RelativeLayout{

    public SquareRelativeLayout(Context context) {
        super(context);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int wms, int hms){
        int widthSize = MeasureSpec.getSize(wms);
        int heightSize = MeasureSpec.getSize(hms);
        int length = Math.min(widthSize, heightSize);

        measureChildren(MeasureSpec.makeMeasureSpec(length, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(length, MeasureSpec.AT_MOST));

        setMeasuredDimension(length,length);
    }

}
