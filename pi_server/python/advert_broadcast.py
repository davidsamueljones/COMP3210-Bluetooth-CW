import argparse
import random
import packet_gen
from btle_broadcast import *

###################################################
### MAIN
###################################################
if __name__ == "__main__":
    parser = argparse.ArgumentParser("file_broadcast")
    parser.add_argument('-n', metavar='n_reps', default=None, type=int,
                                      help='defines the number of packets to transmit as \
                                            the amount of blocks * n. If not set packets will \
                                            be sent until the process is interrupted.')
    parser.add_argument('-i', metavar='chunk_id', default=None, type=int,
                                  help='the chunk id that identifies the broadcast')
    parser.add_argument('-s', metavar='block_seed', default=None, type=int,
                                  help='the seed to use for block generation')
    parser.add_argument('-d', metavar='device', default='hci0',
                                  help='the device to broadcast from')
    args = parser.parse_args()
    
    if (args.i is None):
        # Use a random chunk ID
        random.seed(None)
        cid = random.randint(0, 2**16-1)
    else:
        cid = int(args.i)
    
    # Configuration
    data_type = ADVERT_TYPE_ID

    # Define some objects
    bss = list()
    bss.append(packet_gen.gen_canvas_bitstring((1000, 500), (1, 0, 0)))
    bss.append(packet_gen.gen_img_bitstring(1, (23, 52), (1, 0), 0))
    bss.append(packet_gen.gen_text_bitstring((50, 50), 1, (1, 0, 0), 50, 90, "Richard is a cuck!"))
    bss.append(packet_gen.gen_polygon_bitstring((50, 50, 50), ((50, 50), (50, 50), (25, 50), (123, 988))))
    bss.append(packet_gen.gen_text_bitstring((50, 50), 1, (1, 0, 0), 50, 90, "Yep, still a cuck!"))
    # Create a bitstream of all the objects
    bit_stream = bitstring.BitString()
    for bs in bss:
        bit_stream.append(bs)

    # Start, configure BTLE
    execute_cmds(get_btle_setup_cmds(args.d))
    # Send the file!
    data_broadcast_bytes(cid, bit_stream.bytes, data_type, 
        packet_count=None, block_seed=args.s, bt_device=args.d)
    # Finish, shutoff BTLE
    execute_cmds(get_btle_disable_cmds(args.d))
