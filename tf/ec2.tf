data "template_file" "zookeeper" {
  template = file("tf/files/userdata_zookeeper.sh")

  vars = {
    ZOOKEEPER_VERSION = var.zookeeper_version
  }
}

resource "aws_instance" "zookeeper" {
  ami                         = "ami-07cda0db070313c52"
  availability_zone           = aws_subnet.public[0].availability_zone
  instance_type               = "t2.nano"
  subnet_id                   = aws_subnet.public[0].id
  associate_public_ip_address = true
  vpc_security_group_ids = [
    aws_security_group.zookeeper.id
  ]
  iam_instance_profile = aws_iam_instance_profile.zookeeper.name
  tags = {
    Name : "mrfoosball-zookeeper"
  }
  user_data = data.template_file.zookeeper.rendered
}
