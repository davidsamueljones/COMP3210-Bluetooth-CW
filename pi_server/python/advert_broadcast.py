import packet_gen
import subprocess
import bitstring
import time

from struct import pack
from lt import encode, decode

import custom_lt
import io
import math
import zlib


# The type identifier to use when sending test packets
TEST_BEACON_TYPE = "02 15"
# The type identifier to use when sending data packets
DATA_BEACON_TYPE = "B0 DC"

# Size to send blocks as in packets
LT_BLOCK_SIZE = 10

# Version identifier, used in checksums to verify that the recevier
# is on the same version of advert formats. [Major (UINT8), Minor (UINT8)]
FORMAT_VER = 0x0100
CHECKSUM_NONSE = 0xBEEF + FORMAT_VER

def execute_cmds(cmd_list):
    for cmd in cmd_list:
        subprocess.call(cmd, shell=True)
    return True


def get_btle_setup_cmds():
    cmd_list = list()
    # Turn on the interface
    cmd_list.append("sudo hciconfig hci0 up")
    # Disable scanning for other BT devices, transmit only
    cmd_list.append("sudo hciconfig hci0 noscan")
    # Decrease transmission interval to 100ms [max for non-connectable] (normally ~1s)
    cmd_list.append("sudo hcitool -i hci0 cmd 0x08 0x0006 A0 00 A0 00 03 00 00 00 00 00 00 00 00 07 00")
    # Enable advertising mode without causing interval to increase
    # Equivalent to leadv 3 (non-connectable undirected advertising)
    cmd_list.append("sudo hcitool -i hci0 cmd 0x08 0x000a 01")
    return cmd_list


def get_btle_disable_cmds():
    cmd_list = list()
    # Stop advertising
    cmd_list.append("sudo hciconfig hci0 noleadv")
    # Turn off the interface
    cmd_list.append("sudo hciconfig hci0 down")
    return cmd_list


def send_test_stream(length):
    for i in range(length):
        test_stream_id = "11 AA 22 BB 33 CC 44 DD"
        uuid = bitstring.pack(">L", i).hex
        uuid =  "0"*(16-len(uuid)) + uuid
        uuid = ' '.join(uuid[i:i+2] for i in range(0, len(uuid), 2))
        str_bytes = "1E 02 01 1A 1A FF 4C 00 {} {} {} 00 0A 00 0B".format( \
            TEST_BEACON_TYPE,test_stream_id, uuid)
        str_cmd = "sudo hcitool -i hci0 cmd 0x08 0x0008 {}".format(str_bytes)
        print(str_cmd)
        execute_cmds([str_cmd])


def data_broadcast_stream(cid, stream, block_seed=None):
    # Override default encoder
    custom_lt.configure_encoder(encode)
    # Start encoding the blocks, blocks returned to generator as they are used
    block_size = LT_BLOCK_SIZE
    block_generator = encode.encoder(stream, block_size, seed=block_seed)
    # Encapsulate all blocks under a single chunk ID
    cid_bytes = pack(">H", cid)
    while True:
        # Build packet with header
        packet = next(block_generator)
        packet = cid_bytes + packet
        # Add a checksum at the front to verify that it is a broadcast-payload
        checksum = zlib.crc32(packet)
        checksum = (checksum + CHECKSUM_NONSE) & 0xFFFF
        checksum_bytes = pack(">H", checksum)
        packet = checksum_bytes + packet
        yield packet


def data_broadcast_bytes(cid, data, block_seed=None):
    data_len = len(data)
    print("Input Size (bytes): {}".format(data_len))
    return data_broadcast_stream(cid, io.BytesIO(data), block_seed)


def make_test_ad():
    bss = list()
    bss.append(packet_gen.gen_canvas_bitstring((1000, 500), (1, 0, 0)))
    bss.append(packet_gen.gen_img_bitstring(1, (23, 52), (1, 0), 0))
    bss.append(packet_gen.gen_text_bitstring((50, 50), 1, (1, 0, 0), 50, 90, "Richard is a cuck!"))
    bss.append(packet_gen.gen_polygon_bitstring((50, 50, 50), ((50, 50), (50, 50), (25, 50), (123, 988))))
    bss.append(packet_gen.gen_text_bitstring((50, 50), 1, (1, 0, 0), 50, 90, "Yep, still a cuck!"))
    packets = packet_gen.generate_ad(1, bss)
    # if (len(packets) > 255):
    #     raise ValueError("Too many packets to be an advert, maximum is 255.")
    return packets


if __name__ == "__main__":
    packets = make_test_ad()
    gen = data_broadcast_bytes(5, packets[0].tobytes(), 1);
    while True:
        print(next(gen).hex())
    #execute_cmds(get_btle_setup_cmds())
    #time.sleep(3)
    #send_test_stream(1000)
    #execute_cmds(get_btle_disable_cmds())