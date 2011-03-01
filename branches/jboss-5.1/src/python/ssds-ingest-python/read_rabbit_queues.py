import urllib2
import re

# This is the URL that will be authenticated against
theurl = 'http://messaging.shore.mbari.org:55672/api/queues'

# The username
username = 'ssdsadmin'

# The password
password = 'water4u'

# Create a password maanger
passman = urllib2.HTTPPasswordMgrWithDefaultRealm()

# Add the username and password and associate with the URL base
passman.add_password(None, theurl, username, password)

# because we have put None at the start it will always
# use this username/password combination for  urls
# for which `theurl` is a super-url

# create the AuthHandler
authhandler = urllib2.HTTPBasicAuthHandler(passman)

# Create and install the opener
opener = urllib2.build_opener(authhandler)
urllib2.install_opener(opener)

# All calls to urllib2.urlopen will now use our handler
# Make sure not to include the protocol in with the URL, or
# HTTPPasswordMgrWithDefaultRealm will be very confused.
# You must (of course) use it when fetching the page though.

ssdsqueues = urllib2.urlopen('http://messaging.shore.mbari.org:55672/api/queues/ssds').read()

queues = re.findall('queue_details":{"name":"(\\w+)"', ssdsqueues)

print queues