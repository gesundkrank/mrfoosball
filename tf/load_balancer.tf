resource "aws_lb" "load_balancer" {
  name = "mrfoosball-load-balancer"
  security_groups = [
    aws_security_group.load_balancer.id
  ]
  subnets = aws_subnet.public.*.id

  access_logs {
    bucket  = aws_s3_bucket.access_logs.bucket
    enabled = true
  }
}

resource "aws_lb_target_group" "target_group" {
  name        = "mrfoosball-lb-target-group"
  port        = var.app_port
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = aws_vpc.main.id
  slow_start  = 60

  health_check {
    protocol = "HTTP"
    path     = "/api/health"
    matcher  = "200"
    timeout  = 5
    interval = 10
  }
}

resource "aws_lb_listener" "default" {
  load_balancer_arn = aws_lb.load_balancer.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = aws_acm_certificate.cert.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.target_group.arn
  }
}

resource "aws_lb_listener" "redirect" {
  load_balancer_arn = aws_lb.load_balancer.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      status_code = "HTTP_301"
      protocol    = "HTTPS"
      port        = "443"
    }
  }
}
