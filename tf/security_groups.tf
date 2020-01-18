resource "aws_security_group" "load_balancer" {
  name        = "load-balancer"
  vpc_id      = aws_vpc.main.id
  description = "Load Balancer Security Group"

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "zookeeper" {
  name        = "zookeeper"
  vpc_id      = aws_vpc.main.id
  description = "Security group for zookeeper"

  ingress {
    from_port   = 2181
    to_port     = 2181
    protocol    = "tcp"
    cidr_blocks = aws_subnet.private.*.cidr_block
  }

  ingress {
    from_port   = 2888
    to_port     = 3888
    protocol    = "tcp"
    cidr_blocks = aws_subnet.private.*.cidr_block
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "postgres" {
  name        = "postgres"
  vpc_id      = aws_vpc.main.id
  description = "Security group for postgres rds server"

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = aws_subnet.private.*.cidr_block
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "ecs" {
  name        = "mrfoosball-ecs"
  vpc_id      = aws_vpc.main.id
  description = "Security group for mrfoosball application containers"

  ingress {
    from_port = var.app_port
    to_port   = var.app_port
    protocol  = "tcp"
    security_groups = [
      aws_security_group.load_balancer.id
    ]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
