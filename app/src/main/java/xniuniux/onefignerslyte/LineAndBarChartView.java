package xniuniux.onefignerslyte;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;

public class LineAndBarChartView extends ImageView {

    private String LOG_TAG = "LineAndBarChart";

    public ArrayList<String> mHour = new ArrayList<>();
    public ArrayList<Integer> mTemperature = new ArrayList<>();
    public ArrayList<Integer> mPop = new ArrayList<>();
    public ArrayList<Integer> mQpf = new ArrayList<>();
    public ArrayList<Integer> mHumidity = new ArrayList<>();

    float perColumnWidth;
    private float barWidth;
    int dataSize;
    float markerSize;
    float fontSize;
    float density = getResources().getDisplayMetrics().density;

    private Path tempPath = new Path();
    private Paint tempPaint = new Paint();
    private Paint tempMarkerPaint = new Paint();

    private Path HumiPath = new Path();
    private Paint HumiPaint = new Paint();
    private Paint HumiMarkerPaint = new Paint();

    private Path qpfPath = new Path();
    private Paint qpfBarPaint = new Paint();
    private Paint qpfTextPaint = new Paint();
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Path popPath = new Path();
    private Paint popBarPaint = new Paint();
    private Paint popTextPaint = new Paint();

    private PorterDuffXfermode pdfDstOver = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);

    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    public LineAndBarChartView(Context context) {
        super(context);
        init(null, 0);
    }

    public LineAndBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LineAndBarChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        Log.d(LOG_TAG, "init");
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.LineAndBarChartView, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.LineAndBarChartView_exampleString);

        /*if (a.hasValue(R.styleable.LineAndBarChartScrollView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.LineAndBarChartScrollView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }*/

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();


        /*String[] hourarray = {"06","07","08","09","10","11","12","13","14","15","16'","17","18","19"};
        mHour.addAll(Arrays.asList(hourarray));
        Integer[] temparray = {30,31,32,34,32,30,28,29,27,26,25,27,29,32};
        mTemperature.addAll(Arrays.asList(temparray));
        Integer[] humiarray = {55,60,67,65,70,77,72,69,73,75,77,79,82,85};
        mHumidity.addAll(Arrays.asList(humiarray));
        Integer[] qpfarray = {155,60,67,10,1,2,20,49,73,100,180,200,282,285};
        mQpf.addAll(Arrays.asList(qpfarray));*/
    }

    private void invalidateTextPaintAndMeasurements() {

        tempPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_temp_marker));
        tempPaint.setStyle(Paint.Style.STROKE);
        tempPaint.setStrokeWidth(10);

        tempMarkerPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_temp_marker));
        tempMarkerPaint.setStyle(Paint.Style.FILL);

        HumiPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_humidity_marker));
        HumiPaint.setStyle(Paint.Style.STROKE);
        HumiPaint.setStrokeWidth(10);

        HumiMarkerPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_humidity_marker));
        HumiMarkerPaint.setStyle(Paint.Style.FILL);

        qpfBarPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_qpf_bar));
        qpfBarPaint.setStyle(Paint.Style.FILL);
        qpfTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_qpf_number));

        popBarPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_pop_bar));
        popBarPaint.setStyle(Paint.Style.FILL);
        popTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_qpf_number));

        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.color_text));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, textPaint);
            setLayerType(LAYER_TYPE_SOFTWARE, qpfTextPaint);
        }
    }

    @Override
    protected void onMeasure(int wms, int hms){
        super.onMeasure(wms,hms);
        //Log.d(LOG_TAG,"SpecSize: width-" + MeasureSpec.getSize(wms) + ", Height-" + MeasureSpec.getSize(hms));
        if (mHour != null){
            dataSize = mHour.size();
        }

        perColumnWidth = (float) ((View) getParent().getParent()).getWidth() / 18;
        barWidth = perColumnWidth;
        markerSize = perColumnWidth/1.5f < 18* density ? perColumnWidth/1.5f : 18* density;
        fontSize = markerSize*0.6f;
        textPaint.setTextSize(fontSize);
        qpfTextPaint.setTextSize(fontSize);
        popTextPaint.setTextSize(fontSize);
        float width = perColumnWidth*dataSize + getPaddingLeft() + getPaddingRight();
        setMeasuredDimension((int) width, getMeasuredHeight());
        //Log.d(LOG_TAG,"markerSize: " + markerSize + ", getWidth: " + getWidth() + ", mHour size: " + mHour.size());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth()<500 ){return;}
        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        float contentHeight = getHeight() - paddingTop - paddingBottom;

        if (mHour == null || mHour.isEmpty()){
            //Log.d(LOG_TAG,"mHour null");
            return;
        }

        float bottomSpace = 2 * fontSize + density;
        float informationSpace = 4 * fontSize;
        float middleSpace = fontSize;
        contentHeight = contentHeight - bottomSpace;
        int sc = canvas.saveLayer(0,0,canvas.getWidth(),canvas.getHeight() ,null, Canvas.ALL_SAVE_FLAG);
        float linesDrawHeight = (contentHeight - informationSpace - middleSpace)/2 - markerSize;

        Paint.FontMetrics metric = textPaint.getFontMetrics();
        int textHeight = (int) Math.ceil(metric.descent - metric.ascent);
        int baseY = (int)(textHeight - metric.descent);

        int tempMax = Collections.max(mTemperature);
        int humiMax = Collections.max(mHumidity);
        int qpfMax = Math.max(Collections.max(mQpf),5);

        float perTempHeight = linesDrawHeight/Math.max(( tempMax - Collections.min(mTemperature)), 1);
        float perHumiHeight  = linesDrawHeight/Math.max(( humiMax - Collections.min(mHumidity)), 1);
        float perQpfHeight = contentHeight/qpfMax;
        float perPopHeight = contentHeight/100;

        float toX = perColumnWidth/2 + paddingLeft;
        float toTempY = paddingTop + markerSize/2 + (tempMax - mTemperature.get(0))*perTempHeight;
        float toHumiY = paddingTop + markerSize * 1.5f + linesDrawHeight + middleSpace + (humiMax - mHumidity.get(0))*perHumiHeight;
        float qpfTop = paddingTop + (qpfMax - mQpf.get(0))*perQpfHeight;
        float popTop = paddingTop + (100 - mPop.get(0))*perPopHeight;

        tempPath.moveTo( toX, toTempY);
        HumiPath.moveTo( toX, toHumiY);
        canvas.drawCircle(toX,toTempY,markerSize/2, tempMarkerPaint);
        canvas.drawCircle(toX,toHumiY,markerSize/2, HumiMarkerPaint);

        qpfPath.addRect(toX - barWidth/2, qpfTop, toX - barWidth/4, paddingTop + contentHeight + density, Path.Direction.CW);
        popPath.addRect(toX - barWidth/4, popTop, toX + barWidth /2, paddingTop + contentHeight + density, Path.Direction.CW);

        canvas.drawText(String.valueOf(mTemperature.get(0)),toX - fontSize/2,toTempY + baseY/2,textPaint);
        canvas.drawText(String.valueOf(mHumidity.get(0)),toX - fontSize/2,toHumiY + baseY/2,textPaint);

        canvas.drawText(String.valueOf(mQpf.get(0)), toX - barWidth/2 + fontSize/4, contentHeight + paddingTop - fontSize/2, qpfTextPaint);
        canvas.drawText(String.valueOf(mPop.get(0)) + "%", toX - barWidth/2 + fontSize/4, contentHeight + paddingTop - fontSize*2, popTextPaint);

        canvas.drawText(mHour.get(0),toX - fontSize/2, contentHeight + paddingTop + 1.5f*fontSize, textPaint);
        //float oldX = toX, oldTempY = toTempY, oldHumiY = toHumiY;

        for (int i = 1; i < dataSize; i++){
            toX += perColumnWidth;

            toTempY += (mTemperature.get(i-1) - mTemperature.get(i))*perTempHeight;
            tempPath.lineTo(toX, toTempY);
            canvas.drawCircle(toX,toTempY,markerSize/2,tempMarkerPaint);
            canvas.drawText(String.valueOf(mTemperature.get(i)),toX - fontSize/2,toTempY + baseY/2,textPaint);
            //oldTempY = toTempY;

            toHumiY += (mHumidity.get(i-1) - mHumidity.get(i))*perHumiHeight;
            HumiPath.lineTo(toX, toHumiY);
            canvas.drawCircle(toX,toHumiY,markerSize/2, HumiMarkerPaint);
            canvas.drawText(String.valueOf(mHumidity.get(i)),toX - fontSize/2,toHumiY + baseY/2,textPaint);
            //oldHumiY = toHumiY;

            qpfTop = (qpfMax - mQpf.get(i))*perQpfHeight + paddingTop;
            qpfPath.addRect(toX - barWidth/2, qpfTop , toX - barWidth/4, paddingTop + contentHeight + density, Path.Direction.CW);
            canvas.drawText(String.valueOf(mQpf.get(i)), toX - barWidth/2 + fontSize/4, contentHeight + paddingTop - fontSize/2, qpfTextPaint);

            popTop = paddingTop + (100 - mPop.get(i))*perPopHeight;
            popPath.addRect(toX - barWidth/4, popTop , toX + barWidth /2, paddingTop + contentHeight + density, Path.Direction.CW);
            canvas.drawText(String.valueOf(mPop.get(i)) + "%", toX - barWidth/2 + fontSize/4, contentHeight + paddingTop - fontSize*2, popTextPaint);

            canvas.drawText(mHour.get(i),toX - fontSize/2, contentHeight + paddingTop + 1.5f*fontSize, textPaint);
            //oldX = toX;
        }
        //Log.d(LOG_TAG,"text size: " + textPaint.getTextSize());
        tempPaint.setXfermode(pdfDstOver);
        HumiPaint.setXfermode(pdfDstOver);
        canvas.drawPath(tempPath, tempPaint);
        canvas.drawPath(HumiPath, HumiPaint);
        tempPaint.setXfermode(null);
        HumiPaint.setXfermode(null);

        qpfBarPaint.setXfermode(pdfDstOver);
        popBarPaint.setXfermode(pdfDstOver);
        canvas.drawPath(popPath,popBarPaint);
        canvas.drawPath(qpfPath,qpfBarPaint);
        qpfBarPaint.setXfermode(null);
        popBarPaint.setXfermode(null);

    }


    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
