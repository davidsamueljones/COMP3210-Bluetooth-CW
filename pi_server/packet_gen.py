import struct
import math
import bitstring # https://pythonhosted.org/bitstring/packing.html
import zlib

from enum import Enum

UINT16_MAX = pow(2, 16) - 1
UINT8_MAX = pow(2, 8) - 1

INT16_MAX = pow(2, 15) - 1
INT16_MIN = -pow(2, 15)

INT8_MAX = pow(2, 7) - 1
INT8_MIN = -pow(2, 7)

class PacketSection(Enum):
    AD_CFG = 0x01
    TITLE = 0x02
    CANVAS = 0x03
    IMAGE = 0x04
    TEXT = 0x05
    SHAPE_POLYGON = 0x06
    SHAPE_CIRCLE = 0x07

    def __int__(self):
        return self.value

def make_ad_cfg(packet_cnt, data_bitstream):
    bits = bitstring()
    # Make CRC from bitstream
    #zlib.crc32

    #hex(zlib.crc32(data_bitstream) & 0xffffffff)

def gen_canvas_bitstring(dims, bg_col):
    """
    Create the bitstring for a canvas packet section. 

    Arguments:
        dims : Size of the canvas (in generic points) to use as a scale reference. As width/height.
        bg_col : Tuple of three values corresponding to RGB, each between 0 and 255.

    Returns:
        The generated bitstring
        Output Structure:
            * CANVAS (UINT_8)
            * Width/Height Dimensions [Width (UINT_16), Height (UINT_16)]
            * Background Colour [R (UINT_8), G (UINT_8), B (UINT_8)]
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
    bits.append(struct.pack('>B', int(PacketSection.CANVAS)))
    bits.append(struct.pack('>2H', *dims))
    bits.append(struct.pack('>3B', *bg_col))
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
            * IMAGE (UINT_8)
            * Image ID (UINT_8)
            * XY Position [X (UINT_16), Y (UINT_16)]
            * Width/Height Dimensions [Width (INT_16), Height (INT_16)]
            * Rotation where each increment corresponds to a 1.41 degree (UINT_8)
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
    bits.append(struct.pack('>B', int(PacketSection.IMAGE)))
    bits.append(struct.pack('>B', img_id))
    bits.append(struct.pack('>2H', *position))
    bits.append(struct.pack('>2h', *dims))
    bits.append(struct.pack('>B', conv_rotation))
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
            * TEXT (UINT_8)
            * XY Position [X (UINT_16), Y (UINT_16)]
            * Font ID (UINT_8)
            * Font Colour [R (UINT_8), G (UINT_8), B (UINT_8)]
            * Font Size (UINT_8)
            * Rotation where each increment corresponds to a 1.41 degree (UINT_8)
            * Text String (UINT_8[]) Up until null terminator '\0' (0x00)
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
    bits.append(struct.pack('>B', int(PacketSection.TEXT)))
    bits.append(struct.pack('>2H', *position))
    bits.append(struct.pack('>B', font_id))
    bits.append(struct.pack('>3B', *font_col))
    bits.append(struct.pack('>B', font_size))
    bits.append(struct.pack('>B', conv_rotation))
    for c in text_str:
        bits.append(struct.pack('>B', ord(c))) 
    bits.append(struct.pack('>B', ord('\0')))  
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
            * SHAPE_POLYGON (UINT_8)
            * Fill Colour [R (UINT_8), G (UINT_8), B (UINT_8)]
            * Number of Points (UINT_8)
            * Points [P1_X (UINT_16), P1_Y (UINT_16),  P#...]
    """
    # Validate inputs
    if len(points) < 3: # TODO: Add check for points
        raise ValueError('The provided points for the polygon are not valid ', points)
     
    if len(fill_col) is not 3 or any(map(lambda x: x < 0 or x > UINT8_MAX, fill_col)):
        raise ValueError('The provided RGB value for the polygon fill is not valid ', fill_col) 
    # # Constrain inputs to sending limits

    bits = bitstring.BitString()
    bits.append(struct.pack('>B', int(PacketSection.SHAPE_POLYGON)))
    bits.append(struct.pack('>3B', *fill_col))
    bits.append(struct.pack('>B', len(points)))
    for p in points:
        if len(p) is not 2 or any(map(lambda x: x < 0 or x > UINT16_MAX, p)):
            raise ValueError('The provided position value for a polygon point is not valid ', p) 
        bits.append(struct.pack('>2H', *p))

    return bits


# First byte of each packet is a checksum over the whole packet
# CHECKSUM (1 byte) : AD_ID (1 byte) : P_ID (1 byte)

# AD_Start (1 byte)  : Num_Packets (1 byte) : Full CRC (4 bytes) [CREATE LAST]
# Title :: Title (1 byte++)
# Text_Settings_REF_ID (1 byte) : Position (2 bytes) : Font_ID (1 byte): Size (1 byte) : Rotation (1 byte)
# Text_REF_ID (1 byte) : Text... (1 byte for each character terminated by null terminator)
# Shape_Circle : Radius : Position : Color

# Create a bitstream of all the objects
bit_stream = bitstring.BitString()
bit_stream.append(gen_canvas_bitstring((1000, 500), (1, 0, 0)))
bit_stream.append(gen_img_bitstring(1, (23, 52), (1, 0), 0))
bit_stream.append(gen_text_bitstring((50, 50), 1, (1, 0, 0), 50, 90, "Richard is a cuck!"))
bit_stream.append(gen_polygon_bitstring((50, 50, 50), ((50, 50), (50, 50), (25, 50), (123, 988))))
#make_ad_cfg(bit_stream)
print(bit_stream)
# CRC Check
print(hex(zlib.crc32(bit_stream.bytes) & 0xffffffff))

# Segment bitstream into packets
# while remaining bitstream
# create new ad_packet = bitarray() 