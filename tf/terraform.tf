terraform {
  required_version = "~> 0.12.0"
  backend "s3" {
    bucket = "kicker-tf-state"
    region = "eu-central-1"
    key    = "terraform-backend"

    dynamodb_table = "kicker-tf-locks"
  }
}

provider "aws" {
  region  = "eu-central-1"
  version = "~> 2.46.0"
}

provider "template" {
  version = "~> 2.1.2"
}

provider "null" {
  version = "~> 2.1.2"
}
