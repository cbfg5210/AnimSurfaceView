package com.demo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.ArrayRes;

import com.bumptech.glide.Glide;

/**
 * 添加人：  Tom Hawk
 * 添加时间：2019/9/9 11:13
 * 功能描述：使用 SurfaceView 播放动画,避免 OOM
 * <p>
 * 修改人：  Tom Hawk
 * 修改时间：2019/9/9 11:13
 * 修改内容：
 */
public class AnimSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "AnimSurfaceView";

    @ArrayRes
    private int imgArrayRes;
    private int[] imgResArray;
    private int imgIndex;

    private Paint paint;
    private Rect mSrcRect;
    private Rect mDestRect;

    private PorterDuffXfermode clearMode;
    private PorterDuffXfermode srcMode;

    private Thread drawThread;
    private AnimCallback animCallback;

    private boolean shouldDraw;
    private boolean isPaused;
    private boolean autoStart;
    //画面时间间隔
    private int interval;

    public AnimSurfaceView(Context context) {
        this(context, null);
    }

    public AnimSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        paint = new Paint();
        mSrcRect = new Rect();
        mSrcRect.left = 0;
        mSrcRect.top = 0;
        mDestRect = new Rect();
        mDestRect.left = 0;
        mDestRect.top = 0;

        clearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        srcMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);

        getHolder().addCallback(this);

        //设置透明背景
        //setZOrderOnTop(true) 必须在setFormat方法之前，不然png的透明效果不生效
        //setZOrderOnTop(true);
        //mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.AnimSurfaceView);
        imgArrayRes = ta.getResourceId(R.styleable.AnimSurfaceView_imageArray, 0);
        autoStart = ta.getBoolean(R.styleable.AnimSurfaceView_autoStart, false);
        interval = ta.getInteger(R.styleable.AnimSurfaceView_interval, 100);
        ta.recycle();

        imgResArray = imgArrayRes == 0 ? new int[1] : getImgResArray(imgArrayRes);
    }

    public void setAnimCallback(AnimCallback animCallback) {
        this.animCallback = animCallback;
    }

    public void setImgArrayRes(@ArrayRes int imgArrayRes) {
        if (imgArrayRes != 0 && this.imgArrayRes != imgArrayRes) {
            this.imgArrayRes = imgArrayRes;
            imgResArray = getImgResArray(imgArrayRes);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated");
        if (autoStart) {
            start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
        destroy();
    }

    @Override
    public void run() {
        if (animCallback != null) {
            animCallback.onStart();
        }

        while (shouldDraw) {
            //Log.e(TAG, "is running...");

            long startTime = System.currentTimeMillis();

            if (!isPaused) {
                draw();
            }

            /*
             * 计算需要休眠的时间
             */
            long endTime = System.currentTimeMillis();
            int diffTime = (int) (endTime - startTime);
            Log.e(TAG, "diffTime=" + diffTime);

            if (diffTime < interval) {
                try {
                    Thread.sleep(interval - diffTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (animCallback != null) {
            animCallback.onStop();
        }
    }

    /**
     * 绘制图片
     */
    private void draw() {
        if (imgResArray == null || imgResArray.length == 0) {
            return;
        }

        Canvas mCanvas = getHolder().lockCanvas();
        if (mCanvas == null) {
            return;
        }

        if (imgIndex >= imgResArray.length) {
            imgIndex = 0;
        }

        Bitmap imgBitmap = loadBitmap(imgResArray[imgIndex++], getWidth(), getHeight());
        if (imgBitmap == null) {
            return;
        }

        /*
         * clear Canvas
         */
        paint.setXfermode(clearMode);
        mCanvas.drawPaint(paint);
        paint.setXfermode(srcMode);

        //mSrcRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        mSrcRect.right = imgBitmap.getWidth();
        mSrcRect.bottom = imgBitmap.getHeight();

        //mDestRect = new Rect(0, 0, getWidth(), getHeight());
        mDestRect.right = getWidth();
        mDestRect.bottom = getHeight();

        mCanvas.drawBitmap(imgBitmap, mSrcRect, mDestRect, paint);

        getHolder().unlockCanvasAndPost(mCanvas);
    }

    public void start() {
        shouldDraw = true;
        isPaused = false;

        if (drawThread == null) {
            drawThread = new Thread(this);
            drawThread.start();
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    private void destroy() {
        shouldDraw = false;
        isPaused = true;

        if (drawThread != null) {
            drawThread.interrupt();
            drawThread = null;
        }
    }

    /**
     * 使用 Glide 加载图片,避免 OOM
     *
     * @param resId     图片 id
     * @param reqWidth  图片宽度
     * @param reqHeight 图片高度
     * @return Bitmap
     */
    private Bitmap loadBitmap(int resId, int reqWidth, int reqHeight) {
        try {
            return Glide.with(getContext())
                    .asBitmap()
                    .load(resId)
                    .override(reqWidth, reqHeight)
                    .submit()
                    .get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读取属性获取图片资源数组
     *
     * @param imgArrayRes 图片数组 id
     * @return 图片资源数组
     */
    private int[] getImgResArray(@ArrayRes int imgArrayRes) {
        TypedArray array = getResources().obtainTypedArray(imgArrayRes);
        int len = array.length();
        int[] intArray = new int[array.length()];

        for (int i = 0; i < len; i++) {
            intArray[i] = array.getResourceId(i, 0);
        }
        array.recycle();

        return intArray;
    }

    /**
     * 动画回调
     */
    public interface AnimCallback {
        /**
         * 动画开始
         */
        void onStart();

        /**
         * 动画结束
         */
        void onStop();
    }
}
