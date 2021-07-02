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


