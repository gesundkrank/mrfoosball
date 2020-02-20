resource "aws_db_subnet_group" "postgres" {
  name       = "postgres_subnets"
  subnet_ids = aws_subnet.public.*.id
}

resource "aws_db_instance" "postgres" {
  identifier           = "mrfoosball-db"
  name                 = var.db_name
  instance_class       = "db.t2.micro"
  engine               = "postgres"
  allocated_storage    = 5
  storage_type         = "gp2"
  username             = var.db_user
  password             = var.db_password
  db_subnet_group_name = aws_db_subnet_group.postgres.name
  vpc_security_group_ids = [
    aws_security_group.postgres.id
  ]
  multi_az            = false
  skip_final_snapshot = true
  publicly_accessible = true
}
