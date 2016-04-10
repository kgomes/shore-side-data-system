# Introduction #

These classes provide utilities for working with object that are represented by the DataModel classes.  The information that is captured in the DataModel can be represented in different forms.  This is done to provide various clients a variety of ways in working with the information in the DataModel.  The three representations that are supported to date are:

  * XML Representation: The DataModel classes can be serialized and deserialized in XML form that matches the SSDS Metadata Schema found here: http://code.google.com/p/shore-side-data-system/source/browse/branches/jboss-5.1/src/xml/SSDS_Metadata.xsd
  * JSON Representation: The DataModel classes can also be serialized to and from JSON form and Google's GSON is utilized for this transformation.
  * Custom String Representation:  This is mostly for logging and does not serialize relationships.  It simply serializes an object to and from a custom form that is more easily readable in a log file.

# Details #

Here is a class diagram of the various classes used in the translation of forms of metadata.


<a href='http://shore-side-data-system.googlecode.com/svn/wiki/images/MetadataUtilities.jpg'><img src='http://shore-side-data-system.googlecode.com/svn/wiki/images/MetadataUtilities.jpg' width='800' /></a>