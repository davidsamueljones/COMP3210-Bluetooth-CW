package ecs.soton.dsjrtc.adobjects;

import java.awt.Color;
import java.util.ArrayList;

public class ObjPolygon implements Drawable {
	Color fill_color;
	int num_points;
	ArrayList<Point> points;
	
	public ObjPolygon(Color fill_color, int num_points, ArrayList<Point> points) {
		this.fill_color = fill_color;
		this.num_points = num_points;
		this.points = points;
	}
}
