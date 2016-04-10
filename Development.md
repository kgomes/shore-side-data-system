## Setup the Development Environment ##

In order to deploy SSDS or develop using the SSDS code base, there are some things you should install to help in the development process.  Here are some suggestions:

  1. Install Java 7+ SDK (http://java.sun.com)
  1. Install Ant (http://ant.apache.org)

## The Build Process ##

I use an ant script to build the components of the SSDS project.  Here are the steps that happen when ant gets run.

  1. The path-defs.xml file gets imported.
    1. The first thing that happens in the path-defs.xml file is that custom.properties file is read in.  These are the properties that somebody would edit when they are building their own SSDS installation.  There is a custom.properties.template file that can be used as a starting point for a custom.properties file.
    1. The next thing that happens in the path-defs.xml file is that based on the database that was specified in the custom.properties file, a mysql.properties or a mssql.properties file is read in to establish all the appropriate properties for the backing database that the SSDS build will be utilizing.
    1. Based on the database properties that were defined, some common database properties are read from the database specific properties so they could be used generically during the build.
    1. Then build.properties are then loaded to get the rest of the properties for the SSDS build and configuration.
    1. Then various filesets, paths and patternsets are created that will be used throughout the build.
    1. And the last thing is that any necessary ant tasks are defined that will be needed during the build.