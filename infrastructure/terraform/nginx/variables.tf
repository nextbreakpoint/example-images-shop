###################################################################
# AWS configuration below
###################################################################

variable "aws_region" {
  default = "eu-west-1"
}

variable "aws_profile" {
  default = "default"
}

##############################################################################
# Resources configuration below
##############################################################################

### MANDATORY ###
variable "environment" {}

### MANDATORY ###
variable "colour" {}

### MANDATORY ###
variable "account_id" {}

### MANDATORY ###
variable "secrets_bucket_name" {}

### MANDATORY ###
variable "hosted_zone_name" {}