package ecs.soton.dsjrtc.parser;

public class tester {

	public static void main(String[] args) {
		Parser p = new Parser();
		
		byte b0 = (byte) Integer.parseInt("00000011", 2);
		// width
		byte b1 = (byte) Integer.parseInt("00000001", 2);
		byte b2 = (byte) Integer.parseInt("00000001", 2);
		// height
		byte b3 = (byte) Integer.parseInt("00000001", 2);
		byte b4 = (byte) Integer.parseInt("00000001", 2);
		// colour
		byte b5 = (byte) Integer.parseInt("11111111", 2);
		byte b6 = (byte) Integer.parseInt("00000000", 2);
		byte b7 = (byte) Integer.parseInt("11111111", 2);
		byte b8 = (byte) Integer.parseInt("11111110", 2);
		
		byte[] byteArray = {b0, b1, b2, b3, b4, b5, b6, b7, b0, b1, b2, b3, b4, b5, b6, b8};
		p.parseStream(byteArray);
	}
}
