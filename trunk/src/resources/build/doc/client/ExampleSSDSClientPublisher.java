import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import moos.ssds.jms.PublisherComponent;

import org.mbari.siam.distributed.MetadataPacket;
import org.mbari.siam.distributed.SensorDataPacket;
import org.mbari.siam.operations.utils.ExportablePacket;

public class ExampleSSDSClientPublisher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Create the SSDS publisher component
		PublisherComponent publisherComponent = new PublisherComponent();

		// Create the output stream to help export the SIAM packet
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		// Create the ExportablePacket to use to wrap and export a SIAM packet
		ExportablePacket exportablePacket = new ExportablePacket();

		// Here is an example of a metadata packet
		MetadataPacket metadataPacket = new MetadataPacket(101, ("Cause")
				.getBytes(), ("Buffer bytes").getBytes());
		metadataPacket.setMetadataRef(0);
		metadataPacket.setParentId(100);
		metadataPacket.setRecordType(0);
		metadataPacket.setSequenceNo(1);
		metadataPacket.setSystemTime(new Date().getTime());

		// Wrap and export
		exportablePacket.wrapPacket(metadataPacket);
		try {
			exportablePacket.export(dos);
		} catch (IOException e) {
		}

		// Now publish the SIAM formatted byte array to the TransmogrifyMDB
		// input topic
		publisherComponent.publishBytes(bos.toByteArray());

		// Now for a Data packet
		SensorDataPacket sensorDataPacket = new SensorDataPacket(101,
				"Sensor Data Packet Payload".getBytes().length);
		sensorDataPacket.setMetadataRef(0);
		sensorDataPacket.setParentId(100);
		sensorDataPacket.setRecordType(1);
		sensorDataPacket.setSequenceNo(2);
		sensorDataPacket.setDataBuffer("Sensor Data Packet Payload".getBytes());
		sensorDataPacket.setSystemTime(new Date().getTime());

		// Create the output stream to help export the SIAM packet
		bos = new ByteArrayOutputStream();
		dos = new DataOutputStream(bos);

		// Create the ExportablePacket to use to wrap and export a SIAM packet
		exportablePacket = new ExportablePacket();

		// Wrap and export
		exportablePacket.wrapPacket(sensorDataPacket);
		try {
			exportablePacket.export(dos);
		} catch (IOException e) {
		}

		// Now publish the SIAM formatted byte array to the TransmogrifyMDB
		// input topic
		publisherComponent.publishBytes(bos.toByteArray());

	}

}
