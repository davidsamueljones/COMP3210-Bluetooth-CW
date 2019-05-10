from advert_broadcast import *

def setup_broadcast():
    execute_cmds(get_btle_setup_cmds())

if __name__ == "__main__":
    setup_broadcast()