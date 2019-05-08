package ecs.soton.dsjrtc.adobjects;

import java.awt.Color;

public class ObjText implements Drawable {
	Point position;
	int font_ID;
	Color font_color;
	int font_size;
	int rotation;
	String content;
	
	public ObjText(Point position, int font_ID, Color font_color, int font_size, int rotation, String content) {
		this.position = position;
		this.font_ID = font_ID;
		this.font_color = font_color;
		this.font_size = font_size;
		this.rotation = rotation;
		this.content = content;
	}
}
