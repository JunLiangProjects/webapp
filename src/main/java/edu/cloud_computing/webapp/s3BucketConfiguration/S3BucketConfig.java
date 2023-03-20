//package edu.cloud_computing.webapp.s3BucketConfiguration;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client;
//
//@Configuration
//public class S3BucketConfig {
//    @Bean
//    public S3Client createS3Client() {
//        Region region = Region.US_EAST_1;
//        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("AKIAQJ4SCHKIRFDX3CVD", "0Y3JLytg7BXvsv4x5hK4ZlfyMJtpQu9G7lkoNYFY");//改成IAM role验证方式
//        return S3Client.builder().region(region).credentialsProvider(StaticCredentialsProvider.create(awsCreds)).build();
//    }
//}