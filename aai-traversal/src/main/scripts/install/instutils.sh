#!/bin/ksh

###
# ============LICENSE_START=======================================================
# org.openecomp.aai
# ================================================================================
# Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================
###

ECHO=${ECHO:-echo}

q_flags="-qq -k$$"

show_install=${PROJECT_HOME}/install/show_install

############################################################################
# checkgroup groupname gid
# checks if group is already in /etc/group and if it has the right gid
# if it's not there, it adds it
# gid can be DC if you don't care
############################################################################
checkgroup () {
	ecode=0
	OFILE=/tmp/group.$$
	getent group "$1" > $OFILE
	if [ $? -eq 0 ]
	then
		CHECKGID=$( grep "^$1:" $OFILE | cut -f3 -d: 2>/dev/null )
		CHECKGROUPPRESENT=$( grep "^$1:" $OFILE | cut -f1 -d: 2>/dev/null )
		CHECKGROUP=$( grep ":$2:" $OFILE | cut -f1 -d: 2>/dev/null )
	fi
	if [ "${CHECKGID}" = "" -a "${CHECKGROUP}" = "" ]
	then
		${ECHO} "Adding $1 group ..."
		if [ "$2" = "DC" ]
		then
			groupadd $1 
		else
			groupadd -g $2 $1 
		fi
		if [ "$?" != "0" ]
		then
			${ECHO} "Cannot add group $1, continuing..."
			ecode=1
		fi
	else
		if [ "${CHECKGROUPPRESENT}" = "$1" ]
		then
			if [ "$2" != "DC" ]  
			then
				if [ "${CHECKGID}" != "$2" ]
				then
					${ECHO} "ERROR:  $1 group added but with wrong gid \"${CHECKGID}\"; should be $2"
					ecode=1
				fi
				if [ "${CHECKGROUP}" != "$1" ]
				then
					${ECHO} "ERROR:  wrong group \"${CHECKGROUP}\" for gid $2, group should be $1"
					ecode=1
				fi
			else
				${ECHO} "$1 group has already been added"
			fi
		fi
	fi
	rm -f $OFILE
	return ${ecode}
}

############################################################################
# checkuser username uid homedir shell group
# checks if the username/uid/homedir/shell combo is already in /etc/passwd
# if not, it adds it
# if the login is there and the uid belongs to a different user, it errors
# if the login is there and the shell is not correct, it errors
# uid may be DC for don't care
############################################################################
checkuser () {
	ecode=0
	OFILE=/tmp/user.$$
	getent passwd $1 > $OFILE
	if [ $? -eq 0 ]
	then	
		CHECKUID=$( grep "^$1:" $OFILE | cut -f3 -d: 2>/dev/null )
		CHECKLOGIN=$( grep ":x:$2:" $OFILE | cut -f1 -d: 2>/dev/null )
		CHECKLOGINPRESENT=$( grep "^$1:" $OFILE | cut -f1 -d: 2>/dev/null )
		CHECKSHELL=$( grep "^$1:" $OFILE | cut -f7 -d: 2>/dev/null )
		CHECKHOME=$( grep "^$1:" $OFILE | cut -f6 -d: 2>/dev/null )
	fi
	
	if [ ! -d $3 ]
	then
		mkdir -p $3
		if [ "$?" != "0" ]
		then
			${ECHO} "mkdir -p $3 failed"
			ecode=1
		fi
		chmod -R 755 $3
	fi
	if [ "${CHECKUID}" = "" -a "${CHECKLOGIN}" = "" ]
	then
		${ECHO} "Adding $1 login ..."
		if [ "$2" = "DC" ]
		then
			useradd -g $5 -d $3 -s $4 -c "$1 LOGIN" -m $1
		else
			useradd -u $2 -g $5 -d $3 -s $4 -c "$1 LOGIN" -m $1
		fi
		if [ "$?" != "0" ]
		then
			${ECHO} "Cannot add $1 login, continuing..."
			ecode=1
		fi
	elif [ "${CHECKLOGINPRESENT}" = "$1" -a "$2" = "DC" -a "${CHECKSHELL}" = "$4" -a "${CHECKHOME}" = "$3" ]
	then
		${ECHO} "The '$1' login has already been added to system with UID ${CHECKUID}."
	
	elif [ "${CHECKUID}" = "$2" -a "${CHECKLOGIN}" = "$1" -a "${CHECKSHELL}" = "$4" -a "${CHECKHOME}" = "$3" ]
	then
		${ECHO} "The '$1' login has already been added to system."
	else
		if [ "$2" != "DC" -a "${CHECKUID}" != "$2" ]
		then
			${ECHO} "ERROR:  $1 login added but with wrong uid \"${CHECKUID}\"; should be $2"
			ecode=1
		fi
		if [ "$2" != "DC" -a "${CHECKLOGIN}" != "$1" ]
		then
			${ECHO} "ERROR:  wrong login \"${CHECKLOGIN}\" for uid $2, login should be $1"
			ecode=1
		fi
		if [ "${CHECKHOME}" != "$3" ]
		then
			${ECHO} "ERROR:  wrong home directory \"${CHECKHOME}\" for login $1, should be $3"
			ecode=1
		fi
		if [ "${CHECKSHELL}" != "$4" ]
		then
			${ECHO} "ERROR:  $1 login not set up with $4"
			ecode=1
		fi
	fi
	rm -f $OFILE
	return ${ecode}
}

############################################################################
# checkhome username homedir action
# if the user doesn't exist, it errors
# checks if the username has homedir as its home directory
# if not and action is null, it modifies it
# if not and action is mod, it modifies it
# if not and action is error, it errors
############################################################################
checkhome () {
	ecode=0
	OFILE=/tmp/user.$$
	getent passwd $1 > $OFILE
	if [ $? -eq 0 ]
	then	
		CHECKUID=$( grep "^$1:" $OFILE | cut -f3 -d: 2>/dev/null )
		CHECKGID=$( grep "^$1:" $OFILE | cut -f4 -d: 2>/dev/null )
		CHECKHOME=$( grep "^$1:" $OFILE | cut -f6 -d: 2>/dev/null )
		
		if [ "${CHECKHOME}" = "$2" ] 
		then
			if [ ! -d $2 ]
			then
				mkdir -p $2
				if [ "$?" != "0" ]
				then
					${ECHO} "mkdir -p $2 failed"
					ecode=1
				fi
				chown ${CHECKUID}:${CHECKGID} $2
				chmod -R 755 $2
			fi
		else
			# modify the user to set the new home dir and move any current home dir to there
			usermod -d $2 -m $1
			if [ "$?" != "0" ]
			then
				${ECHO} "usermod -d $2 -m $1 failed"
				ecode=1
			fi
		fi
	else
		${ECHO} "user $1 doesn't exist"
		ecode=1
	fi
	
	rm -f $OFILE
	return ${ecode}
}

##################################################################
#checkloginsforpwds checks /etc/shadow for logins without passwords
# the first argument is a list of logins to check
##################################################################
checkloginsforpwds () {
	for i in $1
	do
		CHECK_LOGIN=$( grep "^${i}:" /etc/shadow | grep "!!" )
		if [ "${CHECK_LOGIN}" != "" ]
		then
			NOPWD="${NOPWD} ${i}"
		fi
	done
	
	if [ "${NOPWD}" != "" ]
	then
	   ${ECHO} ""
	   ${ECHO} "REMINDER:  The following logins must have a passwords assigned to them.\n"
	   ${ECHO} "##############################################################"
	   ${ECHO} "		${NOPWD}	"
	   ${ECHO} "##############################################################"
	   ${ECHO} ""
	   ${ECHO} "           This must be done by executing the following command:"
	   ${ECHO} ""
	   ${ECHO} "           $ passwd <login>"
	   ${ECHO} ""
	   ${ECHO} "           After typing the \"passwd\" command you will be prompted for"
	   ${ECHO} "           the password for the login."          
	   ${ECHO} ""
	fi
}

##################################################################
# checkassignpasswords checks /etc/shadow for logins without passwords
# and then asks the user to assign one
# the first argument is a list of logins to check
##################################################################
checkassignpasswords () {
	for i in $1
	do
		CHECK_LOGIN=$( grep "^${i}:" /etc/shadow | grep LK )
		if [ "${CHECK_LOGIN}" != "" ]
		then
			${ECHO} "Please assign a password for the '${i}' login"
			passwd ${i}
			${ECHO}
		fi
	done
}

############################################################################
# copywithperms origfile destfile owner group perms [save suffix]
# copies origfile to destfile, giving destfile ownership and permssions
# from owner, group, and perms.  If the sixth argument is "save", the
# original is saved in the same place with the seventh argument as the
# suffix.  If the seventh arg is null, $$ is used
############################################################################
copywithperms () {
	SAVE=0
	ECODE=0
	if [ "$6" = "save" -a -f "$2" ]
	then
		if [ "$7" = "" ]
		then
			cp $2 $2.$$
		else
			cp $2 $2.$7
		fi
	fi
	if [ -f $1 ]
	then
		cp $1 $2
		ECODE=$?
		chown ${3}:${4} $2
		chmod $5 $2
	else
		${ECHO} "$1 is not a file.  No copy done!"
	fi
	return ${ECODE}
}

############################################################################
# mkdirwithperms dirname owner group perms ifExist
# makes directory dirname , giving dirname ownership and permssions
# from owner, group, and perms.  
# perms can be DC if you don't care
# ifExist can be rm, error, dontcreate
############################################################################
mkdirwithperms () {
	ECODE=0
	if [ -f $1 ]
	then
		ECODE=1
		${ECHO} "$1 exists but is a file.  No mkdir done!"
	elif [ -d $1 ]
	then
		if [ "$5" = "rm" ]
		then
			rm -rf $1
			mkdir -p $1
			if [ "$?" != "0" ]
			then
				${ECHO} "mkdir -p $1 failed"
				ECODE=1
			fi
		elif [ "$5" = "error" ]
		then
			ECODE=1
			${ECHO} "$1 is a directory.  No mkdir done!"
		elif [ "$5" != "dontcreate" ]
		then
			mkdir -p $1
			if [ "$?" != "0" ]
			then
				${ECHO} "mkdir -p $1 failed"
				ECODE=1
			fi
		fi
	else
		mkdir -p $1
		if [ "$?" != "0" ]
		then
			${ECHO} "mkdir -p $1 failed"
			ECODE=1
		fi
	fi
	if [ "${ECODE}" = "0" ]
	then
		chown ${2}:${3} $1
		if [ "$4" != "DC" ]
		then
			chmod $4 $1
		fi
	fi
	return ${ECODE}
}


############################################################################
# chownwithperms owner group file mode
# changes the ownership and mode for the specified file
############################################################################
chownwithperms () {
	chown ${1}:${2} $3
	chmod $4 $3
}

verifywhosrunning () {
	userid=$( id | cut -f2 -d"(" | cut -f1 -d")" )
	if [ "${userid}" != "$1" ]
	then
		${ECHO} "You must be $1 to run $0"
		exit 1
	fi
}

replaceline() {

	name=$1
	value=$2
	file=$3

	if [ -z "${file}" ]
	then
		${ECHO} "replaceline: ERROR: insufficient arguments: $1 $2" >&2
		return 1
	fi

	if [ -n "$4" ]
	then
		${ECHO} "replaceline: ERROR: too many arguments: $1 $2 $3 $4" >&2
		return 1
	fi

	if [ -f ${file} ]
	then
		grep -v "^${name}=" ${file} > ${file}.$$
		${ECHO} "${name}=${value}" >> ${file}.$$
		mv -f ${file}.$$ ${file}
	else 
		${ECHO} "${name}=${value}" > ${file}
	fi
}

replaceline_with_quotes() {

	name=$1
	value=$2
	file=$3

	if [ -z "${file}" ]
	then
		${ECHO} "replaceline: ERROR: insufficient arguments: $1 $2" >&2
		return 1
	fi

	if [ -n "$4" ]
	then
		${ECHO} "replaceline: ERROR: too many arguments: $1 $2 $3 $4" >&2
		return 1
	fi

	if [ -f ${file} ]
	then
		grep -v "^${name}=" ${file} > ${file}.$$
		${ECHO} "${name}=\"${value}\"" >> ${file}.$$
		mv -f ${file}.$$ ${file}
	else 
		${ECHO} "${name}=\"${value}\"" > ${file}
	fi
}

# this deleteline will not actually delete the entry
# but only delete the value leaving the name=
# when siteconf.pl went from Boilerplate to Fillin,
# we changed this because Fillin can handle null values.

deleteline() {

	name=$1
	file=$2

	if [ -z "${file}" ]
	then
		${ECHO} "deleteline: ERROR: insufficient arguments" >&2
		return 1
	fi

	if [ -f ${file} ]
	then
		cp ${file} ${file}.$$
		lno=$( grep -n "^${name}=" ${file} | cut -d: -f1 )
		if [ "${lno}" != "" ] 
		then
			sed "${lno}d" ${file} > ${file}.$$
		fi
		${ECHO} "${name}=" >> ${file}.$$
		mv -f ${file}.$$ ${file}
	else 
		${ECHO} "${name}=" > ${file}
	fi
}

# dropline will drop the line from the file
# unlike the deleteline function above

dropline() {

	name=$1
	file=$2

	if [ -z "${file}" ]
	then
		${ECHO} "dropline: ERROR: insufficient arguments" >&2
		return 1
	fi

	if [ -f ${file} ]
	then
		grep -v "^${name}=" ${file} > ${file}.$$
		mv -f ${file}.$$ ${file}
	fi
}

pause_install() {

	if [ "${Pause}" =  "1" ]
	then
		if ${chkyn} -y "Continue with ${Itype}?"
		then
			return 0
		else
			${ECHO} "${PNAME}: quitting" >&2
			exit 1
		fi
	fi
}

get_ITYPE() {
	ITYPE=$( ${chkyn} -fer ${q_flags} -h\? ${ITYPE:+-D"${ITYPE}"} -H \
"	If you are doing a fresh install, answer 'I' or answer 'U' for upgrade." \
"Is this a fresh 'install' or 'upgrade' (I or U):${ITYPE:+ [${ITYPE}]}" \
	'^[IU]$' \
'*** ERROR *** Entry must be I or U.' )
}


###
# Change an /etc/group entry to allow a give user to change group into it.
# arg1 = comma-sep group list (e.g., sylantro,other)
# arg2 = user
###
addUserToGroup()
{
	if [ -z "$1" -o -z "$2" ]
	then
		${ECHO} "addUserToGroup failed, need two args, group and user"
		return 1
	else
		usermod -G $1 $2
	fi
	return 0
}

################### BACKUP AND RESTORE METHODS ########################
###################       VARIABLES          ##########################
###################       VARIABLES          ##########################
###################       VARIABLES          ##########################
###################       VARIABLES          ##########################

NO_FILE_INDICATOR="__NO_PREVIOUS_FILE__"
SAVE_SUFFIX=${Project}save

###################       SUBROUTINES        ##########################
###################       SUBROUTINES        ##########################
###################       SUBROUTINES        ##########################
###################       SUBROUTINES        ##########################
###################       SUBROUTINES        ##########################

##############################################################################
# Purpose:  make a backup copy of a file in such a way that the backup
# won't be lost by re-running your script PLUS give you a predictable name
# for the most recent back up to use when you roll back.
#
# Input:
# - Arg1 = file to back up
#
# Requirement: 
# - Remove $1.save before calling this function or else a copy won't be made.
# - Make sure to set the value of env value TODAY to use as a suffix.
#
# Description:
# Copy $1 to $1.${SAVE_SUFFIX}.${TODAY}, then link that to $1.save.
#
##############################################################################
make_backup_copy ()
{
	if [ -z "${TODAY}" ]
	then
		${ECHO} "make_backup_copy - TODAY variable is unset" >&2
		return 1
	fi

	if [ -f $1.${SAVE_SUFFIX}.${TODAY} -a -h $1.save ]
	then
		${ECHO} "Note: backup already exists for $1"
	else
		# if existing file doesn't exist, set up for later delete by rollback
		if [ ! -f $1 -a ! -h $1 ]
		then
			${ECHO} ${NO_FILE_INDICATOR} > $1
		fi
		cp -p $1 $1.${SAVE_SUFFIX}.${TODAY}
		ln -s $1.${SAVE_SUFFIX}.${TODAY} $1.save
	fi
}

################################################################################
# Purpose: Find the actual file that belongs to $1, which can be a symbolic 
# link.
# 
# Input:
# - Arg1 = path to file or link
# - Arg2 = true if you want _SRCFILE to be null if no actual file is 
#  	found.  If Arg2 is NOT true, then _SRCFILE is set to Arg1.
# 
# Side Effect:
# Sets value of _SRCFILE variable
################################################################################
find_source_file ()
{
	if [ -z "$1" ]
	then
		${ECHO} "find_source_file - needs at least one argument" >&2
		return 1
	fi

	ls -l $1 > /tmp/tls$$
	cat /tmp/tls$$ | sed 's/  */	/g' |cut -f11 > /tmp/cuts$$
	_SRCFILE=$( cat /tmp/cuts$$ )

	if [ "$_SRCFILE" = "" ]
	then
		if [ "$2" != "true" ]
		then
			_SRCFILE=$1
		fi
	fi
	rm -f /tmp/tls$$ /tmp/cuts$$
}

#######################################################################
# Purpose: Expands template file using data in COPT variable.
# Diffs expanded template against existing file and installs if different.
# If arg5 = true, sets _config_changes=1 so you know that changes were installed
#
# Makes its own backup copy using make_backup_copy.
# Does install if different using install_if_different.
#
# Input:
# Arg1 = template path without .tmpl extension
# Arg2 = install path
# Arg3 = owner and group (e.g., root:other)
# Arg4 = permissions (e.g., 750)
# Arg5 = true/false, if expanded file is different than installed .
#		Set _config_changes to 1 if Arg5 is true.  Otherwise, don't touch 
#		_config_changes
# 
# Requirement: set COPT to the value of the -c option to siteconf.pl
#
# Side Effect: sets _config_changes=1 if changes were installed
#######################################################################
install_from_template () 
{
	if [ -z "${COPT}" ]
	then
		${ECHO} "install_from_template - COPT is unset" >&2
		return 1
	fi

	TMPL=$( basename ${1} )
	OFILE=/tmp/${TMPL}
	if [ -f ${1}.tmpl ]
	then

		${PROJECT_HOME}/bin/siteconf.pl -t ${1}.tmpl -c ${COPT} -o ${OFILE}
		install_if_different ${OFILE} ${2} ${3} ${4} ${5}

	else
		${ECHO} "install_from_template: ERROR: Missing ${TMPL}.tmpl" >&2
	fi
	rm -f ${OFILE}
}


#######################################################################
# Purpose: Copies source to destination if the two are different.
# If arg5 = true, sets _config_changes=1 so you know that changes were installed
#
# Makes its own backup copy using make_backup_copy.
#
# Input:
# Arg1 = source path
# Arg2 = install path
# Arg3 = owner and group (e.g., root:other)
# Arg4 = permissions (e.g., 750)
# Arg5 = true/false, if expanded file is different than installed .
#		Set _config_changes to 1 if Arg5 is true.  Otherwise, don't touch 
#		_config_changes
# 
# Side Effect: sets _config_changes=1 if changes were installed
#######################################################################
install_if_different()
{
		# Take backup before changing.
		# Only change if different.
		if [ -f ${2} ]
		then
			diff ${1} ${2} > /dev/null
			diffrc=$?
			if [ "${diffrc}" != "0" ]
			then
				${ECHO} "Installing ${2}"
				make_backup_copy ${2}
				mv -f ${1} ${2}
				chown ${3} ${2}
				chmod ${4} ${2}
				if [ "${5}" = "true" ]
				then
					_config_changes=1
				fi
			fi
		else
			# creates backup containing ${NO_FILE_INDICATOR} for rollback removal
			make_backup_copy ${2}  
			mv -f ${1} ${2}
			chown ${3} ${2}
			chmod ${4} ${2}
			if [ "${5}" = "true" ]
			then
				_config_changes=1
			fi
		fi
}
###################################################################
# Purpose: rollback a file whose backup was made with make_backup_copy
#
# Input:
# Arg1 is path of installed file. Subroutine will look for ${1}.save
# Arg2 = true/false, if expanded file is different than installed, 
#		set _config_changes to 1 if Arg2 is true.  Otherwise, don't touch 
#		_config_changes
#
# Side Effect: sets _config_changes=1 if changes were rolled back
###################################################################
rollback_from_save ()
{
	if [ -f ${1}.save -o -h ${1}.save ]
	then
		find_source_file ${1}.save false
		${ECHO} "rollback_from_save: rolling back to $( basename ${_SRCFILE} )"
		grep ${NO_FILE_INDICATOR} ${_SRCFILE} > /dev/null
		if [ $? -eq 0 ]
		then
			rm -f ${_SRCFILE} ${1}
		else
			mv -f ${_SRCFILE} ${1}
		fi
		if [ "${2}" = "true" ]
		then
			_config_changes=1
		fi
		rm -f ${1}.save
	fi
}
