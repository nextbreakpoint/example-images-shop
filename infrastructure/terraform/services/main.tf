##############################################################################
# Provider
##############################################################################

provider "aws" {
  region = "${var.aws_region}"
  profile = "${var.aws_profile}"
  version = "~> 0.1"
}

provider "terraform" {
  version = "~> 0.1"
}

provider "template" {
  version = "~> 0.1"
}

##############################################################################
# ECS Services and Tasks
##############################################################################

resource "aws_iam_role" "server_role" {
  name               = "Service"
  assume_role_policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ecs.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
POLICY
}

resource "aws_iam_role_policy" "server_role_policy" {
  name = "ServicePolicy"
  role = "${aws_iam_role.server_role.id}"

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ec2:AuthorizeSecurityGroupIngress",
                "ec2:Describe*",
                "elasticloadbalancing:DeregisterInstancesFromLoadBalancer",
                "elasticloadbalancing:DeregisterTargets",
                "elasticloadbalancing:Describe*",
                "elasticloadbalancing:RegisterInstancesWithLoadBalancer",
                "elasticloadbalancing:RegisterTargets",
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage"
            ],
            "Resource": "*"
        }
    ]
}
EOF
}

resource "aws_iam_role" "container_role" {
  name               = "Container"
  assume_role_policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ecs.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
POLICY
}

resource "aws_iam_role_policy" "container_role_policy" {
  name = "ContainerPolicy"
  role = "${aws_iam_role.container_role.id}"

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ec2:AuthorizeSecurityGroupIngress",
                "ec2:Describe*",
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage"
            ],
            "Resource": "*"
        }
    ]
}
EOF
}

resource "aws_iam_role" "container_tasks_role" {
  name               = "ContainerTasks"
  assume_role_policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
POLICY
}

resource "aws_iam_role_policy" "container_tasks_role_policy" {
  name = "ContainerTasksPolicy"
  role = "${aws_iam_role.container_tasks_role.id}"

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage"
            ],
            "Resource": "*"
        }
    ]
}
EOF
}

data "template_file" "accounts_template" {
  template = "${file("task-definitions/accounts.json")}"

  vars {
    account_id         = "${var.account_id}"
    bucket_name        = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  }
}

data "template_file" "designs_template" {
  template = "${file("task-definitions/designs.json")}"

  vars {
    account_id         = "${var.account_id}"
    bucket_name        = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  }
}

data "template_file" "auth_template" {
  template = "${file("task-definitions/auth.json")}"

  vars {
    account_id         = "${var.account_id}"
    bucket_name        = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  }
}

data "template_file" "web_template" {
  template = "${file("task-definitions/web.json")}"

  vars {
    account_id         = "${var.account_id}"
    bucket_name        = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  }
}

resource "aws_ecs_service" "accounts" {
  name            = "accounts"
  cluster         = "${data.terraform_remote_state.ecs.ecs-cluster-id}"
  task_definition = "${aws_ecs_task_definition.accounts.arn}"
  desired_count   = 1
  depends_on      = ["aws_iam_role_policy.container_role_policy"]

  placement_strategy {
    type  = "spread"
    field = "instanceId"
  }

  placement_constraints {
    type       = "memberOf"
    expression = "${format("attribute:ecs.availability-zone in [%sa, %sb]", var.aws_region, var.aws_region)}"
  }
}

resource "aws_ecs_task_definition" "accounts" {
  family                = "accounts"
  container_definitions = "${data.template_file.accounts_template.rendered}"
  task_role_arn         = "${aws_iam_role.container_tasks_role.arn}"

  placement_constraints {
    type       = "memberOf"
    expression = "${format("attribute:ecs.availability-zone in [%sa, %sb]", var.aws_region, var.aws_region)}"
  }
}

resource "aws_ecs_service" "designs" {
  name            = "designs"
  cluster         = "${data.terraform_remote_state.ecs.ecs-cluster-id}"
  task_definition = "${aws_ecs_task_definition.designs.arn}"
  desired_count   = 1
  depends_on      = ["aws_iam_role_policy.container_role_policy"]

  placement_strategy {
    type  = "spread"
    field = "instanceId"
  }

  placement_constraints {
    type       = "memberOf"
    expression = "${format("attribute:ecs.availability-zone in [%sa, %sb]", var.aws_region, var.aws_region)}"
  }
}

resource "aws_ecs_task_definition" "designs" {
  family                = "designs"
  container_definitions = "${data.template_file.designs_template.rendered}"
  task_role_arn         = "${aws_iam_role.container_tasks_role.arn}"

  placement_constraints {
    type       = "memberOf"
    expression = "${format("attribute:ecs.availability-zone in [%sa, %sb]", var.aws_region, var.aws_region)}"
  }
}

resource "aws_ecs_service" "auth" {
  name            = "auth"
  cluster         = "${data.terraform_remote_state.ecs.ecs-cluster-id}"
  task_definition = "${aws_ecs_task_definition.auth.arn}"
  desired_count   = 1
  depends_on      = ["aws_iam_role_policy.container_role_policy"]

  placement_strategy {
    type  = "spread"
    field = "instanceId"
  }

  placement_constraints {
    type       = "memberOf"
    expression = "${format("attribute:ecs.availability-zone in [%sa, %sb]", var.aws_region, var.aws_region)}"
  }
}

resource "aws_ecs_task_definition" "auth" {
  family                = "auth"
  container_definitions = "${data.template_file.auth_template.rendered}"
  task_role_arn         = "${aws_iam_role.container_tasks_role.arn}"

  placement_constraints {
    type       = "memberOf"
    expression = "${format("attribute:ecs.availability-zone in [%sa, %sb]", var.aws_region, var.aws_region)}"
  }
}

resource "aws_ecs_service" "web" {
  name            = "web"
  cluster         = "${data.terraform_remote_state.ecs.ecs-cluster-id}"
  task_definition = "${aws_ecs_task_definition.server.arn}"
  desired_count   = 1
  depends_on      = ["aws_iam_role_policy.server_role_policy"]

  placement_strategy {
    type  = "spread"
    field = "instanceId"
  }

  iam_role        = "${aws_iam_role.server_role.arn}"

  load_balancer {
    elb_name       = "${data.terraform_remote_state.ecs.ecs-cluster-elb-name}"
    container_name = "web"
    container_port = 8080
  }

  placement_constraints {
    type       = "memberOf"
    expression = "${format("attribute:ecs.availability-zone in [%sa, %sb]", var.aws_region, var.aws_region)}"
  }
}

resource "aws_ecs_task_definition" "server" {
  family                = "web"
  container_definitions = "${data.template_file.web_template.rendered}"
  task_role_arn         = "${aws_iam_role.container_tasks_role.arn}"

  placement_constraints {
    type       = "memberOf"
    expression = "${format("attribute:ecs.availability-zone in [%sa, %sb]", var.aws_region, var.aws_region)}"
  }
}

##############################################################################
# S3 Bucket objects
##############################################################################

resource "aws_s3_bucket_object" "accounts" {
  bucket = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  key    = "environments/production/config/accounts.json"
  source = "environments/production/config/accounts.json"
  etag   = "${md5(file("environments/production/config/accounts.json"))}"
}

resource "aws_s3_bucket_object" "designs" {
  bucket = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  key    = "environments/production/config/designs.json"
  source = "environments/production/config/designs.json"
  etag   = "${md5(file("environments/production/config/designs.json"))}"
}

resource "aws_s3_bucket_object" "auth" {
  bucket = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  key    = "environments/production/config/auth.json"
  source = "environments/production/config/auth.json"
  etag   = "${md5(file("environments/production/config/auth.json"))}"
}

resource "aws_s3_bucket_object" "web" {
  bucket = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  key    = "environments/production/config/web.json"
  source = "environments/production/config/web.json"
  etag   = "${md5(file("environments/production/config/web.json"))}"
}

resource "aws_s3_bucket_object" "keystore-auth" {
  bucket = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  key    = "environments/production/keystores/keystore-auth.jceks"
  source = "environments/production/keystores/keystore-auth.jceks"
  etag   = "${md5(file("environments/production/keystores/keystore-auth.jceks"))}"
}

resource "aws_s3_bucket_object" "keystore-client" {
  bucket = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  key    = "environments/production/keystores/keystore-client.jks"
  source = "environments/production/keystores/keystore-client.jks"
  etag   = "${md5(file("environments/production/keystores/keystore-client.jks"))}"
}

resource "aws_s3_bucket_object" "keystore-server" {
  bucket = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  key    = "environments/production/keystores/keystore-server.jks"
  source = "environments/production/keystores/keystore-server.jks"
  etag   = "${md5(file("environments/production/keystores/keystore-server.jks"))}"
}

resource "aws_s3_bucket_object" "truststore-client" {
  bucket = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  key    = "environments/production/keystores/truststore-client.jks"
  source = "environments/production/keystores/truststore-client.jks"
  etag   = "${md5(file("environments/production/keystores/truststore-client.jks"))}"
}

resource "aws_s3_bucket_object" "truststore-server" {
  bucket = "${data.terraform_remote_state.ecs.ecs-cluster-bucket-name}"
  key    = "environments/production/keystores/truststore-server.jks"
  source = "environments/production/keystores/truststore-server.jks"
  etag   = "${md5(file("environments/production/keystores/truststore-server.jks"))}"
}