output "vaultUri" {
  value = "${module.sscs-case-loader-key-vault.key_vault_uri}"
}

output "vaultName" {
  value = "${local.azureVaultName}"
}

output "microserviceName" {
  value = "${var.component}"
}
