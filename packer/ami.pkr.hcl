variable "profile" {
  type    = string
  default = "dev"
}
variable "aws_region" {
  type    = string
  default = "us-east-1"
}
variable "ssh_username" {
  type    = string
  default = "ec2-user"
}
variable "source_ami" {
  type    = string
  default = "ami-0dfcb1ef8550277af"
}
variable "vpc_id" {
  type    = string
  default = "vpc-03e10d3f04590bf02"
}
variable "shared_account_id" {
  type    = string
  default = "810882337805"
}

# Configure the builder
source "amazon-ebs" "csye6225_ami" {
  profile         = "${var.profile}"
  region          = "${var.aws_region}"
  ami_regions     = ["us-east-1"]
  ami_name        = "csye6225_${formatdate("YYYY_MM_DD_hh_mm_ss", timestamp())}"
  ami_description = "An example AMI created with Packer"
  ssh_username    = "${var.ssh_username}"
  source_ami      = "${var.source_ami}"
  vpc_id          = "${var.vpc_id}"
  instance_type   = "t2.micro"

  launch_block_device_mappings {
    delete_on_termination = true
    device_name           = "/dev/xvda"
    volume_size           = 8
    volume_type           = "gp2"
  }
  ami_users = ["${var.shared_account_id}"]
}

# Build the AMI
build {
  sources = ["source.amazon-ebs.csye6225_ami"]
  provisioner "shell" {
    script = "packer/buildingAMI.sh"
  }
  provisioner "file" {
    source      = "target/webapp-0.0.1-SNAPSHOT.jar"
    destination = "/home/ec2-user/webapp/webapp-0.0.1-SNAPSHOT.jar"
  }
  provisioner "file" {
    source      = "packer/cloudWatchConfig.json"
    destination = "/home/ec2-user/webapp/cloudWatchConfig.json"
  }
  provisioner "file" {
    source      = "packer/cloudWatch.service"
    destination = "/home/ec2-user/webapp/cloudWatch.service"
  }
}