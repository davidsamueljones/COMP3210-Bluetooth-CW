sudo hciconfig hci0 up
sudo hciconfig hci0 leadv 3
sudo hciconfig hci0 noscan

i=0
while true
do
i=$((i+1))
hi=$(printf "%02X", $i)
sudo hcitool -i hci0 cmd 0x08 0x0008 1E 02 01 1A 1A FF 4C 00 02 15 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 $hi 00 00 00 00 C8 00
sleep 1
#sudo hciconfig hci0 noleadv
done