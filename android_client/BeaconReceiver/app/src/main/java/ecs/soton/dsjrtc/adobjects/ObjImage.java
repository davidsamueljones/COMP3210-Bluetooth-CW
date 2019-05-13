package ecs.soton.dsjrtc.adobjects;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import ecs.soton.dsjrtc.beaconreceiver.MainActivity;
import ecs.soton.dsjrtc.beaconreceiver.R;

public class ObjImage implements Drawable {
	int image_ID;
	byte[] imageBytes = null;

	Point position;
	int width;
	int height;
	int rotation;

	private ObjImage(Point position, int width, int height, int rotation) {
		this.position = position;
		this.width = width;
		this.height = height;
		this.rotation = rotation;
	}

	public ObjImage(int image_ID, Point position, int width, int height, int rotation) {
		this(position, width, height, rotation);
		this.image_ID = image_ID;
	}

	public ObjImage(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

	@Override
	public void draw(Context context, Canvas canvas, Paint mPaint) {
		Resources res = context.getResources();
		Bitmap imageBMP = null;

		// draw from bytes
		// TODO: Convert ID to image
		if (imageBytes == null) {
			int resourceID = res.getIdentifier("kirk", "drawable", context.getPackageName());
			imageBMP = BitmapFactory.decodeResource(res, resourceID);
		} else {
			imageBMP = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
		}

		canvas.drawBitmap(imageBMP, 0, 0, mPaint);
	}
}
