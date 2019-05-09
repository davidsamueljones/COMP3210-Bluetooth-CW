package ecs.soton.dsjrtc.parser;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecs.soton.dsjrtc.adobjects.*;

public class Parser {

	boolean PRINTING = true;
	
	ArrayList<Drawable> objList;

	final int AD_CFG = 1;
	final int AD_CFG_LENGTH = 1;
	final int TITLE = 2;
	final int TITLE_LENGTH = 0; // ...then the text
	final int CANVAS = 3;
	final int CANVAS_LENGTH = 8;
	final int IMAGE = 4;
	final int IMAGE_LENGTH = 11;
	final int TEXT = 5;
	final int TEXT_LENGTH = 12; // and then some
	final int SHAPE_POLYGON = 6;
	final int POLYGON_LENGTH = 8; // 
	final int SHAPE_CIRCLE = 7;
	final int CIRCLE_LENGTH = 11;

	private Map<Integer, Integer> instructions;

	public Parser() {
		objList = new ArrayList<Drawable>();
		instructions = new HashMap<Integer, Integer>();

		instructions.put(AD_CFG, AD_CFG_LENGTH);
		instructions.put(TITLE, TITLE_LENGTH);
		instructions.put(CANVAS, CANVAS_LENGTH);
		instructions.put(IMAGE, IMAGE_LENGTH);
		instructions.put(TEXT, TEXT_LENGTH);
		instructions.put(SHAPE_POLYGON, POLYGON_LENGTH);
		instructions.put(SHAPE_CIRCLE, CIRCLE_LENGTH);
	}
	
	public boolean parseStream(byte[] stream) {
		ArrayList<ArrayList<Byte>> streamInstructions = new ArrayList<ArrayList<Byte>>();
		
		ArrayList<Byte> currentInstruction = null;
		int instructionType = -1;
		int j = 0;
		for (int i = 0; i < stream.length; i++) {
			byte currByte = stream[i];
			
			if (j == 0) {
				if (currentInstruction != null) {
					streamInstructions.add(currentInstruction);
				}
				
				instructionType = currByte & 0xFF;
				int numArgs = instructions.get(instructionType);
				int numPoints = 0;
				if (currByte == this.SHAPE_POLYGON) {
					numPoints = stream[i + 4];
				} else if (currByte == this.TEXT) {
					numPoints = -1 - numArgs;
				}
				j = numArgs + numPoints;
				
				currentInstruction = new ArrayList<Byte>();
				currentInstruction.add(currByte);
			}
			else
			{
				if (instructionType == this.TEXT) {
					if (currByte == 0x0) {
						j = 1;
					}
				}
				
				currentInstruction.add(currByte);
			}
			
			j--;
		}
		streamInstructions.add(currentInstruction);
		
		for (ArrayList<Byte> instruction : streamInstructions) {
			int currentType = instruction.get(0); 
			switch (currentType) {
			case AD_CFG:
				break;
			case TITLE:
				break;
			case CANVAS:
				parseCanvasInstruction(instruction);
				break;
			case IMAGE:
				parseImageInstruction(instruction);
				break;
			case TEXT:
				parseTextInstruction(instruction);
				break;
			case SHAPE_POLYGON:
				parsePolygonInstruction(instruction);
				break;
			case SHAPE_CIRCLE:
				break;
			default:
				System.out.println("Instruction type not found!");
				break;
			}
		}
		
		return true;
	}

	/*
	 * Input Structure:
       [0] 			CANVAS (UINT_8)
       [1-2, 3-4]	Width/Height Dimensions [Width (UINT_16), Height (UINT_16)]
       [5-7]		Background Colour [R (UINT_8), G (UINT_8), B (UINT_8)]
    */
	public boolean parseCanvasInstruction(ArrayList<Byte> args) {
		if ((args.size() != this.CANVAS_LENGTH) || args.get(0) != this.CANVAS) {
			System.err.println("Error parsing canvas instructions...\n");
			return false;
		}
		
		int width = bytesToInt(args.get(1), args.get(2));
		int height = bytesToInt(args.get(3), args.get(4));
		
		Color bg_color = bytesToColor(args.get(5), args.get(6), args.get(7));
		
		objList.add(new ObjCanvas(width, height, bg_color));
		
		if (PRINTING) {
			System.out.println("Width: " + width);
			System.out.println("Height: " + height);
			System.out.println("Colour: " + bg_color);
		}
		
		return true;
	}
	
	/*
	 * Input Structure:
	 * [0] 			IMAGE (UINT_8)
	 * [1] 			Image ID (UINT_8)
	 * [2-3, 4-5] 	XY Position [X (UINT_16), Y (UINT_16)]
	 * [6-7, 8-9] 	Width/Height Dimensions [Width (INT_16), Height (INT_16)]
	 * [10] 		Rotation where each increment corresponds to a 1.41 degree (UINT_8)
	 */
	public boolean parseImageInstruction(ArrayList<Byte> args) {
		if ((args.size() != this.IMAGE_LENGTH) || args.get(0) != this.IMAGE) {
			System.err.println("Error parsing image instructions...\n");
			return false;
		}

		// kinda using magic numbers, but you can see where I get them from
		// above
		int image_ID = args.get(1) & 0xFF;
		System.out.println(image_ID);

		int xPos = bytesToInt(args.get(2), args.get(3));
		int yPos = bytesToInt(args.get(4), args.get(5));

		int width = bytesToInt(args.get(6), args.get(7));
		int height = bytesToInt(args.get(8), args.get(9));

		int rotation = args.get(10) & 0xFF;

		objList.add(new ObjImage(image_ID, new Point(xPos, yPos), width, height, rotation));

		return true;
	}
	
	/*
	 * Input Structure:
     * [0]			TEXT (UINT_8)
     * [1-2, 3-4]	XY Position [X (UINT_16), Y (UINT_16)]
     * [5]			Font ID (UINT_8)
     * [6-8]		Font Colour [R (UINT_8), G (UINT_8), B (UINT_8)]
     * [9]			Font Size (UINT_8)
     * [10]			Rotation where each increment corresponds to a 1.41 degree (UINT_8)
     * [11+]		Text String (UINT_8[]) Up until null terminator '\0' (0x00)
	 */
	public boolean parseTextInstruction(ArrayList<Byte> args) {
		if ((args.size() < this.TEXT_LENGTH) || args.get(0) != this.TEXT) {
			System.err.println("Error parsing text instructions...\n");
			return false;
		}
		
		int xPos = bytesToInt(args.get(1), args.get(2));
		int yPos = bytesToInt(args.get(3), args.get(4));
		
		int font_ID = args.get(5) & 0xFF;
		
		Color font_color = bytesToColor(args.get(6), args.get(7), args.get(8));
		
		int font_size = args.get(9) & 0xFF;
		
		int rotation = args.get(10) & 0xFF;
		
		String content = "";
		try {
			content = new String(byteListToArray(args.subList(11, args.size())), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		objList.add(new ObjText(new Point(xPos, yPos), font_ID, font_color, font_size, rotation, content));
		
		return true;
	}
	
	/*
	 * Output Structure:
     * [0]			SHAPE_POLYGON (UINT_8)
     * [1-3]		Fill Colour [R (UINT_8), G (UINT_8), B (UINT_8)]
     * [4]			Number of Points (UINT_8)
     * [5-6, ...]	Points [P1_X (UINT_16), P1_Y (UINT_16),  P#...]
	 */
	public boolean parsePolygonInstruction(ArrayList<Byte> args) {
		if ((args.size() < this.POLYGON_LENGTH) || args.get(0) != this.SHAPE_POLYGON) {
			System.err.println("Error parsing polygon instructions...\n");
			return false;
		}
		
		Color fill_color = bytesToColor(args.get(1), args.get(2), args.get(3));
		
		int num_points = args.get(4) & 0xFF;
		
		ArrayList<Point> points = new ArrayList<Point>();
		for (int i = 0; i < num_points; i++) {
			int offset = 5 + (4 * i);
			
			int xPos = bytesToInt(args.get(offset), args.get(offset + 1));
			int yPos = bytesToInt(args.get(offset + 2), args.get(offset + 3));
			
			points.add(new Point(xPos, yPos));
		}
		
		objList.add(new ObjPolygon(fill_color, num_points, points));
		
		return true;
	}
	
	
	
	public int bytesToInt(byte a, byte b) {
		byte[] args = {a, b};
		return ByteBuffer.wrap(args).getShort(0) & 0xFFFF;
	}
	
	public Color bytesToColor(byte r, byte g, byte b) {
		return new Color(r & 0xFF, g & 0xFF, b & 0xFF);
	}
	
	public byte[] byteListToArray(List<Byte> list) {
		byte[] array = new byte[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		
		return array;
	}
}
