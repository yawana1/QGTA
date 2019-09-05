#!/bin/sh -l

args='-j trialanalysis'
logDir=$1/ceres
queue=$2
procs=$3
MAX_PROCS=12
jobName=TA
qsubCmd=/usr/local/bin/qsub

while getopts "U:J:e:l:q:p:m:" opt; do
  case $opt in
	e)args="$args -e $OPTARG" jobName=$OPTARG;;
	l)logDir="$OPTARG/ceres";;
	q)queue="$OPTARG";;
	p)procs="$OPTARG";;
	m)mem="$OPTARG";;
	J)args="$args -J $OPTARG";;
	U)args="$args -U $OPTARG";;
    \?)echo "Invalid option: -$OPTARG" >&2;;
  esac
done

#get trial name from file path
jobName=$(echo $jobName|grep --only-matching '[a-zA-Z0-9]*.[a-zA-Z0-9]*.xml$') 
jobName=$(echo $jobName | sed 's/.xml//g') #remove .xml

if [ $procs -gt $MAX_PROCS ]
then
	procs=$MAX_PROCS
fi

if [ -n "$mem" ]
then
	mem=",mem=$mem"
	args="$args -m=$mem"
else
	mem=''
fi

#legecy
#get log directory out of App.xml to send ceres stdout and sterr to
#logDir=$(perl -ne 'print if m{<logDir>} .. m{</logDir>}' config/App.xml | sed "s/<logDir>//g" | sed "s/<\/logDir>/\/ceres/g")

#push directory this script resides in
directory=$(readlink -f $0 | xargs dirname)
pushd $directory

#set stdout and stderr PBS directives to logDir
$qsubCmd -o $logDir -e $logDir -q $queue -l nodes=1:ppn=$procs$mem -N $jobName -v args="$args" trialAnalysis.sh

popd
