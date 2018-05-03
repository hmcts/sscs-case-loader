resource "azurerm_key_vault_secret" "vault-s2s-url" {
  name      = "vault-s2s-url"
  value     = "${data.vault_generic_secret.idam_api.data["value"]}"
  vault_uri = "${module.sscs-case-loader-key-vault.key_vault_uri}"
}

resource "azurerm_key_vault_secret" "vault-s2s-secret" {
  name      = "vault-s2s-secret"
  value     = "${data.vault_generic_secret.sscs_s2s_secret.data["value"]}"
  vault_uri = "${module.sscs-case-loader-key-vault.key_vault_uri}"
}
