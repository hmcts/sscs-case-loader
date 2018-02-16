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

variable "management_security_enabled" {
  type    = "string"
  default = "true"
}

variable "subscription" {
  type = "string"
}

variable "ilbIp"{}

variable "core_case_data_user_id"{
  default = "16"
}

variable "core_case_data_jurisdiction_id"{
  default = "SSCS"
}
variable "core_case_data_case_type_id"{
  default = "Benefit"
}
variable "core_case_data_event_id"{
  default = "appealCreated"
}

variable "idam_url"{
  type    = "string"
  default = "http://betaDevBccidamAppLB.reform.hmcts.net"
}

variable "idam_s2s_auth_microservice"{
  default = "sscs"
}
variable "idam_s2s_auth"{
  type    = "string"
  default = "http://betaDevBccidamS2SLB.reform.hmcts.net"
}

variable "idam_oauth2_client_id"{
  default = "sscs"
}

variable "idam_oauth2_redirect_url"{
  default = "http://localhost"
}

variable "gaps2_sftp_host"{
  default = "sftp-dev.reform.hmcts.net"
}
variable "gaps2_sftp_port"{
  default = 9000
}
variable "gaps2_sftp_user"{
  default = "sscs-sftp-test"
}
variable "gaps2_sftp_dir"{
  default = "incoming"
}
