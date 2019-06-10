#!/usr/bin/env bash

R1=$RANDOM
R2=$RANDOM

INCOMING_DIR="docker/sftp/data/incoming"
CASE_REFERENCE="SC068-$R1-$R2"

echo "Cleaning process directory"
sudo rm $INCOMING_DIR/processed/*
sudo chmod -R 777 $INCOMING_DIR &> /dev/null

#################################################################################
# Run from root of repository
#################################################################################

#################################################################################
# Change to your own details
#################################################################################
PHONE=07813072017
EMAIL=christopher.moreton@hmcts.net

BODY="{\"benefitType\":{\"description\":\"Personal Independence Payment\",\"code\":\"PIP\"},\"postCodeCheck\":\"ln8 3dy\",\"mrn\":{\"date\":\"15-05-2019\",\"dateAppealSubmitted\":\"30-05-2019\",\"dwpIssuingOffice\":\"DWP PIP (1)\"},\"isAppointee\":false,\"appellant\":{\"title\":\"Mrs.\",\"firstName\":\"Ap\",\"lastName\":\"Pellant\",\"dob\":\"01-01-1998\",\"nino\":\"AB123456C\",\"contactDetails\":{\"addressLine1\":\"1 Appellant Ave\",\"addressLine2\":\"\",\"townCity\":\"Appellant-ville\",\"county\":\"Appellant County\",\"postCode\":\"TS1 1ST\",\"phoneNumber\":\"${PHONE}\",\"emailAddress\":\"${EMAIL}\"}},\"smsNotify\":{\"wantsSMSNotifications\":true,\"useSameNumber\":true,\"smsNumber\":\"07831292016\"},\"hasRepresentative\":false,\"reasonsForAppealing\":{\"reasons\":[{\"whatYouDisagreeWith\":\"Under payment\",\"reasonForAppealing\":\"I need more money.\"}],\"otherReasons\":\"\"},\"evidenceProvide\":false,\"hearing\":{\"wantsToAttend\":false},\"signAndSubmit\":{\"signer\":\"Mr Ap Pellant\"}}\n"

#################################################################################
echo "Deleting all benefit cases from CCD database..."
#################################################################################
docker exec -it compose_ccd-shared-database_1 psql -U postgres ccd_data -c "delete from case_event; delete from case_data;"

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

function loadCase() {
  echo "Updating XML"

  cp sscs-5270-00$1.xml $XML_FILE
  sed -i "s/{APPEAL_CASE_ID}/${APPEAL_CASE_ID}/g" $2
  sed -i "s/{EMAIL}/${EMAIL}/g" $2
  sed -i "s/{PHONE}/${PHONE}/g" $2
  sed -i "s/{CASE_REFERENCE}/${CASE_REFERENCE}/g" $2

  sleep 10

  echo "Removing processed files to trigger loading of new XML..."
  sudo rm $INCOMING_DIR/processed/*

  printf "Waiting for new files to appear in processed directory..."

  until test -f $INCOMING_DIR/processed/SSCS_Extract_Delta_2018-05-24-16-14-19.xml
  do
    printf "."
    sleep 1
  done

  echo
}


#################################################################################
echo "Setting Appeal Lodged Status..."
#################################################################################
XML_FILE="${INCOMING_DIR}/SSCS_Extract_Delta_2018-05-24-16-14-19.xml"

loadCase 1 $XML_FILE

##################################################################################
echo "Setting DWP Responded Status..."
##################################################################################

loadCase 2 $XML_FILE

sudo chmod -R 777 ${INCOMING_DIR}

