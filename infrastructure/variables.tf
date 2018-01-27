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
  default = "8082"
}

variable "management_security_enabled" {
  type    = "string"
  default = "true"
}

// CCD
variable "ccd-url" {
  default = "https://case-data-app.test.ccd.reform.hmcts.net:4481"
}

variable "ccd-userId" {
  default = "19"
}

variable "ccd-jurisdictionId" {
  default = "SSCS"
}

variable "ccd-caseTypeId" {
  default = "Benefit"
}

variable "ccd-eventId" {
  default = "appealCreated"
}

// IDAM
variable idam-s2s-auth-totp_secret {
  type = "string"
  default = "XXXXXX"
}

variable idam-s2s-auth-microservice {
  type = "string"
  default = "sscs"
}
