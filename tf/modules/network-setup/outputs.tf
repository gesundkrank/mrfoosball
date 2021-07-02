output "main_id" {
  value = aws_route53_zone.main.id
}

output "main_zone_id" {
  value = aws_route53_zone.main.zone_id
}

output "private_zone_id" {
  value = aws_route53_zone.private.zone_id
}
