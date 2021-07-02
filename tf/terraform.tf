terraform {
  required_version = "~> 1.0.0"
  backend "s3" {
    bucket = "kicker-tf-state"
    region = "eu-central-1"
    key    = "terraform-backend"

    dynamodb_table = "kicker-tf-locks"
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 3.0"
    }
  }
}

provider "aws" {
  region = "eu-central-1"
}

//provider "null" {
//  version = "~> 2.1.2"
//}
