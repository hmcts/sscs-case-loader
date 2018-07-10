resource "azurerm_key_vault_secret" "vault-sftp-host" {
  name      = "vault-sftp-host"
  value     = "${data.vault_generic_secret.sftp_host.data["value"]}"
  vault_uri = "${module.sscs-case-loader-key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "vault-sftp-port" {
  name      = "vault-sftp-port"
  value     = "${data.vault_generic_secret.sftp_port.data["value"]}"
  vault_uri = "${module.sscs-case-loader-key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "vault-sftp-key" {
  name      = "vault-sftp-key"
  value     = "${local.sftp_key}"
  vault_uri = "${module.sscs-case-loader-key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "vault-sftp-user" {
  name      = "vault-sftp-user"
  value     = "${var.gaps2_sftp_user}"
  vault_uri = "${module.sscs-case-loader-key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "vault-sftp-dir" {
  name      = "vault-sftp-dir"
  value     = "${var.gaps2_sftp_dir}"
  vault_uri = "${module.sscs-case-loader-key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "idam-user" {
  name      = "idam-user"
  value     = "${data.vault_generic_secret.idam_sscs_systemupdate_user.data["value"]}"
  vault_uri = "${module.sscs-case-loader-key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "idam-password" {
  name      = "idam-password"
  value     = "${data.vault_generic_secret.idam_sscs_systemupdate_password.data["value"]}"
  vault_uri = "${module.sscs-case-loader-key-vault.key_vault_uri}"
}
