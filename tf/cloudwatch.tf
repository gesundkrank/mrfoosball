resource "aws_cloudwatch_log_group" "logs" {
  name              = "mrfoosball"
  retention_in_days = 30
}
