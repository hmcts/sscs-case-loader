provider "vault" {
  address = "https://vault.reform.hmcts.net:6200"
}

# Make sure the resource group exists
resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location}"
  tags = "${merge(var.common_tags,
    map("lastUpdated", "${timestamp()}")
    )}"
}

data "azurerm_key_vault" "sscs_key_vault" {
  name = "${local.azureVaultName}"
  resource_group_name = "${local.azureVaultName}"
}

data "azurerm_key_vault_secret" "sscs-s2s-secret" {
  name = "sscs-s2s-secret"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam-sscs-systemupdate-user" {
  name = "idam-sscs-systemupdate-user"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam-sscs-systemupdate-password" {
  name = "idam-sscs-systemupdate-password"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam-sscs-oauth2-client-secret" {
  name = "idam-sscs-oauth2-client-secret"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam-api" {
  name = "idam-api"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam-s2s-api" {
  name = "idam-s2s-api"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "sftp-host" {
  name = "sftp-host"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "sftp-port" {
  name = "sftp-port"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "gaps2-service-sftp-private-key" {
  name = "${var.sftp_key_name}"
  vault_uri = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}


locals {
  aseName = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.aseName}"

  ccdApi = "http://ccd-data-store-api-${local.local_env}.service.${local.local_ase}.internal"
  s2sCnpUrl = "http://rpe-service-auth-provider-${local.local_env}.service.${local.local_ase}.internal"

  previewVaultName       = "${var.product}-${var.component}"
  nonPreviewVaultName    = "${var.product}-${var.component}-${var.env}"
  vaultName              = "${(var.env == "preview") ? local.previewVaultName : local.nonPreviewVaultName}"

  azurePreviewVaultName       = "${var.raw_product}-aat"
  azureNonPreviewVaultName    = "${var.raw_product}-${var.env}"
  azureVaultName              = "${(var.env == "preview" || var.env == "spreview") ? local.azurePreviewVaultName : local.azureNonPreviewVaultName}"
}

module "sscs-case-loader" {
  source       = "git@github.com:contino/moj-module-webapp?ref=master"
  product      = "${var.product}-${var.component}"
  location     = "${var.location}"
  env          = "${var.env}"
  ilbIp        = "${var.ilbIp}"
  is_frontend  = false
  subscription = "${var.subscription}"
  capacity     = "1"
  common_tags  = "${var.common_tags}"

  app_settings = {
    CORE_CASE_DATA_API_URL = "${local.ccdApi}"
    CORE_CASE_DATA_JURISDICTION_ID = "${var.core_case_data_jurisdiction_id}"
    CORE_CASE_DATA_CASE_TYPE_ID = "${var.core_case_data_case_type_id}"

    IDAM_URL = "${data.azurerm_key_vault_secret.idam-api.value}"

    IDAM.S2S-AUTH.TOTP_SECRET = "${data.azurerm_key_vault_secret.sscs-s2s-secret.value}"
    IDAM.S2S-AUTH = "${local.s2sCnpUrl}"
    IDAM.S2S-AUTH.MICROSERVICE = "${var.idam_s2s_auth_microservice}"

    IDAM_SSCS_SYSTEMUPDATE_USER = "${data.azurerm_key_vault_secret.idam-sscs-systemupdate-user.value}"
    IDAM_SSCS_SYSTEMUPDATE_PASSWORD = "${data.azurerm_key_vault_secret.idam-sscs-systemupdate-password.value}"

    IDAM_OAUTH2_CLIENT_ID = "${var.idam_oauth2_client_id}"
    IDAM_OAUTH2_CLIENT_SECRET = "${data.azurerm_key_vault_secret.idam-sscs-oauth2-client-secret.value}"
    IDAM_OAUTH2_REDIRECT_URL = "${var.idam_redirect_url}"

    GAPS2_KEY_LOCATION = "${data.azurerm_key_vault_secret.gaps2-service-sftp-private-key.value}"
    GAPS2_SFTP_HOST = "${data.azurerm_key_vault_secret.sftp-host.value}"
    GAPS2_SFTP_PORT = "${data.azurerm_key_vault_secret.sftp-port.value}"
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

module "sscs-case-loader-key-vault" {
  source              = "git@github.com:hmcts/moj-module-key-vault?ref=master"
  name                = "${local.vaultName}"
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  product_group_object_id = "70de400b-4f47-4f25-a4f0-45e1ee4e4ae3"
}
