import java.util.Date;

import moos.ssds.io.util.PacketUtility;
import moos.ssds.jms.PublisherComponent;

public class ExampleSSDSIngestClientPublisher {

	public static void main(String[] args) {
		// The component for publishing to SSDS
		PublisherComponent publisherComponent = new PublisherComponent();
		// Create the various parameters for the packet
		long sourceID = 101;
		long parentID = 100;
		// The type of packet to be sent Data = 0, Metadata = 1, Device Message
		// = 4
		int packetType = 0;
		// This is the record type
		long packetSubType = 1;
		long metadataSequenceNumber = 0;
		long dataDescriptionVersion = 0;
		Date now = new Date();
		// TODO kgomes this needs to be change after but SSDS-77 is fixed
		long timestampSeconds = now.getTime() / 1000;
		long timestampNanoseconds = (now.getTime() % 1000) * 1000;
		long sequenceNumber = 1;
		byte[] firstBuffer = "Sensor Data First Buffer".getBytes();
		byte[] secondBuffer = "SensorData Second Buffer (ignored)".getBytes();

		// Create and publish the byte array in SSDS format
		publisherComponent.publishBytes(PacketUtility
				.createSSDSFormatByteArray(sourceID, parentID, packetType,
						packetSubType, metadataSequenceNumber,
						dataDescriptionVersion, timestampSeconds,
						timestampNanoseconds, sequenceNumber, firstBuffer,
						secondBuffer));
	}
}
