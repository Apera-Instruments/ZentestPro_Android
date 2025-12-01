package com.zen.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Log;


import static android.graphics.Canvas.ALL_SAVE_FLAG;

public class MyDashBoardView2{
    private final  static String TAG = "MyDashBoardView2";
    public static final int MODE_OVER = 1;
    public static final int MODE_ATOP = 0;
    private Bitmap bitmap;
    private Bitmap newBitmap;
    private Paint paint = new Paint();
    private int startAngleL;
    private double sweepAngleL;
    private int startAngleH;
    private int sweepAngleH;
    private Path pathL=null,pathH=null;
    private  PorterDuffXfermode mPorterDuffXfermode=new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);//SRC_OVER SRC_ATOP);
    private  PorterDuffXfermode mPorterDuffXfermode2=new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);//SRC_OVER SRC_ATOP);
    private int imageResource;
    private int width;
    private int height;
    private Context mContext;
    private boolean dirty=false;
    private int mode;

    public MyDashBoardView2(Context context) {
        this.mContext =context;
    }

/*    public MyDashBoardView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDashBoardView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyDashBoardView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    */
    public Bitmap getBitmap(){
        if(dirty){
            invalidate();
        }
        return newBitmap;
    }

/*
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
    }*/
    protected void drawCanvas(Canvas canvas){


        int w = canvas.getWidth();
        int h = canvas.getHeight();
        int m= Math.min(w,h);


        int bw = bitmap.getWidth();
        int bh = bitmap.getHeight();
        int layoutId = canvas.saveLayer(
                0,0,w,h,
                null,ALL_SAVE_FLAG);
        int r = w/3;

        //paint.setColor(Color.YELLOW);
        //canvas.drawCircle(r,r,r,paint);
        canvas.drawBitmap(bitmap, (w-bw)/2,(h-bh)/2,paint);
        paint.setAntiAlias(true);
        if (mode == MODE_ATOP) {
            paint.setXfermode(mPorterDuffXfermode);
        } else if (mode == MODE_OVER) {
            paint.setXfermode(mPorterDuffXfermode2);
        }
        paint.setColor(Color.RED);
        //canvas.drawRect(r,r,3*r,3*r,paint);

        if(pathL!=null)canvas.drawPath(pathL,paint);
        if(pathH!=null)canvas.drawPath(pathH,paint);
        paint.setXfermode(null);
        canvas.restoreToCount(layoutId);

    }
    private int mR1;
    private int mR2;

    private void measureR(Bitmap bitmap) {

        int cx= bitmap.getWidth()/2+10;
        int cy= bitmap.getHeight()/2;
        int cy0 =cy;
        int r1=0,r2=0;
        for (int r0 = 0; cy > 0; cy--) {
            int c = bitmap.getPixel(cx, cy);
           // Log.i(TAG, "#" + Integer.toHexString(c));
            r0++;

            if (c !=0 && r1 == 0) {
                r1 = r0;
            }
            if (r1 != 0 && c == 0) {
                r2 = r0;
                break;
            }

        }
        //r1 238 r2 258 cy 137 cx 394
        Log.i(TAG,"measureR r1 "+r1 +" r2 "+r2 +" cy0 "+cy0+" cx "+cx);
        if((r2==0) || (r2+10 >=cy0)){
            r2 = (int) (r1);
        }

        mR1 = r1;
        mR2 = Math.max(r2,r1);
        if(mode ==MODE_OVER){
            mR2 =   (39 * mR2 / 40);
            mR1 =   (39 * mR1 / 40);
        }

    }

    public Path getPath(float startAngle, double sweepAngle) {
        Log.i(TAG, "getPath startAngle " + startAngle + " sweepAngle " + sweepAngle);
        Path path = null;
        if (mR2 > 0 && sweepAngle > 0 && startAngle>0 && startAngle<360 && sweepAngle<360) {
            path = new Path();
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            //path.moveTo(cx, cy);
            int r = mR2;// getWidth()*5/16;
            //  path.lineTo(3*r,r);
            //  path.lineTo(3*r,3*r);
            // int r1 = getWidth()/2;
            RectF oval = new RectF(cx - r, cy - r, cx + r, cy + r);
            path.arcTo(oval, startAngle, (float) sweepAngle);
            int r1 = mR2 == mR1 ? (37 * mR2 / 40) : mR1;
            RectF oval2 = new RectF(cx - r1, cy - r1, cx + r1, cy + r1);
            path.arcTo(oval2,  startAngle + (float)sweepAngle, (float)-sweepAngle);

            path.close();
            //invalidate();
            Log.i(TAG, "getPath r " + r + " getWidth " + getWidth());
        } else {
            path = null;
        }

        return path;
    }

    public void invalidate() {
        if(dirty) {
            dirty = false;
            Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            drawCanvas(c);
            newBitmap = b;
            Log.i(TAG, "invalidate " + newBitmap);
        }
    }

    private void setBitmap(Bitmap bitmap) {
        mR1 = 0;
        mR2 = 0;
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        pathH = null;
        pathL = null;
        startAngleH = 0;
        sweepAngleH = 0;
        startAngleL = 0;
        sweepAngleL = 0;
        measureR(bitmap);
        dirty =true;
        this.bitmap = bitmap;

    }

    public void setImageResource(int imageResource) {
        if (this.imageResource != imageResource) {
            this.imageResource = imageResource;
            setBitmap(BitmapFactory.decodeResource(getResources(), imageResource));
        }
    }

    private Resources getResources() {
        return mContext.getResources();
    }

    public int getImageResource() {
        return imageResource;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setPathL(int i, double i1) {
        if(startAngleL!=i || sweepAngleL !=i1 ) {
            startAngleL = i;
            sweepAngleL = i1;
            pathL = getPath(i, i1);
            dirty =true;
        }
    }
    public void setPathH(int i, int i1) {
        if(startAngleH!=i || sweepAngleH !=i1 ) {
            startAngleH = i;
            sweepAngleH = i1;
            pathH = getPath(i, i1);
            dirty =true;
        }
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }
}
