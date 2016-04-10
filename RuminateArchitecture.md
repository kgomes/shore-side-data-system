# Introduction #

**UNDER CONSTUCTION** - This documentation is currently in flux (7/15/2011)

The Ruminate component of the SSDS was designed to be the handler for incoming metadata messages from SSDS clients.  When metadata arrives, Ruminate is tasked to examine everything in the message and perform a sequence of steps to ensure that the metadata is recorded (persisted) in the SSDS appropriately.  The incoming metadata packets should have an XML payload that conforms to the SSDS Metadata Schema in order for it to be handled correctly (otherwise it will be ignored).

# Details #

After transmogrify and ingest deal with the incoming stream of packets and record them in the database, the message, if metadata, gets forwarded on to the JMS topic where the Ruminate Message Driven Bean (MDB) is listening.  The MDB is responsible for a sequence of steps in order to make sure the incoming metadata is understood and is accurately reflected in the persistent store of the SSDS database.

A fundamental thing to understand when thinking about metadata coming in through XML embedded in messages, is that it MUST be assumed that the incoming XML is an incomplete picture of the current state of the objects represented in the XML.  For example, often times in XML coming from streaming instruments, they will not include all attributes about the device in the XML.  This lack of information does not mean that the information has gone away, it is just that it is not included.  This is confusing, so let's illustrate.

Let's say a message comes in about the deployment of a certain device:

```
<DataProducer type="Deployment">
    <Device id="100" name="Widget" mfgSerialNumber="004X567"/>
</DataProducer>
```

In the case of devices, they should already be identified in the SSDS persistent metadata so there may be more metadata defined for that device already.  Just because the 'mfgModel' attribute is not specified in the XML, does not mean it should be removed from the existing entry in the SSDS persistent store.

The bottom line is that SSDS will have to use some rules to decide how it wants to deal with the incoming metadata, it cannot just store it blindly.  Also, the SSDS takes a conservative approach with attributes in that if the attribute is not specified in the XML, the field is left untouched in the persistent store.  The drawback to that is that if you want to clear an attribute using XML, you still have to specify it, but you have to leave it empty, then SSDS will persist an empty field.  Another thing to note is that very often with the SSDS, it will receive messages out of order, from the past, or duplicates and it must be able to deal with all of them correctly.  To fulfill it's role, here are the steps that Ruminate goes through to deal with incoming metadata.

### Ruminate Steps ###
  1. Extracts the payload and converts the bytes to ASCII
  1. Figures out if the ASCII is a JSON, XML, or String representation of the metadata
  1. Uses the appropriate translator to convert the payload into SSDS metadata objects.
  1. If: Top of Object tree is DataProducer
    1. If: DataProducer is of type Deployment
      1. If: DataProducer has an output
        1. If: The output is of type Stream
    1. elif: DataProducer is of type ProcessRun

  1. Extracts the XML metadata from the JMS payload
  1. Stores the XML to disk (which is then made available over HTTP)
  1. Creates the corresponding SSDS object graph using ObjectBuilder (direct translation from XML to objects).
  1. It then iterates over the list of top levels metadata objects (almost always just one) and takes the following steps:
    1. Creates a new Resource object that represents the XML file that was just saved to disk and made available over HTTP and does the following:
      1. Creates a name
      1. Creates a description
      1. Sets the start and end date to match the timestamp of the packet that the metadata arrived in
      1. Assigns the URL to the URI attribute
      1. Sets the MIME type to "application/xhtml+xml"
      1. Sets the content length to the size of the XML file
      1. Creates a new ResourceType object named "application/xhtml+xml" and sets it as the ResourceType
      1. Creates a new Keyword object named "XML" and adds it to the new Resource
      1. Creates a new Keyword object named "Metadata" and adds it to the new Resource
    1. Rescursively adds the new XML Resource to the DataProducer and any child DataProducers as well.


  1. When a packet arrives, it can be checked against data packets that have already been received. However, this cannot be done by just looking in the data store as the packet will have already been stored by ingest. How do we do this? I used to use XML tracker,but that was kludgy and not very robust.
  1. The ideal situation is one in which the object graph that is handed to ruminate should be handled the same regardless of how it got to ruminate.  Again, it should be thought of as an instance of objects from a graph, or portion of, that may or may not exist in SSDS already.
  1. The very first thing that ruminate does is take the incoming XML and attempts to convert it straight to an SSDS object graph.
  1. Next, depending on what the head object is, it should run through a set of steps to ensure the graph is "grafted" onto the information in the metadata database.
  1. Let's start with DataProducer since that is the most common head object
  1. We can think about the metric for equality for objects (I.e. The overridden equals method) to look for the object in the database.  This can be done using the "findEquivalentPersistantObject" method in the DAOs.


  1. Checks to see if this metadata has arrived before for the instrument-parent combination (just compares the text blobs)
  1. If it looks to be new, it keeps going, otherwise it stops processing
  1. The DataProducer is then examined to see if there are any outputs (from children also) that are DataContainers of type DATA\_STREAM. If that is the case then some special processing happens as follows:
  1. Ruminate searches SSDS for all DataProducers that are associated with the Device that is associated with the data streams found here.
  1. If the top level DataProducer does not have a name, ruminate will do it's best to create some name that makes sense
  1. If the top level DataProducer does not have a description, a &quot;No description&quot; is added
  1. If the top level DataProducer does not have a role assigned to it, since it is a DataProducer with a stream, the role of ROLE\_INSTRUMENT is set
  1. If the top level DataProducer does not have a start date, the date of the packet the metadata arrived in is used as the start date
  1. If the top level DataProducer does not have a &quot;Type&quot; set, one of TYPE\_DEPLOYMENT will be set since this is a DataProducer with a stream of data
  1. The output of the DataProducer is examined and if it has no name, one is created from the date and Device UUID.
  1. The output of the DataProducer is examined and if the URL is empty, one is constructed using the GetOriginalDataServlet so that the URL will return the last 10 packets
  1. The output of the DataProducer is examined and if there is no start date, the date of the packet in which the metadata arrived is used to set the start date of the output
  1. The output of the DataProducer is examine and if there is no description, one is generated that talks about which instrument this stream comes from
  1. The above set of steps is then repeated recursively on all the child DataProducers.
  1. If the incoming metadata packet had a parent ID specified, ruminate will attempt to add the newly created DataProducer to the most recent parent DataProducer associated with the parent device using the following steps:
  1. Query for all DataProducers that are associated with the parent Device sorting by start dates
  1. Grab the DataProducer with the most recent start date
  1. If there are no parent DataProducers or the most recent one has an end date already, create new parent DataProducer
  1. Add the newly created DataProducer to the most recent parent DataProducer or the newly created DataProducer
  1. Lastly, if the newly created DataProducer does not have a start date, use the date from the metadata packet that was received as the start date of the DataProducer.