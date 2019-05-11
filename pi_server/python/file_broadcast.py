import argparse
import random
import os
from advert_broadcast import *

###################################################
### MAIN
###################################################
if __name__ == "__main__":
    parser = argparse.ArgumentParser("file_broadcast")
    parser.add_argument('f', metavar='file',
                                  help='the file to broadcast')
    parser.add_argument('-t', metavar='datatype', default=None, type=int,
                                  help='the type being broadcast, automatically \
                                        inferred from file extension if not provided. \
                                        [TEXT = 1, IMAGE = 2, ADVERT = 3]')
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
    
    # Configuration A
    data_type = UNKNOWN_TYPE_ID
    _, ext = os.path.splitext(args.f)
    if ext in [".png", ".jpg", ".jpeg"]:
        data_type = IMAGE_TYPE_ID
    elif ext in [".txt"]:
        data_type = TEXT_TYPE_ID
    else:
        raise ValueError("Cannot infer datatype from file extension," \
            + " provide one with the -t argument")

    # Start, configure BTLE
    execute_cmds(get_btle_setup_cmds(args.d))
    # Send the file!
    data_broadcast_file(cid, args.f, data_type, 
        reptitions=args.n, block_seed=args.s, bt_device=args.d)
    # Finish, shutoff BTLE
    execute_cmds(get_btle_disable_cmds(args.d))

