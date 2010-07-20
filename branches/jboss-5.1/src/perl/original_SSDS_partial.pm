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
package SSDS;

use strict;
use Carp;


#
# Private variables
#
my $_ssdsServer = '@CLIENT_TOOL_PERL_MODULE_HTTP_SERVER_BASE@';
my $_baseServlet = 'servlet/MetadataAccessServlet?responseType=text&';
my $_delim = '|';
my $_baseUrl = $_ssdsServer . $_baseServlet . "&delimiter=" . $_delim . "&";
my $_debug = 0;

my %_debugLevel = ( ERROR => 1, WARN => 2,  INFO => 3 );

=head1 SSDS

SSDS.pm - Access and modify SSDS Metadata

=head1 SYNOPSIS

	use SSDS;
	
	$ssds = new SSDS();
	$ssds->ssdsServer('@CLIENT_TOOL_PERL_MODULE_HTTP_SERVER_BASE@');
	
	$dpAccess = new SSDS::DataProducerAccess();
	
	$newDP = new SSDS::DataProducer();
	$name = "My new DataProducer";
	$newDP->name($name);
	$newDP->description("A description of a new DataProducer for testing");

	$id = $dpAccess->insert($newDP);
	
	$dps = $dpAccess->findByName($name, 'true', "name", 'false');
	@attrs = ${$dps}[0]->get_attribute_names();
	foreach my $obj (@{$dps}) {
		foreach my $a (@attrs) {
		        next unless $obj->$a;
		        print "  $a = " . $obj->$a . "\n";
		}
	}
	
	$ret = $dpAccess->delete($newDP);	
        	
	Will print out:
	
	  id = <an integer id>
	  name = My new DataProducer
	  description = A description of a new DataProducer for testing


=head1 DESCRIPTION

The SSDS class provides a Perl interface to the SSDS metadata services
provided by the SSDS Web Services.  Most all of the Java interface to 
the SSDS metadata and service objects is implemented here.  This module 
was written to make it easy to write external data processing scripts 
for SSDS data.  (Please refer to the testSSDS.pl script that is included 
in the .zip file of this distribution for examples of using this module.)

The syntax used to dereference the return values from the Access methods
looks a little hairy, but it's really not that bad.  If the method is designed
to return a single object (such as C<findByPK()>) then the return value will 
be an object which you may immediatley use to call its attributes.  
If the method returns multiple objects (such as
C<listOutputs()>) then the return value will be a reference to a list of
objects, which you must dereference with a '$' or '@' before using it.  
Plenty of examples are provided in the method descriptions of this document.


The SSDS package contains these methods which are inherited by all the SSDS objects declared in the other packages.

=over 4

=item *
ssdsServer() - set to base SSDS server, e.g. "http://ssds.shore.mbari.org:8080/"

=item *
baseServlet() - set to the MetadataAccessServlet, e.g. 'access/MetadataAccessServlet?replyInHTML=false&';

=item *
baseUrl() - the concatenation of ssdsServer and baseServlet

=item *
delim() - the Delimiter to use for field separation of HTTP responses from SSDS, e.g. '|';

=item *
debug() - set it to 1 for ERROR, 2 for WARN, 3 for INFO, 0 for no output (the default).
It's a good idea to run with debug set to 2, then set it to 0 when put into production.

=item *
get_attribute_names() - Inherited from the Class::ObjectTemplate module which may need to be installed on your system.

=back

=head2 Base class with private methods

=cut


sub new {
	my ($class) = @_;
	my $self = {};
	bless $self, $class;
}

sub ssdsServer { shift; @_ ? $_ssdsServer = shift : $_ssdsServer;}

sub baseServlet { shift; @_ ? $_baseServlet = shift : $_baseServlet;}

sub baseUrl { 
	shift; 
	@_ ? $_baseServlet = shift : $_ssdsServer . $_baseServlet . "delimiter=" . $_delim . "&";
}

sub delim { shift; @_ ? $_delim = shift : $_delim;}

sub debug { shift; @_ ? $_debug = shift : $_debug;}



#====================================================================
#                             ObjectCreator
#====================================================================
package SSDS::ObjectCreator;
use LWP::Simple;
use strict;
use Carp;
our @ISA = qw(SSDS);

=head3 ObjectCreator

ObjectCreator - Contains private support methods to create SSDS objects.  The methods below are inherited by all the SSDS objects declared in the child packages.


=over 4


#--------------------------------------------------------------------
#

=item B<new()>
	 
Empty constructor.

=cut

sub new {
	my $pkg = shift;
	bless {}, $pkg;
}

#--------------------------------------------------------------------
#

=item B<_buildHash()>
	 
Function to parse return of MetadataAccessServlet into a single (key, value) hash list.

Example:

	my ($returnedObject, $oHash) = $obj->_buildHash($obj->delim, $data);

=cut

sub _buildHash {
	
	my $obj = shift;
	my $delim = shift;
	my $servletResponse = shift;
	
	my %hash = ();
	
	my ($k, $v, $ssdsObjectName);
	
	$delim = '\|' if $delim eq '|';
	print "_buildHash(): delimiter set to $delim" if $obj->debug == $_debugLevel{'INFO'};
	
	foreach my $line (split('\n', $servletResponse)) {
		print "_buildHash(): line = $line\n" if $obj->debug == $_debugLevel{'INFO'};
		if ( $line =~ /^Fault:$/ ) {
			if ( $obj->debug == $_debugLevel{'INFO'} || $obj->debug == $_debugLevel{'ERROR'} || $obj->debug == $_debugLevel{'WARN'} ) {
				print "$servletResponse\n";
			}
			last;
		}
		foreach my $prop ( split($delim, $line) ) {
			if ( $prop =~ /(.+?)=(.+)/ ) {
				$k = $1;
				$v = $2;
				print "_buildHash(): k = $k, v = $v\n" if $obj->debug == $_debugLevel{'INFO'};
				if ($v && $v !~ /null/) {
					$hash{${k}} = $v;
				}	
			}
			elsif ( $prop =~ /(^[A-Z].+[^=])/ ) {
				$ssdsObjectName = $prop;
				print "_buildHash(): ssdsObjectName = $ssdsObjectName\n" if $obj->debug == $_debugLevel{'INFO'};
			}
		}	
	}

	return ($ssdsObjectName, \%hash);
	
} # End _buildHash()


#--------------------------------------------------------------------
#

=item B<_buildHashes()>

Function to parse return of MetadataAccessServlet into a list of (key, value) hashes list.

Example:

	my ($returnedObject, $oHashes) = $obj->_buildHashes($obj->delim, $data);

=cut

sub _buildHashes {
	
	my $obj = shift;
	my $delim = shift;
	my $servletResponse = shift;
	
	my $rhash = {};		# Need to use a reference to the hash in order to push onto @hashes list
	my @hashes = ();
	my @foo = ();
	
	my ($k, $v, $ssdsObjectName);
	
	$delim = '\|' if $delim eq '|';
	
	foreach my $line (split('\n', $servletResponse)) {
		print "_buildHashes(): line = $line\n" if $obj->debug == $_debugLevel{'INFO'};
		if ( $line =~ /^Fault:$/ ) {
			if ( $obj->debug == $_debugLevel{'INFO'} || $obj->debug == $_debugLevel{'ERROR'} || $obj->debug == $_debugLevel{'WARN'} ) {
				print "$servletResponse\n";
			}
			last;
		}
		$rhash = {};
		foreach my $prop ( split($delim, $line) ) {
			if ( $prop =~ /(.+?)=(.*)/ ) {
				$k = $1;
				$v = $2;
				print "_buildHashes(): k = $k, v = $v\n" if $obj->debug == $_debugLevel{'INFO'};
				if ( $v && $v !~ /null/ ) {
					##print "_buildHashes(): adding to hash\n" if $obj->debug == $_debugLevel{'INFO'};
					${$rhash}{$k} = $v;
				}
			}
			elsif ( $prop =~ /(^[A-Z].+[^=])/ ) {
				$ssdsObjectName = $prop;
				print "_buildHashes(): ssdsObjectName = $ssdsObjectName\n" if $obj->debug == $_debugLevel{'INFO'};
			}	
		}
		push @hashes, $rhash;	

	}

	print "\n\nChecking final hash list before returning it...\n" if $obj->debug == $_debugLevel{'INFO'};
	foreach my $rh (@hashes) {
		print "_buildHashes(): id = ${$rh}{'id'}\n" if $obj->debug == $_debugLevel{'INFO'};
		foreach my $key (keys %{$rh}) {
			print "_buildHashes(): key = $key, value = ${$rh}{$key}\n" if $obj->debug == $_debugLevel{'INFO'};
		}
		print "\n" if $obj->debug == $_debugLevel{'INFO'};
	}
	return ($ssdsObjectName, \@hashes);
	
} # End _buildHashes()


#--------------------------------------------------------------------
# Function that returns a reference to a hash 

=item B<_createSSDSobject()>

Build and return an SSDS object given a URL to a MetadataAccessServlet call.
If a second argument is given then that SSDS object will be instantiated rather than the object returned from
the MetadataAccessServlet.  If the types do not match a warning message will be printed.

Example:

	# Request and return a Device object
	my $url = $obj->baseUrl . 
	 "className=DeviceTypeAccess&methodName=findByPK&param1Type=Long&param1Value=" .
	 $pk;	
	return $obj->_createSSDSobject($url);
	

=cut

sub _createSSDSobject {
	
	my $obj = shift;
	my $url = shift;
	my $ssdsObject = shift;
	
	print "Getting metadata from url = $url\n" if $obj->debug == $_debugLevel{'INFO'};
	my $data = get($url);
	if  ( !$data ) {
		if ( $obj->debug == $_debugLevel{'INFO'} || $obj->debug == $_debugLevel{'ERROR'} || $obj->debug == $_debugLevel{'WARN'} ) {
			print "\nERROR _createSSDSobject(): No data returned from url:\n$url\n\n";
		}
		return;
	}
	
	#
	# Pull off attributes and return an SSDS object
	# Return a $returnedObject unless $ssdsObject specified
	#
	my ($returnedObject, $oHash) = $obj->_buildHash($obj->delim, $data);
	return unless $returnedObject;
	if ( $ssdsObject && $ssdsObject ne $returnedObject ) {
		if ( $obj->debug == $_debugLevel{'INFO'} || $obj->debug == $_debugLevel{'WARN'} ) {
			print "WARNING _createSSDSobject(): returned object is $returnedObject, requested to cast to $ssdsObject\n";
		}
	}
	$ssdsObject = ($ssdsObject) ? $ssdsObject : $returnedObject;
	
	my $o = "SSDS::$ssdsObject"->new();
	print "_createSSDSobject(): $ssdsObject object:\n" if $obj->debug == $_debugLevel{'INFO'};
	print "_createSSDSobject(): --------------\n" if $obj->debug == $_debugLevel{'INFO'};
	foreach my $k (keys %{$oHash}) {
		print "_createSSDSobject(): Assigning values to object attributes: $k = " . ${$oHash}{$k} . "\n" if $obj->debug == $_debugLevel{'INFO'};
		$o->$k(${$oHash}{$k});
	}
	
	return $o;
	
} # end _createSSDSobject


#--------------------------------------------------------------------
# Function that returns a reference to a list of hash references

=item B<_createSSDSobjects()>

Build and return a list of SSDS objects given a URL to a MetadataAccessServlet call.
If a second argument is given then that SSDS object will be instantiated rather than the object returned from
the MetadataAccessServlet.  If the types do not match a warning message will be printed.

Example:

	# Request and return a Deployment 
	my $url = $obj->baseUrl . 
	 "className=Deployment&methodName=listChildDeployments&findBy=PK&findByType=String&findByValue=" . 
	 $obj->id;
	return $obj->_createSSDSobjects($url);

=cut

sub _createSSDSobjects {
	
	my $obj = shift;
	my $url = shift;
	my $ssdsObject = shift;
	
	my @os = ();	# List of SSDS objects to be returned
	
	print "Getting metadata from url = $url\n" if $obj->debug == $_debugLevel{'INFO'};
	my $data = get($url);
	if  ( !$data ) {
		if ( $obj->debug == $_debugLevel{'INFO'} || $obj->debug == $_debugLevel{'ERROR'} || $obj->debug == $_debugLevel{'WARN'} ) {
			print "\nERROR _createSSDSobject(): No data returned from url:\n$url\n\n";
		}
		return;
	}
	
	#
	# Pull off attributes and return a list of SSDS objects
	# Return $returnedObjects unless $ssdsObject specified
	#
	my ($returnedObject, $oHashes) = $obj->_buildHashes($obj->delim, $data);
	return unless $returnedObject;
	if ( $ssdsObject && $ssdsObject ne $returnedObject ) {
		if ( $obj->debug == $_debugLevel{'INFO'} || $obj->debug == $_debugLevel{'WARN'} ) {
			print "WARNING _createSSDSobject(): returned object is $returnedObject, requested to cast to $ssdsObject\n";
		}
	}
	$ssdsObject = ($ssdsObject) ? $ssdsObject : $returnedObject;
	
	
	print "_createSSDSobjects(): $ssdsObject objects:\n" if $obj->debug == $_debugLevel{'INFO'};
	print "_createSSDSobjects(): -------------------\n" if $obj->debug == $_debugLevel{'INFO'};
	foreach my $oh (@{$oHashes}) {
		my $o = "SSDS::$ssdsObject"->new();
		foreach my $k (keys %{$oh}) {
			print "_createSSDSobjects():   $k = " . ${$oh}{$k} . "\n" if $obj->debug == $_debugLevel{'INFO'};
			$o->$k(${$oh}{$k});
		}
		push @os, $o;
		
	}
	return \@os;
	
} # end _createSSDSobjects



#--------------------------------------------------------------------
# Function that returns a reference to a hash 

=item B<_execSSDSmethod()>

Execute an SSDS access method that does not return an object.  Typically this is 
used for calling set()s, insert()s, update()s, and delete()s.  Returns 1 if successful, 0
if a Fault is detected or any data is returned from the call - there should be no data returned by the service.

Example:

	my $url = $obj->baseUrl . 
	 "className=DeviceTypeAccess&methodName=insert&param1Type=DeviceType&param1Value=DeviceType";
	$url .= $obj->delim() . "name=" . $dt->name() if $dt->name();
	$url .= $obj->delim() . "description=" . $dt->description() if $dt->description();
	$url .= $obj->delim() . "defaultDeploymentRole=" . 
	 $dt->defaultDeploymentRole() if $dt->defaultDeploymentRole(); 
	$url .= $obj->delim() . "displayInDeviceTypesPicklist=" . 
	 $dt->displayInDeviceTypesPicklist() if $dt->displayInDeviceTypesPicklist();
	return $obj->_execSSDSmethod($url);

=cut

sub _execSSDSmethod {
	
	my $obj = shift;
	my $url = shift;
	my $ssdsObject = shift;
	
	print "Executing method in url = $url\n" if $obj->debug == $_debugLevel{'INFO'};
	my $data = get($url);
	
	if  ( !$data ) {
		return 1;
	}
	else {
		if ( $data =~ /^Fault:$/ ) {
			if ( $obj->debug == $_debugLevel{'INFO'} || $obj->debug == $_debugLevel{'ERROR'} || $obj->debug == $_debugLevel{'WARN'} ) {
				print "$data\n";
			}
		}
		return $data;
	}
	
} # end execSSDSmethod


#--------------------------------------------------------------------
# Function that returns a reference to a hash 

=item B<_returnSSDSinfo()>

Execute an SSDS access method that might return information.  Typically this is 
used for calling an is_ method that would return "java.lang.Boolean:true" or "java.lang.Boolean:false".

Example:


	my $obj = shift;
	my $url = $obj->baseUrl . 
	 "className=DataFile&methodName=isWebAccessible&findBy=PK&findByType=String&findByValue=" . 
	 $obj->id;
	my $ret = $obj->_returnSSDSinfo($url);		
	if ( $obj->debug == $_debugLevel{'INFO'} ) {
		print "Return from URL = $ret\n";
	}
	if ($ret =~ /true/) {
		return 1;
	}
	else {
		return 0;
	}

=cut

sub _returnSSDSinfo {
	
	my $obj = shift;
	my $url = shift;
	my $ssdsObject = shift;
	
	print "Executing method in url = $url\n" if $obj->debug == $_debugLevel{'INFO'};
	my $data = get($url);
	
	if  ( $data ) {
		return $data;
	}
	else {
		return;
	}
	
} # end _returnSSDSinfo


=back

=head2 Value and DataAccessObject classes

=cut


