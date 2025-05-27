package com.example.tnglogistics.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class CameraXOverlay extends View {
    private Paint clearPaint;
    private static Rect rect;

    public CameraXOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public static Rect getCropRect() {
        return rect;
    }

    private void init() {
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        clearPaint.setAntiAlias(true);
    }

//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//
//        // สร้างกรอบโปร่งใสขนาดประมาณ 80% ของความกว้าง และ 25% ของความสูงตรงกลางหน้าจอ
//        int left = (int) (w * 0.1);
//        int right = (int) (w * 0.9);
//        int top = (int) (h * 0.35);
//        int bottom = (int) (h * 0.6);
//
//        rect = new Rect(left, top, right, bottom);
//    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // ความกว้าง 80% ของหน้าจอ
        int left = (int) (w * 0.1);
        int right = (int) (w * 0.9);

        // ความสูง 15% ของหน้าจอ วางไว้ตรงกลาง
        int boxHeight = (int) (h * 0.15);
        int centerY = h / 2;
        int top = centerY - (boxHeight / 2);
        int bottom = centerY + (boxHeight / 2);

        rect = new Rect(left, top, right, bottom);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#80000000"));

        if (rect != null) {
            canvas.drawRect(rect, clearPaint);
        }
    }
}

//public class CameraXOverlay extends View {
//    private Paint clearPaint;
//    private Rect rect;
//
//    public CameraXOverlay(Context context) {
//        super(context);
//        init();
//    }
//
//    public CameraXOverlay(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init();
//    }
//
//    private void init() {
//        clearPaint = new Paint();
//        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        clearPaint.setAntiAlias(true);
//
//        // กำหนดขนาดของกรอบโปร่งใส
//        rect = new Rect(90, 930, 990, 1230);
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        // สร้าง overlay สีดำทับทั้งหน้าจอ
//        canvas.drawColor(Color.parseColor("#80000000"));
//
//        // ลบพื้นที่ที่ต้องการให้โปร่งใส
//        canvas.drawRect(rect, clearPaint);
//    }
//}
