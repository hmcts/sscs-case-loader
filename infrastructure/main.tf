provider "azurerm" {
  features {}
}

# Make sure the resource group exists
resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location

  tags = (merge(var.common_tags,
    tomap({ "lastUpdated" = "${timestamp()}" })
  ))
}

data "azurerm_user_assigned_identity" "jenkins" {
  name                = "jenkins-${var.env}-mi"
  resource_group_name = "managed-identities-${var.env}-rg"
}

data "azurerm_key_vault" "case_loader" {
  name                = "${var.product}-${var.component}-${var.env}"
  resource_group_name = "${var.product}-${var.component}-${var.env}"
}

resource "azurerm_key_vault_access_policy" "jenkins" {
  key_vault_id = data.azurerm_key_vault.case_loader.id
  tenant_id    = var.tenant_id
  object_id    = data.azurerm_user_assigned_identity.jenkins.principal_id

  certificate_permissions = [
    "Create",
    "Delete",
    "DeleteIssuers",
    "Get",
    "GetIssuers",
    "Import",
    "List",
    "ListIssuers",
    "SetIssuers",
    "Update",
    "ManageContacts",
    "ManageIssuers",
  ]

  key_permissions = [
    "Create",
    "List",
    "Get",
    "Delete",
    "Update",
    "Import",
    "Backup",
    "Restore",
    "Decrypt",
    "Encrypt",
    "UnwrapKey",
    "WrapKey",
    "Sign",
    "Verify",
    "GetRotationPolicy",
  ]

  secret_permissions = [
    "Set",
    "List",
    "Get",
    "Delete",
    "Recover",
    "Purge",
  ]
}
