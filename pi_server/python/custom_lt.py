from lt import encode, decode, sampler
import sys
import random
from struct import pack, unpack

def custom_encoder(f, blocksize, seed=None, c=sampler.DEFAULT_C, delta=sampler.DEFAULT_DELTA):
    """Generates an infinite sequence of blocks to transmit
    to the receiver
    """
    # Generate seed if not provided
    if seed is None:
        seed = random.randint(0, 1 << 32 - 1)
    
    # Split the file into blocks of data
    filesize, blocks = encode._split_file(f, blocksize)

    # Initialise generation vars
    K = len(blocks)
    prng = sampler.PRNG(params=(K, delta, c))
    prng.set_seed(seed)
    
    # Block generation loop
    while True:
        blockseed, d, ix_samples = prng.get_src_blocks()
        block_data = 0
        for ix in ix_samples:
            block_data ^= blocks[ix]

        # Generate blocks of XORed data in network byte order (big endian)
        block = (filesize, blocksize, blockseed, int.to_bytes(block_data, blocksize, sys.byteorder))
        # Use custom header format (2B - 1B - 4B)
        yield pack('>HBL%ss'%blocksize, *block)


def read_custom_header(stream):
    """Read block header from input stream
    """
    header_bytes = stream.read(7)
    return unpack('>HBL', header_bytes)


def configure_encoder(encode):
	encode.encoder = custom_encoder


def configure_decoder(decode):
	decode._read_header = read_custom_header