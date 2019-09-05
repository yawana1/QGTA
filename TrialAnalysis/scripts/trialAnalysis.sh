#!/bin/sh

args=$args #reads from args variable if called from qsub script

while getopts "U:J:j:e:m:" opt; do
  case $opt in
	e)args="$args -exp=$OPTARG";;
	j)args="$args -job=$OPTARG";;
	J)args="$args -jobId=$OPTARG";;
	U)args="$args -asUrl=$OPTARG";;
	m)mem="$OPTARG";;
    \?)echo "Invalid option: -$OPTARG" >&2;;
  esac
done

#QG java sym link
JAVA=/share/apps/QG/java7

if [ -n "$PBS_O_WORKDIR" ]
then
	cd $PBS_O_WORKDIR
fi

heapSize=''
if [ -n "$mem" ]
 then
 	if grep -q trialAnalysis <<<$args;
 	then
 		heapSize="-Xmx"$mem
 	fi
fi

#move args to different variable as the asreml.sh script also uses the args variable
arguments=$args
unset args

$JAVA $heapSize -jar bin/trialAnalysis.jar $arguments
