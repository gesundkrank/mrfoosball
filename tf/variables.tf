variable "container_name" {
  default = "mrfoosball"
}

variable "app_port" {
  default = 8080
  type    = number
}

variable "image_tag" {
  default = "latest"
}

variable "slack_client_id" {
  description = "Slack App Client Id"
}

variable "slack_client_secret" {
  description = "Slack App Client Secret"
}

variable "slack_signing_secret" {
  description = "Slack App Signing Secret"
}

variable "db_name" {
  description = "Postgres Database Name"
  default     = "mrfoosball"
}

variable "db_user" {
  description = "Postgres User Name"
  default     = "mrfoosball"
}

variable "db_password" {
  description = "Postgres Password"
}

variable "zookeeper_version" {
  default = "3.5.6"
}

variable "hibernate_hbm2ddl" {
  default = "validate"
}
