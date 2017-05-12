#!/usr/bin/perl
# CC_ID_SITECONF_PL[] = "@(#)/vobs/waas/src/oam/siteconf.pl@@/main/4"

#.Description
# This perl script takes as input template file, 
# and one or more configuration files.  It uses the values in the 
# configuration files as substitutions for the matching tags in the template 
# file.

#.Constraints
# The input files must be readable by the script.

#.See Also
#

use Getopt::Std;

local $dbg=0;

getopts ('dt:c:');

if ($opt_d) {
	$dbg=$opt_d;
}

if ($dbg) {
	print STDERR "opt_d=$opt_d\n";
	print STDERR "opt_t=$opt_t\n";
	print STDERR "opt_c=$opt_c\n";
}

###
# Print usage if no arguments passed
if (! $opt_t) {
	print STDERR "Usage:  $0 -t templatefile -c configfilelist\n";
	exit (1);
}

# process the template file variable
if ($opt_t) {
	if (! -r $opt_t) {
		print STDERR "Error:  Can't read template file $opt_t\n";
		exit (2);
	}
	$templatefile = $opt_t;
}
else {
	print STDERR "Error:  You must enter the template file name\n";
	exit (2);
}

###
# Global error flag for return code when exiting
$err = 0;

$configlist = '';
if ($opt_c) {
	$configlist = $opt_c;
}

# process the site configuration file variable
if ($configlist) {
	@siteary = split /,/, $configlist; 
}

# Add PROJECT_HOME to Conf dictionary
$Conf{'PROJECT_HOME'} = $ENV{'PROJECT_HOME'};

foreach $arg (@siteary) {
	if ($dbg) { print STDERR "Opening $arg\n" }

	open(CONF, $arg) || die $!, ", '$arg'\n";

	while (<CONF>) {
	    #1 while chomp();
	    $_ =~ s/[\r\n]$//g; # strip newlines and dos-injected carriage returns
		if ( /=/ ) {
			($attr,$value) = split(/=/,$_,2);
			$value =~ s/\$PHOME/$ENV{'PROJECT_HOME'}/;
			$value =~ s/\$PROJECT_HOME/$ENV{'PROJECT_HOME'}/;
			$Conf{$attr} = $value;
		}
	}

	close CONF;
}

if ($dbg) { print STDERR "Expanding $templatefile\n" }

# Expand a config file 

open(TEMPLATE, $templatefile) || die $!, ", '$templatefile'\n";
while (<TEMPLATE>) {
	# handle strings such as @HTTP_ROOT@@HTTP_PORT@
	s/@(\w+)@/$Conf{$1}/g;
	print;
}
	
close TEMPLATE;

