output "vaultUri" {
  value = "${data.azurerm_key_vault.sscs_key_vault.vault_uri}"
}

output "vaultName" {
  value = "${local.azureVaultName}"
}

output "microserviceName" {
  value = "${var.component}"
}

output "send_to_dwp_enabled" {
  value = "${var.send_to_dwp_enabled}"
}
