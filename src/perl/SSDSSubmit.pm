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
package SSDSSubmit;

use strict;
use Carp;
use LWP::Simple;
use MIME::Base64 qw(encode_base64 decode_base64);



#
# Private variables
#
my $_ssdsServer = 'http://ssdsmac.shore.mbari.org:8080/';
my $_baseServlet = 'servlet/DataAccessServlet?';
my $_servletString1 = 'objectToInvokeOn=PacketSubmissionAccess&method=submitPacketAsByteArray&p1Type=long&p1Value=';
my $_servletString2 = '&p2Type=byte[]&p2Value=';
my $_debug = 3;  # print all messages at or below this level (recommended is '1', to get error and warning msgs)
my %_debugLevel = ( ERROR => 0, WARN => 1,  INFO => 2 ,  DEBUG => 3);
my $ERROR_RETURN = -1;
my $INT32BITS = 4294967296; # = 2**32 = biggest 32 bit int (+ 1)
my $NANOPERMICRO = 1000;  # number of nanoseconds in a microsecond


=head1 SSDSSubmit

SSDSSubmit.pm - Submit new SSDS data records

=head1 SYNOPSIS

	# Test code
	use SSDSSubmit;
	use strict;

	my $deviceId = 1001;
	my $parentId = 999;
	my $packetType = 1;
	my $packetSubType = 1;
	my $dataDescriptionId = 1;
	my $dataDescriptionVersion = 1;
	my $timestampSeconds = time();   # For test purposes, make the timestamp 'now' on every packet
	my $timestampNanoseconds = 0;
	my $sequenceNumber = 1;
	my $bufferBytes = "Buffer 1 from SSDSSubmit Test Code (by John G)";
	my $bufferLen = length($bufferBytes);

	my $bytesSubmitted = SSDSSubmit::submitSsdsRecord($deviceId,$parentId,$packetType,$packetSubType,
		$dataDescriptionId,$dataDescriptionVersion,$timestampSeconds,$timestampNanoseconds,
		$sequenceNumber,$bufferLen,$bufferBytes);
		
	if ($bytesSubmitted >= 0) {
	       print "Device $deviceId data record of $bytesSubmitted bytes submitted to SSDS.\n";
	} 
	else {
	   print "Data record from device $deviceId not successfully submitted.\n";
	}
        	
	Will submit the record to SSDS, and will print out:
	
	  Device 1001 data record of 59 bytes submitted to SSDS.


=head1 DESCRIPTION

The SSDSSubmit utilities provide a Perl interface to the SSDS data services
provided by the SSDS Web Services. This module was written to make it easy to 
submit data records into the SSDS system.

C<$Id: SSDS.pm,v 1.32 2005/10/10 22:25:44 mccann Exp $>



=head2 Declaration of methods (if needed)

=cut


###############################################
# SSDS record submission subroutine 	      #
###############################################

=head 3 submitSsdsRecord

All of the following parameters must be provided.

=over 4

=item *
deviceID - the unique device ID (from the SSDS device database) of the device or data source that generated the data

=item *
parentID - the device ID of the parent device to the submitting device

=item *
packetType - number identifying (internally to SSDS) what type of packet is being submitted: 0 = data (typical), 1 = metadata, 2 = message; 
the latter two values, and others not documented here, are specific to SIAM data records; see 

=item *
packetSubType - number identifying packet type (equivalent to SSDS and XML 'recordType'): 0 = XML metadata description packet, 
>0 = recordType (as defined in XML metadata description packet)

=item *
dataDescriptionID - the major version number from the XML file describing the unique device ID

=item *
dataDescriptionVersion - the minor version number from the XML file describing the unique device ID

=item *
sequenceNumber - an optional number identifying the sequence of this packet in the series of packets being submitted (set to 0 if not used)

=item *
timestampSeconds - number of seconds since 1/1/1970 (standard Unix epoch) at which this record was generated

=item *
timestampNanoseconds - number of nanoseconds (added to timestampSeconds) at which this record was generated

=item *
bufferBytes - number of bytes in the data packet (data record)

=item *
bufferLen - bytes that make up the data packet (data reocrd)

=cut

sub submitSsdsRecord{

	my($deviceID,$parentID,$packetType,$packetSubType,$dataDescriptionID,
		$dataDescriptionVersion,$timestampSeconds,$timestampNanoseconds,
 		$sequenceNumber,$bufferLen,$bufferBytes,$bufferTwoLen,$bufferTwoBytes)=@_;

	
	if (@_ != 11) {
	    print "Error! Wrong number of arguments (should be 11, was" .  scalar(@_). "\n";
	    return($ERROR_RETURN);
	}

	if ($_debug >= $_debugLevel{'DEBUG'}) {
	    print "\t\t\tData to be encoded is:\n" .
	    "\t\t\tdeviceID: $deviceID\n" .
		"\t\t\tparentID: $parentID\n" .
		"\t\t\tpacketType: $packetType\n" .
		"\t\t\tpacketSubType: $packetSubType\n" .
		"\t\t\tdataDescriptionID: $dataDescriptionID\n" .
	    "\t\t\tdataDescriptionVersion: $dataDescriptionVersion\n" .
	    "\t\t\ttimestampSeconds: $timestampSeconds\n" .
	    "\t\t\ttimestampNanoseconds: $timestampNanoseconds\n" .
	    "\t\t\tsequenceNumber: $sequenceNumber\n" .
	    "\t\t\tbufferLen: $bufferLen\n" .
	    "\t\t\tbufferBytes: $bufferBytes\n"  ;
	}

	# Write all the data to the byte array
	# Zeroes are inserted before all 64-bit values, since many Perls can't handle 64 bits gracefully.
	# So we front-load the 32-bit packed number with 32 bits of 0, to get the right 64-bit number.
	
	# Except we take extra care on the one number (timestampSeconds) that may someday use more than 32 bits
	my $tsSecondsHigh = int($timestampSeconds / $INT32BITS);
	my $tsSecondsLow = $timestampSeconds % $INT32BITS;

    # The pack command puts 18 numbers into 4 bytes each, and appends an ASCII string.
    # Divide the number of nanoseconds so that it turns into a number of microseconds, which is what SSDS currently expects.
	my $byteArray = pack ('N18a*',0,$deviceID,0,$parentID,$packetType,0,$packetSubType,0,$dataDescriptionID,
			0,$dataDescriptionVersion,$tsSecondsHigh,$tsSecondsLow,0,$timestampNanoseconds/$NANOPERMICRO,0,
	 		$sequenceNumber,$bufferLen,$bufferBytes);

	if ($_debug >= $_debugLevel{'DEBUG'}) {
	    my( $hex ) = unpack( 'H*', $byteArray );
	    print "\t\t\tHex after unpacking is\n$hex\n\n";
	}

	# Encode as base 64.
	# Set the line ending to "" so there are no newlines in the encoding
	my $byteArrayBase64 = encode_base64($byteArray, "");
	
	if ($_debug >= $_debugLevel{'DEBUG'}) {
	    print "\t\t\tHex after encoding is\n$byteArrayBase64 \n\n";
	}

	# But now I have to URL encode it as the Base64 encoding creates 
	# characters that are special within URLs, so they must be escaped.

	my $theUrlPacket = $byteArrayBase64;
	$theUrlPacket =~ s/([\W])/"%" . uc(sprintf("%2.2x",ord($1)))/eg;

	if ($_debug >= $_debugLevel{'DEBUG'}) {
	    print "\t\t\tHex after URL-encoding is\n$theUrlPacket\n\n" ; 
	}
	    

	my $wholeUrl = $_ssdsServer . $_baseServlet . $_servletString1 . $deviceID . $_servletString2 . $theUrlPacket;

	if ($_debug >= $_debugLevel{'DEBUG'}) {
	    print "\t\t\tThe whole URL is\n$wholeUrl\n\n" ;
	}
	    
	our $result = get($wholeUrl);

	if ($_debug >= $_debugLevel{'DEBUG'}) {
	    print "\t\tResult returned is $result\n\n" ;
	}
	    
	    
	my $bytesSubmitted = $bufferLen;
	
	if ($_debug >= $_debugLevel{'INFO'}) {
	    print "\t\tBytes submitted: $bytesSubmitted; " ;
	}	
	
	# Good response must start with <html> and include 'null' to be called a success.
	my $submissionResult = (defined ($result) and ($result =~ /^<html>.*null/s ) ) ?
	    "Success: " . $bytesSubmitted . " bytes submitted" : "Failure: " . $result . "from:\n" .
	    "\t$wholeUrl\n" ;
	
	if ($_debug >= $_debugLevel{'INFO'}) {
	    print "\t\tResult is $submissionResult\n\n" ;
	}

	return($bytesSubmitted);
}


