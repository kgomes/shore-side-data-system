# In order for this script to run, python must be installed and the protocol buffers modules from
# Google must be installed as well.  Please download the protocol buffers distribution from Google,
# unpack it and look at the README.txt file in the python directory.

# Import sys
import sys

# Import the SSDS Protocol buffer module
import ssds_device_packet_pb2

# Import the unit test module
import unittest

class TestSSDSProtocolBuffers(unittest.TestCase):

    # Create a new SSDS message packet object
    ssds_packet = ssds_device_packet_pb2.MessagePacket()
    
    # Setup method
    def setUp(self):
        # Set all the parameters
        self.ssds_packet.sourceID = 101
        self.ssds_packet.parentID = 100
        self.ssds_packet.packetType = 0 
        self.ssds_packet.packetSubType = 1
        self.ssds_packet.metadataSequenceNumber = 199
        self.ssds_packet.dataDescriptionVersion = 199
        self.ssds_packet.timestampSeconds = 0
        self.ssds_packet.timestampNanoseconds = 0
        self.ssds_packet.sequenceNumber = 200
        self.ssds_packet.bufferBytes = b'First Buffer Bytes'
        self.ssds_packet.bufferTwoBytes = b'Second Buffer Bytes'
        
    # Test the construction, serialization and deserialization of a packet
    def testPacketSerializationDeserialization(self):
        
        # Make sure the object construction went OK
        self.assertEqual(self.ssds_packet.sourceID, 101)
        
        # Now use procotol buffers to serialize out to a string
        ssds_packet_as_string = self.ssds_packet.SerializeToString()

        # Now deserialize
        ssds_packet_deserialized = ssds_device_packet_pb2.MessagePacket()
        ssds_packet_deserialized.ParseFromString(ssds_packet_as_string)

        # Now make sure the tests pass
        self.assertEqual(ssds_packet_deserialized.sourceID, 101)
        self.assertEqual(ssds_packet_deserialized.parentID, 100)
        self.assertEqual(ssds_packet_deserialized.packetType, 0)
        self.assertEqual(ssds_packet_deserialized.packetSubType, 1)
        self.assertEqual(ssds_packet_deserialized.metadataSequenceNumber, 199)
        self.assertEqual(ssds_packet_deserialized.dataDescriptionVersion, 199)
        self.assertEqual(ssds_packet_deserialized.timestampSeconds, 0)
        self.assertEqual(ssds_packet_deserialized.timestampNanoseconds, 0)
        self.assertEqual(ssds_packet_deserialized.sequenceNumber, 200)
        self.assertEqual(ssds_packet_deserialized.bufferBytes, b'First Buffer Bytes')
        self.assertEqual(ssds_packet_deserialized.bufferTwoBytes, b'Second Buffer Bytes')

# Run tests if done from command line
if __name__ == '__main__':
    unittest.main()
