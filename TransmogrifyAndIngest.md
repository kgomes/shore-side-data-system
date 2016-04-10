# Transmogrify and Ingest Components #

This page documents the components that are known as "Transmogrify" (nod to Bill Watterson here) and "Ingest". These are the components that take in data and metadata streams and perform various tasks with them as they are brought into the SSDS. There are two messaging infrastructures that are used to ingest data into the SSDS, JMS and AMQP.  JMS was the original messaging scheme and works really well, especially considering you can spin up Message Driven Beans in JBoss and JBoss will manage the pools.  This, however, limited the clients to Java for publishing messages.  While certainly fine for our initial implementation for the MOOS program, we wanted to open up the messaging to more than just Java clients.  We added an application that connects to an AMQP broker and processes messages.  This allows non-Java clients to publish packets to the SSDS.

Here is a logical diagram of the message flow for ingesting data into the SSDS.

<a href='http://shore-side-data-system.googlecode.com/svn/wiki/images/TransIngestLogical.jpg'><img src='http://shore-side-data-system.googlecode.com/svn/wiki/images/TransIngestLogical.jpg' width='800' /></a>

As I mentioned, the JMS flow consumers are all implemented as Message Driven Beans (EJB) and are wired in series with Transmogrify feeding into Ingest. The original reason for having both was that Transmogrify was to handle data and metadata from the SIAM system that had some special characteristics and needed to be converted to the generic message format that the SSDS is expecting. If someone wanted to send the generic format, they could bypass the Transmogrify and send straight to Ingest. As with all things, patchwork becomes production and Transmogrify lives on to this day.

Here is a sequence diagram of the basic steps that occur when a packet is submitted via JMS to the SSDS (at least through transmogrify and ingest).

<a href='http://shore-side-data-system.googlecode.com/svn/wiki/images/TransmogrifyIngest.jpg'><img src='http://shore-side-data-system.googlecode.com/svn/wiki/images/TransmogrifyIngest.jpg' width='800' /></a>

After the ingest step, if the packet is a metadata packet, it is forwarded on to the RuminateMDB (covered on another wiki page).  The protocol buffers ingest MDB operates the same way except that in the IngestProtoMDB, the packet gets converted from the incoming protocol buffer to an SSDSDevicePacket which is then persisted in the same was as the IngestMDB does it.

## Transmogrify Packet Structure ##

Our initial and primary publisher of data for the SSDS was the SIAM infrastructure.  Initially they would publish serialized Java objects that were SIAM DevicePacket classes.  In the SSDS, we created SSDSDevicePacket sub classes to map the SIAM DevicePacket information to the SSDS world.  Here is a class diagram of the classes involved:

<a href='http://shore-side-data-system.googlecode.com/svn/wiki/images/Device%20Packet%20Classes.jpg'><img src='http://shore-side-data-system.googlecode.com/svn/wiki/images/Device%20Packet%20Classes.jpg' width='800' /></a>

The classes on the left are the SIAM classes.  The SSDSDevicePacket classes were subclassed from the SIAM classes to add some functionality to assist in converting between the two.  The PacketUtility class is a helper class that is used to convert between the different formats of serialization and objects.  The SSDSDeviceProto and inner MessagePacket class were the Java classes that were generated from the Google protocol buffers definition that you can view here:

http://code.google.com/p/shore-side-data-system/source/browse/branches/jboss-5.1/src/proto/ssds-device-packet.proto

Just as a note, the idea with the SSDSGeoLocatedDevicePacket was that if there was a way to link a particular device to another device that was providing geospatial data, you could correlate all the SSDSDevicePackets by time with that device and tag each individual packet with a geospatial reference.

When the Transmogrify component receives a SIAM DevicePacket, it converts it to a SSDSDevicePacket using the PacketUtility class. Here are the various attributes on the classes and how they map to each other.

| **DevicePacket** | **MetadataPacket** | **SensorDataPacket** | **DeviceMessagePacket** | **SensorStatusPacket** | **SSDSDevicePacket** | **SSDSGeoLocatedDevicePacket** | **SSDSDevicePacketProto::MessagePacket** | **Description** |
|:-----------------|:-------------------|:---------------------|:------------------------|:-----------------------|:---------------------|:-------------------------------|:-----------------------------------------|:----------------|
| **sourceID**     | _inherited_        | _inherited_          | _inherited_             | _inherited_            | _inherited_          | _inherited_                    | **sourceID**                             | This is the ID of the device that generated the packet |
| **systemTime**   | _inherited_        | _inherited_          | _inherited_             | _inherited_            | _inherited_          | _inherited_                    | Split into two fields **timestampSeconds** and **timestampNanoseconds** | This is the timestamp (in epoch milliseconds) when the packet was created by the system.  This may or may not match instrument time if the clocks are not synchronized |
| **sequenceNo**   | _inherited_        | _inherited_          | _inherited_             | _inherited_            | _inherited_          | _inherited_                    | **sequenceNumber**                       | This is a number that should indicate the order of generation of the packet from the device |
| **metadataRef**  | _inherited_        | _inherited_          | _inherited_             | _inherited_            | _inherited_          | _inherited_                    | **metadataSequenceNumber**               | This is the sequence number of the packet that contains the metadata that describes the contents of this packet.  If it is a MetadataPacket, this has no meaning. |
| **parentID**     | _inherited_        | _inherited_          | _inherited_             | _inherited_            | _inherited_          | _inherited_                    | **parentID**                             | This is the SSDS ID of the device to which the generated device was connected when it generated this packet. Null means no parent. |
| **recordType**   | _inherited_        | _inherited_          | _inherited_             | _inherited_            | _inherited_          | _inherited_ but also equal the local recordType | **packetSubType**                        | This defines the "Type" of record that this packet contains.  Devices can send many forms of records, error messages, etc. and this help define what is actually in the payload for this message.  There are three main options here: **-1** = This means the record type has not been defined; **0** = Metadata packet which contains information about the instrument or other aspects of the observatory.  The SSDS definition of a metadata packet encompasses all the various metadata packets in SIAM.  So this means that MetadataPacket and DeviceMessagePacket from the SIAM world are both just tagged a record type 0. **1+** = Data packets and they can be of any kind.  The record type allows the device driver writer to group messages that are of the same format (usually).  Since the serialized class method is not used anymore, transmogrify ignores SensorStatusPackets which were developed later and use a different serialization method. |
| _X_              | **bytes**          | _X_                  | _X_                     | _X_                    | **dataBuffer**       | _inherited_                    | **bufferBytes**                          | This is a payload that contains information like service properties, SSDS XML, etc.  In the SSDSDevicePacket constructor, the _bytes_ buffer is mapped into the dataBuffer |
| _X_              | **cause**          | _X_                  | _X_                     | _X_                    | **otherBuffer**      | _inherited_                    | **bufferTwoBytes**                       | Another set of bytes that was meant to hold information about why the metadata packet was generated.  In the SSDSDevicePacket constructor, it is mapped to the otherBuffer |
| _X_              | _X_                | **dataBuffer**       | _X_                     | _X_                    | **dataBuffer**       | _inherited_                    | **bufferBytes**                          | This is the sample from the device that is packaged in an array of bytes.  In the SSDSDevicePacket constructor, the _dataBuffer_ is mapped to the dataBuffer |
| _X_              | _X_                | _X_                  | **message**             | _X_                    | **dataBuffer**       | _inherited_                    | **bufferBytes**                          | This is the message contents that are packaged into an array of bytes.  In the SSDSDevicePacket constructor, the _message is mapped to the dataBuffer_|
| _X_              | _X_                | _X_                  | _X_                     | **statusBytes**        | _X_                  | _X_                            | _X_                                      | This is the message about the instrument status as an array of bytes.  Since we broke from serialized objects before this class existed, SSDS ignores this type of object. |
| _X_              | _X_                | _X_                  | _X_                     | **cause**              | _X_                  | _X_                            | _X_                                      | Some message, as an array of bytes, that describes why the status message was sent.  Since we broke from serialized objects before this class existed, SSDS ignores this type of object. |
| _X_              | _X_                | _X_                  | _X_                     | _X_                    | **dataDescriptionVersion** | _inherited_                    | **dataDescriptionVersion**               | This is used to indicate minor metadata changes that were not enough to create new SSDS "buckets" which were actual storage file before moving to a database. |
| _X_              | _X_                | _X_                  | _X_                     | _X_                    | **packetType**       | _inherited_                    | **packetType**                           | This is an integer to define what type of packet this is: **0** = MetadataPacket; **1** = SensorDataPacket; **2** = DeviceMessagePacket |
| _X_              | _X_                | _X_                  | _X_                     | _X_                    | _X_                  | **longitude**                  | _X_                                      | Longitude where the packet was generated |
| _X_              | _X_                | _X_                  | _X_                     | _X_                    | _X_                  | **latitude**                   | _X_                                      | Latitude where the packet was generated |
| _X_              | _X_                | _X_                  | _X_                     | _X_                    | _X_                  | **depth**                      | _X_                                      | Depth (m) where the packet was generated |

We quickly ran into versioning and deserialization issues, so instead, SIAM began publishing their packets as javax.jms.BytesMessages that had a structured payload that was the result of the export method of the Exportable interface. So, what comes across in a BytesMessage is an array of bytes in a payload that looks like the following:

<a href='http://shore-side-data-system.googlecode.com/svn/wiki/images/SIAMByteArrayFormat.jpg'><img src='http://shore-side-data-system.googlecode.com/svn/wiki/images/SIAMByteArrayFormat.jpg' width='800' /></a>

where:

| **Name** | **Type** | **Description** |
|:---------|:---------|:----------------|
| **StreamID** | java.lang.short | This basically states that the bytes are coming from a SIAM ExportablePacket class. SIAM uses constants defined in the org.mbari.siam.distributed.Exportable.java class to enumerate things like this and the short value for this is always 0x0100. SSDS Doesn't really care so we essentially ignore it. |
| **DevicePacketVersion** | java.lang.long | This is the "serialVersionUID" on the class that was used to export the bytes.  It is the version of SIAM class that generated the byte array.  SSDS does not really care and as of this writing, it is always 0. |
| **SourceID** | java.lang.long | The ID of the device that the message was generated by. |
| **Timestamp** | java.lang.long | Epoch milliseconds (number of elapsed milliseconds since 1/1/1970 00:00:00) that the packet was generated by the device |
| **SequenceNumber** | java.lang.long | A number that is supposed to show the order of generation of packets from the device |
| **MetadataRef** | java.lang.long | This is the sequence number of the packet that contains the metadata that describes the information in this packet |
| **ParentID** | java.lang.long | The ID of the device that the generated device was attached to when it generated the packet |
| **RecordType** | java.lang.long | The type of record that this packet contains: **0** = MetadataPacket, **1** = Non-MetadataPacket (Data and other) |
| **SecondStreamID** | java.lang.short | This defines the type of DevicePacket that was used to construct the byte array.  The values are as follows: **MetadataPacket** = 0x101, **SensorDataPacket** = 0x102, **DeviceMessagePacket** = 0x103, **SummaryPacket** = 0x102 (same as SensorDataPacket) |
| **SecondPacketVersion** | java.lang.long | This is the "serialVersionUID" on the class that was used to export the bytes.  It is the version of SIAM class that generated the byte array. As of this writing, it is the same as the DevicePacketVersion.  Since currently it is always 0, SSDS ignores it. |
| **FirstBufferLength** | java.lang.int | This is the length of the array that holds the bytes of the first buffer |
| **FirstBuffer** | java.lang.byte `[]` | This is the bytes array that represents the first buffer |
| **SecondBufferLength** | java.lang.int | This is the length of the array that holds the bytes of the second buffer. |
| **SecondBuffer** | java.lang.byte `[]` | This is the array that holds the bytes of the second buffer. |

Now, in order to handle both types of inputs in Transmogrify (DevicePackets and BytesMessage structure), Transmogrify would take both and convert to a common format that would contain the information to cover both types of messages.  Since the BytesMessage structure encompasses all the information in the DevicePacket, we simply used that byte structure and in Transmogrify, a DevicePacket is converted to a SSDSDevicePacket which is then converted to the same BytesMessage structure using the methods in the PacketUtility class.  Once the DevicePacket object has been converted to a SIAMFormatByteArray, Transmogrify then converts that message, using the PacketUtility class, to an SSDSFormatByteArray that is then used to send on to the Ingest component.  Here is the mapping that is used to convert from SIAMFormatByteArray to the SSDSFormatByteArray.

<a href='http://shore-side-data-system.googlecode.com/svn/wiki/images/SIAMToSSDSConvert.jpg'><img src='http://shore-side-data-system.googlecode.com/svn/wiki/images/SIAMToSSDSConvert.jpg' width='800' /></a>

Notes on the conversion:

  1. The dashes lines mean that, as of this writing, SSDS ignores those attributes in the array.  The reason the arrow is drawn is that if SIAM decides to use this attribute, SSDS may have to pay attention to it in the construction of its byte array.
  1. The DevicePacketVersion, !SecondStreamID, and SecondPacketVersion are used to determine the correct packetType (although, right now, DevicePacketVersion and SecondPacketVersion are ignored).
  1. For MetadataPackets, the packetType is 1.
  1. For SensorDataPackets, the packetType is 0 (SummaryPackets come across as SensorDataPacket and are differentiated by their recordType).
  1. For DeviceMessagePackets, the packetType is 4.
  1. If the incoming packet is a MetadataPacket, the packetSubType is set to 0.  Otherwise, it is set to the RecordType field.
  1. The RecordType is set to zero if the packet is a MetadataPacket and set equal to the RecordType from SIAM if not a MetadataPacket.
  1. The buffers are swapped if it is a MetadataPacket.  It always seemed to logical to do it that way.
  1. This timestamp (epoch milliseconds) is split into seconds and nanoseconds.

**WARNING**: Please note that because byte arrays are limited to 32 bit sizes, the largest payload of a message that can be converted by SSDS is 2GB.  While this does not seem like a major restriction, it can be hit if somebody is using straight JMS messaging (or other) and makes a payload bigger than 2GB.  SSDS will just ignore such a message.

## Ingest Packet Structure ##

So now we have all messages coming into Ingest in a format that SSDS is expecting (i.e. that matches the SSDS view of the world). For the diagram in the previous section, the attributes in the SSDS Bytes Array are:

| Attribute | Type | Description |
|:----------|:-----|:------------|
| **sourceID** | java.lang.long | This is what is known as the SSDS ID for the device (i.e. DeviceID) that actually generated the packet of information. |
| **parentID** | java.lang.long | This is the SSDS ID for the parent that the device was connected to when it sent the packet. If the ID is zero (0), then the generating device was not connected to a parent. |
| **packetType** | java.lang.int | This is the "Type" of packet that is being sent. It is basically an enumerated list the with following context: **0** = Data Packet, **1** = Metadata Packet, **2** = , **3** = , 4 = Device Message Packet |
| **packetSubType** | java.lang.long | This is the equivalent of the "recordType" listed in the Transmogrify component. It is used to provide the hook to tell the client applications what type of record was sent. It really only has meaning in the context of data packets as a device can often send data packets of different formats. This basically tells the application which record form is being sent in this packet. |
| **metadataSequenceNumber** | java.lang.long | Also referred to as dataDescriptionID |
| **dataDescriptionVersion** | java.lang.long |             |
| **timestampSeconds** | java.lang.long |             |
| **timestampNanoseconds** | java.lang.long |             |
| **sequenceNumber** | java.lang.long |             |
| **bufferLen** | java.lang.int |             |
| **bufferBytes** | java.lang.byte `[bufferLen]` |             |
| **bufferTwoLen** | java.lang.int |             |
| **bufferTwoBytes** | java.lang.byte `[bufferTwoLen]` |             |

The Ingest Message Driven Bean (MDB) then takes that byte array and using a PacketOutput class that corresponds to the correct source ID, metadataSequenceNumber, packetSubType, and parentID, it writes the packet to disk.  It then uses a !PacketSQLOutput to write that same packet to a table in the database.

## Packet Translations ##

So, through all this, there are basically six representations of data packets in the SSDS ecosystem:

  1. SIAM Device Packet (and its sub classes MetadataPacket, SensorDataPacket, DeviceMessagePacket)
  1. SIAM Byte array (from Exportable class)
  1. SSDSDevicePacket
  1. SSDSGeoLocatedDevicePacket
  1. SSDS Byte array
  1. SSDS Protocol Buffers Format

Here is a diagram of these various forms of data

<a href='http://shore-side-data-system.googlecode.com/svn/wiki/images/VariousPackets.jpg'><img src='http://shore-side-data-system.googlecode.com/svn/wiki/images/VariousPackets.jpg' width='800' /></a>

And the translation rules (some of these may seem very strange for legacy reasons).

### DevicePacket to SSDSDevicePacket ###
**This translation is done in the constructor of SSDSDevicePacket which takes in a DevicePacket**

| DevicePacket | Translation Rule | SSDSDevicePacket |
|:-------------|:-----------------|:-----------------|
| sourceID     | direct copy      | sourceID         |
| systemTime   | direct copy      | systemTime       |
| sequenceNo   | direct copy      | sequenceNo       |
| metadataRef  | direct copy: metadataRef->metadataRef,  metadataRef->metadataSequenceNumber, metadataRef->dataDescriptionID | metadataRef, metadataSequenceNumber, dataDescriptionID |
| parentId     | direct copy: parentId->parentId, parentId->platformID | parentId, platformID |
| recordType   | MetadataPacket: recordType to 0, Other: direct copy | recordType       |
|              | If MetadataPacket: packetType = 0, If SensorDataPacket: packetType = 1, If DeviceMessagePacket: packetType = 2 | packetType       |
| firstBufferLength | ignored          |                  |
| firstBuffer  | First buffer depends on which type of packet: If MetadataPacket: copy "bytes" buffer, If SensorDataPacket: copy "dataBuffer", If DeviceMessagePacket, copy "message" | firstBuffer      |
| secondBufferLength | ignored          |                  |
| secondBuffer | Only exists if MetadataPacket and will copy over "cause" buffer | secondBuffer     |

### DevicePacket to SIAM Byte Array ###

This is done by the SIAM Exportable Packet class
| DevicePacket | Translation Rule | SIAM Byte Array |
|:-------------|:-----------------|:----------------|
|              | This is a static value that is set to indicate the byte array is a DevicePacket and is set to 0x0100 | EX\_DEVICEPACKET |
|              | This is just the serial version UID of the class which is always 0 | serialVersionUID |
| sourceID     | direct copy      | sourceID        |
| systemTime   | direct copy      | systemTime      |
| sequenceNo   | direct copy      | sequenceNo      |
| metadataRef  | direct copy      | metadataRef     |
| parentId     | direct copy      | parentId        |
| recordType   | direct copy      | recordType      |
|              | This is set based on what type of packet: If MetadataPacket: set to 0x0101, If SensorDataPacket: set to 0x0102, If DeviceMessagePacket: set to 0x0103 | EX\_XXXXXXPACKET |
|              | This is just the serial version UID of the class which is always 0 | serialVersionUID |
| firstBufferLength | direct copy (see note for first buffer) | firstBufferLength |
| firstBuffer  | This depends on the type of packet: If MetadataPacket: "cause" bytes are copied over, if SensorDataPacket: "dataMessage" bytes are copied over, if DeviceMessagePacket, "message" bytes are copied over | firstBuffer     |
| secondBufferLength | only valid with MetadataPacket, but is copied directly over | secondBufferLength |
| secondBuffer | only valid with MetadataPacket, and the "buffer" bytes are copied over | secondBuffer    |

### SSDSDevicePacket to SIAM Byte Array ###

**Originally done in TransmogrifyMDB by calling SSDSDevicePacket.convertToPublishableVersion3ByteArray before passing byte array to method to translate to SSDS format and then send to ingest**
| SSDSDevicePacket | Translation Rule | SIAM Byte Array |
|:-----------------|:-----------------|:----------------|
| This is a static value that is set to indicate the byte array is a DevicePacket and is set to 0x0100 | EX\_DEVICEPACKET |
|                  | This is just the serial version UID of the class which is always 0 | serialVersionUID |
| sourceID         | direct copy      | sourceID        |
| systemTime       | direct copy      | systemTime      |
| sequenceNo       | direct copy      | sequenceNo      |
| metadataSequenceNumber | direct copy      | metadataRef     |
| parentID         | direct copy      | parentID        |
| recordType       | If packetType = 0 then set recordType = 0, if packetType = 1 then set recordType = recordType, if packetType = 2 then set recordType = recordType | recordType      |
|                  | This is set based on what type of packet: If packetType = 0 then set to 0x0101, if packetType = 1 then set to 0x0102, if packetType = 2 then set to 0x0103 | EX\_XXXXXXPACKET |
|                  | This is just the serial version UID of the class which is always 0 | serialVersionUID |
|                  | This depends on the type of packet: if packetType = 0 then set to length of "otherBuffer", if packetType = 1 then set to length of "dataBuffer", if packetType = 2 then set to length of "dataBuffer" | firstBufferLength |
| other/dataBuffer | This depends on the type of packet: If packetType = 0 then set to bytes from "otherBuffer", if packetType = 1 then set to bytes from "dataBuffer", if packetType = 2 then set to bytes from "dataBuffer" | firstBuffer     |
|                  | This only exists if it is packetType = 0 then it is set to the length of the "dataBuffer" | secondBufferLength |
| dataBuffer       | This only exists if it is packetType = 0 then it is set to the byte from  the "dataBuffer" | secondBuffer    |

### SSDSDevicePacket to SSDS Byte Array ###

**Originally done in SSDSDevicePacket.convertToVersion3ByteArray now in PacketUtility**

| SSDSDevicePacket | Translation Rule | SSDS Byte Array |
|:-----------------|:-----------------|:----------------|
| sourceID         | direct copy      | sourceID        |
| systemTime       | ignored during the translation directly, but used through getter methods for seconds and nanoseconds |                 |
| timestampSeconds | direct copy (note that this is _sort of_ a direct copy, there are getter methods on SSDSDevicePacket that convert the systemTime to seconds and nanoseconds when called). | timestampSeconds |
| timestampNanoseconds | direct copy (note that this is _sort of_ a direct copy, there are getter methods on SSDSDevicePacket that convert the systemTime to seconds and nanoseconds when called). | timestampNanoseconds |
| sequenceNo       | direct copy      | sequenceNumber  |
| metadataRef      | ignored          |                 |
| parentID         | ignored          |                 |
| recordType       | If packetType = 0, set packetSubType to 0, otherwise set to recordType | packetSubType   |
| packetType       | Depends on packetType: if packetType = 0 then set to 1, if packetType = 1 then set to 0,  if packetType = 2 then set to 4 | packetType      |
| metadataSequenceNumber | direct copy      | metadataSequenceNumber |
| dataDescriptionVersion | direct copy      | dataDescriptionVersion |
| platformID       | direct copy      | parentID        |
|                  | copy length of dataBuffer | firstBufferLength |
| dataBuffer       | direct copy      | firstBuffer     |
|                  | copy length of otherBuffer | secondBufferLength |
| otherBuffer      | direct copy      | secondBuffer    |

### SIAM Byte Array to SSDS Byte Array ###

**Originally done in TransmogrifyMDB in checkAndPublishBytes method now in PacketUtility**

| SIAM Byte Array | Translation Rules | SSDS Byte Array |
|:----------------|:------------------|:----------------|
| EX\_DEVICEPACKET | ignored           |                 |
| serialVersionUID | ignored           |                 |
| sourceID        | direct copy       | sourceID        |
|                 | Depending on EX\_XXXXXXXPACKET: if MetadataPacket then set packetType to 1, if SensorDataPacket then set packetType to 0, if DeviceMessagePacket, set packetType to 4 | packetType      |
|                 | This was set using the SIAMMetadataTracker that tried to keep track of real version numbers based on XML in payload but that no longer exists, it is a direct copy | metadataSequenceNumber |
| systemTime      | Split into timestampSeconds and timestampNanoseconds | timestampSeconds and timestampNanoSeconds |
| sequenceNo      | direct copy       | sequenceNumber  |
| metadataRef     | direct copy       | dataDescriptionVersion |
| parentId        | direct copy       | parentID        |
| recordType      | If MetadataPacket (determined from EX\_XXXXXXXPACKET), recordType set to 0, otherwise set to recordType | packetSubType   |
| EX\_XXXXXXXPACKET | ignored in storage, but used in logic |                 |
| serialVersionUID | ignored           |                 |
| first/secondBufferLength | If MetadataPacket (determined from EX\_XXXXXXXPACKET), firstBufferLength is set to secondBufferLength so we can flip the "cause" and "buffer" bytes because it just made more sense since the cause was rarely populated. Otherwise set to firstBufferLength | firstBufferLength |
| first/secondBuffer | If MetadataPacket (determined from EX\_XXXXXXXPACKET), firstBuffer is set to secondBuffer so we can flip the "cause" and "buffer" bytes because it just made more sense since the cause was rarely populated. Otherwise set to firstBuffer | firstBuffer     |
| firstBufferLength | If MetadataPacket (determined from EX\_XXXXXXXPACKET), secondBufferLength is set to firstBufferLength to flip "cause" and "buffer" bytes | secondBufferLength |
| firstBuffer     | If MetadataPacket (determined from EX\_XXXXXXXPACKET), secondBuffer is set to firstBuffer to flip "cause" and "buffer" bytes | secondBuffer    |

## Transmogrify and Ingest Servlets ##

To enable clients to send data in to the SSDS over HTTP, there are two servlets that can be called to insert data.  The Transmogrify servlet takes in http request parameters that should match the fields that are found in SIAM.  The servlet then extracts the values from the query parameters and creates a byte array in SIAM form and publishes it to the Transmogrify topic as if it came in from a JMS client.  This allows for non-Java clients to send in data through the normal messaging pipeline.  The same goes for the Ingest servlet, but that servlet takes in parameters that match the SSDS format of data.  So, for the Transmogrify servlet, the URL would be constructed of the following pieces:

http://your.host.com:8080/transmogrify/Transmogrify?
  1. response=true (this indicates if you want the call to send back and HTTP response or not)
  1. &SourceID=101 (this is the ID of the device that is sending the packet)
  1. &Timestamp=2011-02-11T18:50:00 (this is the timestamp on the packet expressed in ISO form)
  1. &SequenceNumber=1 (this is the sequence number on the packet)
  1. &SecondStreamID=0x102 (this is important as it defines the type of packet that is being sent.  The options are: 0x101 for a metadata packet, 0x102 for a sensor or summary data packet, 0x103 for a device message packet)
  1. &FirstBuffer=SGVsbG8gSW5nZXN0IQ== (this is the byte array in 64 bit encoded form)
  1. &RecordType=1 (if the second stream ID indicates this packet is a sensor data packet, this field defines a type identifier to specify which type of data is being sent.  Some devices can send multiple kinds of data)
  1. &ParentID=100 (the id of the parent that the source ID was attached to when it created the packet)
  1. &MetadataRef=0 (this is the sequence number of the metadata packet that contains the information that describes what is in the payload of this packet)

For the Ingest servlet, the parameters are:

http://your.host.com:8080/ingest/Ingest?
  1. response=true (this indicates if you want the call to send back and HTTP response or not)
  1. &SourceID=101 (this is the ID of the device that is sending the packet)
  1. &ParentID=100 (the id of the parent that the source ID was attached to when it created the packet)
  1. &PacketType=1 (this is the type of packet being sent: 0 = data packet, 1 = metadata packet, 4 = device message packet)
  1. &PacketSubType=1 (this is the type of payload if the packet is a data packet)
  1. &MetadataSequenceNumber=0 (this is the sequence number of the packet that contains the metadata describing the payload of this packet)
  1. &DataDescriptionVersion=0 ()
  1. &Timestamp=2011-02-11T18:50:00 (this is the timestamp on the packet expressed in ISO form)
  1. &SequenceNumber=1 (this is the sequence number on the packet)
  1. &FirstBuffer=SGVsbG8gSW5nZXN0IQ== (this is the byte array in 64 bit encoded form)