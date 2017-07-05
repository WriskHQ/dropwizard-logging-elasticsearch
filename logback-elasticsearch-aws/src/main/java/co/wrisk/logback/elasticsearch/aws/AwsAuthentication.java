package co.wrisk.logback.elasticsearch.aws;

import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.internetitem.logback.elasticsearch.config.Authentication;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

public class AwsAuthentication implements Authentication {

    private static final String SERVICE_NAME = "es";
    private final DefaultAwsRegionProviderChain regionProvider = new DefaultAwsRegionProviderChain();
    private final AWSCredentialsProvider credsProvider = new DefaultAWSCredentialsProviderChain();

    public void addAuth(HttpURLConnection urlConnection, String body) {
        AWS4Signer signer = new AWS4Signer();

        signer.setServiceName(SERVICE_NAME);
        signer.setRegionName(regionProvider.getRegion());


        Request<?> request = new DefaultRequest<Void>(SERVICE_NAME);
        request.setContent(new ByteArrayInputStream("".getBytes()));
        try {
            request.setEndpoint(urlConnection.getURL().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        request.setHttpMethod(HttpMethodName.GET);

        // Sign request with supplied creds
        signer.sign(request, credsProvider.getCredentials());

        urlConnection.setRequestProperty("host", request.getHeaders().get("Host"));
        urlConnection.setRequestProperty("x-amz-date", request.getHeaders().get("X-Amz-Date"));
        urlConnection.setRequestProperty("authorization", request.getHeaders().get("Authorization"));
        String securityToken = request.getHeaders().get("x-amz-security-token");
        if (securityToken != null) urlConnection.setRequestProperty("x-amz-security-token", securityToken);

    }
}
