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
			int resourceID = res.getIdentifier("image_" + image_ID, "drawable", context.getPackageName());
			imageBMP = BitmapFactory.decodeResource(res, resourceID);
			canvas.drawBitmap(imageBMP, position.getX(), position.getY(), mPaint);
		} else {
			imageBMP = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
			Bitmap newBitmap = null;

			int canvasWidth = canvas.getWidth();
			int canvasHeight = canvas.getHeight();

			int imageWidth = imageBMP.getWidth();
			int imageHeight = imageBMP.getHeight();

			double heightRatio = (double) canvasHeight/imageHeight;
			int newWidth = (int) Math.round(imageWidth * heightRatio);
			if (newWidth <= canvasWidth) {
				newBitmap = Bitmap.createScaledBitmap(imageBMP, newWidth, canvasHeight, true);
			} else {
				double widthRatio = (double) canvasWidth/imageWidth;
				int newHeight = (int) Math.round(imageHeight * widthRatio);

				newBitmap = Bitmap.createScaledBitmap(imageBMP, canvasWidth, newHeight, true);
			}

			canvas.drawBitmap(newBitmap, 0, 0, mPaint);
		}
	}
}
