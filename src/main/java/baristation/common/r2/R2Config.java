package baristation.common.r2;

import java.net.URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
@EnableConfigurationProperties(R2Properties.class)
@ConditionalOnProperty(prefix = "app.r2", name = "enabled", havingValue = "true", matchIfMissing = true)
public class R2Config {

    @Bean
    public S3Client s3Client(R2Properties props) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                props.accessKeyId(),
                props.secretAccessKey()
        );

        return S3Client.builder()
                .endpointOverride(URI.create("https://" + props.accountId() + ".r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto"))
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .chunkedEncodingEnabled(false)
                                .build()
                )
                .build();
    }
}