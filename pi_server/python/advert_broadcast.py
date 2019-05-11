import subprocess
import bitstring
import time

from struct import pack
from lt import encode, decode

import custom_lt
import io
import math
import zlib
import random

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

# Sendable Data Types
TEXT_TYPE_ID = 1
IMAGE_TYPE_ID = 2
ADVERT_TYPE_ID = 3

# Device to send from
BT_DEVICE = "hci0"

def execute_cmds(cmd_list):
    for cmd in cmd_list:
        subprocess.call(cmd, shell=True)
        time.sleep(0.050) # 50ms
    return True


def get_btle_setup_cmds():
    cmd_list = list()
    # Turn on the interface
    cmd_list.append("sudo hciconfig {} up".format(BT_DEVICE))
    # Disable scanning for other BT devices, transmit only
    cmd_list.append("sudo hciconfig {} noscan".format(BT_DEVICE))
    # Decrease transmission interval to 100ms [max for non-connectable] (normally ~1s)
    cmd_list.append("sudo hcitool -i {} cmd 0x08 0x0006 A0 00 A0 00 03 00 00 00 00 00 00 00 00 07 00".format(BT_DEVICE))
    # Enable advertising mode without causing interval to increase
    # Equivalent to leadv 3 (non-connectable undirected advertising)
    cmd_list.append("sudo hcitool -i {} cmd 0x08 0x000a 01".format(BT_DEVICE))
    return cmd_list


def get_btle_disable_cmds():
    cmd_list = list()
    # Stop advertising
    cmd_list.append("sudo hciconfig {} noleadv")
    # Turn off the interface
    cmd_list.append("sudo hciconfig {} down")
    return cmd_list


def send_test_packet(num):
    test_stream_id = "11 AA 22 BB 33 CC 44 DD"
    uuid = bitstring.pack(">L", i).hex
    uuid =  "0"*(16-len(uuid)) + uuid
    uuid = ' '.join(uuid[i:i+2] for i in range(0, len(uuid), 2))
    str_bytes = "1E 02 01 1A 1A FF 4C 00 {} {} {} 00 0A 00 0B".format( \
        TEST_BEACON_TYPE, test_stream_id, uuid)
    str_cmd = "sudo hcitool -i {} cmd 0x08 0x0008 {}".format(BT_DEVICE, str_bytes)
    print(str_cmd)
    execute_cmds([str_cmd])


def send_test_stream(length):
    for i in range(length):
        send_test_packet(i)


def send_data_packet(data_bytes):
    data_bytes = ' '.join(data_bytes[i:i+2] for i in range(0, len(data_bytes), 2))
    str_bytes = "1E 02 01 1A 1A FF 4C 00 {} {}".format(DATA_BEACON_TYPE, data_bytes)
    str_cmd = "sudo hcitool -i {} cmd 0x08 0x0008 {}".format(BT_DEVICE, str_bytes)
    #print(str_cmd)
    execute_cmds([str_cmd])


def _encode_chunk_stream(cid, stream, block_seed=None):
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


def encode_chunk_bytes(cid, data_type, data, block_seed=None):
    # Append a header
    data_type_bytes = pack(">B", data_type)
    send_data = data_type_bytes + data
    crc = zlib.crc32(send_data)
    crc_bytes = pack(">I", crc)
    send_data = crc_bytes + send_data
    # Create a byte stream
    data_stream = io.BytesIO(send_data)
    # Debug print
    data_len = len(data)
    print("Transmission Size (bytes): {}".format(data_len))
    # Use the chunk stream generator
    return _encode_chunk_stream(cid, data_stream, block_seed)

def data_broadcast_bytes(cid, tx_bytes, data_type, packet_count, block_seed=None):
    # Get the encoded packet generator
    gen = encode_chunk_bytes(cid, data_type, tx_bytes, block_seed)
    # Run until the required number of packets are sent, if this is
    # None then an infinite number of packets are sent  
    i = 0
    while packet_count is None or i < packet_count:
        packet = next(gen).hex()
        send_data_packet(packet)
        i += 1

def data_broadcast_file(cid, filename, data_type, repetitions=None, block_seed=None):
    # Find filelength
    f_bytes = None
    f_len = 0
    with open(filename, 'rb') as f:
        f_bytes = f.read()		
        f_len = len(f_bytes)
        packet_count = None
        if repetitions is not None:
            packet_count = math.ceil(f_len / LT_BLOCK_SIZE) * repetitions
        data_broadcast_bytes(cid, f_bytes, data_type, packet_count, block_seed)

###################################################
### MAIN
###################################################
if __name__ == "__main__":
    # Configuration A
    filename = "test_string.txt"
    data_type = TEXT_TYPE_ID
    # Configuration B
    filename = "km_small.png"
    data_type = IMAGE_TYPE_ID

    # Use a random chunk ID
    random.seed(None)
    cid = random.randint(0, 2**16-1)

    # Start, configure BTLE
    execute_cmds(get_btle_setup_cmds())
    # Send!
    data_broadcast_file(cid, filename, data_type)
    # Finish, shutoff BTLE
    execute_cmds(get_btle_disable_cmds())