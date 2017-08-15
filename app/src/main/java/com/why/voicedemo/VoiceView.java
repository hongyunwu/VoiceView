package com.why.voicedemo;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
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

    private int voice_shadow_width;
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
    private float search_lessen_size = 8;
    private float search_larger_size = 18;
    private ValueAnimator searchScaleAnimator1;
    private float search_scale_size_1;
    private ValueAnimator searchScaleAnimator2;
    private float search_scale_size_2;
    private float mSearchDegree;
    private ValueAnimator rotateSearchAnimator;
    private ValueAnimator preSearchAnimator;
    private long pre_search_duration = 2000;
    private float mPreSearchRadius;
    private ValueAnimator voiceAlphaAnimator;
    private int voiceAlpha;
    private int voiceCircleAlpha;
    private ValueAnimator voiceCircleAlphaAnimator;

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
        voice_shadow_width = typedArray.getDimensionPixelSize(R.styleable.VoiceStyle_voice_circle_shadow_width,16);
        search_lessen_size = typedArray.getDimensionPixelSize(R.styleable.VoiceStyle_voice_search_circle_small_width,8);
        search_larger_size = typedArray.getDimensionPixelSize(R.styleable.VoiceStyle_voice_search_circle_large_width,18);
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
        drawVoiceCircle(canvas);
        drawThreeCircle(canvas, shift,mDefaultRadius+shift/2,mDefaultRadius+shift/2,mDefaultRadius+shift/2);
        drawEightCircle(canvas);
        drawFourCircle(canvas);
        drawVoice(canvas);
    }



    private void drawFourCircle(Canvas canvas) {
        if (current_state==PRE_SEARCH_STATE){
            //需要一個渐显的圆和三个逐渐缩小的圆
            //mDegree shift值获取当前位置，目标位置
            LinearGradient linearGradient1 = new LinearGradient(
                    (float) (measuredWidth/2 + Math.cos(mDegree+Math.PI/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))) + (float) (Math.cos(mDegree)*mPreSearchRadius),
                    (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius+shift/2 - search_lessen_size)))) + (float) (Math.sin(mDegree)*mPreSearchRadius),
                    (float) (measuredWidth/2 + Math.cos(mDegree+Math.PI/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))) + (float) (Math.cos(mDegree+Math.PI)*mPreSearchRadius),
                    (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius+shift/2 - search_lessen_size)))) + (float) (Math.cos(mDegree+Math.PI)*mPreSearchRadius),
                    Color.GRAY, Color.BLUE, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(linearGradient1);
            mDefaultPaint.setColor(getThreeColor(0));
            canvas.drawCircle(
                        (float) (measuredWidth/2 + Math.cos(mDegree+Math.PI/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))),
                        (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius+shift/2 - search_lessen_size)))),
                        mPreSearchRadius,mDefaultPaint);
            LinearGradient linearGradient2 = new LinearGradient(
                    (float) (measuredWidth/2 + Math.cos(mDegree+Math.PI*3/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))) + (float) (Math.cos(mDegree+Math.PI/2)*mPreSearchRadius),
                    (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI*3/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius+shift/2 - search_lessen_size)))) + (float) (Math.sin(mDegree+Math.PI/2)*mPreSearchRadius),
                    (float) (measuredWidth/2 + Math.cos(mDegree+Math.PI*3/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))) + (float) (Math.cos(mDegree+Math.PI*3/2)*mPreSearchRadius),
                    (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI*3/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius+shift/2 - search_lessen_size)))) + (float) (Math.cos(mDegree+Math.PI*3/2)*mPreSearchRadius),
                    Color.YELLOW, Color.CYAN, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(linearGradient2);
            canvas.drawCircle(
                    (float) (measuredWidth/2 + Math.cos(mDegree+Math.PI*3/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius+shift/2 - search_lessen_size)))),
                    (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI*3/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))),
                    mPreSearchRadius,mDefaultPaint);
            LinearGradient linearGradient3 = new LinearGradient(
                    (float) (measuredWidth/2 + Math.cos(mDegree+Math.PI*5/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))) + (float) (Math.cos(mDegree+Math.PI)*mPreSearchRadius),
                    (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI*5/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius+shift/2 - search_lessen_size)))) + (float) (Math.sin(mDegree+Math.PI)*mPreSearchRadius),
                    (float) (measuredWidth/2 + Math.cos(mDegree+Math.PI*5/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))) + (float) (Math.cos(mDegree+Math.PI*2)*mPreSearchRadius),
                    (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI*5/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius+shift/2 - search_lessen_size)))) + (float) (Math.cos(mDegree+Math.PI*2)*mPreSearchRadius),
                    Color.GRAY, Color.BLUE, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(linearGradient3);
            canvas.drawCircle(
                    (float) (measuredWidth/2 + Math.cos(mDegree+Math.PI*5/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))),
                    (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI*5/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))),
                    mPreSearchRadius,mDefaultPaint);
            LinearGradient linearGradient4 = new LinearGradient(
                    (float) (measuredWidth/2 + Math.cos(mDegree+Math.PI*7/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))) + (float) (Math.cos(mDegree+Math.PI/2)*mPreSearchRadius),
                    (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI*7/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius+shift/2 - search_lessen_size)))) + (float) (Math.sin(mDegree+Math.PI/2)*mPreSearchRadius),
                    (float) (measuredWidth/2 + Math.cos(mDegree+Math.PI*7/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))) + (float) (Math.cos(mDegree+Math.PI*3/2)*mPreSearchRadius),
                    (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI*7/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius+shift/2 - search_lessen_size)))) + (float) (Math.cos(mDegree+Math.PI*3/2)*mPreSearchRadius),
                    Color.LTGRAY, Color.MAGENTA, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(linearGradient4);
            canvas.drawCircle(
                    (float) (measuredWidth/2 + Math.cos(mDegree+Math.PI*7/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))),
                    (float) (measuredHeight/2 + Math.sin(mDegree+Math.PI*7/4)*(mDefaultRadius*((mDefaultRadius+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)))),
                    mPreSearchRadius,mDefaultPaint);
            Log.i(TAG,"drawFourCircle->shift"+shift);
            mDefaultPaint.setShader(null);
        }

    }

    /**
     * 画六个小球
     * 角度差为60,
     * @param canvas
     */
    private void drawEightCircle(Canvas canvas) {
        if (current_state ==SEARCH_STATE){
            mDefaultPaint.setColor(getThreeColor(0));
            /***************************************************/
            LinearGradient linearGradient1 = new LinearGradient(
                    measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*1/4)*mDefaultRadius) + (float) (Math.cos(mSearchDegree)*search_scale_size_1),
                    measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*1/4)*mDefaultRadius) + (float) (Math.sin(mSearchDegree)*search_scale_size_1),
                    measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*1/4)*mDefaultRadius) + (float) (Math.cos(mSearchDegree+Math.PI)*search_scale_size_1),
                    measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*1/4)*mDefaultRadius) + (float) (Math.cos(mSearchDegree+Math.PI)*search_scale_size_1),
                    Color.GRAY, Color.BLUE, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(linearGradient1);
            canvas.drawCircle(measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*1/4)*mDefaultRadius)
                    ,measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*1/4)*mDefaultRadius)
                    , search_scale_size_1
                    ,mDefaultPaint);
            LinearGradient linearGradient2 = new LinearGradient(
                    measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*5/4)*mDefaultRadius) + (float) (Math.cos(mSearchDegree+Math.PI)*search_scale_size_1),
                    measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*5/4)*mDefaultRadius) + (float) (Math.sin(mSearchDegree+Math.PI)*search_scale_size_1),
                    measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*5/4)*mDefaultRadius) + (float) (Math.cos(mSearchDegree+Math.PI*2)*search_scale_size_1),
                    measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*5/4)*mDefaultRadius) + (float) (Math.cos(mSearchDegree+Math.PI*2)*search_scale_size_1),
                    Color.GRAY, Color.BLUE, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(linearGradient2);
            canvas.drawCircle(measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*5/4)*mDefaultRadius)
                    ,measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*5/4)*mDefaultRadius)
                    , search_scale_size_1
                    ,mDefaultPaint);
            mDefaultPaint.setShader(null);
            /***************************************************/
            LinearGradient linearGradient3 = new LinearGradient(
                    measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*3/4)*mDefaultRadius) + (float) (Math.cos(mSearchDegree+Math.PI/2)*search_scale_size_1),
                    measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*3/4)*mDefaultRadius) + (float) (Math.sin(mSearchDegree+Math.PI/2)*search_scale_size_1),
                    measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*3/4)*mDefaultRadius) + (float) (Math.cos(mSearchDegree+Math.PI*3/2)*search_scale_size_1),
                    measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*3/4)*mDefaultRadius) + (float) (Math.cos(mSearchDegree+Math.PI*3/2)*search_scale_size_1),
                    Color.YELLOW, Color.CYAN, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(linearGradient3);
            canvas.drawCircle(measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*3/4)*mDefaultRadius)
                    ,measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*3/4)*mDefaultRadius)
                    , search_scale_size_1
                    ,mDefaultPaint);
            LinearGradient linearGradient4 = new LinearGradient(
                    measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*7/4)*mDefaultRadius) + (float) (Math.cos(mSearchDegree+Math.PI/2)*search_scale_size_1),
                    measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*7/4)*mDefaultRadius) + (float) (Math.sin(mSearchDegree+Math.PI/2)*search_scale_size_1),
                    measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*7/4)*mDefaultRadius) + (float) (Math.cos(mSearchDegree+Math.PI*3/2)*search_scale_size_1),
                    measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*7/4)*mDefaultRadius) + (float) (Math.cos(mSearchDegree+Math.PI*3/2)*search_scale_size_1),
                    Color.LTGRAY, Color.MAGENTA, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(linearGradient4);
            canvas.drawCircle(measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*7/4)*mDefaultRadius)
                    ,measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*7/4)*mDefaultRadius)
                    , search_scale_size_1
                    ,mDefaultPaint);
            mDefaultPaint.setShader(null);
            /***************************************************/
            LinearGradient linearGradient5 = new LinearGradient(measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*4/4)*mDefaultRadius)
                    - search_scale_size_2, measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*4/4)*mDefaultRadius) + search_scale_size_2/2, measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*4/4)*mDefaultRadius)
                    +search_scale_size_2, measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*4/4)*mDefaultRadius) -search_scale_size_2/2, Color.RED, Color.GREEN, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(linearGradient5);
            canvas.drawCircle(measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*4/4)*mDefaultRadius)
                    ,measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*4/4)*mDefaultRadius)
                    , search_scale_size_2
                    ,mDefaultPaint);
            LinearGradient linearGradient6 = new LinearGradient(measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*6/4)*mDefaultRadius)
                    - search_scale_size_2, measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*6/4)*mDefaultRadius) + search_scale_size_2/2, measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*6/4)*mDefaultRadius)
                    +search_scale_size_2, measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*6/4)*mDefaultRadius) - search_scale_size_2/2, Color.RED, Color.GREEN, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(linearGradient6);
            canvas.drawCircle(measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*6/4)*mDefaultRadius)
                    ,measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*6/4)*mDefaultRadius)
                    , search_scale_size_2
                    ,mDefaultPaint);
            LinearGradient linearGradient7 = new LinearGradient(measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*2/4)*mDefaultRadius)
                    - search_scale_size_2, measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*2/4)*mDefaultRadius) +search_scale_size_2/2, measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*2/4)*mDefaultRadius)
                    +search_scale_size_2, measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*2/4)*mDefaultRadius) - search_scale_size_2/2, Color.RED, Color.GREEN, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(linearGradient7);
            canvas.drawCircle(measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*2/4)*mDefaultRadius)
                    ,measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*2/4)*mDefaultRadius)
                    , search_scale_size_2
                    ,mDefaultPaint);
            LinearGradient linearGradient8 = new LinearGradient(measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*8/4)*mDefaultRadius)
                    - search_scale_size_2, measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*8/4)*mDefaultRadius)+search_scale_size_2/2, measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*8/4)*mDefaultRadius)
                    +search_scale_size_2, measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*8/4)*mDefaultRadius) - search_scale_size_2/2, Color.RED, Color.GREEN, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(linearGradient8);
            canvas.drawCircle(measuredWidth/2+(float) (Math.cos(mSearchDegree+Math.PI*8/4)*mDefaultRadius)
                    ,measuredHeight/2+(float) (Math.sin(mSearchDegree+Math.PI*8/4)*mDefaultRadius)
                    , search_scale_size_2
                    ,mDefaultPaint);
            mDefaultPaint.setShader(null);
            Log.i(TAG,"drawEightCircle->mSearchDegree:"+mSearchDegree);
        }

    }

    /**
     * 画中心圆
     *
     * @param canvas
     */
    private void drawVoiceCircle(Canvas canvas) {
        //圆心是中点
        if (current_state==INITIAL_STATE){
            mDefaultPaint.setAlpha((int) (initial_radius/mDefaultRadius*255));
        }
        if (current_state==INITIAL_STATE||current_state==NORMAL_STATE){
            mDefaultPaint.setColor(Color.WHITE);
            RadialGradient radialGradient = new RadialGradient(measuredWidth / 2
                    , measuredHeight / 2,mDefaultRadius-shift/10+voice_shadow_width
                    ,new int[]{Color.WHITE
                    ,Color.WHITE
                    ,Color.WHITE
                    ,Color.WHITE
                    ,Color.WHITE

                    ,Color.WHITE
                    ,Color.WHITE,Color.TRANSPARENT}
                    ,null, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(radialGradient);
            if (current_state==INITIAL_STATE){
                mDefaultPaint.setAlpha(voiceCircleAlpha);
            }else {
                mDefaultPaint.setAlpha(255);
            }
            canvas.drawCircle(measuredWidth/2,measuredHeight/2, mDefaultRadius-shift/10+voice_shadow_width,mDefaultPaint);
            mDefaultPaint.setShader(null);

            mDefaultPaint.setColor(Color.rgb(0,0x00,0xff));
            if (current_state==INITIAL_STATE){
                mDefaultPaint.setAlpha(voiceCircleAlpha);
            }else {
                mDefaultPaint.setAlpha(255);
            }
            canvas.drawCircle(measuredWidth/2,measuredHeight/2, mDefaultRadius - shift/10,mDefaultPaint);
            //canvas.drawBitmap(drawableToBitmap(voiceDrawable),measuredWidth/2 - voiceDrawable.getIntrinsicWidth()/2,measuredHeight/2 - voiceDrawable.getIntrinsicHeight()/2,mDefaultPaint);
            mDefaultPaint.setAlpha(255);
        }
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
            Log.i(TAG,"drawVoiceCircle->larger:"+initialLargerRadius+",lessen:"+initialLessenRadius+",default:"+mDefaultRadius);

        }
        if (current_state==PRE_SEARCH_STATE){
            //(lar+shift/2-mPreSearchRadius)/(mDefaultRadius +shift/2- search_lessen_size)
            mDefaultPaint.setColor(Color.WHITE);
            mDefaultPaint.setAlpha((int) (Math.sqrt((mPreSearchRadius - search_lessen_size)/(mDefaultRadius +shift/2- search_lessen_size))*255));
            RadialGradient radialGradient = new RadialGradient(measuredWidth / 2
                    , measuredHeight / 2,(mDefaultRadius- shift/10+voice_shadow_width)*(mPreSearchRadius - search_lessen_size)/(mDefaultRadius +shift/2- search_lessen_size)
                    ,new int[]{Color.WHITE
                    ,Color.WHITE
                    ,Color.WHITE
                    ,Color.WHITE
                    ,Color.WHITE

                    ,Color.WHITE
                    ,Color.WHITE,Color.TRANSPARENT}
                    ,null, Shader.TileMode.CLAMP);
            mDefaultPaint.setShader(radialGradient);
            canvas.drawCircle(measuredWidth/2,measuredHeight/2, (mDefaultRadius - shift/10)*(mPreSearchRadius - search_lessen_size)/(mDefaultRadius +shift/2- search_lessen_size)+voice_shadow_width,mDefaultPaint);
            mDefaultPaint.setShader(null);
            mDefaultPaint.setAlpha((int) (Math.sqrt((mPreSearchRadius - search_lessen_size)/(mDefaultRadius +shift/2- search_lessen_size))*255));
            mDefaultPaint.setColor(Color.rgb(0,0x00,0xff));
            canvas.drawCircle(measuredWidth/2,measuredHeight/2, (mDefaultRadius - shift/10)*(mPreSearchRadius - search_lessen_size)/(mDefaultRadius +shift/2- search_lessen_size),mDefaultPaint);
            Log.i(TAG,"drawVoiceCircle->：mPreSearchRadius"+(mPreSearchRadius - search_lessen_size)/(mDefaultRadius +shift/2- search_lessen_size));
            mDefaultPaint.setAlpha(255);
        }



    }

    private void drawVoice(Canvas canvas) {
        if (current_state==INITIAL_STATE){
            mDefaultPaint.setAlpha(voiceAlpha);
        }else {
            mDefaultPaint.setAlpha(255);
        }

        canvas.drawBitmap(drawableToBitmap(voiceDrawable),measuredWidth/2 - voiceDrawable.getIntrinsicWidth()/2,measuredHeight/2 - voiceDrawable.getIntrinsicHeight()/2,mDefaultPaint);
        mDefaultPaint.setAlpha(255);
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
        if (current_state == INITIAL_STATE ||current_state == NORMAL_STATE){
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
            }else if (!rotateAnimator.isRunning()){
                rotateAnimator.start();
            }
        }else if(current_state == SEARCH_STATE){
            if (rotateSearchAnimator==null){
                rotateSearchAnimator = ValueAnimator.ofFloat(mDegree, (float) Math.PI*2+mDegree);
                rotateSearchAnimator.setRepeatCount(-1);
                rotateSearchAnimator.setDuration(normal_duration*8);
                rotateSearchAnimator.setRepeatMode(ValueAnimator.RESTART);
                rotateSearchAnimator.setInterpolator(new LinearInterpolator());
                rotateSearchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {

                        mSearchDegree = (float) animation.getAnimatedValue()%360;
                    }
                });
                rotateSearchAnimator.start();
            }
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

                voiceAlphaAnimator = ValueAnimator.ofInt(0,255);
                voiceAlphaAnimator.setDuration(initial_duration/2);
                voiceAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        voiceAlpha = (int)animation.getAnimatedValue();
                    }
                });

                voiceCircleAlphaAnimator = ValueAnimator.ofInt(0,255);
                voiceCircleAlphaAnimator.setDuration(initial_duration/2);
                voiceCircleAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        voiceCircleAlpha = (int)animation.getAnimatedValue();
                    }
                });
                voiceCircleAlphaAnimator.setStartDelay(initial_duration/2);
                voiceCircleAlphaAnimator.start();
                voiceAlphaAnimator.start();
                shiftAnimator.start();
                largerAnimator.start();
                lessenAnimator.start();



            }


            Log.i(TAG,"initial_radius->"+initial_radius);

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
            }else if (!normal_animator.isRunning()){
                normal_animator.start();
            }

        } else if (current_state==SEARCH_STATE){
            if (searchScaleAnimator1 ==null){
                searchScaleAnimator1 = ValueAnimator.ofFloat(search_lessen_size, search_larger_size,search_lessen_size);
                searchScaleAnimator1.setDuration(normal_duration*2);
                searchScaleAnimator1.setRepeatCount(-1);
                searchScaleAnimator1.setRepeatMode(ValueAnimator.RESTART);
                searchScaleAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        search_scale_size_1 = (Float) animation.getAnimatedValue();

                    }
                });
                searchScaleAnimator2 = ValueAnimator.ofFloat(search_lessen_size, search_larger_size,search_lessen_size);
                searchScaleAnimator2.setDuration(normal_duration*2);
                searchScaleAnimator2.setRepeatCount(-1);

                searchScaleAnimator2.setRepeatMode(ValueAnimator.RESTART);
                searchScaleAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        search_scale_size_2 = (Float) animation.getAnimatedValue();

                    }
                });
                searchScaleAnimator2.setStartDelay(normal_duration);
                searchScaleAnimator2.start();
                searchScaleAnimator1.start();
            }


        }else if(current_state == PRE_SEARCH_STATE){//当状态为抬起状态时，
            //需要暂停动画,暂停时就可以获得mDegree和shift值，据此可以得到三个圆的圆心
            if (rotateAnimator!=null&&rotateAnimator.isRunning()){
                rotateAnimator.pause();
            }
            if (normal_animator!=null&& normal_animator.isRunning()){
                normal_animator.pause();
            }
            //
            if (preSearchAnimator==null&&mDefaultRadius!=0){

                Log.i(TAG,"computeScroll->"+mDefaultRadius);
                preSearchAnimator = ValueAnimator.ofFloat(mDefaultRadius + shift / 2, search_lessen_size);
                preSearchAnimator.setDuration(pre_search_duration);
                preSearchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mPreSearchRadius = (float)animation.getAnimatedValue();

                    }
                });
                preSearchAnimator.setRepeatCount(0);
                preSearchAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        current_state = SEARCH_STATE;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                preSearchAnimator.start();
            }/*else if (preSearchAnimator!=null&&!preSearchAnimator.isRunning()){
                preSearchAnimator.start();
            }*/

        }else{


        }
        super.computeScroll();
        postInvalidate();


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

    /**
     * 改变当前的状态
     * @param state
     */
    public void changeState(int state){
        current_state = state;

    }

}
