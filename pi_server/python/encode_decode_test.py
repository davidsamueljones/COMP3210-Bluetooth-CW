import sys
from lt import encode, decode
import custom_lt
import random
import itertools
import math
import io
import packet_gen
from btle_broadcast import ADVERT_TYPE_ID, append_chunk_header

NORMAL_PAYLOAD_SPACE = 14

def dump_for_java_test(input, encoded):
    input_hex = str(input.hex())
    input_bytes = 'byte[] input_bytes = {'
    input_bytes = input_bytes + ', '.join("(byte) 0x" + input_hex[i:i+2] for i in range(0, len(input_hex), 2))
    input_bytes = input_bytes + '};'
    encoded_hex = "String[] encoded = {"
    for e in encoded:
        encoded_hex = encoded_hex + "\"" + e.hex() + "\","
    encoded_hex = encoded_hex + "};"
    
    print(input_bytes)
    print(encoded_hex)

def run_test(test_bytes, output_file=None, dump_java=False):	
    # Override default encoder and decoder
    custom_lt.configure_encoder(encode)
    custom_lt.configure_decoder(decode)

    # Configuration
    block_size = 10
    block_seed = None
    # Check test size
    t_len = len(test_bytes)
    normal_packet_count = math.ceil(t_len / NORMAL_PAYLOAD_SPACE)
    generate = normal_packet_count * 1000
    print("Test Size (bytes): {}".format(t_len))
    print("Would normally need {} received packets!".format(normal_packet_count))	
    print("")
    # Create a byte stream
    data_stream = io.BytesIO(test_bytes)

    # Encode blocks
    taken = itertools.islice(encode.encoder(data_stream, 
                    block_size, seed=block_seed), generate)
    encoded = list(taken)

    # --- Assume all these encoded packets are sent
    print("Payload (bytes):", len(encoded[0]))
    print("Encoded (packets): ", len(encoded))
    #--- Ignore order reflecting packet loss
    random.shuffle(encoded)

    # Create the decoder for the receiver side
    decoder = decode.LtDecoder()
    # Take from the stream of received blocks, and attempt to decode them
    needed = 0
    for block_dat in encoded:
        print(block_dat.hex())
        block = decode.block_from_bytes(block_dat)
        decoder.consume_block(block)
        needed += 1
        if decoder.is_done():
            print("Done!")
            break 	
    rx_bytes = decoder.bytes_dump()
    success = test_bytes == rx_bytes
    print("")
    if success:
        print("Success after {} encoded packets!".format(needed))
    else:
        print("Failed decode!")
    # print("")
    # print(rx_bytes)
    # print("")
    if output_file is not None:
        f = open(output_file, "w+b")
        f.write(rx_bytes)
        f.close()
    if dump_java:
        dump_for_java_test(test_bytes, encoded)

def test_file(filename):
    # Find filelength
    f_bytes = None
    f_len = 0
    with open(filename, 'rb') as f:
        f_bytes = f.read()		

    run_test(f_bytes, output_file=("rx_" + filename))

def test_advert():
    # Define some objects
    bss = list()
    bss.append(packet_gen.gen_canvas_bitstring((1000, 500), (1, 0, 0)))
    bss.append(packet_gen.gen_img_bitstring(1, (23, 52), (1, 0), 0))
    bss.append(packet_gen.gen_text_bitstring((50, 50), 1, (1, 0, 0), 50, 90, "R!"))
    bss.append(packet_gen.gen_polygon_bitstring((50, 50, 50), ((50, 50), (50, 50), (25, 50), (123, 988))))
    bss.append(packet_gen.gen_text_bitstring((50, 50), 1, (1, 0, 0), 50, 90, "Yep, still a cuck!"))
    # Create a bitstream of all the objects
    ad_bytes = packet_gen.generate_ad(bss).bytes
    ad_bytes = append_chunk_header(ADVERT_TYPE_ID, ad_bytes)
    run_test(ad_bytes)

if __name__ == "__main__":
    #test_file("test_string.txt")
    test_advert()