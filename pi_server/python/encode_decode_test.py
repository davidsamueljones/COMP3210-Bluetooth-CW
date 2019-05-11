import sys
from lt import encode, decode
import custom_lt
import random
import itertools
import math

PAYLOAD_SPACE = 20

def main():	
    # Override default encoder and decoder
    custom_lt.configure_encoder(encode)
    custom_lt.configure_decoder(decode)

    # Configuration
    filename = "test_string.txt"
    block_size = 10
    block_seed = 1
    # Find filelength
    f_bytes = None
    f_len = 0
    with open(filename, 'rb') as f:
        f_bytes = f.read()		
        f_len = len(f_bytes)
    normal_packet_count = math.ceil(f_len / PAYLOAD_SPACE)
    generate = normal_packet_count * 10
    print("File Size (bytes): {}".format(f_len))
    print("Would normally need {} received packets!".format(normal_packet_count))	
    print("")

    # Encode blocks
    encoded = []
    with open(filename, 'rb') as f:
        taken = itertools.islice(encode.encoder(f, block_size, seed=block_seed), generate)
        encoded = list(taken)
    # # Comment in for Java formatted encode
    # print("{")
    # [print("\"" + e.hex() + "\",") for e in encoded]
    # print("};")
    # return

    # --- Assume all these encoded packets are sent
    print("Payload (bytes):", len(encoded[0]))
    print("TX (packets): ", len(encoded))
    #--- Assume half of them are received
    keep = int(generate / 2)
    #keep = generate
    random.shuffle(encoded)
    encoded = encoded[0:keep]
    print("RX (packets): ", len(encoded))
    print("")

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
    success = f_bytes == rx_bytes
    print("")
    if success:
        print("Success after {} encoded packets!".format(needed))
    else:
        print("Failed decode!")
    # print("")
    # print(rx_bytes)
    # print("")
    f = open("rx_" + filename, "w+b")
    f.write(rx_bytes)
    f.close()

if __name__ == "__main__":
    main()