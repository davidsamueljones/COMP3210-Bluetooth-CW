package ecs.soton.dsjrtc.adobjects;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

public class ObjText implements Drawable {
	Point position;
	int font_ID;
	int font_color;
	int font_size;
	int rotation;
	String content;
	
	public ObjText(Point position, int font_ID, int font_color, int font_size, int rotation, int textLength, String content) {
		this.position = position;
		this.font_ID = font_ID;
		this.font_color = font_color;
		this.font_size = font_size;
		this.rotation = rotation;
		this.content = content;
	}

	public ObjText(int font_ID, int font_color, int font_size, int rotation, int textLength, String content) {
		this(null, font_ID, font_color, font_size, rotation, textLength, content);
	}

	public ObjText(byte[] content) {
		this.content = new String(content);
	}

	@Override
	public void draw(Context context, Canvas canvas, Paint mPaint) {
		if (position == null) {
			float scale = context.getResources().getDisplayMetrics().density;
			int textWidth = canvas.getWidth() - (int) (16 * scale);

			TextPaint tPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
			tPaint.setColor(Color.rgb(61, 61, 61));
			tPaint.setTextSize((int)(14 * scale));
			tPaint.setShadowLayer(1f, 0f, 1f, Color.GRAY);

			StaticLayout textLayout = new StaticLayout(content, tPaint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
			int textHeight = textLayout.getHeight();

			float xPos = (canvas.getWidth() - textWidth)/2f;
			float yPos = (canvas.getHeight() - textHeight)/2f;

			canvas.save();
			canvas.translate(xPos, yPos);
			textLayout.draw(canvas);
			canvas.restore();
		} else {
			mPaint.setTextSize(font_size);
			// TODO: Decide what to do with font_ID
			mPaint.setColor(font_color);
			mPaint.setStyle(Paint.Style.FILL);

			canvas.save();
			canvas.rotate((float) (rotation / 255.0) * 360, position.getX(), position.getY());
			canvas.drawText(content, position.getX(), position.getY(), mPaint);
			canvas.restore();
		}
	}
}
