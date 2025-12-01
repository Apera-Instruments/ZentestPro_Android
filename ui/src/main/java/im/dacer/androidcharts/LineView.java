package im.dacer.androidcharts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


import com.zen.ui.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Dacer on 11/4/13.
 * Edited by Lee youngchan 21/1/14
 * Edited by dector 30-Jun-2014
 */
public class LineView extends View {
    public static final int SHOW_POPUPS_All = 1;
    public static final int SHOW_POPUPS_MAXMIN_ONLY = 2;
    public static final int SHOW_POPUPS_NONE = 3;
    private final int bottomTriangleHeight = 12;
    private final int popupTopPadding = MyUtils.dip2px(getContext(), 2);
    private final int popupBottomMargin = MyUtils.dip2px(getContext(), 5);
    private final int bottomTextTopMargin = MyUtils.sp2px(getContext(), 5);
    private final int bottomLineLength =0;// MyUtils.sp2px(getContext(), 0);
    private final int DOT_INNER_CIR_RADIUS = MyUtils.dip2px(getContext(), 2);
    private final int DOT_OUTER_CIR_RADIUS = MyUtils.dip2px(getContext(), 5);
    private final int MIN_TOP_LINE_LENGTH = MyUtils.dip2px(getContext(), 12);
    private final int MIN_VERTICAL_GRID_NUM = 4;
    private final int MAX_VERTICAL_GRID_NUM = 20;
    private final int MIN_HORIZONTAL_GRID_NUM = 16;
    private final int BACKGROUND_LINE_COLOR = Color.parseColor("#EEEEEE");
    private final int BACKGROUND_LINE_COLOR2 = Color.parseColor("#656565");
    private final int BACKGROUND_LINE_COLOR3 = Color.parseColor("#bdbdbd");
    private final int BOTTOM_TEXT_COLOR = Color.parseColor("#9B9A9B");
    private final Point tmpPoint = new Point();

    public boolean showPopup = false;
    private int mViewHeight;
    private final boolean autoSetDataOfGird = true;
    private final boolean autoSetGridWidth = false;
    private int dataOfAGird = 10;
    private int bottomTextHeight = 0;
    private List<String> bottomTextList = new ArrayList<String>();
    private List<List<Float>> dataLists;
    private List<Integer> xCoordinateList = new ArrayList<Integer>();
    private List<Float> yLeftCoordinateList = new ArrayList<>();
    private List<Float> yRightCoordinateList = new ArrayList<>();
    private List<String> yLeftTextList = new ArrayList<String>();
    private List<String> yRightTextList = new ArrayList<String>();
    private List<ArrayList<Dot>> drawDotLists = new ArrayList<ArrayList<Dot>>();
    private Paint bottomTextPaint = new Paint();
    private int bottomTextDescent;
    private Paint popupTextPaint = new Paint();
    private boolean showFloatNumInPopup;
    private Dot pointToSelect;
    private Dot selectedDot;
    private int popupBottomPadding = MyUtils.dip2px(getContext(), 2);
    /*
          |  | ←topLineLength
        --+--+--+--+--+--+--
        --+--+--+--+--+--+--
         ↑sideLineLength
     */
    private final int topLineLength = MyUtils.dip2px(getContext(), 10);
    private int sideLineLength = MyUtils.dip2px(getContext(), 45) / 3 * 2;

    private final int BACKGROUND_GW = MyUtils.dip2px(getContext(), 18);
    private int backgroundGridWidth =BACKGROUND_GW;// MyUtils.dip2px(getContext(), 18);
    private int showPopupType = SHOW_POPUPS_NONE;
    private Boolean drawDotLine = false;
    private int[] colorArray = {
            Color.parseColor("#e74c3c"), Color.parseColor("#2980b9"), Color.parseColor("#1abc9c")
    };
    private Runnable animator = new Runnable() {
        @Override public void run() {
            boolean needNewFrame = false;
            for (ArrayList<Dot> data : drawDotLists) {
                for (Dot dot : data) {
                    dot.update();
                    if (!dot.isAtRest()) {
                        needNewFrame = true;
                    }
                }
            }
            if (needNewFrame) {
                postDelayed(this, 1);
            }
            invalidate();
        }
    };
    private int width;
    private int gridCount;
    private int backgroundGridWidth2=backgroundGridWidth;
    private boolean mVerticalGridNeg=false;
    private int mRangY;
    private int mHorizontalGridNum;

    public LineView(Context context) {
        this(context, null);
    }

    public LineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        popupTextPaint.setAntiAlias(true);
        popupTextPaint.setColor(Color.WHITE);
        popupTextPaint.setTextSize(MyUtils.sp2px(getContext(), 12));
        popupTextPaint.setStrokeWidth(5);
        popupTextPaint.setTextAlign(Paint.Align.CENTER);

        bottomTextPaint.setAntiAlias(true);
        bottomTextPaint.setTextSize(MyUtils.sp2px(getContext(), 8));
        bottomTextPaint.setTextAlign(Paint.Align.CENTER);
        bottomTextPaint.setStyle(Paint.Style.FILL);
        bottomTextPaint.setColor(BOTTOM_TEXT_COLOR);
        refreshTopLineLength();
    }

    public void setShowPopup(int popupType) {
        this.showPopupType = popupType;
    }

    public void setDrawDotLine(Boolean drawDotLine) {
        this.drawDotLine = drawDotLine;
    }

    public void setColorArray(int[] colors) {
        this.colorArray = colors;
    }

    public List<String>  getBottomTextList() {
        return bottomTextList ;
    }
    public void setBottomTextList2(List<String> bottomTextList) {
        this.bottomTextList = bottomTextList;
    }

    /**
     * dataList will be reset when called is method.
     *
     * @param bottomTextList The String ArrayList in the bottom.
     */
    public void setBottomTextList(List<String> bottomTextList) {
        this.bottomTextList = bottomTextList;

        Rect r = new Rect();
        int longestWidth = 0;
        String longestStr = "";
        bottomTextDescent = 0;
        for (String s : bottomTextList) {
            bottomTextPaint.getTextBounds(s, 0, s.length(), r);
            if (bottomTextHeight < r.height()) {
                bottomTextHeight = r.height();
            }
            if (autoSetGridWidth && (longestWidth < r.width())) {
                longestWidth = r.width();
                longestStr = s;
            }
            if (bottomTextDescent < (Math.abs(r.bottom))) {
                bottomTextDescent = Math.abs(r.bottom);
            }
        }

        if (autoSetGridWidth) {
            if (backgroundGridWidth < longestWidth) {
                backgroundGridWidth =
                        longestWidth + (int) bottomTextPaint.measureText(longestStr, 0, 1);
            }
            if (sideLineLength < longestWidth / 2) {
                sideLineLength = longestWidth / 2;
            }
        }else{
         //   backgroundGridWidth =width/getHorizontalGridNum();
        }

        refreshXCoordinateList(getHorizontalGridNum());
    }

    /**
     * @param dataLists The Float ArrayLists for showing,
     * dataList.size() must be smaller than bottomTextList.size()
     */
    public void setFloatDataList(List<List<Float>> dataLists) {
        setFloatDataList(dataLists, true);
    }

    public void setDataList(List<List<Integer>> dataLists) {
        List<List<Float>> newList = new ArrayList<>();
        for (List<Integer> list : dataLists) {
            ArrayList<Float> tempList = new ArrayList<>();
            for (int i : list) {
                tempList.add((float) i);
            }
            newList.add(tempList);
        }
        setFloatDataList(newList, false);
    }

    public void setFloatDataList(List<List<Float>> dataLists,
            boolean showFloatNumInPopup) {
        selectedDot = null;
        this.showFloatNumInPopup = showFloatNumInPopup;
        this.dataLists = dataLists;
        for (List<Float> list : dataLists) {
            if (list.size() > bottomTextList.size()) {
                throw new RuntimeException(
                        "dacer.LineView error:" + " dataList.size() > bottomTextList.size() !!!");
            }
        }
        float biggestData = 0;
        for (List<Float> list : dataLists) {
            if (autoSetDataOfGird) {
                for (Float i : list) {
                    if (biggestData < i) {
                        biggestData = i;
                    }
                }
            }
            dataOfAGird = 1;
            while (biggestData / 10 > dataOfAGird) {
                dataOfAGird *= 10;
            }
        }

        refreshAfterDataChanged();
        showPopup = false;
        setMinimumWidth(0); // It can help the LineView reset the Width,
        // I don't know the better way..
        postInvalidate();
    }

    private void refreshAfterDataChanged() {
       // int verticalGridNum = getVerticalGridlNum(0);
        refreshYLeftCoordinateList(getVerticalGridlNum(0));
        refreshYRightCoordinateList(getVerticalGridlNum(1));
        refreshDrawDotList();
    }

    private int getVerticalGridlNum(int index) {
        int verticalGridNum = Math.max(MIN_VERTICAL_GRID_NUM,index==0?mRangY:50);
        boolean neg=false;
        if (dataLists != null && !dataLists.isEmpty() && index<dataLists.size()) {
           // for (ArrayList<Float> list : dataLists)
            List<Float> list = dataLists.get(index);
            {
                for (Float f : list) {
                    if(f<0.0f){
                        neg = true;
                        f = -f;
                    }
                    if (verticalGridNum < (f + 1)) {
                        verticalGridNum = (int) Math.floor(f + 1);
                    }
                }
            }
        }
        mVerticalGridNeg = neg;
        return neg?2*verticalGridNum:verticalGridNum;
    }

    public int getCurrentHorizontalGridNum() {
        return mHorizontalGridNum;
    }


    private int getHorizontalGridNum() {
        int horizontalGridNum = bottomTextList.size() - 1;
     /*   if (horizontalGridNum < MIN_HORIZONTAL_GRID_NUM) {
            horizontalGridNum = MIN_HORIZONTAL_GRID_NUM;
        }*/
        horizontalGridNum = Math.max(horizontalGridNum,MIN_HORIZONTAL_GRID_NUM);
        horizontalGridNum = Math.max(horizontalGridNum,mHorizontalGridNum);
        return horizontalGridNum;
    }

    private void refreshXCoordinateList(int horizontalGridNum) {
        xCoordinateList.clear();
        for (int i = 0; i < (horizontalGridNum + 1); i++) {
            xCoordinateList.add((int) (sideLineLength + zoom*backgroundGridWidth * i));
        }
    }

    private void refreshYLeftCoordinateList(int verticalGridNum) {
        yLeftCoordinateList.clear();
        yLeftTextList.clear();
        if(verticalGridNum%2!=0) verticalGridNum+=1;
        int num = Math.min(MAX_VERTICAL_GRID_NUM,verticalGridNum);
        float h = (mViewHeight
                - topLineLength
                - bottomTextHeight
                - bottomTextTopMargin
                - bottomLineLength
                - bottomTextDescent);

        for (int i = num; i>=0; i--) {
            yLeftCoordinateList.add( (float)topLineLength + ( h* i / (num)));
            if(mVerticalGridNeg){
                yLeftTextList.add("" + ((num - i) * verticalGridNum / num -(verticalGridNum/2)));
            }else {
                yLeftTextList.add("" + (num - i) * verticalGridNum / num);
            }
        }
    }

    private void refreshYRightCoordinateList(int verticalGridNum) {
        yRightCoordinateList.clear();
        yRightTextList.clear();
        if(verticalGridNum%2!=0) verticalGridNum+=1;
        int num = Math.min(MAX_VERTICAL_GRID_NUM,verticalGridNum);
        float h = (mViewHeight
                - topLineLength
                - bottomTextHeight
                - bottomTextTopMargin
                - bottomLineLength
                - bottomTextDescent);
        for (int i = num; i>=0; i--)  {
            yRightCoordinateList.add((float)topLineLength + (h * i / (num)));
            yRightTextList.add(""+(num-i)*verticalGridNum/num);
        }
    }

    private void refreshDrawDotList() {
        int verticalGridNum;
        if (dataLists != null && !dataLists.isEmpty()) {
            if (drawDotLists.size() == 0) {
                for (int k = 0; k < dataLists.size(); k++) {
                    drawDotLists.add(new ArrayList<LineView.Dot>());
                }
            }
            for (int k = 0; k < dataLists.size(); k++) {
                if(k>= drawDotLists.size()) break;
                int drawDotSize = drawDotLists.get(k).isEmpty() ? 0 : drawDotLists.get(k).size();
                verticalGridNum = getVerticalGridlNum(k);
                for (int i = 0; i < dataLists.get(k).size(); i++) {
                    int x = xCoordinateList.get(i);
                    float y = dataLists.get(k).get(i);
                    if (mVerticalGridNeg) {
                        y += verticalGridNum / 2;
                    }
                    y = getYAxesOf(y, verticalGridNum);

                    if (i > drawDotSize - 1) {
                        drawDotLists.get(k).add(new Dot(x, y, x, y, dataLists.get(k).get(i), k));
                    } else {
                        drawDotLists.get(k)
                                .set(i, drawDotLists.get(k)
                                        .get(i)
                                        .setTargetData(x, y, dataLists.get(k).get(i), k));
                    }
                }

                int temp = drawDotLists.get(k).size() - dataLists.get(k).size();
                for (int i = 0; i < temp; i++) {
                    drawDotLists.get(k).remove(drawDotLists.get(k).size() - 1);
                }
            }
        }
        removeCallbacks(animator);
        post(animator);
    }

    private float getYAxesOf(float value, int verticalGridNum) {
        return (float)topLineLength + ((float)(mViewHeight
                - topLineLength
                - bottomTextHeight
                - bottomTextTopMargin
                - bottomLineLength
                - bottomTextDescent) * (verticalGridNum - value) / (verticalGridNum));
    }

    private void refreshTopLineLength() {
        // For prevent popup can't be completely showed when backgroundGridHeight is too small.
       // topLineLength = getPopupHeight() + DOT_OUTER_CIR_RADIUS + DOT_INNER_CIR_RADIUS + 2;
    }

    @Override protected void onDraw(Canvas canvas) {
        drawBackgroundLines(canvas);
        drawLines(canvas);
       // drawDots(canvas);
/*
        for (int k = 0; k < drawDotLists.size(); k++) {
            float maxValue = Collections.max(dataLists.get(k));
            float minValue = Collections.min(dataLists.get(k));
            for (Dot d : drawDotLists.get(k)) {
                if (showPopupType == SHOW_POPUPS_All) {
                    drawPopup(canvas, d.data, d.setupPoint(tmpPoint),
                            colorArray[k % colorArray.length]);
                } else if (showPopupType == SHOW_POPUPS_MAXMIN_ONLY) {
                    if (d.data == maxValue) {
                        drawPopup(canvas, d.data, d.setupPoint(tmpPoint),
                                colorArray[k % colorArray.length]);
                    }
                    if (d.data == minValue) {
                        drawPopup(canvas, d.data, d.setupPoint(tmpPoint),
                                colorArray[k % colorArray.length]);
                    }
                }
            }
        }*/

      /*  if (showPopup && selectedDot != null) {
            drawPopup(canvas, selectedDot.data, selectedDot.setupPoint(tmpPoint),
                    colorArray[selectedDot.linenumber % colorArray.length]);
        }*/
    }

    /**
     * @param canvas The canvas you need to draw on.
     * @param point The Point consists of the x y coordinates from left bottom to right top.
     * Like is
     *
     * 3
     * 2
     * 1
     * 0 1 2 3 4 5
     */
    private void drawPopup(Canvas canvas, float num, Point point, int PopupColor) {
        String numStr = showFloatNumInPopup ? String.valueOf(num) : String.valueOf(Math.round(num));
        boolean singularNum = (numStr.length() == 1);
        int sidePadding = MyUtils.dip2px(getContext(), singularNum ? 8 : 5);
        int x = point.x;
        int y = point.y - MyUtils.dip2px(getContext(), 5);
        Rect popupTextRect = new Rect();
        popupTextPaint.getTextBounds(numStr, 0, numStr.length(), popupTextRect);
        Rect r = new Rect(x - popupTextRect.width() / 2 - sidePadding, y
                - popupTextRect.height()
                - bottomTriangleHeight
                - popupTopPadding * 2
                - popupBottomMargin, x + popupTextRect.width() / 2 + sidePadding,
                y + popupTopPadding - popupBottomMargin + popupBottomPadding);

        NinePatchDrawable popup =
                (NinePatchDrawable) getResources().getDrawable(R.drawable.popup_white);
        popup.setColorFilter(new PorterDuffColorFilter(PopupColor, PorterDuff.Mode.MULTIPLY));
        popup.setBounds(r);
        popup.draw(canvas);
        canvas.drawText(numStr, x, y - bottomTriangleHeight - popupBottomMargin, popupTextPaint);
    }

    private int getPopupHeight() {
        Rect popupTextRect = new Rect();
        popupTextPaint.getTextBounds("9", 0, 1, popupTextRect);
        Rect r = new Rect(-popupTextRect.width() / 2, -popupTextRect.height()
                - bottomTriangleHeight
                - popupTopPadding * 2
                - popupBottomMargin, +popupTextRect.width() / 2,
                +popupTopPadding - popupBottomMargin + popupBottomPadding);
        return r.height();
    }

    private void drawDots(Canvas canvas) {
        Paint bigCirPaint = new Paint();
        bigCirPaint.setAntiAlias(true);
        Paint smallCirPaint = new Paint(bigCirPaint);
        smallCirPaint.setColor(Color.parseColor("#FFFFFF"));
        if (drawDotLists != null && !drawDotLists.isEmpty()) {
            for (int k = 0; k < drawDotLists.size(); k++) {
                bigCirPaint.setColor(colorArray[k % colorArray.length]);
                for (Dot dot : drawDotLists.get(k)) {
                    canvas.drawCircle(dot.x, dot.y, DOT_OUTER_CIR_RADIUS, bigCirPaint);
                    canvas.drawCircle(dot.x, dot.y, DOT_INNER_CIR_RADIUS, smallCirPaint);
                }
            }
        }
    }
    final int  drawLinesWidth =MyUtils.dip2px(getContext(), 1);
    private void drawLines(Canvas canvas) {
        Paint linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(drawLinesWidth);
        int size = drawDotLists.size();
        for (int k = 0; k < size; k++) {
            linePaint.setColor(colorArray[k % colorArray.length]);
            ArrayList<Dot> dots = drawDotLists.get(k);
            int kSize = dots.size() - 1;

            for (int i = 0; i < kSize; i++) {
                Dot dotNext = dots.get(i + 1);
                int x = dotNext.x;
                if (dotNext.x > width - sideLineLength)
                {
                    x = width - sideLineLength;
                }
                Dot dot = dots.get(i);
                int x0 = dot.x;
                if (dot.x > width - sideLineLength)
                {
                  break;
                }
                canvas.drawLine(x0, dot.y,
                        x, dotNext.y,
                        linePaint);
            }
        }
    }
    private final  int  yCoordinateLongWidth = MyUtils.dip2px(getContext(), 5f);
    private final  int  yCoordinateShortWidth = yCoordinateLongWidth/2;
    private void drawBackgroundLines(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(MyUtils.dip2px(getContext(), 1f));
        paint.setColor(BACKGROUND_LINE_COLOR);

        Paint paint2 = new Paint();
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(MyUtils.dip2px(getContext(), 1f));
        paint2.setColor(BACKGROUND_LINE_COLOR2);

        Paint paint3 = new Paint();
        paint3.setStyle(Paint.Style.STROKE);
        paint3.setStrokeWidth(MyUtils.dip2px(getContext(), 1f));
        paint3.setColor(BACKGROUND_LINE_COLOR3);

        PathEffect effects = new DashPathEffect(new float[] { 10, 5, 10, 5 }, 1);

        //draw vertical lines
        for (int i = 0; i <gridCount/* xCoordinateList.size()*/; i++) {
            if (i == 0) {
                canvas.drawLine(sideLineLength + backgroundGridWidth2 * i/*xCoordinateList.get(i)*/, 0,
                        sideLineLength + backgroundGridWidth2 * i,
                        mViewHeight - bottomTextTopMargin - bottomTextHeight - bottomTextDescent,
                        paint2);
            } else if (i == gridCount - 1) {
                canvas.drawLine(sideLineLength + backgroundGridWidth2 * i/*xCoordinateList.get(i)*/, 0,
                        sideLineLength + backgroundGridWidth2 * i,
                        mViewHeight - bottomTextTopMargin - bottomTextHeight - bottomTextDescent,
                        paint2);
            } else if (i % 2 != 0) {
                canvas.drawLine(sideLineLength + backgroundGridWidth2 * i/*xCoordinateList.get(i)*/, 0,
                        sideLineLength + backgroundGridWidth2 * i,
                        mViewHeight - bottomTextTopMargin - bottomTextHeight - bottomTextDescent,
                        paint);
            }
        }

        //draw dotted lines
      /*  paint.setPathEffect(effects);
        Path dottedPath = new Path();
        for (int i = 0; i < yCoordinateList.size(); i++) {
            if ((yCoordinateList.size() - 1 - i) % dataOfAGird == 0) {
                dottedPath.moveTo(0, yCoordinateList.get(i));
                dottedPath.lineTo(getWidth(), yCoordinateList.get(i));
                canvas.drawPath(dottedPath, paint);
            }
        }*/
        //draw bottom text
        if (bottomTextList != null) {
            int n =bottomTextList.size()>16? bottomTextList.size()/4:4;
            int m=0;
            for (int i = 0; i < bottomTextList.size(); i++) {
                if(i==0 || i%n!=0 || i==(2*n) ) continue;
                m++;
                if(sideLineLength + backgroundGridWidth * (8*m-4) >=width-sideLineLength)
                    break;

                canvas.drawText(bottomTextList.get(i), sideLineLength + backgroundGridWidth * (8*m-4),
                        mViewHeight - bottomTextDescent, bottomTextPaint);
            }
        }

        for (int i = 0; i < yLeftCoordinateList.size(); i++) {
            //if ((yCoordinateList.size() - 1 - i) % dataOfAGird == 0)
            {
                if((i)%2==0) {
                    canvas.drawLine(sideLineLength - yCoordinateLongWidth, yLeftCoordinateList.get(i), sideLineLength - 2, yLeftCoordinateList.get(i),
                            paint2);
                    String s = yLeftTextList.get(i);
                    Rect r = new Rect();
                    bottomTextPaint.getTextBounds(s, 0, s.length(), r);
                    canvas.drawText(s, sideLineLength - yCoordinateLongWidth -r.width()/2-10,
                            yLeftCoordinateList.get(i)+r.height()/2, bottomTextPaint);
                   /* canvas.drawLine(width - sideLineLength + 2, yLeftCoordinateList.get(i), width - sideLineLength + 20, yLeftCoordinateList.get(i),
                            paint2);*/
                }else{
                    canvas.drawLine(sideLineLength - yCoordinateShortWidth, yLeftCoordinateList.get(i), sideLineLength - 2, yLeftCoordinateList.get(i),
                            paint3);
/*
                    canvas.drawLine(width - sideLineLength + 2, yCoordinateList.get(i), width - sideLineLength + 10, yCoordinateList.get(i),
                            paint3);*/

                }
            }
        }

        for (int i = 0; i < yRightCoordinateList.size(); i++) {
            //if ((yCoordinateList.size() - 1 - i) % dataOfAGird == 0)
            {
                if((i)%2==0) {


                    canvas.drawLine(width - sideLineLength + 2, yRightCoordinateList.get(i), width - sideLineLength + yCoordinateLongWidth, yRightCoordinateList.get(i),
                            paint2);
                    String s = yRightTextList.get(i);
                    Rect r = new Rect();
                    bottomTextPaint.getTextBounds(s, 0, s.length(), r);
                    canvas.drawText(s, width - sideLineLength + yCoordinateLongWidth+r.width()/2 +5,
                            yRightCoordinateList.get(i)+r.height()/2, bottomTextPaint);

                }else{

                    canvas.drawLine(width - sideLineLength + 2, yRightCoordinateList.get(i), width - sideLineLength + yCoordinateShortWidth, yRightCoordinateList.get(i),
                            paint3);

                }
            }
        }
        int h1 =  mViewHeight - bottomTextTopMargin - bottomTextHeight - bottomTextDescent;
        canvas.drawLine(
                sideLineLength-20, h1,
                width-sideLineLength+20, h1,
                paint2);
        int h = (mViewHeight
                - topLineLength
                - bottomTextHeight
                - bottomTextTopMargin
                - bottomLineLength
                - bottomTextDescent);
        for (int i = 1; i < 4; i++) {

            {
                canvas.drawLine(
                        sideLineLength+1, h1-h*i/3,
                        width-sideLineLength-1, h1-h*i/3,
                        paint);
            }
        }
     /*   if (!drawDotLine) {
            //draw solid lines
            for (int i = 0; i < yCoordinateList.size(); i++) {
                if ((yCoordinateList.size() - 1 - i) % dataOfAGird == 0) {
                    canvas.drawLine(0, yCoordinateList.get(i), getWidth(), yCoordinateList.get(i),
                            paint);
                }
            }
        }*/
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mViewWidth = measureWidth(widthMeasureSpec);
        mViewHeight = measureHeight(heightMeasureSpec);
        //        mViewHeight = MeasureSpec.getSize(measureSpec);
        refreshAfterDataChanged();
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    private int measureWidth(int measureSpec) {
        int horizontalGridNum = getHorizontalGridNum();
        int preferred = backgroundGridWidth * horizontalGridNum + sideLineLength * 2;
        return getMeasurement(measureSpec, preferred);
    }

    private int measureHeight(int measureSpec) {
        int preferred = 0;
        return getMeasurement(measureSpec, preferred);
    }

    private int getMeasurement(int measureSpec, int preferred) {
        int specSize = MeasureSpec.getSize(measureSpec);
        int measurement;
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.EXACTLY:
                measurement = specSize;
                break;
            case MeasureSpec.AT_MOST:
                measurement = Math.min(preferred, specSize);
                break;
            default:
                measurement = preferred;
                break;
        }
        return measurement;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        return false;
/*        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pointToSelect = findPointAt((int) event.getX(), (int) event.getY());
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (pointToSelect != null) {
                selectedDot = pointToSelect;
                pointToSelect = null;
                postInvalidate();
            }
        }

        return true;*/
    }

    private Dot findPointAt(int x, int y) {
        if (drawDotLists.isEmpty()) {
            return null;
        }

        final int width = backgroundGridWidth / 2;
        final Region r = new Region();

        for (ArrayList<Dot> data : drawDotLists) {
            for (Dot dot : data) {
                final int pointX = dot.x;
                final int pointY = (int) dot.y;

                r.set(pointX - width, pointY - width, pointX + width, pointY + width);
                if (r.contains(x, y)) {
                    return dot;
                }
            }
        }

        return null;
    }


    private void calBackgroundGridWidth(){
        backgroundGridWidth2 = (width-2*sideLineLength)/(gridCount-1);
    }
    public void setSideLineLength(int sideLineLength) {
        this.sideLineLength = sideLineLength;
        calBackgroundGridWidth();
    }

    public void setWidth(int width) {
        this.width = width;
        calBackgroundGridWidth();
    }



    public void setGridCount(int gridCount) {
        this.gridCount = gridCount;
        calBackgroundGridWidth();
    }

    public int getGridCount() {
        return gridCount;
    }

    public void setRangeY(double rangeY) {
        this.mRangY = (int) rangeY;
    }

    public void setHorizontalGridNum(int size) {
        this.mHorizontalGridNum = size;
    }

    public void setHorizontalGridNumTwice() {
        this.mHorizontalGridNum *=2;
        refreshXCoordinateList(getHorizontalGridNum());
    }
    private float zoom = 1;
    public void updateGridNum() {
        refreshXCoordinateList(getHorizontalGridNum());
        refreshDrawDotList();
     }
    public void zoom(float lineZoom) {

        zoom = lineZoom;
        refreshXCoordinateList(getHorizontalGridNum());
        refreshDrawDotList();
        // backgroundGridWidth= BACKGROUND_GW*zoom;
     /*   if(zoom == 1){
            zoom = 2;
        }else {
            zoom = 1;
        }
        //backgroundGridWidth = BACKGROUND_GW*zoom;
        refreshXCoordinateList(getHorizontalGridNum());*/
    }

    class Dot {
        final  boolean ANIM = true;
        int x;
        float y;
        float data;
        int targetX;
        float targetY;
        int linenumber;
        int velocity = MyUtils.dip2px(getContext(), 18);

        Dot(int x, float y, int targetX, float targetY, float data, int linenumber) {
            this.x = x;
            this.y = y;
            this.linenumber = linenumber;
            setTargetData(targetX, targetY, data, linenumber);
        }

        Point setupPoint(Point point) {
            point.set(x, (int) y);
            return point;
        }

        Dot setTargetData(int targetX, float targetY, float data, int linenumber) {
            this.targetX = targetX;
            this.targetY = targetY;
            this.data = data;
            this.linenumber = linenumber;
            if(!ANIM){
                update();
            }
            return this;
        }

        boolean isAtRest() {
            return (x == targetX) && (y == targetY);
        }

        void update() {
            x = (int) updateSelf(x, targetX, velocity);
            y = updateSelf(y, targetY, velocity);
        }

        private float updateSelf(float origin, float target, int velocity) {
            if (origin < target) {
                origin += velocity;
            } else if (origin > target) {
                origin -= velocity;
            }
            if (Math.abs(target - origin) < velocity) {
                origin = target;
            }
            return origin;
        }
    }
}
