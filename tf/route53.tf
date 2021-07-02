resource "aws_route53_record" "main" {
  name    = "mrfoosball.com"
  type    = "A"
  zone_id = module.network_setup.main_zone_id

  alias {
    evaluate_target_health = true
    name                   = aws_lb.load_balancer.dns_name
    zone_id                = aws_lb.load_balancer.zone_id
  }
}

resource "aws_route53_record" "cert_validation" {
  count           = length(aws_acm_certificate.cert.domain_validation_options)
  allow_overwrite = true
  name            = element(aws_acm_certificate.cert.domain_validation_options.*.resource_record_name, count.index)
  type            = element(aws_acm_certificate.cert.domain_validation_options.*.resource_record_type, count.index)
  zone_id         = module.network_setup.private_zone_id
  records = [
    element(aws_acm_certificate.cert.domain_validation_options.*.resource_record_value, count.index)
  ]
  ttl = 60
}

resource "aws_route53_record" "zookeeper" {
  name    = "zookeeper"
  type    = "A"
  zone_id =module.network_setup.private_zone_id
  records = [
    aws_instance.zookeeper.private_ip
  ]
  ttl = 30
}

resource "aws_route53_record" "postgres" {
  name    = "postgres"
  type    = "CNAME"
  zone_id = module.network_setup.private_zone_id
  records = [
    aws_db_instance.postgres.address
  ]
  ttl = 30
}
