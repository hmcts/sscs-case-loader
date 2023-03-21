provider "azurerm" {
  features {}
}

# Make sure the resource group exists
resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = var.location

  tags = "${merge(var.common_tags,
    tomap({"lastUpdated", "${timestamp()}"})
    )}"
}
