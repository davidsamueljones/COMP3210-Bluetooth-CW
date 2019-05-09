package ecs.soton.dsjrtc.adobjects;

public class ObjImage implements Drawable {
	int image_ID;
	Point position;
	int width;
	int height;
	int rotation;
	
	public ObjImage(int image_ID, Point position, int width, int height, int rotation) {
		this.image_ID = image_ID;
		this.position = position;
		this.width = width;
		this.height = height;
		this.rotation = rotation;
	}
}
