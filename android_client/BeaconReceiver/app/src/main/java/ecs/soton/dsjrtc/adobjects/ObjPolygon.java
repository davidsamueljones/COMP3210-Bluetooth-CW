package ecs.soton.dsjrtc.adobjects;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import java.util.ArrayList;

public class ObjPolygon implements Drawable {
	int fill_color;
	int num_points;
	ArrayList<Point> points;

	Path polyPath;

	public ObjPolygon(int fill_color, int num_points, ArrayList<Point> points) {
		this.fill_color = fill_color;
		this.num_points = num_points;
		this.points = points;

		polyPath = new Path();
		polyPath.moveTo(points.get(0).getX(), points.get(0).getY());
		for (int i = 1; i < points.size(); i++) {
			polyPath.lineTo(points.get(i).getX(), points.get(i).getY());
		}
		polyPath.lineTo(points.get(0).getX(), points.get(0).getY());
		polyPath.close();
	}

	@Override
	public void draw(Context context, Canvas canvas, Paint mPaint) {
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(fill_color);
		Log.i("OVER HERE", points.toString());
		canvas.drawPath(polyPath, mPaint);
	}
}
