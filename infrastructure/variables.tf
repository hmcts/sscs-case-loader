variable "product" {
  type = "string"
}

variable "component" {
  type = "string"
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

variable "subscription" {
  type = "string"
}

variable "ilbIp"{}

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

variable "idam_redirect_url" {
  default = "https://sscs-case-loader-sandbox.service.core-compute-sandbox.internal"
}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  type        = "string"
}

variable "common_tags" {
  type = "map"
}

variable "raw_product" {
  default = "sscs" // jenkins-library overrides product for PRs and adds e.g. pr-118-cmc
}

variable "sftp_key_name" {
  default = "gaps2-service-sftp-private-key"
}

variable "number_processed_cases_to_refresh_tokens" {
  default = "4000"
}
