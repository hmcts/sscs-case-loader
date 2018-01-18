variable "product" {
  type = "string"
  default = "sscs"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "infrastructure_env" {
  default     = "dev"
  description = "Infrastructure environment to point to"
}

variable "sscs_case_loader_server_port" {
  type    = "string"
  default = "8080"
}

variable "management_security_enabled" {
  type    = "string"
  default = "true"
}


