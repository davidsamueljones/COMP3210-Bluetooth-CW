import struct
import math
import bitstring # https://pythonhosted.org/bitstring/packing.html
import zlib
from enum import Enum

# Structure limits
UINT16_MAX = pow(2, 16) - 1
UINT16_MIN = 0
UINT8_MAX = pow(2, 8) - 1
UINT8_MIN = 0
INT16_MAX = pow(2, 15) - 1
INT16_MIN = -pow(2, 15)
INT8_MAX = pow(2, 7) - 1
INT8_MIN = -pow(2, 7)

class PacketSection(Enum):
    NONE = 0x00
    RESERVED_A = 0x01
    TITLE = 0x02
    CANVAS = 0x03
    IMAGE = 0x04
    TEXT = 0x05
    SHAPE_POLYGON = 0x06
    SHAPE_CIRCLE = 0x07

    def __int__(self):
        return self.value

def gen_canvas_bitstring(dims, bg_col):
    """
    Create the bitstring for a canvas packet section. 

    Arguments:
        dims : Size of the canvas (in generic points) to use as a scale reference. As width/height.
        bg_col : Tuple of three values corresponding to RGB, each between 0 and 255.

    Returns:
        The generated bitstring
        Output Structure:
            * CANVAS (UINT8)
            * Width/Height Dimensions [Width (UINT16), Height (UINT16)]
            * Background Colour [R (UINT8), G (UINT8), B (UINT8)]
    """
    # Validate inputs
    if len(dims) is not 2 or any(map(lambda x: x < 0 or x > UINT16_MAX, dims)):
        raise ValueError('The provided dimensions for the canvas are not valid ', dims) 
    if len(bg_col) is not 3 or any(map(lambda x: x < 0 or x > UINT8_MAX, bg_col)):
        raise ValueError('The provided RGB value for the canvas background is not valid ', bg_col) 
    # # Constrain inputs to sending limits
    # dims = tuple(map(lambda x: round(x), dims))
    # bg_col = tuple(map(lambda x: round(x), bg_col))
    # Create packet
    bits = bitstring.BitString()
    bits.append(bitstring.pack('>B', int(PacketSection.CANVAS)))
    bits.append(bitstring.pack('>2H', *dims))
    bits.append(bitstring.pack('>3B', *bg_col))
    return bits


def gen_img_bitstring(img_id, position, dims, rotation):
    """
    Create the bitstring for an image to load.

    Arguments:
        img_id : ID of an image from the application image dictionary
        position : Tuple of two values corresponding to X Y on a canvas
        dims : Dimensions of the image (use negatives for flipping)
        rotation : Rotation of the image in degrees (0-360) where 0 is horizontal ->

    Returns:
        The generated bitstring
        Output Structure:
            * [0]        IMAGE (UINT8)
            * [1]        Image ID (UINT8)
            * [2-3, 4-5] XY Position [X (UINT16), Y (UINT16)]
            * [6-7, 8-9] Width/Height Dimensions [Width (INT_16), Height (INT_16)]
            * [10]       Rotation where each increment corresponds to a 1.41 degree (UINT8)
    """
    # Validate inputs
    if img_id < 0 or img_id > UINT8_MAX:
        raise ValueError('The provided Image ID is not valid ', img_id) 
    if len(position) is not 2 or any(map(lambda x: x < 0 or x > UINT16_MAX, position)):
        raise ValueError('The provided position value for the image is not valid ', position) 
    if len(dims) is not 2 or any(map(lambda x: x < 0 or x > INT16_MAX, dims)):
        raise ValueError('The provided dimensions for the image are not valid ', dims)
    if rotation < 0 or rotation > 360:
        raise ValueError('The provided image rotation is not valid ', rotation) 

    # Constrain inputs
    conv_rotation = round(255 / 360.0 * rotation)
    # Create packet
    bits = bitstring.BitString()
    bits.append(bitstring.pack('>B', int(PacketSection.IMAGE)))
    bits.append(bitstring.pack('>B', img_id))
    bits.append(bitstring.pack('>2H', *position))
    bits.append(bitstring.pack('>2h', *dims))
    bits.append(bitstring.pack('>B', conv_rotation))
    return bits


def gen_text_bitstring(position, font_id, font_col, font_size, rotation, text_str):
    """
    Create the bitstring for some text.

    Arguments:
        position : Tuple of two values corresponding to X Y on a canvas
        font_id : ID of a font from the application font dictionary
        font_col : Colour of the font
        font_size : Font size in points (relative to canvas)
        rotation : Rotation of the text in degrees (0-360) where 0 is horizontal ->
        text_str : ASCII only string (not unicode) to write

    Returns:
        The generated bitstring
        Output Structure:
            * TEXT (UINT8)
            * XY Position [X (UINT16), Y (UINT16)]
            * Font ID (UINT8)
            * Font Colour [R (UINT8), G (UINT8), B (UINT8)]
            * Font Size (UINT8)
            * Rotation where each increment corresponds to a 1.41 degree (UINT8)
            * Text String (UINT8[]) Up until null terminator '\0' (0x00)
    """

    # Validate inputs
    if len(position) is not 2 or any(map(lambda x: x < 0 or x > UINT16_MAX, position)):
        raise ValueError('The provided position value for the text is not valid ', position) 
    if font_id < 0 or font_id > UINT8_MAX:
        raise ValueError('The provided font ID is not valid ', font_id) 
    if len(font_col) is not 3 or any(map(lambda x: x < 0 or x > UINT8_MAX, font_col)):
        raise ValueError('The provided RGB value for the text is not valid ', font_col) 
    if font_size < 0 or font_size > UINT8_MAX:
        raise ValueError('The provided font size is not valid ', font_size) 
    if rotation < 0 or rotation > 360:
        raise ValueError('The provided text rotation is not valid ', rotation) 
    if len(text_str) == 0:
        raise ValueError('The provided string is not valid ', text_str)
    # Constrain inputs
    conv_rotation = round(255 / 360.0 * rotation)
    # Create packet
    bits = bitstring.BitString()
    bits.append(bitstring.pack('>B', int(PacketSection.TEXT)))
    bits.append(bitstring.pack('>2H', *position))
    bits.append(bitstring.pack('>B', font_id))
    bits.append(bitstring.pack('>3B', *font_col))
    bits.append(bitstring.pack('>B', font_size))
    bits.append(bitstring.pack('>B', conv_rotation))
    bits.append(bitstring.pack('>B', len(text_str)))
    for c in text_str:
        bits.append(bitstring.pack('>B', ord(c)))
    return bits


def gen_polygon_bitstring(fill_col, points):
    """
    Create the bitstring for a polygon. 

    Arguments:
        fill_col : Tuple of three values corresponding to RGB, each between 0 and 255.
        points : A tuple of tuples for a polygon

    Returns:
        The generated bitstring
        Output Structure:
            * SHAPE_POLYGON (UINT8)
            * Fill Colour [R (UINT8), G (UINT8), B (UINT8)]
            * Number of Points (UINT8)
            * Points [P1_X (UINT16), P1_Y (UINT16),  P#...]
    """
    # Validate inputs
    if len(points) < 3: # TODO: Add check for points
        raise ValueError('The provided points for the polygon are not valid ', points)
     
    if len(fill_col) is not 3 or any(map(lambda x: x < 0 or x > UINT8_MAX, fill_col)):
        raise ValueError('The provided RGB value for the polygon fill is not valid ', fill_col) 
    # # Constrain inputs to sending limits

    bits = bitstring.BitString()
    bits.append(bitstring.pack('>B', int(PacketSection.SHAPE_POLYGON)))
    bits.append(bitstring.pack('>3B', *fill_col))
    bits.append(bitstring.pack('>B', len(points)))
    for p in points:
        if len(p) is not 2 or any(map(lambda x: x < 0 or x > UINT16_MAX, p)):
            raise ValueError('The provided position value for a polygon point is not valid ', p) 
        bits.append(bitstring.pack('>2H', *p))

    return bits

def generate_ad(bss):    
    """
    From a list of separate componenets combine them to create an ad.

    Arguments:
        bss : A list of object bitstrings 

    Returns:
        Combined packets as a bitstring
    """
    # Create a bitstream of all the objects
    bit_stream = bitstring.BitString()
    for bs in bss:
        bit_stream.append(bs)
    return bit_stream
    

if __name__ == "__main__":
    bss = list()
    bss.append(gen_canvas_bitstring((1000, 500), (1, 0, 0)))
    bss.append(gen_img_bitstring(1, (23, 52), (1, 0), 0))
    bss.append(gen_text_bitstring((50, 50), 1, (1, 0, 0), 50, 90, "Richard is a cuck!"))
    bss.append(gen_polygon_bitstring((50, 50, 50), ((50, 50), (50, 50), (25, 50), (123, 988))))
    bss.append(gen_text_bitstring((50, 50), 1, (1, 0, 0), 50, 90, "Yep, still a cuck!"))

    ad = generate_ad(bss)
    adstr = str(ad.hex )
    java_bytes = 'byte[] test = {'
    java_bytes = java_bytes + ', '.join("(byte) 0x" + adstr[i:i+2] for i in range(0, len(adstr), 2))
    java_bytes = java_bytes + '};'
    print(java_bytes)