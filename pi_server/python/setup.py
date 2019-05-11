from advert_broadcast import *
import argparse

def setup_broadcast(bt_device):
    cmds = get_btle_setup_cmds(bt_device)
    execute_cmds(cmds)

if __name__ == "__main__":
    parser = argparse.ArgumentParser("setup")
    parser.add_argument('-d', metavar='device', default='hci0',
                                  help='the device to setup')
    args = parser.parse_args()
    setup_broadcast(args.d)