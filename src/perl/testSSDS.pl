#!/usr/bin/perl -w
# Script to test the SSDS.pm module
############################################################################
# Copyright 2009 MBARI                                                     #
#                                                                          #
# Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1        #
# (the "License"); you may not use this file except in compliance          #
# with the License. You may obtain a copy of the License at                #
#                                                                          #
# http://www.gnu.org/copyleft/lesser.html                                  #
#                                                                          #
# Unless required by applicable law or agreed to in writing, software      #
# distributed under the License is distributed on an "AS IS" BASIS,        #
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
# See the License for the specific language governing permissions and      #
# limitations under the License.                                           #
############################################################################
#
# To turn on debugging output in the SSDS module call the dubug method
# on the appropriate object, e.g.:
#       $dpAccess->debug(3);


# Note: Primary Key IDs are used here for testing.  As the database at
#       $server evolves those numbers may need to be changed.

# $Id: testSSDS.pl,v 1.19.2.13 2006/03/11 00:57:05 mccann Exp $

use SSDS;
use POSIX;
use Carp;
use Test::Simple tests => 92;

my $verbose = shift || 0; 	# Set to 1 to show object attributes, etc.

#
# Set SSDS server for all tests
#
$ssds = new SSDS();
$server = '@CLIENT_TOOL_PERL_MODULE_HTTP_SERVER_BASE@';
##$server = 'http://ssdsdevpc:8080/';
$ssds->ssdsServer($server);
if ( $ssds->ssdsServer() ) {
        print "Running tests using server: " . $ssds->ssdsServer() . "\n";
}
else {
        die "Cannot get response from server $server.  Perhaps it is temporarily down (?)\n";
}

#
# Run the tests 
#
dataProducerTests();
dataProducerWithInputOutputTests();
addProcessRunToSSDSTests();
childDataProducerTests();
dataProducerEsecsAndUpdateTests();
recordVariableTests();
makePersistentTests();
specialCharacterTests();
# kgomes 20080814 - I commented out these tests because it does not look like there is any
# way they would pass on a clean database.
#findParentWithRoleTest();
#queryPerformanceTest();
processRunSoftwareTest();


exit;

#--------------------------------------------------------------------------------------
#
sub printAttributes {
	
	my $obj = $_[0];
	
	my @attrs = ($obj->get_attribute_names());
	foreach my $a (@attrs) {
	        next unless $obj->$a;
	        print "  $a = " . $obj->$a . "\n";
	}
	
} # End printAttributes()


#--------------------------------------------------------------------------------------
#
sub printAttributesOfCollection {
	
	my $objs = $_[0];
	
	my @attrs = ${$objs}[0]->get_attribute_names();
	
	foreach my $obj (@{$objs}) {
		foreach my $a (@attrs) {
		        next unless $obj->$a;
		        print "  $a = " . $obj->$a . "\n";
		}
	}
	
} # End printAttributesOfCollection()


#--------------------------------------------------------------------------------------
# Simple DataProducer tests

sub dataProducerTests {
	
	my $dpAccess = new SSDS::DataProducerAccess();
	print "\nRunning Simple DataProducer tests on server " . $dpAccess->ssdsServer . "\n";
	
	my $newDP = new SSDS::DataProducer();
	my $name = "My new DataProducer";
	$newDP->name($name);
	$newDP->description("A description of a new DataProducer for testing");
	printAttributes($newDP) if $verbose;
	ok( $name eq $newDP->name(), "ObjectTemplate 'set' and 'get' methods on name() work");

	my $id = $dpAccess->insert($newDP);
	ok( $id =~ /java.lang.Long\|(\d+)/, "insert() returned id = $1");
	
	my $dps = $dpAccess->findByName($name, 'true', "name", 'false');
	printAttributesOfCollection() if $verbose;
	ok( $name eq ${$dps}[0]->name(), "findByName() name = $name");
	
	# kgomes - 2008-08-13 - I commented out the deletes as the MetadataAccessServlet no longer allows them for security reasons.
	#my $ret = $dpAccess->delete($newDP);
	#ok( $ret eq "null", "delete()");

	#my $dps2 = $dpAccess->findByName($name, 'true', "name", 'false');
	#ok( ! defined $dps2, "$name no longer in the database as determined by findByName()");
	

} # End dataProducerTests()



#--------------------------------------------------------------------------------------
# Child DataProducer tests

sub childDataProducerTests {
	
	my $dpAccess = new SSDS::DataProducerAccess();
	print "\nRunning Child DataProducer tests on server " . $dpAccess->ssdsServer . "\n";
	
	my $parentDP = new SSDS::DataProducer();
	my $childDP1 = new SSDS::DataProducer();
	my $childDP2 = new SSDS::DataProducer();
	$parentDP->name("Parent DataProducer");
	$childDP1->name("Child DataProducer 1");
	$childDP2->name("Child DataProducer 2");

	my $id = $dpAccess->insert($childDP1);
	my $success = defined $id && $id !~ /Fault/;
	ok( $success, "insert() child DP 1 returned: $id");
	if ($success) {
		$id =~ /java.lang.Long\|(\d+)/;
		$foundChildDP1 = $dpAccess->findById($1, 'false');
	}	

	$id = $dpAccess->insert($childDP2);
	$success = defined $id && $id !~ /Fault/;
	ok( $success, "insert() child DP 2 returned: $id");
	if ($success) {
		$id =~ /java.lang.Long\|(\d+)/;
		$foundChildDP2 = $dpAccess->findById($1, 'false');
	}
	
	$id = $dpAccess->insert($parentDP);
	$success = defined $id && $id !~ /Fault/;
	ok( $success, "insert() parent DP returned: $id");
	if ($success) {
		$id =~ /java.lang.Long\|(\d+)/;
		$foundParentDP = $dpAccess->findById($1, 'false');
	}
	
	#
	# Add children to the parent
	#
	my $ret = $foundParentDP->addChildDataProducer($foundChildDP1);
	$success = defined $ret && $ret !~ /Fault/;
	ok( $success, "addChildDataProducer() for DP 1 returned: $ret");

	$ret = $foundParentDP->addChildDataProducer($foundChildDP2);
	$success = defined $ret && $ret !~ /Fault/;
	ok( $success, "addChildDataProducer() for DP 2 returned: $ret");
	
	#
	# Find the child DataProducers from the parent
	#
	my $children = $dpAccess->findChildDataProducers($foundParentDP, 'name', 'false');
	$success = defined $children && $children !~ /Fault/;
	ok( $success, "findChildDataProducers() returned: $children");
	foreach my $child (@{$children}) {
		print "child name = " . $child->name() if $verbose;
	}
	
	#
	# Delete stuff we inserted.  
	#
	# kgomes - 2008-08-14 - I commented out the deletes as the MetadataAccessServlet no longer allows them for security reasons.
	#$dpAccess->debug(0);
	#ok( $dpAccess->delete($foundChildDP1) eq "null", "deleted foundChildDP1");
	#ok( $dpAccess->delete($foundChildDP2) eq "null", "deleted foundChildDP2");
	#ok( $dpAccess->delete($foundParentDP) eq "null", "deleted foundParentDP");

} # End childDataProducerTests()


#--------------------------------------------------------------------------------------
# DataProducer with input(s) and output(s) tests

sub dataProducerWithInputOutputTests {
		
	my $dpAccess = new SSDS::DataProducerAccess();
	my $dcAccess = new SSDS::DataContainerAccess();
	print "\nRunning DataProducer with input(s) and output(s) tests on server " . $dpAccess->ssdsServer . "\n";
		
	#
	# DataContainers must have a URL as this field is used as a key, insert them then
	# Retreive the DataContainers from the database so that we get ID attributes set
	#
	my $inputDC = new SSDS::DataContainer();
	my $inputFileName = "input File";
	my $inputFileURL = "http://foo.bar/input_" . rand();
	$inputDC->name($inputFileName);
	$inputDC->uriString($inputFileURL);
	my $id = $dcAccess->insert($inputDC);
	$success = defined $id && $id !~ /Fault/;
	ok($success, "insert input DataContainer returned: $id");
	ok( $id =~ /java.lang.Long\|(\d+)/, "input DataContainer id = $1");
	my $foundInputDC = $dcAccess->findById($1, 'false');
	$success = defined $foundInputDC && $foundInputDC !~ /Fault/;
	ok( $success, "foundInputDC = $foundInputDC");
	if ($success) {
		printAttributes($foundInputDC) if $verbose;
		ok( $foundInputDC->uriString() eq $inputFileURL, "found input DataContainer, url = $inputFileURL");
	}
		
	my $outputDC = new SSDS::DataContainer();
	my $outputFileName = "output File";
	my $outputFileURL = "http://foo.bar/output_" . rand();
	$outputDC->name($outputFileName);
	$outputDC->uriString($outputFileURL);
	$id = $dcAccess->insert($outputDC);
	$success = defined $id && $id !~ /Fault/;
	ok( $success, "insert output DataContainer returned: $id");
	ok( $id =~ /java.lang.Long\|(\d+)/, "output DataContainer, id = $1");
	my $foundOutputDC = $dcAccess->findById($1, 'false');
	$success = defined $foundOutputDC && $foundOutputDC !~ /Fault/;
	ok( $success, "foundOutputDC = $foundOutputDC");
	if ($success) {
		printAttributes($foundOutputDC) if $verbose;
		ok( $foundOutputDC->uriString() eq $outputFileURL, "found output DataContainer, url = $outputFileURL");
	}
	
	#
	# Create DataProducer, insert it, and retrieve it so that we have an id set
	#
	my $newDP = new SSDS::DataProducer();
	my $name = "My new ProcessRun";
	$newDP->name($name);
	$newDP->description("A DataProducer that has an input and an output");
	$id = $dpAccess->insert($newDP);
	$success = defined $id && $id !~ /Fault/;
	ok( $success, "insert DataProducer returned an id: $id");
	if ($success) {
		ok( $id =~ /java.lang.Long\|(\d+)/, "insert() returned id = $1");
		$foundDPID = $1
	}
	
	my $foundDP = $dpAccess->findById($foundDPID, 'false');
	$success = defined $foundDP && $foundDP !~ /Fault/;
	ok( $success, "foundDP returned: $foundDP");
	if ($success) {
		printAttributes($foundDP) if $verbose;
		ok( $foundDP->name() eq $name, "found DataProducer, name = $name");
	}
	
	#
	# Add input and output, objects must exist in the database
	#
	my $ret = $foundDP->addInput($foundInputDC);
	$success = defined $ret && $ret !~ /Fault/;
	ok( $success, "addInput id = " . $foundInputDC->id . " to DataProducer id = " . $foundDP->id);
		
	$ret = $foundDP->addOutput($foundOutputDC);
	$success = defined $ret && $ret !~ /Fault/;
	ok( $success, "addOutput id = " . $foundOutputDC->id . " to DataProducer id = " . $foundDP->id);
	

	my $dpsIn = $dcAccess->findInputsByDataProducer($foundDP, "name", 'true');	# get full object graph
	$success = defined $dpsIn && $dpsIn !~ /Fault/;
	ok( $success, "findInputsByDataProducer() of DataProducer returned: $dpsIn");
	if ($success) {
		printAttributesOfCollection($dpsIn) if $verbose;
		ok( $inputFileName eq ${$dpsIn}[0]->name(), "findInputsByDataProducer()");
	}
	
	my $dpsOut = $dcAccess->findOutputsByDataProducer($foundDP, "name", 'false');
	$success = defined $dpsOut && $dpsOut !~ /Fault/;
	ok( $success, "findOutputsByDataProducer() of DataProducer returned: $dpsOut");
	my $foundOutputDC2;
	if ($success) {
		printAttributesOfCollection($dpsOut) if $verbose;
		$foundOutputDC2 = ${$dpsOut}[0];
		ok( $outputFileName eq $foundOutputDC2->name(), "findOutputsByDataProducer()");
	}
	
	#
	# Test ability to get the creator of the DataContainer (with getCreator() and findByOutput())
	#
	my $creator = $foundOutputDC2->getCreator();
	$success = defined $creator && $creator  !~ /Fault/;
	if ($success) {
		ok( $creator->id == $foundDP->id, "foundOutputDC2->getCreator found for DataProducerID_FK = " . $creator->id );
	}
	else {
		ok( 0, "dpAccess->findByOutput creator is set, DataContainer id = " . $foundOutputDC2->id );
	}

	$creator = $dpAccess->findByOutput($foundOutputDC2, 'name', 'false');
	$success = defined $creator && $creator  !~ /Fault/;
	if ($success) {
		ok( $creator->id == $foundDP->id, "dpAccess->findByOutput creator is set with DataProducerID_FK = " . $creator->id );
	}
	else {
		ok( 0, "dpAccess->findByOutput creator is set, DataContainer id = " . $foundOutputDC2->id );
	}

	#
	# Now this test is getting long...
	# Create another DataProducer with the last outputDC set as an input and see if its 
	# original creator is preserved. [Problem encountered while processing M0 data 9 Feb 2006.]
	#
	
	#
	# Insert new Data Producer 
	#
	my $newDP2 = new SSDS::DataProducer;
	$newDP2->name("My DP with input that has a creator");
	$id = $dpAccess->insert($newDP2);
	$success = defined $id && $id !~ /Fault/;
	ok( $success, "insert DataProducer2 returned an id: $id");
	if ($success) {
		ok( $id =~ /java.lang.Long\|(\d+)/, "insert() DP2 returned id = $1");
		$foundDP2ID = $1
	}
	my $foundDP2 = $dpAccess->findById($foundDP2ID, 'false');
	$success = defined $foundDP2 && $foundDP2 !~ /Fault/;
	ok( $success, "foundDP2 returned: $foundDP2");
	
	#
	# Add input that is the output of foundDP, retreive it and check its creator
	#
	$ret = $foundDP2->addInput($foundOutputDC);
	$success = defined $ret && $ret !~ /Fault/;
	ok( $success, "addInput id = " . $foundOutputDC->id . " to DataProducer 2 id = " . $foundDP2->id);
		
	my $foundFoundInputDC = $dcAccess->findById($foundOutputDC->id, 'false');
	$success = defined $foundFoundInputDC && $foundFoundInputDC !~ /Fault/;
	ok( $success, "foundFoundInputDC returned: $foundFoundInputDC");
	
	my $inputCreator = $foundFoundInputDC->getCreator;
	$success = defined $inputCreator && $inputCreator !~ /Fault/;
	ok( $success, "foundFoundInputDC->getCreator returned after consumer assigned");
	if ($success) {
		ok( $inputCreator->id == $foundDPID, "input DC has creator with ID = " . $foundDPID);
	}
	
	
	
	#
	# Delete DataContainers and DataProducers we inserted.  Because of relational
	# constraints we must remove DataProducer before the output DataContainer.
	#
	# kgomes - 2008-08-13 - I commented out the deletes as the MetadataAccessServlet no longer allows them for security reasons.
	#ok( $dpAccess->delete($foundDP2) eq "null", "deleted DataProducer 2");
	#ok( $dpAccess->delete($foundDP) eq "null", "deleted DataProducer");
	#ok( $dcAccess->delete($foundInputDC) eq "null", "deleted input DataContainer");
	#ok( $dcAccess->delete($foundOutputDC) eq "null", "deleted output DataContainer");
	

	#my $dps2 = $dpAccess->findByName($name);
	#ok( ! defined $dps2, "$name no longer in the database as determined by findByName(). (Used findByName with one argument.)");
	
	
} # End dataProducerWithInputOutputTests()


#--------------------------------------------------------------------------------------
# Test startDateAsEsec() and endDateAsEsecs() functions and test update() on Access

sub dataProducerEsecsAndUpdateTests {
	
	my $dpAccess = new SSDS::DataProducerAccess();
	print "\nRunning startDateAsEsec(), endDateAsEsecs() and  update()tests on server " . $dpAccess->ssdsServer . "\n";
	
	my $dp = new SSDS::DataProducer();

	$dp->name("DataProducer with time to update");
	
	$dp->startDate("1970-01-01T08:00:00");


	
	my $id = $dpAccess->insert($dp);
	my $success = defined $id && $id !~ /Fault/;
	my $foundId;
	ok( $success, "insert() DP returned: $id");
	if ($success) {
		$id =~ /java.lang.Long\|(\d+)/;
		$foundId = $1;
		$foundDP = $dpAccess->findById($foundId, 'false');
	}
	ok($foundDP->name() eq "DataProducer with time to update", "Found inserted DataProducer");
	
	my $ret = $foundDP->startDateAsEsecs();
	print "dataProducerEsecsAndUpdateTests(): ret = $ret\n";
	ok($ret == 28800, "startDateAsEsecs is 28800 for startDate 1970-01-01T08:00:00");
	
	#
	# Change some attributes of the DataProducer that is in the database
	#
	$foundDP->name("Updated name for DP id $foundId");
	$foundDP->startDate("2006-01-20T00:00:00");
	print "After changing the name and time on the returned DP, they are: " . $foundDP->name() . ", " . $foundDP->startDate() . "\n";
	
	#
	# Update it in the database
	#
	$id = $dpAccess->update($foundDP);
	ok($success, "update() DP returned: $id");
	if ($success) {
		$id =~ /java.lang.Long\|(\d+)/;
		$foundId = $1;
		$foundDP = $dpAccess->findById($foundId, 'false');
	}
	ok($foundDP->name() eq "Updated name for DP id $foundId", "Found updated DataProducer");

	ok($foundDP->startDateAsEsecs() == 1137715200, "startDateAsEsecs is 1137715200 for 2006-01-20T00:00:00");
	
	#
	# Delete stuff we inserted.  
	#
	# kgomes - 2008-08-14 - I commented out the deletes as the MetadataAccessServlet no longer allows them for security reasons.
	#ok( $dpAccess->delete($foundDP) eq "null", "deleted DP");


} # End dataProducerEsecsAndUpdateTests()


#--------------------------------------------------------------------------------------
# Test Getting RecordVariables from a DataContainer

sub recordVariableTests {
	
	print "\nRunning test of getting RecordVariables from DataContainer\n";
	
	my $dcAccess = new SSDS::DataContainerAccess();
	
	my $dc = new SSDS::DataContainer();
	my $fileName = "A File with RecordVariables";
	my $fileURL = "http://foo.mbari.bar/file_" . rand();
	$dc->name($fileName);
	$dc->uriString($fileURL);
	
	#
	# Insert the DataContainer and retrieve it so that we get ID attributes set
	#
	$id = $dcAccess->insert($dc);
	my $success = defined $id && $id !~ /Fault/;
	ok( $success, "insert() DC returned: $id");
	my $foundDcId;
	if ($success) {
		$id =~ /java.lang.Long\|(\d+)/;
		$foundDcId = $1;
		$foundDc = $dcAccess->findById($foundDcId, 'false');
	} else {
		carp "Failed to insert DataContainer with uriString = " . $dc->uriString() . "\n";
	}
	
	#
	# Create some RecordVariables
	#
	my $rv1 = new SSDS::RecordVariable();
	$rv1->name('Temperature');
	$rv1->units('Deg. C');
	my $rv2 = new SSDS::RecordVariable();
	$rv2->name('Salinity');
	$rv2->units('');
	
	#
	# Add the RecordVariables to the found DataContainer
	#
	my $ret = $foundDc->addRecordVariable($rv1);
	$success = defined $ret && $ret !~ /Fault/;
	ok( $success, "addRecordVariable() rv1 returned: $ret");
	$ret = $foundDc->addRecordVariable($rv2);
	$success = defined $ret && $ret !~ /Fault/;
	ok( $success, "addRecordVariable() rv2 returned: $ret");
	
	#
	# Retrieve DataContainer and see if it has the RecordVariables
	#
	my $foundDc2 = $dcAccess->findById($foundDcId, 'false');
	$rrvs = $foundDc2->getRecordVariables;
	my $foundRv1 = 0;
	my $foundRv2 = 0;
	foreach my $rv ( @{$rrvs} ) { 
		$foundRv1 = 1 if  $rv1->name eq $rv->name;
		$foundRv2 = 1 if  $rv2->name eq $rv->name;
    }
	ok($foundRv1, "foundDc2 has rv1: " . $rv1->name);
	ok($foundRv2, "foundDc2 has rv2: " . $rv2->name);
	
	#
	# Delete stuff we inserted.  
	#
	# kgomes - 2008-08-14 - I commented out the deletes as the MetadataAccessServlet no longer allows them for security reasons.
	#ok( $dcAccess->delete($foundDc2) eq "null", "deleted DC");
	
} # End recordVariableTests()


#--------------------------------------------------------------------------------------
# Test calling makePersitent() instread of insert()

sub makePersistentTests {
	
	print "\nRunning test of using makePersitent() instead of insert()\n";
	
	my $dcAccess = new SSDS::DataContainerAccess();
	
	my $dc = new SSDS::DataContainer();
	my $fileName = "A File with RecordVariables";
	my $fileURL = "http://foo.mbari.bar/file_" . rand();
	$dc->name($fileName);
	$dc->uriString($fileURL);
	
	#
	# Delete any existing DataContainer with uriString == http://foo.mbari.bar/file
	#
	# kgomes - 2008-08-14 - I commented out the deletes as the MetadataAccessServlet no longer allows them for security reasons.
	#$dcAccess->delete($dc);
	
	#
	# Insert the DataContainer and retrieve it so that we get ID attribute set
	#
	my $id = $dcAccess->insert($dc);
	my $success = defined $id && $id !~ /Fault/;
	ok( $success, "insert() DC returned: $id");
	my $foundDcId;
	if ($success) {
		$id =~ /java.lang.Long\|(\d+)/;
		$foundDcId = $1;
		$foundDc = $dcAccess->findById($foundDcId, 'false');
	} else {
		carp "Failed to insert DataContainer with uriString = " . $dc->uriString() . "\n";
	}
	
	#
	# Now call makePersitent() and see if the record is still there
	#
	$id = $dcAccess->makePersistent($foundDc);
	$success = defined $id && $id !~ /Fault/;
	ok( $success, "makePersitent() DC returned: $id");
	if ($success) {
		$foundDc2 = $dcAccess->findById($foundDcId);
		ok($fileURL eq $foundDc2->uriString, "Found DataContainer with id = $foundDcId after makePersitent()");
	} else {
		carp "Failed makePersistent() call with DataContainer uriString = " . $foundDc->uriString() . "\n";
	}
	
	#
	# Delete stuff we inserted.  
	#
	# kgomes - 2008-08-14 - I commented out the deletes as the MetadataAccessServlet no longer allows them for security reasons.
	#ok( $dcAccess->delete($foundDc2) eq "null", "deleted DC");
	
	
} # End makePersistentTests()


#--------------------------------------------------------------------------------------
# Test insert()ing objects with "weird" characters in the attributes

sub specialCharacterTests {

	print "\nRunning test specialCharacterTests\n";
	
	my $swAccess = new SSDS::SoftwareAccess();
	my $sw = new SSDS::Software();
	my $webCVSbase = 'http://moonjelly.shore.mbari.org/cgi-bin/cvsweb.cgi/DPforSSDS/';
	my $version = "1.4";
	my $uriStr = $webCVSbase . "survey2netcdf.m?rev=" . $version . rand();
	$sw->name("imctd.pl");
	$sw->softwareVersion($version);
	$sw->uriString($uriStr);
	$id = $swAccess->makePersistent($sw);
	$success = defined $id && $id !~ /Fault/;
	ok( $success, "makePersitent() SW returned: $id");
	if ($success) {
		$id =~ /java.lang.Long\|(\d+)/;
		$foundSWId = $1;
		$foundSW = $swAccess->findById($foundSWId);
		
		#
		# Delete stuff we inserted.  
		#
	    # kgomes - 2008-08-14 - I commented out the deletes as the MetadataAccessServlet no longer allows them for security reasons.
		#$swAccess->delete($foundSW);
	} else {
		carp "Failed to insert Software with uriString = " . $uriStr . "\n";
	}
	
        
} # End specialCharacterTests()


#--------------------------------------------------------------------------------------
# Test the performance of querying for a top level mooring deployment 

sub queryPerformanceTest {
	
	$dpAccess = new SSDS::DataProducerAccess();
	
	my $deplName = 'CIMT Mooring Deployment';
	print "Looking for Deployment(s) named like '$deplName' to test performance (> 3 sec is not ok)...\n";
	my $qStart = time;
	##$dpAccess->debug(3);
	my $moorDepl = $dpAccess->findByName($deplName, 'false', 'name', 'false');
	##$dpAccess->debug(0);
	my $qEnd = time;
	my $elapsedTime = $qEnd - $qStart;
	ok ($elapsedTime < 100, "Found $deplName in $elapsedTime seconds (> 100 sec is not ok).");
	
	$qStart = time;
	processDeployments($moorDepl);
	$qEnd = time;
	$elapsedTime = $qEnd - $qStart;
	ok ($elapsedTime < 100, "Processed all child deployments in $elapsedTime seconds (> 100 sec is not ok).");

} # queryPerformanceTest
	

#------------------------------------------------------------------------------
# Recursively get child DataProducers from the passed DataProducer and
# pass off Instrument role Deployments.

sub processDeployments {

        my $depls = $_[0];

	my $formatStr = "%s %-13s \t %-28s \t %-s\n";
	my $verbose = 1;
	
        my $lastRole = '';
        $indent .= "  ";
        ##print "processDeployments(): looping over some depls = " . $depls . "...\n";
        foreach my $d ( @{$depls} ) {

                $monYr = POSIX::strftime("%b %Y", POSIX::gmtime(($d->startDateAsEsecs)
                        ? $d->startDateAsEsecs : 0) );
                my $device = $d->getDevice;
                $betterDeplName = $device->name . " - " . $monYr;
                my $role = (defined $d->role) ? $d->role : '';
                my $name = (defined $d->name) ? $d->name : "SSDS ID " . $device->id;
                my $platformYYYYMM;

                if ( $role eq 'platform' ) {
                        print "\n" if $verbose;
                        print $indent . "\"" . $name . "\" as $role ($betterDeplName)\n" if $verbose;

                }
                elsif ( $role eq 'instrument' ) {
                        if ( $lastRole ne 'instrument' ) {
                                printf $formatStr, "\n$indent", "deviceType",
                                        "deployment.name", "device.name - deployment.start"
                                        if $verbose;
                                printf $formatStr, $indent, "__________",
                                        "_______________", "______________________________"
                                        if $verbose;
                                print "\n" if $verbose;
                        }

                        ##processInstrumentDeployment($d);
                }
                elsif ( $role eq 'sensor' ) {
                        my $serNum = $d->getDevice->mfgSerialNumber;
                        my $deplDepth = ($d->nominalDepth) ? $d->nominalDepth : '';
                        if ($verbose) {
                                print $indent . "\"" . $name . "\" with serial number $serNum as $role ";
                                print "at $deplDepth m " if $deplDepth;
                                print "($betterDeplName)\n";
                        }
                }

		
		my $qStart = time;
		##$dpAccess->debug(3);
		my $ret = $dpAccess->countFindChildDataProducers($d, 'startDate', 'false');
                ##print "ret = $ret\n";
                $ret =~ /java.lang.Integer\|(\d+)/;
                my $childCount = $1;
                next unless $childCount;
                
                my $childDepls = $dpAccess->findChildDataProducers($d, 'startDate', 'false');
                ##$dpAccess->debug(0);
                my $qEnd = time;
                my $elapsedTime = $qEnd - $qStart;
                my $numChildren = $#$childDepls + 1;
                $nSecs = ($numChildren > 10) ? 7 : 3;
		ok ($elapsedTime < $nSecs, "Found " . $numChildren . " children of $name in $elapsedTime seconds. (> $nSecs sec is not ok)");

    		processDeployments($childDepls);

                $lastRole = $role;

        }
        $indent =~ s/  $//;

} # End processDeployments()


#--------------------------------------------------------------------------------------
# Test of the function that would be used from an application (add this to SSDS.pm?)
# [Tried to add to SSDS.pm and a SSDS_Util.pm with no joy.  Keep it here for now.]
	
sub addProcessRunToSSDSTests {
	
	print "\nRunning test of addProcessRunToSSDS()\n";
	
	my $inputDC = new SSDS::DataContainer();
	my $inputFileName = "input File";
	my $inputFileURL = "http://foo.mbari.bar/input_" . rand();
	$inputDC->name($inputFileName);
	$inputDC->uriString($inputFileURL);
		
	my $outputDC = new SSDS::DataContainer();
	my $outputFileName = "output File";
	my $outputFileURL = "http://foo.mbari.bar/output_" . rand();
	$outputDC->name($outputFileName);
	$outputDC->uriString($outputFileURL);
	
	my $newDP = new SSDS::DataProducer();
	my $name = "My new ProcessRun";
	my $desc = "A DataProducer that has an input and an output";
	$newDP->name($name);
	$newDP->description($desc);
	
	#
	# Add the DataProducer (ProcessRun)
	#
	addProcessRunToSSDS($inputDC, $newDP, $outputDC);
	
	#
	# Now find these things to confirm that they got inserted
	#
	my $dpAccess = new SSDS::DataProducerAccess();
	my $dcAccess = new SSDS::DataContainerAccess();
	
	my $foundInputDC = $dcAccess->findByURIString($inputFileURL);
	ok($foundInputDC->name() eq $inputDC->name(), "inserted input DC name matches");
	my $foundOutputDC = $dcAccess->findByURIString($outputFileURL);
	ok($foundOutputDC->name() eq $outputDC->name(), "inserted output DC name matches");
	my $foundDPs = $dpAccess->findByName($name, 'true', "name", 'false');	# This is not unique
	my $foundDP;
	foreach my $fdp (@{$foundDPs}) {		# Being extra careful
		$foundDP = $fdp;
		last if $fdp->description() eq $desc;
	}
	ok($foundDP->name() eq $newDP->name(), "inserted DP name matches");
	
	
	#
	# Not typically done from a data processing program, but we need to remove these 
	# things in this test program.  Because of relational
	# constraints we must remove DataProducer before the output DataContainer.
	#
	# kgomes - 2008-08-14 - I commented out the deletes as the MetadataAccessServlet no longer allows them for security reasons.
	#ok( $dpAccess->delete($foundDP) eq "null", "deleted DataProducer");
	#ok( $dcAccess->delete($foundInputDC) eq "null", "deleted input DataContainer");
	#ok( $dcAccess->delete($foundOutputDC) eq "null", "deleted output DataContainer");
	
	
} # End addProcessRunToSSDSTests()


#--------------------------------------------------------------------------------------
# Utility function that makes it easy to add a ProcessRun to SSDS
# Copy this function into data processing software to make it easy to add data to SSDS.

sub addProcessRunToSSDS {
	
	#
	# Handle all of the rigamarol of finding inserted objects so that we have IDs
	#
	my ($inputDC, $dP, $outputDC) = @_;
	my $id;
	my $foundInputDC;
	my $foundDP;
	my $foundOutputDC;
	
	my $dpAccess = new SSDS::DataProducerAccess();
	my $dcAccess = new SSDS::DataContainerAccess();
		
	#
	# DataContainers must have a URL as this field is used as a key
	#
	if ( ! $inputDC->uriString() ) {
		carp "Must have uriString set on input DataContainer.\n";
	}
	if ( ! $outputDC->uriString() ) {
		carp "Must have uriString set on output DataContainer.\n";
	}

	#
	# Insert the DataContainers and retreive them so that we get ID attributes set
	#
	$id = $dcAccess->insert($inputDC);
	my $success = defined $id && $id !~ /Fault/;
	if ($success) {
		$id =~ /java.lang.Long\|(\d+)/;
		$foundInputDC = $dcAccess->findById($1, 'false');
	} else {
		carp "Failed to insert DataContainer with uriString = " . $inputDC->uriString() . "\n";
	}

	$id = $dcAccess->insert($outputDC);
	$success = defined $id && $id !~ /Fault/;
	if ($success) {
		$id =~ /java.lang.Long\|(\d+)/;
		$foundOutputDC = $dcAccess->findById($1, 'false');
	} else {
		carp "Failed to insert DataContainer with uriString = " . $outputDC->uriString() . "\n";
	}
	
	#
	# Insert DataProducer and retreive it so that we have an id set
	#
	$id = $dpAccess->insert($dP);
	$success = defined $id && $id !~ /Fault/;
	if ($success) {
		$id =~ /java.lang.Long\|(\d+)/;
		$foundDP = $dpAccess->findById($1, 'false');
	}
	
	#
	# Add input and output, objects must exist in the database
	#
	my $ret = $foundDP->addInput($foundInputDC);
	$success = defined $ret && $ret !~ /Fault/;
	carp "Failed to add input DataContainer.\n" unless $success;

		
	$ret = $foundDP->addOutput($foundOutputDC);
	$success = defined $ret && $ret !~ /Fault/;
	carp "Failed to add output DataContainer.\n" unless $success;
	
} # End addProcessRunToSSDS()

#----------------------------------------------------------------
# This was not working in DStNetCDF.pl, test it here.  (Actually,
# it was working.  There was just one parent platform DP, and
# I thought there were 2 separate ones.)

sub findParentWithRoleTest {
	
	print "\nRunning test findParentWithRoleTest\n";
	
	my $dpAccess = new SSDS::DataProducerAccess();
	
	my $name = "ID 1338 deployed on id 1327";
	my $rdepls = $dpAccess->findByName($name);
	print "findParentWithRoleTest(): Will look up hierarchy of DataProducers for DP with attributes:\n";
	foreach my $d ( @{$rdepls} ) {
		printAttributes($d);
		$depl = $d;
	}
	
	print "findParentWithRoleTest(): Looking for parent that has role 'platform'\n";
	$pd = findParentWithRole($depl, 'platform');
	ok($pd->role eq 'platform', "Found parent '" . $pd->name . "' with role 'platform'");
	printAttributes($pd);
	
} # End findParentWithRoleTest()


#----------------------------------------------------------------
# Look up Deployment tree to find the specified attribute
sub findParentWithRole {

        my ($depl, $role) = @_;

        #
        # If role of $depl is $role, that's great, return $depl
        # otherwise check for the role in the parent deployment, and
        # so on.
        #
        carp "findParentWithRole(): \$depl is not defined\n" unless $depl;
        my $deplToReturn = $depl;
        while ( $role ne $deplToReturn->role ) {
                $deplToReturn = $deplToReturn->getParentDataProducer;
                print "findParentWithRole(): parent name = " . $deplToReturn->name . "\n";
                if ( ! $deplToReturn ) {
                        print "findParentWithRole(): Could not find DataProducer with role of $role ";
                        print "in ancestry of " . $depl->name . "\n";
                        last;
                }
        }

        return $deplToReturn;

} # End findParentWithRole()

#----------------------------------------------------------------
# Test setting Software on ProcessRun.

sub processRunSoftwareTest {
	
	my $dpAccess = new SSDS::DataProducerAccess();
	
	my $debug = 0;
	
	#
	# Build Software object 
	#
	my $swAccess = new SSDS::SoftwareAccess();
	my $sw = new SSDS::Software();
	print "processRunSoftwareTest(): Will insert a Software and ProcessRun and set it\n";
	
	$sw->name("Software foobar");
	$sw->softwareVersion("1.0");
	$sw->uriString("http://foo.bar" . rand());
	my $ret = $swAccess->makePersistent($sw);
	$success = defined $ret && $ret !~ /Fault/;
	carp "Failed to makePersistent Software.\n" unless $success;
	$ret =~ /java.lang.Long\|(\d+)/;
	my $sw_id = $1;
	$software = $swAccess->findById($sw_id, 'false');
	ok ($software->id =~ /\d+/, "Inserted Software with id = " . $software->id);
	
	#
	# Build ProcessRun (DataProducer) object
	#
	my $prAccess = new SSDS::DataProducerAccess();
	my $pr = new SSDS::DataProducer();
	$pr->name("PR that has a Software");
	$pr->dataProducerType("ProcessRun");
	$ret = $prAccess->makePersistent($pr);
	$success = defined $ret && $ret !~ /Fault/;
	carp "Failed to makePersistent Software.\n" unless $success;
	$ret =~ /java.lang.Long\|(\d+)/;
	$pr_id = $1;
	$processRun = $prAccess->findById($pr_id, 'false');
	ok ($processRun->id =~ /\d+/, "Inserted ProcessRun with id = " . $processRun->id);
	
	#
	# Add objects to the persisted ProcessRun
	#
	print "processRunSoftwareTest (): For ProcessRun name, id = " . $processRun->name . ", " . $processRun->id . "\n" if $debug;
	print "processRunSoftwareTest (): Setting Software name, id = " . $software->name . ", " . $software->id . "\n" if $debug;
	$ret = $processRun->setSoftware($software);
	$success = defined $ret && $ret !~ /Fault/;
	carp "Failed to setSoftware.\n" unless $success;
	
	#
	# Check that software is now set on the PR
	#
	$pr2 = $prAccess->findById($pr_id, 'false');
	$success = defined $pr2 && $pr2 !~ /Fault/;
	carp "Failed to find ProcessRun with id = $pr_id\n" unless $success;
	ok($sw_id == $pr2->getSoftware->id, "Software is set in persisted ProcessRun");
	
	#
	# Must delete PR first cause it has a FK to Software.
	#
	# kgomes - 2008-08-14 - I commented out the deletes as the MetadataAccessServlet no longer allows them for security reasons.
	#ok( $prAccess->delete($processRun) eq "null", "deleted processRun");
	#ok( $prAccess->delete($software) eq "null", "deleted software");
	
} # End processRunSoftwareTest()

#--------------------------------------------------------------------------------------
#
sub deviceTests {
	#==============================================================================
	# Device tests
	#==============================================================================
	my $deviceAccess = new SSDS::DeviceAccess();
	print "Getting metadata from server " . $deviceAccess->ssdsServer . "\n";
	print "\nTestA1: Calling deplAccess->findById(1417) to get a single Device\n";
	##$deviceAccess->debug(1);
	##$deviceAccess->delim('~');
	my $device = $deviceAccess->findById(1417, 'false');
	my @deviceAttr = ($device->get_attribute_names());
	foreach my $a (@deviceAttr) {
	        next unless $device->$a;
	        print "  $a = " . $device->$a . "\n";
	}
} # End deviceTests()



