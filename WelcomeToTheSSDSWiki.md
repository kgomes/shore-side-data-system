# Introduction #

The Shore Side Data System (SSDS) software is a J2EE application that was built by the Monterey Bay Aquarium Research Institute (MBARI) to serve as both a data storage and catalog for streaming instrument data as well as a catalog for any other URL accessible data and resources that are related to data products and processes.  Using client libraries, users can also instrument their processes to register information and resources with the SSDS so that data provenance (data product lineage) can be tracked.  This allows users to see the ancestors and descendants of various data products and resources that are registered in the SSDS.

The SSDS was originally developed as a component of the Monterey Ocean Observing System, commonly called MOOS. MOOS was a MBARI development effort to develop key infrastructure elements for national and international ocean observing systems. The initial MOOS Workshop Report cited the need for a reliable, cost-effective data management system. The SSDS was the component that was built to address the data management needs for ocean observatories.

<table cellpadding='0' border='0' cellspacing='2'>
<tr><td>
<img src='http://shore-side-data-system.googlecode.com/svn/wiki/images/OperationalSSDS.jpg' alt='Operational Deployment of SSDS' />
</td></tr>
<tr><td align='center'><i>An example logical deployment of the SSDS at MBARI</i>
</td></tr>
</table>

Most major data management systems work within certain boundaries, dealing with specific data formats or conventions (e.g., netCDF COARDS), or a known and stable data set (WODC). But to meet MBARI's diverse data management requirements, SSDS ingests, catalogs and serves:

  1. a large and ever-changing set of sensors, instruments, and data set formats
  1. varied data collection formats, ASCII and binary, simple and complex
  1. streaming, file-based, and human-entered data, from internal and external data systems, in near-real-time and offline
  1. reprocessed data, including quality control and domain-specific data transformations
  1. operational changes to observatory instrumentation, including calibrations, instrument changes, and platform changes

At the same time, the SSDS provides a coherent, integrated interface to scientists and other users, who want simple access to both raw data from instruments as well as the capability to cull specific variables from data sets. The SSDS was designed to support the data volumes and new instrumentation expected on ocean observing systems like MOOS and MARS (NSF's cabled test observatory in Monterey Bay), with minimal or no reprogramming.

The SSDS design emphasizes flexibility, scalability, expandability, and maintainability, anticipating the highly variable and growing research and operational demands on ocean observing systems. Because it is part of an Ocean Observing System, the SSDS is particularly effective when coupled with a metadata-aware instrument infrastructure such as SIAM. However, it readily accommodates less systematic data sources, recognizing the critical role of external data and system interoperability in scientific data management. By applying an iterative methodology within a systems-oriented development approach, we have been able to tune the architecture for each new application while building toward long-term observing system goals.

<table cellpadding='0' border='0' cellspacing='2'>
<tr><td>
<img src='http://shore-side-data-system.googlecode.com/svn/wiki/images/DataProvenance.jpg' alt='Data Provenance Image' />
</td></tr>
<tr><td align='center'><i>SSDS Web Inteface Showing Example of Data Provenance Tracking for Mooring System</i>
</td></tr>
</table>

Below are links to more information about the design, development, deployment and use of the Shore-Side Data System software.

If you have any questions please contact the SSDS team at ssds at listserver.mbari.org

# Documents #

  1. [Requirements](Requirements.md)
  1. [Design](Design.md)
  1. [Development](Development.md)
  1. [Deployment](Deployment.md)
  1. [Use](Use.md)