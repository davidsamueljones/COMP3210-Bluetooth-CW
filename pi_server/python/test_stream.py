import argparse
from btle_broadcast import *

###################################################
### MAIN
###################################################
if __name__ == "__main__":
    parser = argparse.ArgumentParser("file_broadcast")
    parser.add_argument('-n', metavar='packets', default=1000, type=int,
                                      help='defines the number of test packets \
                                      that will be transmitted.')
    parser.add_argument('-d', metavar='device', default='hci0',
                                  help='the device to broadcast from')
    args = parser.parse_args()

    # Start, configure BTLE
    execute_cmds(get_btle_setup_cmds(args.d))
    # Send the file!
    send_test_stream(args.n, bt_device=args.d)
    # Finish, shutoff BTLE
    execute_cmds(get_btle_disable_cmds(args.d))

