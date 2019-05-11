from advert_broadcast import *
import argparse

def stop_broadcast(bt_device):
    cmds = get_btle_disable_cmds(bt_device)
    execute_cmds(cmds)

if __name__ == "__main__":
    parser = argparse.ArgumentParser("stop")
    parser.add_argument('-d', metavar='device', default='hci0',
                                  help='the device to stop')
    args = parser.parse_args()
    stop_broadcast(args.d)