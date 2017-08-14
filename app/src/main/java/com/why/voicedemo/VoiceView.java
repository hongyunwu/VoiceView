package com.why.voicedemo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.Random;

/**
 * Created by wuhongyun on 17-8-14.
 */

public class VoiceView extends View {

    /**
     * 定义默认宽高
     */
    private int defaultWidth = 100,defaultHeight = 100;

    /**
     *初始状态，三个圆圈由小渐近变大的过程
     */
    public static final int INITIAL_STATE = 0;

    /**
     * 三个圆圈规律性旋转过程
     */
    public static final int NORMAL_STATE = 1;

    /**
     * 三个圆圈规律性旋转，voice图标明亮状态
     */
    public static final int PRESSED_STATE = 2;

    /**
     * 当按压结束时三个圆圈逐渐缩小的过程
     */
    public static final int PRE_SEARCH_STATE = 3;

    /**
     * 正在进行搜索时呈现的6个小球旋转的状态
     */
    public static final int SEARCH_STATE = 4;

    /**
     * 结束搜索时6个小球逐渐变为三个旋转大球的过程，为PRE_SAERCH_STATE的逆向
     */
    public static final int END_SEARCH_STATE = 5;

    /**
     * 测量结果-宽度
     */
    private float measuredWidth;

    /**
     * 测量结果-高度
     */
    private float measuredHeight;

    private Paint mDefaultPaint;

    public static final String TAG = "VoiceView";

    /**
     * 默认的圆半径
     */
    private float mDefaultRadius;
    private float shift;
    private ValueAnimator normal_animator;
    private boolean needChangeColor;
    private int current_state = INITIAL_STATE;
    private int normal_duration = 800;
    private float initial_radius = 5;
    private Drawable voiceDrawable;
    private ValueAnimator rotateAnimator;
    private float mDegree;
    private boolean is_initial_scaled;
    private ValueAnimator lessenAnimator;
    private long initial_duration = 2000;
    private float initialLessenRadius;
    private ValueAnimator largerAnimator;
    private float initialLargerRadius;
    private ValueAnimator shiftAnimator;
    private int initShiftValue = 30;

    public VoiceView(Context context) {
        this(context,null);
    }

    public VoiceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VoiceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDefaultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDefaultPaint.setColor(getVoiceColor());
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VoiceStyle);
        voiceDrawable = typedArray.getDrawable(R.styleable.VoiceStyle_voice_bg);


        typedArray.recycle();

    }

    private int getVoiceColor() {
        return Color.argb(0xff,0x80,0,0);
    }

    /**
     * 绘制过程描述
     * 1.在绘制时先获取系统的宽度值，voice所在的圆的直径为测量宽度的2/3
     *
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDefaultRadius = measuredWidth * 2 / 3 / 2;
        drawVoice(canvas);
        drawThreeCircle(canvas, shift,mDefaultRadius+shift/2,mDefaultRadius+shift/2,mDefaultRadius+shift/2);


    }

    /**
     * 画中心圆
     *
     * @param canvas
     */
    private void drawVoice(Canvas canvas) {
        //圆心是中点
        if (current_state==INITIAL_STATE){
            mDefaultPaint.setAlpha((int) (initial_radius/mDefaultRadius*255));
        }
        mDefaultPaint.setColor(Color.WHITE);
        RadialGradient radialGradient = new RadialGradient(measuredWidth / 2
                , measuredHeight / 2,mDefaultRadius-shift/10+16
                ,new int[]{Color.WHITE
                ,Color.WHITE
                ,Color.WHITE
                ,Color.WHITE
                ,Color.WHITE

                ,Color.WHITE
                ,Color.WHITE,Color.TRANSPARENT}
                ,null, Shader.TileMode.CLAMP);
        mDefaultPaint.setShader(radialGradient);
        canvas.drawCircle(measuredWidth/2,measuredHeight/2, mDefaultRadius-shift/10+16,mDefaultPaint);
        mDefaultPaint.setShader(null);

        mDefaultPaint.setColor(Color.rgb(0,0x00,0xff));
        canvas.drawCircle(measuredWidth/2,measuredHeight/2, mDefaultRadius - shift/10,mDefaultPaint);
        canvas.drawBitmap(drawableToBitmap(voiceDrawable),measuredWidth/2 - voiceDrawable.getIntrinsicWidth()/2,measuredHeight/2 - voiceDrawable.getIntrinsicHeight()/2,mDefaultPaint);
        if (current_state==INITIAL_STATE){
            mDefaultPaint.setAlpha(255);
        }

        if (current_state == INITIAL_STATE){

            mDefaultPaint.setColor(getThreeColor(0));
            canvas.drawCircle((float) (measuredWidth/2 + Math.cos(Math.PI/2)*shift/2), (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI/2)*shift/2),initialLargerRadius,mDefaultPaint);

            mDefaultPaint.setColor(getThreeColor(1));
            //shift开根号
            canvas.drawCircle((float) (measuredWidth/2 + Math.cos(Math.PI*7/6)*shift/2),(float)( measuredHeight/2 + Math.sin(mDegree+Math.PI*7/6)*shift/2), initialLessenRadius,mDefaultPaint);

            mDefaultPaint.setColor(getThreeColor(2));
            canvas.drawCircle((float) (measuredWidth/2 + Math.cos(Math.PI*11/6)*shift/2),(float)( measuredHeight/2 + Math.sin(mDegree+Math.PI*11/6)*shift/2), initialLessenRadius,mDefaultPaint);
            Log.i(TAG,"drawVoice->larger:"+initialLargerRadius+",lessen:"+initialLessenRadius+",default:"+mDefaultRadius);

        }

    }
    public static Bitmap drawableToBitmap(Drawable drawable) {



        Bitmap bitmap = Bitmap.createBitmap(

                drawable.getIntrinsicWidth(),

                drawable.getIntrinsicHeight(),

                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

                        : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);

        //canvas.setBitmap(bitmap);

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        drawable.draw(canvas);

        return bitmap;

    }


    Random random = new Random();
    private int[] threeColors = new int[]{
        0x34ff8640,0x3440ff5d,0x349f3fb5
    };
    /**
     * 画三个圆 角度 90 225 315
     * FFFF8640  FF40FF5D  FF9F3FB5
     * @param canvas
     */
    private void drawThreeCircle(Canvas canvas,float shift,float circle_1_radius,float circle_2_radius,float circle_3_radius) {
        if (current_state == NORMAL_STATE){
            mDefaultPaint.setColor(getThreeColor(0));
            canvas.drawCircle((float) (measuredWidth/2 + Math.cos(mDegree+Math.PI/2)*shift/2), (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI/2)*shift/2),circle_1_radius,mDefaultPaint);

            mDefaultPaint.setColor(getThreeColor(1));
            //shift开根号
            canvas.drawCircle((float) (measuredWidth/2 + Math.cos(mDegree+Math.PI*7/6)*shift/2),(float)( measuredHeight/2 + Math.sin(mDegree+Math.PI*7/6)*shift/2),circle_2_radius,mDefaultPaint);

            mDefaultPaint.setColor(getThreeColor(2));
            canvas.drawCircle((float) (measuredWidth/2 + Math.cos(mDegree+Math.PI*11/6)*shift/2),(float)( measuredHeight/2 + Math.sin(mDegree+Math.PI*11/6)*shift/2),circle_3_radius,mDefaultPaint);
            Log.i(TAG,"drawThreeCircle-shift:"+shift);
            Log.i(TAG,"drawThreeCircle->mDegree:"+mDegree+",1.w:"+Math.cos(mDegree+Math.PI/2)*shift/2+",h:"+Math.sin(mDegree+Math.PI/2)*shift/2);
        }


    }

    private int getThreeColor(int position) {

        return threeColors[position];
    }


    /**
     * 采用system.arrycopy方法
     * @return
     */
    private void changeThreeColor() {

        int color = threeColors[0];

        System.arraycopy(threeColors, 1, threeColors, 0, threeColors.length - 1);

        threeColors[threeColors.length - 1] = color;

    }



    /**
     * 测量
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        measuredWidth = caculateSize(widthMeasureSpec, defaultWidth);
        measuredHeight = caculateSize(heightMeasureSpec, defaultHeight);
        //如果两者的测量结果不想等，那么就默认让宽高值为较小的那个值
        if (measuredHeight!=measuredWidth){

            measuredWidth = measuredHeight = Math.max(measuredWidth,measuredHeight);
        }
        setMeasuredDimension((int) measuredWidth, (int) measuredHeight);

    }

    /**
     * 在此处实现循环变化
     */
    @Override
    public void computeScroll() {

        if (rotateAnimator==null){
            rotateAnimator = ValueAnimator.ofFloat(0, (float) Math.PI*2);
            rotateAnimator.setRepeatCount(-1);
            rotateAnimator.setDuration(normal_duration*3);
            rotateAnimator.setRepeatMode(ValueAnimator.RESTART);
            rotateAnimator.setInterpolator(new LinearInterpolator());
            rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {

                    mDegree = (float) animation.getAnimatedValue()%360;
                }
            });
            rotateAnimator.start();
        }


        if (current_state==INITIAL_STATE){
            if (lessenAnimator==null&&mDefaultRadius!=0){
                Log.i(TAG,"computeScroll->default:"+mDefaultRadius+",mDefaultRadius +(measuredWidth * 1 / 3 / 2:"+(mDefaultRadius +(measuredWidth * 1 / 3 / 2)));
                lessenAnimator = ValueAnimator.ofFloat(mDefaultRadius +(measuredWidth * 1 / 3 / 2)/2 ,mDefaultRadius*3/4,mDefaultRadius +(measuredWidth * 1 / 3 / 2)/2,mDefaultRadius + initShiftValue/2 );
                lessenAnimator.setDuration(initial_duration);
                lessenAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        initialLessenRadius = (float)animation.getAnimatedValue();

                    }
                });
                largerAnimator = ValueAnimator.ofFloat(initial_radius, mDefaultRadius + (measuredWidth * 1 / 3 / 2)/2,mDefaultRadius+initShiftValue/2);
                largerAnimator.setDuration(initial_duration);
                largerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        initialLargerRadius = (float)animation.getAnimatedValue();
                    }
                });
                largerAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        current_state = NORMAL_STATE;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                shiftAnimator = ValueAnimator.ofFloat(measuredWidth * 1 / 3 / 2,initShiftValue);
                shiftAnimator.setDuration(initial_duration);
                shiftAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        shift = (float) animation.getAnimatedValue();
                    }
                });
                shiftAnimator.start();
                largerAnimator.start();
                lessenAnimator.start();
            }


            Log.i(TAG,"initial_radius->"+initial_radius);
            postInvalidate();

        }else if (current_state==NORMAL_STATE){
            if (normal_animator ==null){
                normal_animator = ValueAnimator.ofFloat(initShiftValue, measuredWidth * 1 / 3 / 2,initShiftValue);
                normal_animator.setRepeatMode(ValueAnimator.RESTART);//循环模式
                normal_animator.setRepeatCount(-1);//永远循环
                normal_animator.setDuration(normal_duration);
                //normal_animator.setInterpolator(new AccelerateDecelerateInterpolator());
                normal_animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        shift = (float) animation.getAnimatedValue();
                        Log.i(TAG,"shift->"+shift);

                    }
                });
                normal_animator.start();
                normal_animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        needChangeColor = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                        //changeThreeColor();
                    }
                });
            }
            postInvalidate();

        }else{
            if (normal_animator !=null){
                normal_animator.cancel();
            }
        }
        super.computeScroll();



    }


    /**
     * 根据给出的spec计算出不同测量模式下的size值
     * @param measureSpec 测量说明，包含测量模式和测量值
     * @param defaultSize 默认测量值
     * @return
     */
    public int caculateSize(int measureSpec,int defaultSize){
        int specSize = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);
        int size = 0;
        switch (mode){
            case MeasureSpec.AT_MOST://取小值
                size = Math.min(defaultSize,specSize);
                break;
            case MeasureSpec.EXACTLY://取spec值
                size = specSize;
                break;
            case MeasureSpec.UNSPECIFIED://取默认值
                size = defaultSize;
                break;
        }
        Log.i(TAG,"measureMode->"+mode+",size:"+size+",specSize:"+specSize);

        return size;
    }


    /**
     * 触摸事件
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }


}
