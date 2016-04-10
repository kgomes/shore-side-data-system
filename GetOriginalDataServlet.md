# Introduction #

One method of extracting data from the SSDS is to use an HTTP service called "GetOriginalDataServlet".  Behind the scenes it is a java servlet, but the interface to the clients is through standard HTTP.  This document gives the user some information on how to use this service.

# Usage #

The URL for this service is located on the SSDS host at the relative URL /servlet/GetOriginalDataServlet and contains the following parameters (example here is shown for MBARI's SSDS server):
```
  http://new-ssds.mbari.org/servlet/GetOriginalDataServlet
    ?deviceID=<DVID>
    &startDataDescriptionVersion=<SDDV>
    &endDataDescriptionVersion=<EDDV>
    &startPacketSubType=<SPST>
    &endPacketSubType=<EPST>
    &startParentID=<SPID>
    &endParentID=<EPID>
    &startTimestampSeconds=<STS>
    &endTimestampSeconds=<ETS>
    &lastNumberOfPackets=<LNP>
    &numHoursOffset=<NHO>
    &outputAs=<binary>
    &displayPacketHeaderInfo=<true|false>
    &noHTMLHeader=<1|0|true|false>
    &delimiter=<DELIM>
    &recordDelimiter=<RECDEL>
    &prependWith=<oasis>
    &convertTo=<hex>
```

### Parameters: ###
| **Parameter Name** | **Opt/Required** | **Query or Return Format** | **Available Options** | **Description** |
|:-------------------|:-----------------|:---------------------------|:----------------------|:----------------|
| deviceID           | **required**     | query                      | any number            | This is the SSDS ID of the device that you would like the data for |
| startDataDescriptionVersion | optional         | query                      | any number            | This is the number that will be used as the starting query number for the dataDescriptionVersion number to search for.  This is also known as the metadata version (or reference) number. |
| endDataDescriptionVersion | optional         | query                      | any number            | This is the number that will be used as the ending query number for the dataDescriptionVersion number to search for.  If this is not specified, but the startDataDescriptionVersion is, only the startDataDescriptionVersion will be searched for.  This is also    known as the metadata version (or reference) number. |
| startPacketSubType | optional         | query                      | any number            | This is the starting number for the "type" of packet to retrieve from the device.  If a device generates multiple streams of data, these are usually broken up and uniquely identified by a packet sub type (also known as record type).  This is the starting record type. |
| endPacketSubType   | optional         | query                      | any number            | This is the ending number for the "type" of packet to retrieve from the device.  If a device generates multiple streams of data, these are usually broken up and uniquely identified by a packet sub type (also known as record type).  This is the end record type.  If this is not specified, but the startPacketSubType is, only the startPacketSubType will be queried for. |
| startParentID      | optional         | query                      | any number            | This is the SSDS ID of the starting parent to search for.  In other words, the query will look for all packets from the device specified while it was deployed on parents starting at ID of startingParentID. This was also known as platform ID. |
| endParentID        | optional         | query                      | any number            | This is the SSDS ID of the ending parent to search for.  In other words, the query will look for all packets from the device specified while it was deployed on parents starting at ID of startingParentID and ending with parent with endParentID.  If this is not specified and the startParentID is, only the startParentID will be searched for. ParentID is also known as platform ID. |
| startTimestampSeconds | optional         | query                      | Any Number            | These are epoch seconds (number of seconds since January 1, 1970) of the start of the time window of data you are looking for |
| endTimestampSeconds | optional         | query                      | Any Number            | These are epoch seconds (number of seconds since January 1, 1970) of the end of the time window of data you are looking for |
| lastNumberOfPackets | optional         | query                      | Any Number            | This is the number of packets back in time (from the most recent packet) to retrieve.  This option over-rides the timestamp specifications and the nice thing about it is that if the source has sent data, you will always get a return. Sometimes when you specify a time window, and the source has not sent data in that time, you will get nothing back. |
| numHoursOffset     | optional         | query                      | Any Number            | This is the number of hours wide that the time query window should be.  This is only applicable when one of either start or end times have been specified. If the start time is specified, this offset is added to the start time to get the end time.  If the end time is specified, it is subtracted from the end time to get the start time. |
| outputAs           | optional         | return format              | **binary**            | This is a flag that indicates if the data should be returned in binary format.  In order for this to happen, it must be set to **binary**.  Otherwise, the return will be in ASCII |
| displayPacketHeaderInfo | optional         | return format              | true|false            | This is the flag that turns on the printout of the packet header information before each packet. |
| noHTMLHeader       | optional         | return format              | 1|0|true|false        | If this value is set to **true** or **1**, the return from this URL call will not be wrapped with HTML tags and no content-type will be specified.  Otherwise the content-type will be text/html and there will be HTML tags around the results. |
| delimiter          | optional         | return format              | any text              | This is the text that will be used to separate the packet header and data columns of the return.  If this is not specified a comma will be used as a default. |
| recordDelimiter    | optional         | return format              | any text              | This is the text that will be used to separate the packet records of the return.  This is useful if the payload contents contain normal line feed characters.  If this is not specified then nothing is added to the return to separate the records. |
| prependWith        | optional         | return format              | **oasis**             | This is a flag that indicates what is to prepend each returned packet. There is currently only one option, which is **oasis** which prepends each record with an OASIS style timestamp |
| convertTo          | optional         | return format              | **hex**               | This is a flag that indicates what the data buffer is to be converted to (if any).  Currently only the **hex** option does any conversions. |


---

**For legacy reasons, the following are also supported**

| dsURL | see description | query | String form of URL | This parameter is the URL that points to a file of serialized packets.  This would only be used if the deviceID parameter was not used (see above).  So this is required if there is no deviceID, but will be ignored if deviceID is specified |
|:------|:----------------|:------|:-------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| metadataID | _same as startDataDescriptionVersion above_ |
| recordType | _same as startPacketSubType above_ |
| platformID | _same as startParentID above_ |
| startDateTime | optional        | query | Date/Time          | This is the string representation of the date and time of the start of the time window over which you are requesting data.  It can be in one of two formats: **YYYYMMDD.hhmmss** or **YYYY-MM-DDTHH:MM:SSZ**                                   |
| endDateTime | optional        | query | Date/Time          | This is the string representation of the date and time of the end of the time window over which you are requesting data.  It can be in one of two formats: **YYYYMMDD.hhmmss** or **YYYY-MM-DDTHH:MM:SSZ**|
| isi   | optional        | return format | 1|0|true|false     | This is the flag that turns on the printout of the SIAM packet header information before each packet.                                                                                                                                          |
| numHoursBack | _same as numHoursOffset above_ |

### Example: ###

http://new-ssds.mbari.org/servlet/GetOriginalDataServlet?deviceID=1613&recordTypeID=1&isi=1&lastNumberOfPackets=10

### Example in a Perl program ###

```
  use LWP::Simple;

  $url = "http://new-ssds.mbari.org/servlet/GetOriginalDataServlet
    ?deviceID=1294
    &metadataID=0
    &recordTypeID=1
    &platformID=1295
    &isi=0
    &numHoursBack=24
    &convertTo=hex
    &noHTMLHeader=1
    &prependWith=oasis";

    $data = get($url);

    print $data;
```