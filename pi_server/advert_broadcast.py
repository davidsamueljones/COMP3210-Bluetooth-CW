import packet_gen
import subprocess
import bitstring
import time

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
        uuid = bitstring.pack(">L", i).hex
        uuid =  "0"*(32-len(uuid)) + uuid
        uuid = ' '.join(uuid[i:i+2] for i in range(0, len(uuid), 2))
        str_bytes = "1E 02 01 1A 1A FF 4C 00 02 15 {} 00 0A 00 0B".format(uuid)
        str_cmd = "sudo hcitool -i hci0 cmd 0x08 0x0008 {}".format(str_bytes)
        print(str_cmd)
        execute_cmds([str_cmd])

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
    execute_cmds(get_btle_setup_cmds())
    time.sleep(3)
    send_test_stream(1000)
    execute_cmds(get_btle_disable_cmds())