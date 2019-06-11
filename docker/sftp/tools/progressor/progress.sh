#!/usr/bin/env bash

#################################################################################
# This script will progress a case through various states, relieving you
# of a lot of the manual steps:
#
# - Prepare the incoming/processed/failed directories
# - Create a new CCD case via the Tribunals API
# - Determine the ID of the case in the CCD database
# - Use the CCD GUI to add a Case Reference to the case
# - Place the case in Appeal Lodged state
# - Place the case into Response Received state
# - Add a hearing date
#
# Prerequisites
#
# 1) You have the sscs-docker setup running
#################################################################################

if [ -z $2 ]; then
  echo "#########################################################"
  echo "   Please supply your phone number and email address"
  echo
  echo "   ./progress.sh 07182992882 iama.example@hmcts.net"
  echo "#########################################################"
  exit
fi

PHONE=$1
EMAIL=$2

INCOMING_DIR="../../data/incoming"

CASE_REFERENCE="SC068-$RANDOM-$RANDOM"

echo "Cleaning up the XML directories"

sudo rm ${INCOMING_DIR}/*Delta*
sudo rm ${INCOMING_DIR}/processed/*
sudo rm ${INCOMING_DIR}/failed/*

function loadCase() {

  DATE_STAMP=$(date +%F_%T)
  DATE_STAMP_FORMATTED=${DATE_STAMP//[_:]/-}

  EXTRACT_DELTA_XML="SSCS_Extract_Delta_${DATE_STAMP_FORMATTED}.xml"
  INCOMING_XML_FILE="${INCOMING_DIR}/${EXTRACT_DELTA_XML}"
  PROCESSED_XML_FILE="${INCOMING_DIR}/processed/${EXTRACT_DELTA_XML}"

  SOURCE_FILE=sscs-5270-00$1.xml
  TMP_FILE=/tmp/${SOURCE_FILE}

  echo "Copying ${SOURCE_FILE} to ${TMP_FILE}"
  cp ${SOURCE_FILE} ${TMP_FILE}

  echo "Updating XML"

  sed -i "s/{APPEAL_CASE_ID}/${APPEAL_CASE_ID}/g" ${TMP_FILE}
  sed -i "s/{EMAIL}/${EMAIL}/g" ${TMP_FILE}
  sed -i "s/{PHONE}/${PHONE}/g" ${TMP_FILE}
  sed -i "s/{CASE_REFERENCE}/${CASE_REFERENCE}/g" ${TMP_FILE}

  echo "Copying ${TMP_FILE} to ${INCOMING_XML_FILE}"
  cp ${TMP_FILE} ${INCOMING_XML_FILE}

  sleep 1

  printf "Waiting for new files to appear in processed directory..."

  until test -f ${PROCESSED_XML_FILE}
  do
    printf "."
    sleep 1
  done

  echo
}

echo "Making sure I can write to the output directories"
sudo chmod -R 777 $INCOMING_DIR &> /dev/null

BODY="{\"benefitType\":{\"description\":\"Personal Independence Payment\",\"code\":\"PIP\"},\"postCodeCheck\":\"ln8 3dy\",\"mrn\":{\"date\":\"15-05-2019\",\"dateAppealSubmitted\":\"30-05-2019\",\"dwpIssuingOffice\":\"DWP PIP (1)\"},\"isAppointee\":false,\"appellant\":{\"title\":\"Mrs.\",\"firstName\":\"Ap\",\"lastName\":\"Pellant\",\"dob\":\"01-01-1998\",\"nino\":\"AB123456C\",\"contactDetails\":{\"addressLine1\":\"1 Appellant Ave\",\"addressLine2\":\"\",\"townCity\":\"Appellant-ville\",\"county\":\"Appellant County\",\"postCode\":\"TS1 1ST\",\"phoneNumber\":\"${PHONE}\",\"emailAddress\":\"${EMAIL}\"}},\"smsNotify\":{\"wantsSMSNotifications\":true,\"useSameNumber\":true,\"smsNumber\":\"${PHONE}\"},\"hasRepresentative\":false,\"reasonsForAppealing\":{\"reasons\":[{\"whatYouDisagreeWith\":\"Under payment\",\"reasonForAppealing\":\"I need more money.\"}],\"otherReasons\":\"\"},\"evidenceProvide\":false,\"hearing\":{\"wantsToAttend\":false},\"signAndSubmit\":{\"signer\":\"Mr Ap Pellant\"}}\n"

#################################################################################
echo "Deleting all benefit cases from CCD database..."
#################################################################################
docker exec -it compose_ccd-shared-database_1 psql -U postgres ccd_data -c "delete from case_event;"
docker exec -it compose_ccd-shared-database_1 psql -U postgres ccd_data -c "delete from case_data;"

#################################################################################
echo "Creating case..."
#################################################################################
echo $BODY | \
  http POST http://localhost:8080/appeals \
  Accept:'*/*' \
  Cache-Control:no-cache \
  Connection:keep-alive \
  Content-Type:application/json \
  Host:localhost:8080 \
  User-Agent:PostmanRuntime/7.13.0 \
  accept-encoding:'gzip, deflate' \
  cache-control:no-cache \
  content-length:900 &>/dev/null

#################################################################################
echo "Discovering case ID..."
#################################################################################
SELECT_OUTPUT=($(docker exec -it compose_ccd-shared-database_1 psql -U postgres ccd_data -c "select max(id) from case_data"))
APPEAL_CASE_ID=$(echo ${SELECT_OUTPUT[3]}|tr -d '\r')

if [ $APPEAL_CASE_ID = "(0" ]; then
  echo "Case creation failed..."
  exit 1
fi

echo "Case ID is $APPEAL_CASE_ID"

SELECT_OUTPUT=($(docker exec -it compose_ccd-shared-database_1 psql -U postgres ccd_data -c "select reference from case_data"))
DB_REFERENCE=$(echo ${SELECT_OUTPUT[3]}|tr -d '\r')

echo "Reference is ${DB_REFERENCE}"

echo "Use the CCD front end to add the case reference ${CASE_REFERENCE} to the case."
URL="http://localhost:3451/case/SSCS/Benefit/${DB_REFERENCE}/trigger/caseUpdated/caseUpdated1.0"
echo "Opening URL: $URL"
xdg-open $URL &>/dev/null
printf "Waiting for user to update CCD... press [RETURN] when ready."

read

echo

#################################################################################
echo "Setting Appeal Lodged Status..."
#################################################################################

loadCase 1

##################################################################################
echo "Setting DWP Responded Status..."
##################################################################################

loadCase 2

sudo chmod -R 777 ${INCOMING_DIR}

