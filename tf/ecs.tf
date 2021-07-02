resource "aws_ecs_cluster" "mrfoosball" {
  name = "MrFoosballCluster"

  capacity_providers = [
    "FARGATE"
  ]
}

resource "aws_ecs_task_definition" "mrfoosball" {
  family       = "mrfoosball"
  network_mode = "awsvpc"
  container_definitions = template_file("tf/files/container_definition.json", {
    CONTAINER_NAME       = var.container_name
    PORT                 = var.app_port
    IMAGE_NAME           = aws_ecr_repository.mrfoosball.repository_url
    IMAGE_TAG            = var.image_tag
    LOG_GROUP            = aws_cloudwatch_log_group.logs.name
    AWS_REGION           = data.aws_region.current.name
    ZOOKEEPER_HOSTS      = aws_route53_record.zookeeper.fqdn
    SLACK_CLIENT_ID      = var.slack_client_id
    SLACK_CLIENT_SECRET  = var.slack_client_secret
    SLACK_SIGNING_SECRET = var.slack_signing_secret
    DB_HOST              = aws_route53_record.postgres.fqdn
    DB_NAME              = var.db_name
    DB_USER              = var.db_user
    DB_PASSWORD          = var.db_password
    HIBERNATE_HBM2DDL    = var.hibernate_hbm2ddl
  })
  execution_role_arn = aws_iam_role.task_execution.arn
  task_role_arn      = aws_iam_role.task.arn
  cpu                = "256"
  memory             = "512"

  requires_compatibilities = [
    "FARGATE"
  ]
}

resource "aws_ecs_service" "service" {
  name                               = var.container_name
  task_definition                    = aws_ecs_task_definition.mrfoosball.arn
  cluster                            = aws_ecs_cluster.mrfoosball.id
  desired_count                      = 1
  deployment_minimum_healthy_percent = 50
  deployment_maximum_percent         = 200
  launch_type                        = "FARGATE"
  health_check_grace_period_seconds  = 60

  load_balancer {
    target_group_arn = aws_lb_target_group.target_group.arn
    container_name   = var.container_name
    container_port   = var.app_port
  }

  network_configuration {
    subnets          = aws_subnet.public.*.id
    assign_public_ip = true

    security_groups = [
      aws_security_group.ecs.id
    ]

  }
}
