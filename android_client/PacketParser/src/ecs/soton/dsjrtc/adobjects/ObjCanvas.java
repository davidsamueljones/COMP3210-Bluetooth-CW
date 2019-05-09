package ecs.soton.dsjrtc.adobjects;

import java.awt.Color;

public class ObjCanvas implements Drawable {
	int width;
	int height;
	Color bg_color;
	
	public ObjCanvas(int width, int height, Color bg_color) {
		this.width = width;
		this.height = height;
		this.bg_color = bg_color;
	}
}
