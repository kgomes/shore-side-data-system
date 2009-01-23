#!/usr/bin/perl -w
#By Andrew C. Chase
# Modified by Kevin Gomes
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
################## USAGE #################################
# This script will run through the list of instrument    #
# ids in the "rawPacketIdsToMonitor.txt file, connect up #
# to the data storage DB and check for recent data.      #
# If the last date                                       #
# is greater than the "expectedModificationTime", a value#
# defined within this script, then it will be added to a #
# list of badIds. When all instrument IDs have been      #
# checked, an email will be fired off                    #
# to notify interested parties of the    #
# problem.						 #
##########################################################

use LWP::Simple;
use strict;
use diagnostics; 

##################  SCRIPT VARIABLES  ################### 
#the maximum time, in days, that a raw packet should have last been modified.
my $expectedModificationInterval = .15;

# Database connection parameters
my $baseDataUrl = 'http://new-ssds.mbari.org:8080/servlet/GetOriginalDataServlet?deviceID=';
my $urlPostPend = '&lastNumberOfPackets=1&isi=1&noHTMLHeader=1&convertTo=hex';

#the listing of device id's to check
my $idFile = '/u/kgomes/scripts/rawPacketIdsToMonitor.txt';

# The current time (start of script)
my $currentTimeInEpochSeconds = time;

###################  EMAIL VARIABLES  ####################
my $sendmail = '/usr/sbin/sendmail -t';
my $from = 'From: ssdsadmin@mbari.org';
my $replyTo = 'Reply-to: ssdsadmin@mbari.org';
my $to = 'To: graybeal@mbari.org, kgomes@mbari.org, chma@mbari.org, oreilly@mbari.org, headley@mbari.org, meed@mbari.org, bkieft@mbari.org'; 
# This is used when I am testing/debugging
#my $to = 'To: kgomes@mbari.org'; 

#a hash of the device id's to look for
my %deviceIds = %{getIDsToCheck()};

#id's that have not had an update in the specified time range
my %badIds = ();

################### MAIN EXECUTION LOOP ##################
# Loop over all the device IDs
for my $id (keys(%deviceIds)){
  my $lastUpdateTime = checkIDModificationTime($id);
#  print 'Device ' . $id . ' has a last update time of ' . $lastUpdateTime . "\n";
  if($lastUpdateTime && ($lastUpdateTime != 0)){
    my $idName = $deviceIds{$id};
    $badIds{$id}  = [$idName, $lastUpdateTime];
    print 'Added device ' . $id . ' to list of bad IDs' . "\n";
  } else {
    if (!(defined $lastUpdateTime)) {
      my $now = localtime time;
      print $now . ': Nothing was returned for the lastUpdateTime from device ' . $id . "\n";
    }
  }
}

unless(scalar(keys(%badIds)) == 0){
  emailErrorReport(\%badIds);
}


################## END MAIN EXECUTION LOOP #################

# This subroutine reads in a list of device IDs from a text file
# and returns a hashmap of those IDs where the key is the device
# ID and the value is the human readable name of the instrument
sub getIDsToCheck{
  # Open the file for reading
  open(IDFILE, "<$idFile") or die "Cannot open $idFile: $!"; 
  # Create the hashmap
  my %ids = ();
  # Loop over the lines of the file and try to find a device ID
  for my $idString (<IDFILE>){
    # Clean up any whitespace
    chomp ($idString);
    # If it starts with a '#' sign, skip it (comment)
    if($idString =~ /^\s*#.*/){
      next;
    }
    # Parse for the device ID and name
    $idString =~ /(.*?) (.*)/;
    my $id = $1;
    my $idName = $2;
    # Stuff it in the hashmap
    $ids{$id} = $idName;
  }
  # Return the hashmap
  return \%ids;
}

# This subroutine connects up to SSDS, reads in the device data and returns
# the timestamp from the most recent data packet
sub checkIDModificationTime{
  # Grab the ID
  my $id = shift(@_);

  # Create the URL
  my $deviceDataUrl = $baseDataUrl . $id . $urlPostPend;

  # Read in the data line
  my $content = get $deviceDataUrl;
    die "Couldn't get $deviceDataUrl " unless defined $content;

  # Clean up any whitespace
  chomp ($content);

  # Now parse out for the timestamp
  $content =~ /\d+,\d+\s\S+\s\d+\s\d+:\d+:\d+\s\S+,(\d+),.*/;

  # Grab the timestamp
  my $latestTimestampInEpochMilliseconds = $1;
  # Calculate the differenct between the current time and the latest timestamp
  my $timeDiff = ($currentTimeInEpochSeconds - ($latestTimestampInEpochMilliseconds/1000)) / (60*60*24);

  # Now check to see if the time difference is more than what is expected
  if($timeDiff > $expectedModificationInterval){
    # Convert it to hours for readability
    my $ageToReturn = $timeDiff * 24;
    # Strip off any extraneous digits
    $ageToReturn =~ /(\d+\.\d{1})\d+/;    
    my $longAgeToReturn = $1;
    if (!($longAgeToReturn)) {
      $ageToReturn =~ /(\d+)/;    
      $longAgeToReturn = $1;
    }
    # Now return it
    return ($longAgeToReturn);
  } else{
    # Since the time difference was OK, just return 0
    return 0;
  }

}

sub getErrorMessageSubjectAndContent{
  my %badIds = %{shift(@_)};
  my @ids = keys(%badIds);
  my $subject = 'Subject: SSDS: No recent data stream update from instruments:';
  foreach(@ids){
    $subject .= " $_";
  }
  my $content = "A problem has been encountered while checking on the status of the following data streams";
  $content .= " that SSDS is monitoring:\n\n";
  $content .= "Device ID :: Last update time (in hours) :: Device Name";
  #note, $idName contains both the device name and the deployment

  # The first thing to do is group them by the "platform" they are on.  This is
  # defined as the stuff that follows the :: in the idName.
  my %platformHash = ();
  foreach(@ids) {
    my $idName = ${$badIds{$_}}[0];
    $idName =~ /(.*)::(.*)/;
    my $devName = $1;
    my $platformName = $2;
    if (exists $platformHash{$platformName}) {
      # Add the information to the existing array
      push @{$platformHash{$platformName}}, $_ . " :: " . ${$badIds{$_}}[1] . " :: " . $devName;
    } else {
      # Create a new array
      my @platformArray = ($_ . " :: " . ${$badIds{$_}}[1] . " :: " . $devName);
      $platformHash{$platformName} = [ @platformArray ];
    }
  }
 
  # OK, so we now have a hash with the platform names and the strings to be printed for
  # device and time
  my @platforms = keys(%platformHash); 
  foreach(@platforms) {
    my $platformName = $_;
    $content .= "\n-------------------------------------\n";
    $content .= "         $platformName\n";
    $content .= "-------------------------------------\n";
    foreach my $i ( 0 .. $#{ $platformHash{$platformName} } ) {
      $content .= $platformHash{$platformName}[$i] . "\n";
    }
  }
#  foreach(@ids){
#    my $idName = ${$badIds{$_}}[0];
#    my $updateTime = ${$badIds{$_}}[1];
#    $content .= "$_ :: $idName :: $updateTime \n";
#  }
  return ($subject, $content);
} 


sub emailErrorReport{
  my %badIds = %{shift(@_)};
  my ($subject, $content) = getErrorMessageSubjectAndContent(\%badIds);

  open (SENDMAIL, "|$sendmail") or die "Cannot open $sendmail: $!";
  print SENDMAIL $from."\n"; 
  print SENDMAIL $to."\n";
  print SENDMAIL $replyTo."\n";
  print SENDMAIL $subject."\n";
  print SENDMAIL "Content-type: text/plain\n\n";
  print SENDMAIL $content;
  close(SENDMAIL);
}
