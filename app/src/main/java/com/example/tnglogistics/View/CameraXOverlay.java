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
    private Rect rect;

    public CameraXOverlay(Context context) {
        super(context);
        init();
    }

    public CameraXOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        clearPaint.setAntiAlias(true);

        // กำหนดขนาดของกรอบโปร่งใส
        rect = new Rect(90, 930, 990, 1230);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // สร้าง overlay สีดำทับทั้งหน้าจอ
        canvas.drawColor(Color.parseColor("#80000000"));

        // ลบพื้นที่ที่ต้องการให้โปร่งใส
        canvas.drawRect(rect, clearPaint);
    }
}
