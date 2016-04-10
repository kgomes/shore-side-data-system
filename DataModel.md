# Introduction #

In order for the SSDS to track all relative information about the data in an observatory, a data model was created to hold all the information and relevant concepts to manage the data for storage and access of information in the observatory.  This documents that data model for the SSDS.  Here are a couple of UML diagrams to show the complete and detailed view of the core data model classes.  Below the images are some notes about each class.

<a href='http://shore-side-data-system.googlecode.com/svn/wiki/images/DataProducers.jpg'><img src='http://shore-side-data-system.googlecode.com/svn/wiki/images/DataProducers.jpg' width='800' /></a>

<a href='http://shore-side-data-system.googlecode.com/svn/wiki/images/DataContainers.jpg'><img src='http://shore-side-data-system.googlecode.com/svn/wiki/images/DataContainers.jpg' width='800' /></a>

## CommentTag ##
This class is used to represent symbols/characters that identify entries in DataContainers as comments. This allows parsing routines to skip things in files (or streams) that are not data.  This is heavily used when dealing with DataContainers that are files and have headers.  This came from MBARI's AUV data files which are binary files that have ASCII headers. Without the concept of CommentTag, these files would be un-parseable.

## DataContainer ##
This class contains the metadata that describes data that is stored in two (currently SSDS deals with two) formats: files and SSDS streams.  The first thing to know is that every DataContaiber in the SSDS needs to have a valid and unique URI as that is what SSDS uses as a unique key (not the primary key, but an alternative unique key).  For files, that usually means they are accessible through some protocol, whether it is HTTP, FTP, etc.  We felt this requirement was reasonable because if a file was not accessible via some protocol there was not much use registering it in the SSDS. For streams, the URI is set to point to the GetOriginalDataServlet with a parameter that identifies the device the stream came from. The stream URI should also contain some limiting parameter (like lastNumberOfPackets) to limit the data return so that if some view rendering allows the user to click on it, it returns a small subset of data.  Currently, the
Ruminate MDB creates this URI with appropriate limiting parameters. Also see CommentTag, HeaderDescription, RecordDescription and RecordVariable.

## DataContainerGroup ##
This class came along later in the design process and was really designed as a way to categorize DataContainers. By defining DataContainerGroups and linking them to various DataContainers, you could create loose associations of DataContainers for the purposes of query and discovery.  It was created in parallel to the DataProducerGroup class and in reality is rarely used in comparison to the use of DataProducerGroup.

## DataProducer ##
This class is one of the most heavily used concepts in the SSDS. It is really more of a logical concept than something physical.  Normally, people think of data being produced by a device or processing unit.  What we have found though the years is that to accurately describe a data producing entity, there was more information than just the processing unit that was necessary.  You really need the context of the device (or software) and the environment it is operating in to describe the data production process.  For example, a single device can be configured to produce completely different data products based on many things: time, location, configuration files, calibration files, input parameters (sensing), etc.  It was this complete description of all these things that gives you an accurate picture of the data production environment for the output (DataContainer). That is the reason you will see a large number of associations to the DataProducer class.

## DataProducerGroup ##
Like DataContainerGroup, this class was created for the purposes of creating loose, many-to-many associations.  Unlike DataContainerGroup, this class is used :).  It was helpful in user interfaces where we could present DataProducer groupings in drop down type lists.  For example, we have groupings like AUVs, Moorings, etc.

## Device ##
This class usually identifies a piece of hardware (although we have started considering concepts of virtual devices but it is not a baked in concept yet) that can take on different roles. The discussions around terms like instrument, platform and sensor often would degrade into chaos and it often seemed to center around the fact that hardware would be called something different based on how it was being viewed.  Sometimes an AUV was called and instrument, sometimes a platform. To work around this and make the model extensible, we created a simple device class that has the concept of roles. In conjunction with the DataProducer class you could describe the same piece of hardware in different ways to give context to the DataContainer is was outputting.  For this reason, Devices don't have direct associations to DataContainers. This may seem strange and does complicate some queries, but enables more complex concepts to be captured.

## DeviceType ##
In the same way that we use DataContainerGroup and DataProducerGroup to create loose many-to-many associations, we use DeviceType to create associations between devices.  These associations tend to be more constrained than with the grouping concepts, but comes from the fact that devices can be described in many ways. For example, a CTD can be a CTD device, a pressure device, a temperature device, or a conductivity/salinity device.

## Event ##
This class represents any moment interval concept.  While it was created to capture any time spanning concept (like DataProducers do), we have not used the class to date. We definitely have plans for this class, but have not leveraged it to date.  It could be used to capture information like sensing events, user annotations, experiment log entires (I.e. "when I launched the AUV, the water looked really red"), etc.

## HeaderDescription ##
When parsing files, there are often sections at the beginning of the file that are considered headers and do not have any parseable contents.  In essence, it should just be skipped.  HeaderDescription gives the parser the information necessary to skip over the header of the file.  This can be described in different ways (comment tags, number of lines, number of bytes, etc) and the HeaderDescription class holds the information necessary for parsers to work effectively.

## Keyword ##
Keywords are not heavily used in the SSDS to date, but were included in the data model to represent a concept similar to tags.  It is another grouping/association mechanism and while there are keywords in the SSDS system, we don't have any clients to date that use them (to the best of our knowledge anyway)

## Person ##
A class that simply represents contact information for various information in the SSDS.  It have many associations as you can imagine.  One thing we would probably change moving forward is that most of these associations are one-to-one and we have many, many cases where more than one person could, and should, be associated with a concept.

## RecordDescription ##
The RecordDescription class in combination with RecordVariable is key to describing what the data in a DataContainer looks like.  It contains information about records in a DataContainer that are helpful in parsing the information therein.  Things like record terminators, ASCII/binary, endianess, regular expressions, and something called recordType.  RecordType is a key attribute as it shows up in the packet structure that is streamed to the SSDS.  A large number of the instruments in oceanography output many different records.  For example, a GPS can output records that detail the location of the GPS unit, but can also output records of satellite status and error status.  Without the concept of recordType, there is no way to tag the various data packets to distinguish them from each other.  This means that a device driver will have to tag the outgoing packets with the correct recordType so that a parser will know which corresponding RecordDescription to use in parsing.

## RecordVariable ##
This class represents a single variable that has data associated with it in a DataContainer.  It contains information about the variable such as which column in the record it is located, what the regular expression is that can be used to parse the data from the column, what valid mins and maxs are, units, etc.  In conjunction with RecordDescription, it is key in allowing agents to parse data from the DataContainer.

## Resource ##
The Resource class is the grab bag class. It was created as a place to attach things that were necessary to track, but did not fall into the category of DataContainer. Originally thought of as a place to capture information about things like calibration files, it has exploded in it's usage because of its grab bag nature.  Often times it is because developers and users don't know where else to put things and they become Resources.  This is great in some ways, because we finally have a way to capture things and information that used to be lost, but on the flip side, it is left to the application to interpret what that Resource is.  We included information about mime type so there was an extensible way for application to assign and interpret information that empowers usage of Resources.  There is much power here, but also much work to do to harness that power.  As is said, "with great power, comes great responsibility".

## ResourceBLOB ##
This class has not been used to date (as far as we know) but the idea behind it was the an agent could actually store the bytes of a Resource (like an image) in the database itself instead of having it hosted elsewhere and referenced by URI.  Not sure about the future of this class.

## ResourceType ##
This is analogous to the DeviceType class in being able to created loose associations and groupings of Resources.

## Software ##
This class is an analog to the Device class but is meant to capture information about software processes.  It contains concepts of URIs for accessing the source code in some repository, version numbers to track what version of the software was utilized by the associated DataProducer.  As time has moved on, there has been a thought convergence on the class Device and Software as it is really software on the device that produces data in most instances.  There is more thought and work to be done here, but there is definitely a structure that would align the concepts of Device and Software and should be done on the future.

## StandardXXXXXXX classes ##
These classes were designed to be the mechanism for mapping SSDS concepts to the various standards that exist in the community.  As an example, if you had a RecordVariable called "temperature" on a CTD, it could represent the CF standard sea-surface-temperature (SST) if the CTD was deployed on the surface.  While you would not want to list the device's variable as SST because it is not always deployed at the surface (think about a yo-yo pattern of an AUV), in certain contexts, the variable would be equivalent to SST.  The idea with these classes is that you could define something like CF sea-surface-temperature as a StandardVariable and then make the association to a RecordVariable so that if somebody was querying by CF SST, they would find the DataContainer associated with CF SST.  While we still hold these concepts as key and very valuable, in practice, it is left to an outside entity (rules engine, expert user, etc.) to make these associations and they have not been used much, if at all, to date.

  * StandardDomain
  * StandardKeyword
  * StandardReferenceScale
  * StandardUnit
  * StandardVariable

## UserGroup ##
Early on, the SSDS did not leverage any concept of authentication realms and so to be able to capture groupings of people (think LDAP group or role) we created the UserGroup class.  It was never used and will most likely be removed in the future.