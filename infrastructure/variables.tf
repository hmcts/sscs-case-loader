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
  default = "false"
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

variable "max_capacity" {
  default = "1"
}

variable "sftp_key_location" {
  default = "gaps2_service_sftp_private_key"
}
