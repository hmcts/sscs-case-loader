provider "azurerm" {
  version = "<= 1.2.0"
}

provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "sscs_s2s_secret" {
  path = "secret/${var.infrastructure_env}/ccidam/service-auth-provider/api/microservice-keys/sscs"
}

data "vault_generic_secret" "idam_sscs_systemupdate_user" {
  path = "secret/${var.infrastructure_env}/ccidam/idam-api/sscs/systemupdate/user"
}

data "vault_generic_secret" "idam_sscs_systemupdate_password" {
  path = "secret/${var.infrastructure_env}/ccidam/idam-api/sscs/systemupdate/password"
}

data "vault_generic_secret" "idam_oauth2_client_secret" {
  path = "secret/${var.infrastructure_env}/ccidam/idam-api/oauth2/client-secrets/sscs"
}

data "vault_generic_secret" "gaps2_key_location" {
  path = "secret/${var.infrastructure_env}/sscs/gaps2_service_sftp_private_key"
}

data "vault_generic_secret" "idam_api" {
  path = "secret/${var.infrastructure_env}/sscs/idam_api"
}

data "vault_generic_secret" "idam_s2s_api" {
  path = "secret/${var.infrastructure_env}/sscs/idam_s2s_api"
}

data "vault_generic_secret" "sftp_host" {
  path = "secret/${var.infrastructure_env}/sscs/sftp_host"
}

data "vault_generic_secret" "sftp_port" {
  path = "secret/${var.infrastructure_env}/sscs/sftp_port"
}

locals {
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
}

module "sscs-case-loader" {
  source       = "git@github.com:contino/moj-module-webapp?ref=capacity-param"
  product      = "${var.product}-case-loader"
  location     = "${var.location}"
  env          = "${var.env}"
  ilbIp        = "${var.ilbIp}"
  is_frontend  = false
  subscription = "${var.subscription}"
  capacity     = "1"

  app_settings = {
    MANAGEMENT_SECURITY_ENABLED = "${var.management_security_enabled}"
    CORE_CASE_DATA_API_URL = "http://ccd-data-store-api-${var.env}.service.${local.aseName}.internal"
    CORE_CASE_DATA_USER_ID = "${var.core_case_data_user_id}"
    CORE_CASE_DATA_JURISDICTION_ID = "${var.core_case_data_jurisdiction_id}"
    CORE_CASE_DATA_CASE_TYPE_ID = "${var.core_case_data_case_type_id}"
    CORE_CASE_DATA_EVENT_ID = "${var.core_case_data_event_id}"

    IDAM_URL = "${data.vault_generic_secret.idam_api.data["value"]}"

    IDAM.S2S-AUTH.TOTP_SECRET ="${data.vault_generic_secret.sscs_s2s_secret.data["value"]}"
    IDAM.S2S-AUTH = "${data.vault_generic_secret.idam_s2s_api.data["value"]}"
    IDAM.S2S-AUTH.MICROSERVICE = "${var.idam_s2s_auth_microservice}"

    IDAM_SSCS_SYSTEMUPDATE_USER = "${data.vault_generic_secret.idam_sscs_systemupdate_user.data["value"]}"
    IDAM_SSCS_SYSTEMUPDATE_PASSWORD = "${data.vault_generic_secret.idam_sscs_systemupdate_password.data["value"]}"

    IDAM_OAUTH2_CLIENT_ID = "${var.idam_oauth2_client_id}"
    IDAM_OAUTH2_CLIENT_SECRET = "${data.vault_generic_secret.idam_oauth2_client_secret.data["value"]}"
    IDAM_OAUTH2_REDIRECT_URL = "https://sscs-case-loader-${var.env}.service.${local.aseName}.internal"

    GAPS2_KEY_LOCATION = "${data.vault_generic_secret.gaps2_key_location.data["value"]}"
    GAPS2_SFTP_HOST = "${data.vault_generic_secret.sftp_host.data["value"]}"
    GAPS2_SFTP_PORT = "${data.vault_generic_secret.sftp_port.data["value"]}"
    GAPS2_SFTP_USER = "${var.gaps2_sftp_user}"
    GAPS2_SFTP_DIR = "${var.gaps2_sftp_dir}"

    SSCS_CASE_LOADER_CRON_SCHEDULE = "${var.sscs_case_loader_cron_schedule}"
    IGNORE_CASES_BEFORE_DATE = "${var.ignore_cases_before_date}"

    # addtional log
    ROOT_LOGGING_LEVEL = "${var.root_logging_level}"
    LOG_LEVEL_SPRING_WEB = "${var.log_level_spring_web}"
    LOG_LEVEL_SSCS = "${var.log_level_sscs}"

  }
}

module "sscs-case-loader-vault" {
  source              = "git@github.com:contino/moj-module-key-vault?ref=master"
  name                = "sscs-case-loader-${var.env}"
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.sscs-case-loader.resource_group_name}"
  product_group_object_id = "87099fce-881e-4654-88d2-7c36b634e622"
}
