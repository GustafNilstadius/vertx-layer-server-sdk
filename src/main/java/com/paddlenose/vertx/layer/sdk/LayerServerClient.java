package com.paddlenose.vertx.layer.sdk;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

/**
 * @author Gustaf Nilstadius
 *         Created by Gustaf Nilstadius ( hipernx ) on 2017-01-07.
 */
public class LayerServerClient {
    /**
     * A HttpClient, requires default host
     */
    private final HttpClient client;
    /**
     * Application id for Layer API
     */
    private final String layer_app_id;

    /**
     * Application token for Layer API
     */
    private final String layer_app_token;
    /**
     * LayerClient implements LayerInterface.
     *
     * @param client A HttpClient, requires default host
     * @param layer_Identity Requires layer_app_id and layer_app_token
     */
    public LayerServerClient(HttpClient client, LayerServerOptions options) {
        this.client = client;
        this.layer_app_id = options.getString("layer_app_id");
        this.layer_app_token = options.getString("layer_app_token");
    }

    /**
     * Gets 100 conversations for authenticated user from Layer API. Sorted by last message.
     *
     * @param future Handler, requires error handler.
     * @param user_ID User id for which conversations will be requested
     */
    public void getConversationsAsUser(Handler<HttpClientResponse> future, String user_ID) {
        client.request(HttpMethod.GET, "/apps/" + layer_app_id + "/user/" + user_ID + "/conversations?sort_by=last_message", future)
                .putHeader("accept", "application/vnd.layer+json; version=2.0")
                .putHeader("Authorization", "Bearer " + layer_app_token )
                .putHeader("Content-Type", "application/json")
                .exceptionHandler(err -> {System.err.println(err.getMessage()); err.printStackTrace(); future.handle(null);})
                .end();
    }

    /**
     * Gets the last 100 messages in a conversation as User
     *
     * @param future Handler, requires error handler.
     * @param conversation_UUID UUID specifying conversation
     * @param user_ID User id for which conversation messages will be requested
     */
    public void getConversationMessageAsUser(Handler<HttpClientResponse> future, String conversation_UUID, String user_ID) {
        client.request(HttpMethod.GET, "/apps/" + layer_app_id + "/user/" + user_ID + "/conversations/" + conversation_UUID + "/messages", future)
                .putHeader("accept", "application/vnd.layer+json; version=2.0")
                .putHeader("Authorization", "Bearer " + layer_app_token )
                .putHeader("Content-Type", "application/json")
                .exceptionHandler(err -> {
                    err.printStackTrace();
                    future.handle(null);
                })
                .end();
    }

    /**
     * Sends a single message to the Layer API.
     *
     * @param future Handler, requires error handler.
     * @param conversation_UUID String for URI
     * @param message JsonObject Message, see Layer API documentation.
     * @param user_ID User ID specifying the layer user
     */
    public void postMessageAsUser(Handler<HttpClientResponse> future, String conversation_UUID, JsonObject message, String user_ID) {
        client.request(HttpMethod.POST, "/apps/" + layer_app_id + "/user/" + user_ID + "/conversations/" + conversation_UUID + "/messages", future)
                .putHeader("accept", "application/vnd.layer+json; version=2.0")
                .putHeader("Authorization", "Bearer " + layer_app_token )
                .putHeader("Content-Type", "application/json")
                .exceptionHandler(err -> future.handle(null))
                .end(new JsonObject().put("parts", message.getJsonArray("parts")).encode());
    }

    /**
     * Creates a single conversation via the Layer API
     *
     * @param future       Handler, requires error handler.
     * @param conversation JsonObject Message, see Layer API documentation.
     */
    public void postConversationAsUser(Handler<HttpClientResponse> future, JsonObject conversation, String user_ID) {
        client.request(HttpMethod.POST, "/apps/" + layer_app_id + "/user/" + user_ID + "/conversations", future)
                .putHeader("accept", "application/vnd.layer+json; version=2.0")
                .putHeader("Authorization", "Bearer " + layer_app_token )
                .putHeader("Content-Type", "application/json")
                .exceptionHandler(err -> future.handle(null))
                .end(conversation.encode());
    }

    /**
     * Gets one conversation
     * @param future Handler, requires error handler.
     * @param conversation_UUID Conversation UUID for the requested conversation
     */
    public void getConversation(Handler<HttpClientResponse> future, String conversation_UUID){
        client.request(HttpMethod.GET, "/apps/" + layer_app_id + "/conversations/" + conversation_UUID, future)
                .putHeader("accept", "application/vnd.layer+json; version=2.0")
                .putHeader("Authorization", "Bearer " + layer_app_token )
                .putHeader("Content-Type", "application/json")
                .exceptionHandler(err -> future.handle(null))
                .end();
    }
    /**
     * Gets messages for specified conversation, messages is retrieved as server, NOT USER
     * @param future Handler, requires error handler.
     * @param conversation_UUID Conversation UUID for the requested conversation
     */
    public void getConversationMessage(Handler<HttpClientResponse> future, String conversation_UUID){
        client.request(HttpMethod.GET, "/apps/" + layer_app_id + "/conversations/" + conversation_UUID + "/messages", future)
                .putHeader("accept", "application/vnd.layer+json; version=2.0")
                .putHeader("Authorization", "Bearer " + layer_app_token )
                .putHeader("Content-Type", "application/json")
                .exceptionHandler(err -> {
                    err.printStackTrace();
                    future.handle(null);
                })
                .end();
    }

    /**
     * Creates a conversation as server
     * @param future Handler, requires error handler.
     * @param conversation Conversation, requires String[] participants, boolean distinct, Object metadata
     */
    public void postConversation(Handler<HttpClientResponse> future, JsonObject conversation){
        client.request(HttpMethod.POST, "/apps/" + layer_app_id + "/conversations", future)
                .putHeader("accept", "application/vnd.layer+json; version=2.0")
                .putHeader("Authorization", "Bearer " + layer_app_token )
                .putHeader("Content-Type", "application/json")
                .exceptionHandler(err -> future.handle(null))
                .end(conversation.encode());
    }

    /**
     * Sends message as user specified in JsonObject message
     * @param future Handler, requires error handler.
     * @param conversation_UUID Conversation UUID of which to send the message
     * @param message Message requires String sender_id, MessagePart[] parts. Optional Object notification
     */
    public void postMessage(Handler<HttpClientResponse> future, String conversation_UUID, JsonObject message){
        client.request(HttpMethod.POST, "/apps/" + layer_app_id + "/conversations/" + conversation_UUID + "/messages", future)
                .putHeader("accept", "application/vnd.layer+json; version=2.0")
                .putHeader("Authorization", "Bearer " + layer_app_token )
                .putHeader("Content-Type", "application/json")
                .exceptionHandler(err -> future.handle(null))
                .end(message.encode());
    }

    /**
     * Sends a announcement.
     * @param future Handler, requires error handler.
     * @param announcement Announcement requires String[] recipients, String sender_id, MessageParts[] parts, Object notification
     */
    public void postAnnouncement(Handler<HttpClientResponse> future, JsonObject announcement){
        client.request(HttpMethod.POST, "/apps/" + layer_app_id + "/announcements", future)
                .putHeader("accept", "application/vnd.layer+json; version=2.0")
                .putHeader("Authorization", "Bearer " + layer_app_token )
                .putHeader("Content-Type", "application/json")
                .exceptionHandler(err -> future.handle(null))
                .end(announcement.encode());
    }
}
