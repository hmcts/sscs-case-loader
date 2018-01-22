module "sscs-case-loader" {
  source = "git@github.com:contino/moj-module-webapp?ref=0.0.78"
  product = "${var.product}-case-loader"
  location = "${var.location}"
  env = "${var.env}"
  asename = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  app_settings = {
    SSCS_CASE_LOADER_PORT = "${var.sscs_case_loader_server_port}"
    MANAGEMENT_SECURITY_ENABLED = "${var.management_security_enabled}"

    // CCD
    CORE_CASE_DATA_API_URL = "${var.ccd-url}"
    CORE_CASE_DATA_USER_ID = "${var.ccd-userId}"
    CORE_CASE_DATA_JURISDICTION_ID = "${var.ccd-jurisdictionId}"
    CORE_CASE_DATA_CASE_TYPE_ID = "${var.ccd-caseTypeId}"
    CORE_CASE_DATA_EVENT_ID = "${var.ccd-eventId}"
  }

}
