package com.example.sg.dynamicprogress;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

/**
 * 模拟动态的ProgressBar
 * Created by SG on 2017/11/6.
 */

public class DynamicProgress extends View {

    private final String TAG = "DynamicProgress";

    private int h, w;
    /**
     * 外框的宽度
     */
    private int mWidthBorder;
    /**
     * 外框的宽度 dp
     */
    private final int WIDTH_BORDER_DP = 2;
    /**
     * 半径
     */
    private int mRadius;
    /**
     * 边框PATH
     */
    private Path mBorderPath;
    /**
     * 画边框的Paint
     */
    private Paint mBorderPaint;
    /**
     * 画内部底色的Paint
     */
    private Paint mInnerBGPaint;
    /**
     * 画小矩形的Paint
     */
    private Paint mForgroundPaint;
    /**
     * 进度背景Paint
     */
    private Paint mProgressPaint;
    /**
     * 用于控制小菱形的滚动
     */
    private int mProgresssOffset;
    /**
     * 进度值
     */
    private int mProgress = 0;
    /**
     * 进度条前景，即每一个小的斜菱形
     */
    private Path mPathForeground;
    /**
     * 进度填充矩形
     */
    private Path mPathProgress;
    /**
     * 标识每一个小菱形的计数器
     */
    private int mCnt = 0;
    /**
     * 最多能画小菱形的个数
     */
    private int mNumForeGround;
    /**
     * 小菱形的宽度
     */
    private int mWidth;
    /**
     * 前景色1
     */
    private int mColor1;
    /**
     * 前景色2
     */
    private int mColor2;
    /**
     * 小菱形运动周期
     */
    private int mPeriod;
    /**
     * 小菱形斜边与水平方向成角,弧度
     */
    private final float mAngle = 10.46f;
    /**
     * 让小菱形无线运动的属性动画
     */
    private ObjectAnimator oaMoving;

    public DynamicProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initSize(context, attrs);
        initObject();
        initBorderPath();
    }

    /**
     * 初始化对象
     */
    private void initObject() {
        mBorderPath = new Path();

        mPathForeground = new Path();

        mBorderPaint = new Paint();
        mBorderPaint.setColor(Color.parseColor("#cdcdcd"));
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(mWidthBorder);
        mBorderPaint.setAntiAlias(true);

        mInnerBGPaint = new Paint();
        mInnerBGPaint.setColor(Color.parseColor("#ededed"));
        mInnerBGPaint.setAntiAlias(true);

        mForgroundPaint = new Paint();
        mForgroundPaint.setColor(mColor1);
        mForgroundPaint.setAntiAlias(true);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(mColor2);
        mProgressPaint.setAntiAlias(true);

        oaMoving = ObjectAnimator.ofInt(this, "mProgressOffset", 0, mWidth * 2);
        oaMoving.setRepeatCount(ValueAnimator.INFINITE);
        oaMoving.setInterpolator(new LinearInterpolator());
        oaMoving.setDuration((long) mPeriod);
    }

    /**
     * 计算各种size
     */
    private void initSize(Context context, @Nullable AttributeSet attrs) {
        //从xml中获得宽高
        int[] attrsArray = new int[]{
                android.R.attr.layout_width,
                android.R.attr.layout_height,
        };
        TypedArray tArrayAndroid = getContext().obtainStyledAttributes(attrs, attrsArray);
        w = tArrayAndroid.getDimensionPixelSize(0, ViewGroup.LayoutParams.MATCH_PARENT);
        h = tArrayAndroid.getDimensionPixelSize(1, ViewGroup.LayoutParams.MATCH_PARENT);
        tArrayAndroid.recycle();
        //从xml中获得自定义属性
        TypedArray tArray = getContext().obtainStyledAttributes(attrs,R.styleable.DynamicProgress);
        mColor1 = tArray.getColor(R.styleable.DynamicProgress_color1, Color.parseColor("#42dd2c"));
        mColor2 = tArray.getColor(R.styleable.DynamicProgress_color2, Color.parseColor("#a1ef97"));
        mWidth = tArray.getDimensionPixelSize(R.styleable.DynamicProgress_unit_width, DisplayUtils.dp2px(context, 8));
        mPeriod = tArray.getInteger(R.styleable.DynamicProgress_period, 1000);
        tArray.recycle();

        mWidthBorder = DisplayUtils.dp2px(context, WIDTH_BORDER_DP);
        mRadius = (h - (2 * mWidthBorder)) / 2;
        mNumForeGround = w / mWidth;
    }

    /**
     * 初始化圆角矩形的Path
     */
    private void initBorderPath() {
        mBorderPath.moveTo(mRadius + (mWidthBorder / 2), mWidthBorder / 2);
        mBorderPath.lineTo(w - mRadius - (mWidthBorder / 2), mWidthBorder / 2);
        mBorderPath.arcTo(
                (w - (2 * mRadius) - (mWidthBorder / 2)),
                mWidthBorder / 2,
                w - (mWidthBorder / 2),
                h - (mWidthBorder / 2),
                270,
                180,
                false
        );
        mBorderPath.lineTo(mRadius + (mWidthBorder / 2), h - (mWidthBorder / 2));
        mBorderPath.arcTo(
                mWidthBorder / 2,
                mWidthBorder / 2,
                mWidthBorder / 2 + (mRadius * 2),
                h - (mWidthBorder / 2),
                90,
                180,
                false
        );
        mBorderPath.close();

        initPathProgress();

        updateForeground();
    }

    /**
     * 初始化进度北京的Path
     */
    private void initPathProgress() {
        mPathProgress = new Path();
        mPathProgress.moveTo(WIDTH_BORDER_DP, 0);
        mPathProgress.lineTo(w * mProgress / 100, 0);
        mPathProgress.lineTo(w * mProgress / 100, h);
        mPathProgress.lineTo(WIDTH_BORDER_DP, h);
        mPathProgress.close();
        mPathProgress.op(mBorderPath, Path.Op.INTERSECT);
    }

    /**
     * 分别计算小菱形的Path
     */
    private void updateForeground() {
        mPathForeground.reset();

        Log.d(TAG, "updateForeground: " + this.mProgresssOffset);

        mPathForeground.moveTo(mCnt * mWidth + mProgresssOffset, 0);
        mPathForeground.lineTo((mCnt + 1) * mWidth + mProgresssOffset, 0);
        mPathForeground.lineTo((float) ((mCnt + 1) * mWidth + (h * Math.cos(mAngle))) + mProgresssOffset, h);
        mPathForeground.lineTo((float) ((mCnt) * mWidth + (h * Math.cos(mAngle))) + mProgresssOffset, h);
        mPathForeground.close();
        mPathForeground.op(mBorderPath, Path.Op.INTERSECT);

        mPathForeground.op(mPathProgress, Path.Op.INTERSECT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawInnerBackground(canvas);
        drawForeground(canvas);
    }

    /**
     * 画进度框内的背景
     */
    private void drawInnerBackground(Canvas canvas) {
        canvas.drawPath(mBorderPath, mInnerBGPaint);
    }

    /**
     * 画边框
     */
    private void drawBackground(Canvas canvas) {
        canvas.drawPath(mBorderPath, mBorderPaint);
    }

    /**
     * 画出所有的小菱形和进度背景色
     */
    private void drawForeground(Canvas canvas) {
        canvas.drawPath(mPathProgress, mProgressPaint);
        for (mCnt = 0; mCnt <= mNumForeGround; mCnt += 2) {
            updateForeground();
            canvas.drawPath(mPathForeground, mForgroundPaint);
        }
    }

    /**
     * @return 获得进度条Progress
     */
    public int getProgress() {
        return mProgress;
    }

    /**
     * @param progress 设置进度条Progress并更新界面
     */
    public void setProgress(int progress) {
        this.mProgress = progress;
        initPathProgress();
        invalidate();
    }

    /**
     * 让小菱形开始运动
     */
    public void start(){
        oaMoving.start();
    }

    /**
     * 让小菱形停止运动
     */
    public void stop(){
        oaMoving.cancel();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        oaMoving.cancel();
    }

    public int getMProgressOffset() {
        return mProgresssOffset;
    }

    /**
     * 仅用于被属性动画调用，不要手动调用这个方法
     */
    public void setMProgressOffset(int mProgressOffset) {
        this.mProgresssOffset = mProgressOffset;
        postInvalidate();
    }
}
