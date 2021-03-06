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
import java.net.URI;
import java.net.URISyntaxException;

import static com.amazonaws.auth.internal.SignerConstants.*;

public class AwsAuthentication implements Authentication {

    private static final String SERVICE_NAME = "es";
    private final DefaultAwsRegionProviderChain regionProvider = new DefaultAwsRegionProviderChain();
    private final AWSCredentialsProvider credsProvider = new DefaultAWSCredentialsProviderChain();

    public void addAuth(HttpURLConnection urlConnection, String body) {
        AWS4Signer signer = new AWS4Signer();

        signer.setServiceName(SERVICE_NAME);
        signer.setRegionName(regionProvider.getRegion());


        Request<?> request = new DefaultRequest<Void>(SERVICE_NAME);
        request.setHttpMethod(HttpMethodName.fromValue(urlConnection.getRequestMethod()));
        request.setContent(new ByteArrayInputStream(body.getBytes()));

        try {
            URI uri = urlConnection.getURL().toURI();
            request.setEndpoint(new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null));
            request.setResourcePath(uri.getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


        // Sign request with supplied creds
        signer.sign(request, credsProvider.getCredentials());

        String amzDate = request.getHeaders().get(X_AMZ_DATE);
        if (amzDate != null) urlConnection.setRequestProperty(X_AMZ_DATE, amzDate);

        String authorization = request.getHeaders().get(AUTHORIZATION);
        if (authorization != null) urlConnection.setRequestProperty(AUTHORIZATION, authorization);

        String securityToken = request.getHeaders().get(X_AMZ_SECURITY_TOKEN);
        if (securityToken != null) urlConnection.setRequestProperty(X_AMZ_SECURITY_TOKEN, securityToken);

    }
}
