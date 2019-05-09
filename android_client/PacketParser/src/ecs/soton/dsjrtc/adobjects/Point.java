package ecs.soton.dsjrtc.adobjects;

public class Point {
	int xPos;
	int yPos;
	
	public Point(int xPos, int yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
	}
	
	public int getX() {
		return this.xPos;
	}
	
	public int getY() {
		return this.yPos;
	}
	
	@Override
	public String toString() {
		return "(" + xPos + ", " + yPos + ")";
	}
}
