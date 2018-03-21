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

variable "idam_s2s_auth_microservice"{
  default = "sscs"
}

variable "idam_oauth2_client_id"{
  default = "sscs"
}

variable "gaps2_sftp_user"{
  default = "sscs-sftp-test"
}

variable "gaps2_sftp_dir"{
  default = "incoming"
}

variable "sscs_case_loader_cron_schedule" {
  default = "0 0/5 * * * ?"
}

variable "ignore_cases_before_date" {
  default = "2018-07-01"
}

variable "root_logging_level" {
  default = "INFO"
}

variable "log_level_spring_web" {
  default = "INFO"
}

variable "log_level_sscs" {
  default = "INFO"
}

variable "jenkins_AAD_objectId" {
  type = "string"
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environemnt variables and not normally required to be specified."
}

variable "client_id" {
  description = "(Required) The object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies. This is usually sourced from environment variables and not normally required to be specified."
}
