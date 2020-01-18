resource "aws_route53_zone" "main" {
  name = "mrfoosball.com"
}

resource "aws_route53_record" "private-ns" {
  zone_id = aws_route53_zone.main.zone_id
  name    = aws_route53_zone.private.name
  type    = "NS"
  ttl     = "30"

  records = aws_route53_zone.private.name_servers
}

resource "aws_route53_record" "main" {
  name    = "mrfoosball.com"
  type    = "A"
  zone_id = aws_route53_zone.main.id

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
  zone_id         = aws_route53_zone.main.zone_id
  records = [
    element(aws_acm_certificate.cert.domain_validation_options.*.resource_record_value, count.index)
  ]
  ttl = 60
}

resource "aws_route53_record" "google-txt" {
  zone_id = aws_route53_zone.main.zone_id
  name    = aws_route53_zone.main.name
  type    = "TXT"
  ttl     = "30"

  records = [
    "google-site-verification=jYcAVGKJV7ccLMIa4awhvhy_Fui3YvVKpBT-w6YSlGo"
  ]
}

resource "aws_route53_record" "google-mx" {
  zone_id = aws_route53_zone.main.zone_id
  name    = aws_route53_zone.main.name
  type    = "MX"
  ttl     = "86400"

  records = [
    "1 ASPMX.L.GOOGLE.COM",
    "5 ALT1.ASPMX.L.GOOGLE.COM",
    "5 ALT2.ASPMX.L.GOOGLE.COM",
    "10 ASPMX2.GOOGLEMAIL.COM",
    "10 ASPMX3.GOOGLEMAIL.COM"
  ]
}

resource "aws_route53_zone" "private" {
  name = "private.mrfoosball.com"

  vpc {
    vpc_id = aws_vpc.main.id
  }
}

resource "aws_route53_record" "zookeeper" {
  name    = "zookeeper"
  type    = "A"
  zone_id = aws_route53_zone.private.zone_id
  records = [
    aws_instance.zookeeper.private_ip
  ]
  ttl = 30
}

resource "aws_route53_record" "postgres" {
  name    = "postgres"
  type    = "CNAME"
  zone_id = aws_route53_zone.private.zone_id
  records = [
    aws_db_instance.postgres.address
  ]
  ttl = 30
}


