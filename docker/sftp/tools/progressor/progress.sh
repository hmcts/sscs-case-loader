#!/usr/bin/env bash

###################################################################################################
#                                                                                                 #
# This script is discussed in more detail at                                                      #
#                                                                                                 #
# https://tools.hmcts.net/confluence/display/SSCS/Case+Creation+and+Progression+Automation+Script #
#                                                                                                 #
###################################################################################################

if [ -z $3 ]; then
  echo "#########################################################"
  echo " Usage:
  echo "
  echo " ./progress.sh <phone_number> <hmcts_email> <prefix>"
  echo
  echo " phone_number: Your mobile number for text messages"
  echo " hmcts_email : Your @hmcts.net email address"
  echo " prefix      : The directory of your XML templates"
  echo
  echo " E.g.:"
  echo " ./progress.sh 01827983728 jane.smith@hmcts.net sscs-5270"
  echo
  echo "#########################################################"
  exit
fi

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
FILE_PREFIX="$3"

PHONE=$1
EMAIL=$2

INCOMING_DIR="${SCRIPT_DIR}/../../data/incoming"

CASE_REFERENCE="SC068-$RANDOM-$RANDOM"
FILE_SUFFIX="$3"

echo "Cleaning up the XML directories"

sudo rm ${INCOMING_DIR}/* &> /dev/null
sudo rm ${INCOMING_DIR}/processed/* &> /dev/null
sudo rm ${INCOMING_DIR}/failed/* &> /dev/null

function loadCase() {

  #################################################################################
  echo "Processing file ${SOURCE_FILENAME}"
  #################################################################################

  DATE_STAMP=$(date +%F_%T)
  DATE_STAMP_FORMATTED=${DATE_STAMP//[_:]/-}

  EXTRACT_DELTA_XML="SSCS_Extract_Delta_${DATE_STAMP_FORMATTED}.xml"
  INCOMING_XML_FILE="${INCOMING_DIR}/${EXTRACT_DELTA_XML}"
  PROCESSED_XML_FILE="${INCOMING_DIR}/processed/${EXTRACT_DELTA_XML}"
  SOURCE_REFERENCE_FILE="${SCRIPT_DIR}/SSCS_Extract_Reference.xml"
  REFERENCE_XML_FILE="${INCOMING_DIR}/SSCS_Extract_Reference_${DATE_STAMP_FORMATTED}.xml"

  TMP_FILE="/tmp/${SOURCE_FILENAME}"

  echo "Copying template to temporary directory for updating..."
  cp ${SOURCE_FILE} ${TMP_FILE}

  echo "Updating XML..."

  sed -i "s/{APPEAL_CASE_ID}/${APPEAL_CASE_ID}/g" ${TMP_FILE}
  sed -i "s/{EMAIL}/${EMAIL}/g" ${TMP_FILE}
  sed -i "s/{PHONE}/${PHONE}/g" ${TMP_FILE}
  sed -i "s/{CASE_REFERENCE}/${CASE_REFERENCE}/g" ${TMP_FILE}

  echo "Moving populated template file to incoming directory..."
  cp ${TMP_FILE} ${INCOMING_XML_FILE}

  echo "Creating reference file..."
  cp ${SOURCE_REFERENCE_FILE} ${REFERENCE_XML_FILE}

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
printf "Waiting for user to update CCD"

EVENT_COUNT="0"
while [ ${EVENT_COUNT} != "2" ];
do
  SELECT_OUTPUT=($(docker exec -it compose_ccd-shared-database_1 psql -U postgres ccd_data -c "select count(*) from case_event where case_data_id=${APPEAL_CASE_ID};"))
  EVENT_COUNT=$(echo ${SELECT_OUTPUT[3]}|tr -d '\r')
  printf "."
  sleep 1
done

echo

XML_FILE_NUMBER=1

echo "Source file is ${SOURCE_FILE}"

while true;
do

  if [ $(echo -n ${XML_FILE_NUMBER} | wc -c) = "1" ]; then
    XML_FILE_NUMBER_PADDED="0${XML_FILE_NUMBER}"
  else
    XML_FILE_NUMBER_PADDED="${XML_FILE_NUMBER}"
  fi

  SOURCE_FILENAME="${FILE_PREFIX}-${XML_FILE_NUMBER_PADDED}.xml"
  SOURCE_FILE="${SCRIPT_DIR}/${FILE_SUFFIX}/${SOURCE_FILENAME}"

  if [ ! -f "${SOURCE_FILE}" ]; then
     echo "All files processed."
     exit 0
  fi

  loadCase $XML_FILE_NUMBER
  let XML_FILE_NUMBER=${XML_FILE_NUMBER}+1

done

sudo chmod -R 777 ${INCOMING_DIR}

