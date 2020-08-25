package com.aliyun.rtc.voicecall.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

public class BitmapUtil {

    public static Bitmap createCircleImage(Context context, int sourceId) {
        Resources resources = context.getResources();
        Bitmap source = BitmapFactory.decodeResource(resources, sourceId);
        return createCircleImage(source);
    }

    public static Bitmap createCircleImage(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        float raduis = Math.min(width, height) * 0.5f;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //paint.setColor(Color.RED);
        //画布设置遮罩效果
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        //处理图像数据
        Bitmap bitmap = Bitmap.createBitmap(width, height, source.getConfig());
        Canvas canvas = new Canvas(bitmap);
        //bitmap的显示由画笔paint来决定
        canvas.drawCircle(width * 0.5f, height * 0.5f, raduis, paint);
        return bitmap;
    }

    public static Bitmap createRoundrectImage(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        float raduis = Math.min(width, height) * 0.1f;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //paint.setColor(Color.RED);
        //画布设置遮罩效果
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        //处理图像数据
        Bitmap bitmap = Bitmap.createBitmap(width, height, source.getConfig());
        Canvas canvas = new Canvas(bitmap);
        //bitmap的显示由画笔paint来决定
        canvas.drawRoundRect(new RectF(0, 0, width, height), raduis, raduis, paint);
        return bitmap;
    }
}
