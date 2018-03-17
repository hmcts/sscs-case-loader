output "vaultUri" {
  value = "${module.sscs-case-loader-vault.key_vault_uri}"
}

output "vaultName" {
  value = "${module.sscs-case-loader-vault.key_vault_name}"
}
