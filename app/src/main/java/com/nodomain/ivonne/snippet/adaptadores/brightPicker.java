package com.nodomain.ivonne.snippet.adaptadores;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nodomain.ivonne.snippet.R;

/**
 * Created by Ivonne on 09/11/2017.
 */

public class brightPicker extends View{

    public interface OnSeekBarChangeListener {
        void onProgressChanged(brightPicker brightPicker, int progress);
        void onStartTrackingTouch(brightPicker brightPicker);
        void onStopTrackingTouch(brightPicker brightPicker);
    }

    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    /**
     * Customizable display parameters (in percents)
     */
    private final int paramOuterPadding = 2; // outer padding of the whole color picker view
    private final int paramInnerPadding = 1; // distance between value slider wheel and inner color wheel
    private final int paramButtonWheelRadius = 11;

    private Paint valueSliderPaint; // anillo de brillo

    private Paint brightPointerPaint; // thumb selector
    private RectF brightPointerCoords;
    private int brightPointerWidth = 20;

    private Paint innerButtonPaint; //boton encendido apagado

    private Paint buttonTextPaint;
    private String estado = "ON";
    private int largoTexto = 22;

    private Paint outterCirclePaint;

    private Path valueSliderPath; // anillo de brillo

    private int brightSliderWidth; // ancho del anillo (ajuste de tamaño)
    private int innerPadding;
    private int outerPadding;

    private int brightWheelRadius; // radio del anillo de brillo
    private int buttonWheelRadius; // radio del boton
    private int middleWheelRaduis; // punto medio entre el anillo de brillo y el boton

    private int escala = 360;
    private boolean enabled = false;
    private boolean onOff = true;
    private boolean mIsDragging;

    private Matrix gradientRotationMatrix;

    /** Currently selected color */
    private float brilloHSV = 0f;
    private double brilloSeleccionado;

    public brightPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public brightPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public brightPicker(Context context) {
        super(context);
        init();
    }

    private void init() {

        brightPointerPaint = new Paint();
        brightPointerPaint.setStyle(Paint.Style.STROKE);
        brightPointerPaint.setARGB(128, 0, 0, 0);
        brightPointerPaint.setDither(true);

        valueSliderPaint = new Paint();
        valueSliderPaint.setAntiAlias(true);
        valueSliderPaint.setDither(true);

        innerButtonPaint = new Paint();
        innerButtonPaint.setAntiAlias(true);
        innerButtonPaint.setColor(Color.DKGRAY);
        innerButtonPaint.setDither(true);

        buttonTextPaint = new Paint();
        buttonTextPaint.setColor(Color.argb(255, 128, 203, 196));
        buttonTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.texto_normal));

        valueSliderPath = new Path();

        brightPointerCoords = new RectF();

        outterCirclePaint = new Paint();
        outterCirclePaint.setAntiAlias(true);
        outterCirclePaint.setStyle(Paint.Style.STROKE);
        outterCirclePaint.setDither(true);

        largoTexto = getResources().getDimensionPixelSize(R.dimen.texto_normal)*2/3;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(widthSize, heightSize);
        setMeasuredDimension(size, size);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        //circulo exterior
        outterCirclePaint.setStrokeWidth(outerPadding);
        RadialGradient gradient = new RadialGradient(centerX, centerY, outerPadding  , Color.TRANSPARENT,
                Color.WHITE, Shader.TileMode.MIRROR);
        outterCirclePaint.setShader(gradient);
        canvas.drawCircle(centerX, centerY, brightWheelRadius + 2* outerPadding, outterCirclePaint);

        // boton de enmedio
        //canvas.drawBitmap(colorWheelBitmap, centerX - colorWheelRadius, centerY - colorWheelRadius, null);
        canvas.drawCircle(centerX, centerY, buttonWheelRadius - innerPadding/2, innerButtonPaint);
        canvas.drawText(estado, centerX-largoTexto, centerY+(getResources().getDimensionPixelSize(R.dimen.texto_normal)/2), buttonTextPaint);

        // anillo de brillo
        SweepGradient sweepGradient = new SweepGradient(centerX, centerY, new int[] { Color.WHITE, Color.BLACK, Color.WHITE }, null);
        sweepGradient.setLocalMatrix(gradientRotationMatrix);
        valueSliderPaint.setShader(sweepGradient);

        canvas.drawPath(valueSliderPath, valueSliderPaint);

        //thumb selector
        if (enabled)
            drawPointer(canvas);
    }

    private void drawPointer(Canvas canvas) {

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        double hueAngle = (float) Math.toRadians(brilloHSV);

        double hueAngleX = Math.cos(hueAngle) * middleWheelRaduis;
        double hueAngleY = Math.sin(hueAngle) * middleWheelRaduis;

        int brightPointX = (int) hueAngleX + centerX;
        int brightPointY = (int) hueAngleY + centerY;

        float pointerRadius = brightPointerWidth * 2;//0.075f * brightWheelRadius;
        brightPointerPaint.setStrokeWidth(brightPointerWidth);
        int pointerX = (int) (brightPointX - pointerRadius / 2);
        int pointerY = (int) (brightPointY - pointerRadius / 2);

        RadialGradient gradient = new RadialGradient(brightPointX, brightPointY, pointerRadius, 0xFF000000,
                0xFFFFFFFF, Shader.TileMode.CLAMP);
        brightPointerPaint.setShader(gradient);

        brightPointerCoords.set(pointerX, pointerY, pointerX + pointerRadius, pointerY + pointerRadius);
        canvas.drawOval(brightPointerCoords, brightPointerPaint);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {

        int centerX = width / 2;
        int centerY = height / 2;

        innerPadding = (int) (paramInnerPadding * width / 100);
        outerPadding = (int) (paramOuterPadding * width / 100);

        //brightSliderWidth = (int) (paramBrightSliderWidth * width / 100);
        buttonWheelRadius = (int) (paramButtonWheelRadius * width / 100 );
        brightWheelRadius = width / 3 - outerPadding;
        brightSliderWidth = (int) (brightWheelRadius - buttonWheelRadius);
        middleWheelRaduis = buttonWheelRadius + brightSliderWidth/2;

        //brightPointerWidth = 20 + ((width - 720)/72);
        brightPointerWidth = (200 * width / 7200 );

        gradientRotationMatrix = new Matrix();
        gradientRotationMatrix.preRotate(270, width / 2, height / 2);

        valueSliderPath.addCircle(centerX, centerY, brightWheelRadius, Path.Direction.CCW);//outherCircle
        valueSliderPath.addCircle(centerX, centerY, buttonWheelRadius, Path.Direction.CW);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (enabled) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int cx = x - getWidth() / 2;
            int cy = y - getHeight() / 2;
            double d = Math.sqrt(cx * cx + cy * cy);
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:{
                    if (d <= brightWheelRadius & d > buttonWheelRadius) {
                        onStartTrackingTouch();
                        moverThumb(event);
                    }
                    else if (d < buttonWheelRadius){ // se apaga o enciende al presionar el boton
                        onStartTrackingTouch();
                        toogleOnOff();
                    }
                    return true;
                }
                case MotionEvent.ACTION_MOVE:{
                    if (mIsDragging) {
                        moverThumb(event);
                    } else {
                        if (d <= brightWheelRadius & d > buttonWheelRadius) {
                            onStartTrackingTouch();
                            moverThumb(event);
                        }
                    }
                    return true;
                }
                case MotionEvent.ACTION_UP: {
                    if (mIsDragging) {
                        moverThumb(event);
                        onStopTrackingTouch();
                        setPressed(false);
                    }
                    else
                        if (d <= brightWheelRadius & d > buttonWheelRadius)
                            onStopTrackingTouch();
                    break;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private void moverThumb(MotionEvent event){
        int x = (int) event.getX();
        int y = (int) event.getY();
        int cx = x - getWidth() / 2;
        int cy = y - getHeight() / 2;
        double d = Math.sqrt(cx * cx + cy * cy);

        if (d <= brightWheelRadius & d > buttonWheelRadius) {
            brilloHSV = (float) (Math.toDegrees(Math.atan2(cy, cx)));
            invalidate();
            brilloSeleccionado = ((-Math.sin(Math.toRadians(brilloHSV)) + 1 ) /2 ) * escala;
            if (brilloSeleccionado > (180/escala)) {
                onOff = true;
                estado = "ON";
                largoTexto = getResources().getDimensionPixelSize(R.dimen.texto_normal)*2/3;
            }
            else {
                onOff = false;
                estado = "OFF";
                largoTexto = getResources().getDimensionPixelSize(R.dimen.texto_normal)*4/5;
            }
            onProgressRefresh (escala, (int) brilloSeleccionado);
        }
    }

    private void toogleOnOff(){
        if (onOff){
            onOff = false;
            estado = "OFF";
            largoTexto = getResources().getDimensionPixelSize(R.dimen.texto_normal)*4/5;
            invalidate();
            brilloHSV = 90;
            onProgressRefresh (escala, 0);
        }
        else{
            onOff = true;
            estado = "ON";
            largoTexto = getResources().getDimensionPixelSize(R.dimen.texto_normal)*2/3;
            invalidate();
            brilloHSV = -90;
            onProgressRefresh (escala, 100);
        }
        invalidate();
    }

    void onStartTrackingTouch() {
        mIsDragging = true;
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    void onProgressRefresh(float scale, int progress) {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, progress);
        }
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListener = l;
    }

    void onStopTrackingTouch() {
        mIsDragging = false;
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    public void setBrillo(int brillo, int escala) {
        this.escala = escala;
        double aux = (double) brillo / escala;
        double rad = - (2 * aux ) + 1;
        brilloHSV = (float) rad * 90;
        if (brilloHSV == 90){
            onOff = false;
            estado = "OFF";
            largoTexto = getResources().getDimensionPixelSize(R.dimen.texto_normal)*4/5;
        }
        invalidate();
    }

    public int getBrillo() {
        brilloSeleccionado = ((-Math.sin(Math.toRadians(brilloHSV)) + 1 ) /2 ) * escala;
        return (int) brilloSeleccionado;
    }

    public void setEscala(int i){
        this.escala = i;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
        if (enabled)
            buttonTextPaint.setColor(Color.argb(255, 128, 203, 196));
        else
            buttonTextPaint.setColor(Color.LTGRAY);
    }

    public void setOnOf(boolean onOff){
        this.onOff = onOff;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putFloat("color", brilloHSV);
        state.putParcelable("super", super.onSaveInstanceState());
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            brilloHSV = bundle.getFloat("color");
            super.onRestoreInstanceState(bundle.getParcelable("super"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

}
