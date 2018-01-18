module "sscs-case-loader" {
  source   = "git@github.com:contino/moj-module-webapp?ref=0.0.78"
  product  = "${var.product}-case-loader"
  location = "${var.location}"
  env      = "${var.env}"
  asename  = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  app_settings = {
    SSCS_CASE_LOADER_PORT = "${var.sscs_case_loader_server_port}"
    MANAGEMENT_SECURITY_ENABLED = "${var.management_security_enabled}"
  }
}
