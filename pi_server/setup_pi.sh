
setup()
{
    ip=$1
    scp python/packet_gen.py pi@$ip:packet_gen.py
    scp python/file_broadcast.py pi@$ip:file_broadcast.py
    scp python/advert_broadcast.py pi@$ip:advert_broadcast.py
    scp python/btle_broadcast.py pi@$ip:btle_broadcast.py
    scp python/test_stream.py pi@$ip:test_stream.py
    scp python/custom_lt.py pi@$ip:custom_lt.py
    scp python/encode_decode_test.py pi@$ip:encode_decode_test.py
    scp python/stop.py pi@$ip:stop.py
    scp python/setup.py pi@$ip:setup.py
    scp python/test_string.txt pi@$ip:test_string.txt
    scp python/km_small.png pi@$ip:km_small.png
}

setup 192.168.43.8
setup 192.168.43.156