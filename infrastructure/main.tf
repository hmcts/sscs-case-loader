provider "vault" {
  //  # It is strongly recommended to configure this provider through the
  //  # environment variables described above, so that each user can have
  //  # separate credentials set in the environment.
  //  #
  //  # This will default to using $VAULT_ADDR
  //  # But can be set explicitly
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "sscs_s2s_secret" {
  path = "secret/test/ccidam/service-auth-provider/api/microservice-keys/sscs"
}

data "vault_generic_secret" "idam_sscs_systemupdate_user" {
  path = "secret/test/ccidam/idam-api/sscs/systemupdate/user"
}

data "vault_generic_secret" "idam_sscs_systemupdate_password" {
  path = "secret/test/ccidam/idam-api/sscs/systemupdate/password"
}

data "vault_generic_secret" "idam_oauth2_client_secret" {
  path = "secret/demo/ccidam/idam-api/oauth2/client-secrets/sscs"
}

data "vault_generic_secret" "gaps2_key_location" {
  path = "secret/test/sscs/gaps2_service_sftp_private_key"
}

locals {
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
}


module "sscs-case-loader" {
  source = "git@github.com:contino/moj-module-webapp?ref=master"
  product = "${var.product}-case-loader"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"

  app_settings = {
    MANAGEMENT_SECURITY_ENABLED = "${var.management_security_enabled}"
    CORE_CASE_DATA_API_URL = "http://ccd-data-store-api-${var.env}.service.${local.aseName}.internal"
    CORE_CASE_DATA_USER_ID = "${var.core_case_data_user_id}"
    CORE_CASE_DATA_JURISDICTION_ID = "${var.core_case_data_jurisdiction_id}"
    CORE_CASE_DATA_CASE_TYPE_ID = "${var.core_case_data_case_type_id}"
    CORE_CASE_DATA_EVENT_ID = "${var.core_case_data_event_id}"

    IDAM_URL = "${var.idam_url}"

    IDAM.S2S-AUTH.TOTP_SECRET ="${data.vault_generic_secret.sscs_s2s_secret.data["value"]}"
    IDAM.S2S-AUTH = "${var.idam_s2s_auth}}"
    IDAM.S2S-AUTH.MICROSERVICE = "${var.idam_s2s_auth_microservice}"

    IDAM_SSCS_SYSTEMUPDATE_USER = "${data.vault_generic_secret.idam_sscs_systemupdate_user.data["value"]}"
    IDAM_SSCS_SYSTEMUPDATE_PASSWORD = "${data.vault_generic_secret.idam_sscs_systemupdate_password.data["value"]}"

    IDAM_OAUTH2_CLIENT_ID = "${var.idam_oauth2_client_id}}"
    IDAM_OAUTH2_CLIENT_SECRET = "${data.vault_generic_secret.idam_oauth2_client_secret.data["value"]}"
    IDAM_OAUTH2_REDIRECT_URL = "${var.idam_oauth2_redirect_url}}"

    GAPS2_KEY_LOCATION = "${data.vault_generic_secret.gaps2_key_location.data["value"]}"
    GAPS2_SFTP_HOST = "${var.gaps2_sftp_host}"
    GAPS2_SFTP_PORT = "${var.gaps2_sftp_port}}"
    GAPS2_SFTP_USER = "${var.gaps2_sftp_user}}"
    GAPS2_SFTP_DIR = "${var.gaps2_sftp_dir}"

  }
}
