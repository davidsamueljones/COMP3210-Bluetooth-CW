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
    bss.append(packet_gen.gen_canvas_bitstring((0, 0), (0, 0, 0)))
    bss.append(packet_gen.gen_img_bitstring(3, (300, 200), (0, 0), 0))
    bss.append(packet_gen.gen_text_bitstring((100, 200), 0, (0, 255, 0), 48, 345, "New from ECS productions!"))
    bss.append(packet_gen.gen_text_bitstring((700, 70), 0, (0, 255, 0), 48, 35, "Only limited stock!"))
    bss.append(packet_gen.gen_text_bitstring((100, 800), 0, (255, 0, 0), 64, 0, "Show this message for 99% off!"))
    bss.append(packet_gen.gen_polygon_bitstring((255, 255, 0), ((400, 230), (430, 290), (460, 230), (490, 290), (520, 230), (550, 290), (580, 230), (610, 290), (640, 230), (670, 290), (670, 330), (400, 330))))
    # Create a bitstream of all the objects
    ad_bytes = packet_gen.generate_ad(bss).bytes

    # Start, configure BTLE
    execute_cmds(get_btle_setup_cmds(args.d))
    # Send the file!
    data_broadcast_bytes(cid, ad_bytes, data_type, 
        packet_count=None, block_seed=args.s, bt_device=args.d)
    # Finish, shutoff BTLE
    execute_cmds(get_btle_disable_cmds(args.d))
