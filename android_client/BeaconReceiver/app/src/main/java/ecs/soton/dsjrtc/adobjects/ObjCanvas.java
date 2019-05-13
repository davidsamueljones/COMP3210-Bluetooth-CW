package ecs.soton.dsjrtc.adobjects;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class ObjCanvas implements Drawable {
	int width;
	int height;
	int bg_color;
	
	public ObjCanvas(int width, int height, int bg_color) {
		this.width = width;
		this.height = height;
		this.bg_color = bg_color;
	}

	@Override
	public void draw(Context context, Canvas canvas, Paint mPaint) {
		canvas.drawColor(bg_color);
	}
}
