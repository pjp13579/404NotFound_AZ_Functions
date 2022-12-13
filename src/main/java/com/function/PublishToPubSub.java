package com.function;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.CosmosDBTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * Azure Functions with HTTP Trigger.
 */
public class PublishToPubSub {
    /**
     * This function listens at endpoint "/api/publishToPubSub". Two ways to invoke
     * it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/publishToPubSub
     * 2. curl {your host}/api/publishToPubSub?name=HTTP%20Query
     * 
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws IOException
     */
    @FunctionName("publishToPubSub")
    public HttpResponseMessage toSocket(
            @HttpTrigger(name = "req", methods = { HttpMethod.GET,
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            @BindingName("hub") String hub,
            final ExecutionContext context) throws IOException, InterruptedException, URISyntaxException {
        context.getLogger().info("Java HTTP trigger processed a request.");

        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
                .connectionString(
                        "Endpoint=https://jorge933746395.webpubsub.azure.com;AccessKey=VLjbv5PXwqU5jM0Ne58/kcKMCQ2bz6EtxK6FTMs5ULg=;Version=1.0;")
                .hub(hub)
                .buildClient();

        GetClientAccessTokenOptions options = new GetClientAccessTokenOptions();
        WebPubSubClientAccessToken token = webPubSubServiceClient.getClientAccessToken(options);
        String clientAccessURL = token.getUrl();

        if (!request.getBody().isPresent()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("the wrong is in the request")
                    .build();
        }

        webPubSubServiceClient.sendToAll(request.getBody().get().toString(), WebPubSubContentType.TEXT_PLAIN);
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .build();

    }

    @FunctionName("cosmosDBMonitor")
    public void cosmosDbProcessor(
            @CosmosDBTrigger(name = "items", databaseName = "progMobile", collectionName = "news", createLeaseCollectionIfNotExists = true, connectionStringSetting = "CosmosDBConnectionString") String news,
            final ExecutionContext context) {

        WebPubSubServiceClient webPubSubServiceClient = new WebPubSubServiceClientBuilder()
                .connectionString(
                        "Endpoint=https://jorge933746395.webpubsub.azure.com;AccessKey=VLjbv5PXwqU5jM0Ne58/kcKMCQ2bz6EtxK6FTMs5ULg=;Version=1.0;")
                .hub("global")
                .buildClient();

        GetClientAccessTokenOptions options = new GetClientAccessTokenOptions();
        WebPubSubClientAccessToken token = webPubSubServiceClient.getClientAccessToken(options);
        String clientAccessURL = token.getUrl();
        
        String neww = news.substring(1, news.length()-1);
        System.out.println(neww);
        
        webPubSubServiceClient.sendToAll(neww, WebPubSubContentType.TEXT_PLAIN);

    }

}
