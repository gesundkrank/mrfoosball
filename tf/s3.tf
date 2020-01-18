resource "aws_s3_bucket" "access_logs" {
  bucket = "mrfoosball-access-logs"
  acl    = "private"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::054676820928:root"
      },
      "Action": "s3:PutObject",
      "Resource": "arn:aws:s3:::mrfoosball-access-logs/*"
    }
  ]
 }
EOF
}
