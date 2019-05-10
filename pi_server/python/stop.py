from advert_broadcast import *

def stop_broadcast():
    execute_cmds(get_btle_disable_cmds())

if __name__ == "__main__":
    stop_broadcast()