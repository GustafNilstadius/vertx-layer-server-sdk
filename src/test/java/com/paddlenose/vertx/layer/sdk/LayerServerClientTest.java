package com.paddlenose.vertx.layer.sdk;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Gustaf Nilstadius
 *         Created by Gustaf Nilstadius ( hipernx ) on 2016-12-22.
 */
@SuppressWarnings("SpellCheckingInspection")
@RunWith(VertxUnitRunner.class)
public class LayerServerClientTest {

    private Vertx vertx;

    private LayerServerClient subject;


    @SuppressWarnings("UnusedParameters")
    @Before
    public void setUp(TestContext context) throws Exception {
        vertx = Vertx.vertx();

        HttpClientOptions clientOptions = new HttpClientOptions()
                .setKeepAlive(true)
                .setMaxPoolSize(10)
                .setDefaultHost("localhost")
                .setDefaultPort(8080);
        HttpClient httpClient = vertx.createHttpClient(clientOptions);
        String app_id = "123456789abc";
        subject = new LayerServerClient(httpClient, new LayerServerOptions(new JsonObject().put("layer_app_id", app_id).put("layer_app_token", "thisIsAToken")));

    }

    @After
    public void tearDown(TestContext context) throws Exception {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void getConversationsAsUser(TestContext context) throws Exception {
        final Async async = context.async();
        final Async asyncServer = context.async();

        HttpServer server = vertx.createHttpServer().requestHandler( request -> {
            context.assertTrue(request.absoluteURI().contains("userID/conversations"));
            request.response().end(conversations.encode());
            asyncServer.complete();
        }).listen(8080, context.asyncAssertSuccess());

        subject.getConversationsAsUser(response -> {
            context.assertTrue(response.statusCode() == 200);
            response.bodyHandler(body -> {
                context.assertTrue(body.toJsonArray().encode().contains("participants"));
                context.assertFalse(body.toJsonArray().encode().contains("message"));
                async.complete();
            });
        }, "userID");

        /*
         * Wait for async tasks to complete before cleanup
         */
        asyncServer.awaitSuccess();
        async.awaitSuccess();
        server.close();
    }

    @Test
    public void getConversationMessageAsUser(TestContext context) throws Exception {
        final Async async = context.async();
        final Async asyncServer = context.async();

        String conversation_UUID = "123";
        String user_ID = "user";
        HttpServer server = vertx.createHttpServer().requestHandler( request -> {
            context.assertTrue(request.absoluteURI().contains("/" + conversation_UUID));
            request.response().end(messages.encode());
            asyncServer.complete();
        }).listen(8080, context.asyncAssertSuccess());

        subject.getConversationMessageAsUser(response -> {
            context.assertTrue(response.statusCode() == 200);
            response.bodyHandler(body -> {
                context.assertTrue(body.toJsonArray().encode().contains("layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0"));
                context.assertTrue(body.toJsonArray().encode().contains("layer:///messages/134"));
                context.assertEquals(body.toJsonArray().size(), 12);
                async.complete();
            });
        }, conversation_UUID, user_ID);

        /*
         * Wait for async tasks to complete before cleanup
         */
        asyncServer.awaitSuccess();
        async.awaitSuccess();
        server.close();
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void postConversationAsUser(TestContext context) throws Exception {
        final Async async = context.async();
        final Async asyncServer = context.async();

        String conversationURI = "/conversations";
        String user_ID = "user";
        HttpServer server = vertx.createHttpServer().requestHandler( request -> {
            context.assertTrue(request.absoluteURI().contains(conversationURI));
            request.bodyHandler(body -> {
                context.assertEquals(body.toJsonObject(), createConversation);
                request.response().setStatusCode(201).end(body.toJsonObject().encode());
                asyncServer.complete();
            });
        }).listen(8080, context.asyncAssertSuccess());

        subject.postConversationAsUser(response -> {
            context.assertTrue(response.statusCode() == 201);
            response.bodyHandler(body -> {
                context.assertEquals(body.toJsonObject(), createConversation);
                async.complete();
            });
        }, createConversation, user_ID);

        /*
         * Wait for async tasks to complete before cleanup
         */
        asyncServer.awaitSuccess();
        async.awaitSuccess();
        server.close();
    }

    @Test
    public void postMessageAsUser(TestContext context) throws Exception {
        final Async async = context.async();
        final Async asyncServer = context.async();

        String conversation_UUID = "123";
        String user_ID = "user";
        HttpServer server = vertx.createHttpServer().requestHandler( request -> {
            context.assertTrue(request.absoluteURI().contains(conversation_UUID));
            request.bodyHandler(body -> {
                context.assertEquals(body.toJsonObject(), new JsonObject().put("parts", messages.getJsonObject(0).getJsonArray("parts")));
                request.response().setStatusCode(201).end(body.toJsonObject().encode());
                asyncServer.complete();
            });
        }).listen(8080, context.asyncAssertSuccess());

        subject.postMessageAsUser(response -> {
            context.assertTrue(response.statusCode() == 201);
            response.bodyHandler(body -> {
                context.assertEquals(body.toJsonObject(), new JsonObject().put("parts", messages.getJsonObject(0).getJsonArray("parts")));
                async.complete();
            });
        }, conversation_UUID, messages.getJsonObject(0), user_ID);

        /*
         * Wait for async tasks to complete before cleanup
         */
        asyncServer.awaitSuccess();
        async.awaitSuccess();
        server.close();
    }

    @Test
    public void getConversation(TestContext context) throws Exception {
        final Async async = context.async();
        final Async asyncServer = context.async();

        String conversation_UUID = "convId";

        HttpServer server = vertx.createHttpServer().requestHandler( request -> {
            context.assertTrue(request.absoluteURI().contains("conversations/" + conversation_UUID));
            request.response().end(conversations.getJsonObject(0).encode());
            asyncServer.complete();
        }).listen(8080, context.asyncAssertSuccess());

        subject.getConversation(response -> {
            context.assertTrue(response.statusCode() == 200);
            response.bodyHandler(body -> {
                context.assertTrue(body.toJsonObject().encode().contains("participants"));
                context.assertFalse(body.toJsonObject().encode().contains("message"));
                async.complete();
            });
        }, conversation_UUID);

        /*
         * Wait for async tasks to complete before cleanup
         */
        asyncServer.awaitSuccess();
        async.awaitSuccess();
        server.close();
    }

    @Test
    public void getConversationMessage(TestContext context) throws Exception {
        final Async async = context.async();
        final Async asyncServer = context.async();

        String conversation_UUID = "123";
        HttpServer server = vertx.createHttpServer().requestHandler( request -> {
            context.assertTrue(request.absoluteURI().contains("/" + conversation_UUID));
            request.response().end(messages.encode());
            asyncServer.complete();
        }).listen(8080, context.asyncAssertSuccess());

        subject.getConversationMessage(response -> {
            context.assertTrue(response.statusCode() == 200);
            response.bodyHandler(body -> {
                context.assertTrue(body.toJsonArray().encode().contains("940de862-3c96-11e4-baad-164230d1df67/parts/0"));
                context.assertTrue(body.toJsonArray().encode().contains("layer:///messages/134"));
                context.assertEquals(body.toJsonArray().size(), 12);
                async.complete();
            });
        }, conversation_UUID);

        /*
         * Wait for async tasks to complete before cleanup
         */
        asyncServer.awaitSuccess();
        async.awaitSuccess();
        server.close();
    }

    @SuppressWarnings("Duplicates")
    @Test
    public void postConversation(TestContext context) throws Exception {
        final Async async = context.async();
        final Async asyncServer = context.async();

        String conversationURI = "/conversations";
        HttpServer server = vertx.createHttpServer().requestHandler( request -> {
            context.assertTrue(request.absoluteURI().contains(conversationURI));
            request.bodyHandler(body -> {
                context.assertEquals(body.toJsonObject(), createConversation);
                request.response().setStatusCode(201).end(body.toJsonObject().encode());
                asyncServer.complete();
            });
        }).listen(8080, context.asyncAssertSuccess());

        subject.postConversation(response -> {
            context.assertTrue(response.statusCode() == 201);
            response.bodyHandler(body -> {
                context.assertEquals(body.toJsonObject(), createConversation);
                async.complete();
            });
        }, createConversation);

        /*
         * Wait for async tasks to complete before cleanup
         */
        asyncServer.awaitSuccess();
        async.awaitSuccess();
        server.close();
    }

    @Test
    public void postMessage(TestContext context) throws Exception {
        final Async async = context.async();
        final Async asyncServer = context.async();

        String conversation_UUID = "123";
        HttpServer server = vertx.createHttpServer().requestHandler( request -> {
            context.assertTrue(request.absoluteURI().contains(conversation_UUID));
            request.bodyHandler(body -> {
                context.assertEquals(body.toJsonObject(), messages.getJsonObject(0).put("sender_id", "1234"));
                request.response().setStatusCode(201).end(body.toJsonObject().encode());
                asyncServer.complete();
            });
        }).listen(8080, context.asyncAssertSuccess());

        subject.postMessage(response -> {
            context.assertTrue(response.statusCode() == 201);
            response.bodyHandler(body -> {
                context.assertEquals(body.toJsonObject(), messages.getJsonObject(0).put("sender_id", "1234"));
                async.complete();
            });
        }, conversation_UUID, messages.getJsonObject(0).put("sender_id", "1234"));

        /*
         * Wait for async tasks to complete before cleanup
         */
        asyncServer.awaitSuccess();
        async.awaitSuccess();
        server.close();
    }

    @Test
    public void postAnnouncement(TestContext context) throws Exception {
        final Async async = context.async();
        final Async asyncServer = context.async();

        HttpServer server = vertx.createHttpServer().requestHandler( request -> {
            context.assertTrue(request.absoluteURI().contains("/announcements"));
            request.bodyHandler(body -> {
                context.assertEquals(body.toJsonObject(), announcement);
                request.response().setStatusCode(202).end(body.toJsonObject().encode());
                asyncServer.complete();
            });
        }).listen(8080, context.asyncAssertSuccess());

        subject.postAnnouncement(response -> {
            context.assertTrue(response.statusCode() == 202);
            response.bodyHandler(body -> {
                context.assertEquals(body.toJsonObject(), announcement);
                async.complete();
            });
        }, announcement);

        /*
         * Wait for async tasks to complete before cleanup
         */
        asyncServer.awaitSuccess();
        async.awaitSuccess();
        server.close();
    }

    private final JsonObject announcement = new JsonObject("" +
            "{\n" +
            "    \"recipients\": [\n" +
            "        \"layer:///identities/1234\",\n" +
            "        \"layer:///identities/5678\"\n" +
            "    ],\n" +
            "    \"sender_id\": \"layer:///identities/777\",\n" +
            "    \"parts\": [\n" +
            "        {\n" +
            "            \"body\": \"Hello, World!\",\n" +
            "            \"mime_type\": \"text/plain\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"notification\": {\n" +
            "        \"title\": \"New Alert\",\n" +
            "        \"text\": \"This is the alert text to include with the Push Notification.\",\n" +
            "        \"sound\": \"chime.aiff\"\n" +
            "    }\n" +
            "}");

    private final JsonObject createConversation = new JsonObject(
            "{\n" +
                    "    \"participants\": [\n" +
                    "        \"layer://identities/1234\",\n" +
                    "        \"layer://identities/5678\"\n" +
                    "    ],\n" +
                    "    \"distinct\": false,\n" +
                    "    \"metadata\": {\n" +
                    "        \"background_color\": \"#3c3c3c\"\n" +
                    "    }\n" +
                    "}");

    private final JsonArray messages = new JsonArray(
            "[\n" +
                    "  {\n" +
                    "    \"id\": \"layer:///messages/123\",\n" +
                    "    \"url\": \"https://api.layer.com/messages/123\",\n" +
                    "    \"receipts_url\": \"https://api.layer.com/messages/123/receipts\",\n" +
                    "    \"position\": 15032697020,\n" +
                    "    \"conversation\": {\n" +
                    "      \"id\": \"layer:///conversations/123\",\n" +
                    "      \"url\": \"https://api.layer.com/conversations/123\"\n" +
                    "    },\n" +
                    "    \"parts\": [\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is the message.\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"sent_at\": \"2014-09-09T04:44:47+00:00\",\n" +
                    "    \"sender\": {\n" +
                    "      \"id\": \"layer:///identities/1234\",\n" +
                    "      \"url\": \"https://api.layer.com/identities/1234\",\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"One Two Three Four\"\n" +
                    "    },\n" +
                    "    \"is_unread\": true,\n" +
                    "    \"recipient_status\": {\n" +
                    "      \"layer:///identities/777\": \"sent\",\n" +
                    "      \"layer:///identities/999\": \"read\",\n" +
                    "      \"layer:///identities/111\": \"delivered\",\n" +
                    "      \"layer:///identities/1234\": \"read\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": \"layer:///messages/1234\",\n" +
                    "    \"url\": \"https://api.layer.com/messages/123\",\n" +
                    "    \"receipts_url\": \"https://api.layer.com/messages/123/receipts\",\n" +
                    "    \"position\": 15032697020,\n" +
                    "    \"conversation\": {\n" +
                    "      \"id\": \"layer:///conversations/123\",\n" +
                    "      \"url\": \"https://api.layer.com/conversations/123\"\n" +
                    "    },\n" +
                    "    \"parts\": [\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another message.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/1\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another part of message\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"sent_at\": \"2014-09-09T04:44:47+00:00\",\n" +
                    "    \"sender\": {\n" +
                    "      \"id\": \"layer:///identities/1234\",\n" +
                    "      \"url\": \"https://api.layer.com/identities/1234\",\n" +
                    "      \"user_id\": \"3\",\n" +
                    "      \"display_name\": \"One Two Three Four\"\n" +
                    "    },\n" +
                    "    \"is_unread\": true,\n" +
                    "    \"recipient_status\": {\n" +
                    "      \"layer:///identities/777\": \"sent\",\n" +
                    "      \"layer:///identities/999\": \"read\",\n" +
                    "      \"layer:///identities/111\": \"delivered\",\n" +
                    "      \"layer:///identities/1234\": \"read\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": \"layer:///messages/134\",\n" +
                    "    \"url\": \"https://api.layer.com/messages/123\",\n" +
                    "    \"receipts_url\": \"https://api.layer.com/messages/123/receipts\",\n" +
                    "    \"position\": 15032697020,\n" +
                    "    \"conversation\": {\n" +
                    "      \"id\": \"layer:///conversations/123\",\n" +
                    "      \"url\": \"https://api.layer.com/conversations/123\"\n" +
                    "    },\n" +
                    "    \"parts\": [\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another message.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/1\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another part of message \\n \\n \\n\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"sent_at\": \"2014-09-09T04:44:47+00:00\",\n" +
                    "    \"sender\": {\n" +
                    "      \"id\": \"layer:///identities/1234\",\n" +
                    "      \"url\": \"https://api.layer.com/identities/1234\",\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"One Two Three Four\"\n" +
                    "    },\n" +
                    "    \"is_unread\": true,\n" +
                    "    \"recipient_status\": {\n" +
                    "      \"layer:///identities/777\": \"sent\",\n" +
                    "      \"layer:///identities/999\": \"read\",\n" +
                    "      \"layer:///identities/111\": \"delivered\",\n" +
                    "      \"layer:///identities/1234\": \"read\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": \"layer:///messages/123\",\n" +
                    "    \"url\": \"https://api.layer.com/messages/123\",\n" +
                    "    \"receipts_url\": \"https://api.layer.com/messages/123/receipts\",\n" +
                    "    \"position\": 15032697020,\n" +
                    "    \"conversation\": {\n" +
                    "      \"id\": \"layer:///conversations/123\",\n" +
                    "      \"url\": \"https://api.layer.com/conversations/123\"\n" +
                    "    },\n" +
                    "    \"parts\": [\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is the message.\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"sent_at\": \"2014-09-09T04:44:47+00:00\",\n" +
                    "    \"sender\": {\n" +
                    "      \"id\": \"layer:///identities/1234\",\n" +
                    "      \"url\": \"https://api.layer.com/identities/1234\",\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"One Two Three Four\"\n" +
                    "    },\n" +
                    "    \"is_unread\": true,\n" +
                    "    \"recipient_status\": {\n" +
                    "      \"layer:///identities/777\": \"sent\",\n" +
                    "      \"layer:///identities/999\": \"read\",\n" +
                    "      \"layer:///identities/111\": \"delivered\",\n" +
                    "      \"layer:///identities/1234\": \"read\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": \"layer:///messages/1234\",\n" +
                    "    \"url\": \"https://api.layer.com/messages/123\",\n" +
                    "    \"receipts_url\": \"https://api.layer.com/messages/123/receipts\",\n" +
                    "    \"position\": 15032697020,\n" +
                    "    \"conversation\": {\n" +
                    "      \"id\": \"layer:///conversations/123\",\n" +
                    "      \"url\": \"https://api.layer.com/conversations/123\"\n" +
                    "    },\n" +
                    "    \"parts\": [\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another message.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/1\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another part of message\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"sent_at\": \"2014-09-09T04:44:47+00:00\",\n" +
                    "    \"sender\": {\n" +
                    "      \"id\": \"layer:///identities/1234\",\n" +
                    "      \"url\": \"https://api.layer.com/identities/1234\",\n" +
                    "      \"user_id\": \"3\",\n" +
                    "      \"display_name\": \"One Two Three Four\"\n" +
                    "    },\n" +
                    "    \"is_unread\": true,\n" +
                    "    \"recipient_status\": {\n" +
                    "      \"layer:///identities/777\": \"sent\",\n" +
                    "      \"layer:///identities/999\": \"read\",\n" +
                    "      \"layer:///identities/111\": \"delivered\",\n" +
                    "      \"layer:///identities/1234\": \"read\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": \"layer:///messages/134\",\n" +
                    "    \"url\": \"https://api.layer.com/messages/123\",\n" +
                    "    \"receipts_url\": \"https://api.layer.com/messages/123/receipts\",\n" +
                    "    \"position\": 15032697020,\n" +
                    "    \"conversation\": {\n" +
                    "      \"id\": \"layer:///conversations/123\",\n" +
                    "      \"url\": \"https://api.layer.com/conversations/123\"\n" +
                    "    },\n" +
                    "    \"parts\": [\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another message.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/1\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another part of message \\n \\n \\n\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"sent_at\": \"2014-09-09T04:44:47+00:00\",\n" +
                    "    \"sender\": {\n" +
                    "      \"id\": \"layer:///identities/1234\",\n" +
                    "      \"url\": \"https://api.layer.com/identities/1234\",\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"One Two Three Four\"\n" +
                    "    },\n" +
                    "    \"is_unread\": true,\n" +
                    "    \"recipient_status\": {\n" +
                    "      \"layer:///identities/777\": \"sent\",\n" +
                    "      \"layer:///identities/999\": \"read\",\n" +
                    "      \"layer:///identities/111\": \"delivered\",\n" +
                    "      \"layer:///identities/1234\": \"read\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": \"layer:///messages/123\",\n" +
                    "    \"url\": \"https://api.layer.com/messages/123\",\n" +
                    "    \"receipts_url\": \"https://api.layer.com/messages/123/receipts\",\n" +
                    "    \"position\": 15032697020,\n" +
                    "    \"conversation\": {\n" +
                    "      \"id\": \"layer:///conversations/123\",\n" +
                    "      \"url\": \"https://api.layer.com/conversations/123\"\n" +
                    "    },\n" +
                    "    \"parts\": [\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is the message.\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"sent_at\": \"2014-09-09T04:44:47+00:00\",\n" +
                    "    \"sender\": {\n" +
                    "      \"id\": \"layer:///identities/1234\",\n" +
                    "      \"url\": \"https://api.layer.com/identities/1234\",\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"One Two Three Four\"\n" +
                    "    },\n" +
                    "    \"is_unread\": true,\n" +
                    "    \"recipient_status\": {\n" +
                    "      \"layer:///identities/777\": \"sent\",\n" +
                    "      \"layer:///identities/999\": \"read\",\n" +
                    "      \"layer:///identities/111\": \"delivered\",\n" +
                    "      \"layer:///identities/1234\": \"read\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": \"layer:///messages/1234\",\n" +
                    "    \"url\": \"https://api.layer.com/messages/123\",\n" +
                    "    \"receipts_url\": \"https://api.layer.com/messages/123/receipts\",\n" +
                    "    \"position\": 15032697020,\n" +
                    "    \"conversation\": {\n" +
                    "      \"id\": \"layer:///conversations/123\",\n" +
                    "      \"url\": \"https://api.layer.com/conversations/123\"\n" +
                    "    },\n" +
                    "    \"parts\": [\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another message.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/1\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another part of message\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"sent_at\": \"2014-09-09T04:44:47+00:00\",\n" +
                    "    \"sender\": {\n" +
                    "      \"id\": \"layer:///identities/1234\",\n" +
                    "      \"url\": \"https://api.layer.com/identities/1234\",\n" +
                    "      \"user_id\": \"3\",\n" +
                    "      \"display_name\": \"One Two Three Four\"\n" +
                    "    },\n" +
                    "    \"is_unread\": true,\n" +
                    "    \"recipient_status\": {\n" +
                    "      \"layer:///identities/777\": \"sent\",\n" +
                    "      \"layer:///identities/999\": \"read\",\n" +
                    "      \"layer:///identities/111\": \"delivered\",\n" +
                    "      \"layer:///identities/1234\": \"read\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": \"layer:///messages/134\",\n" +
                    "    \"url\": \"https://api.layer.com/messages/123\",\n" +
                    "    \"receipts_url\": \"https://api.layer.com/messages/123/receipts\",\n" +
                    "    \"position\": 15032697020,\n" +
                    "    \"conversation\": {\n" +
                    "      \"id\": \"layer:///conversations/123\",\n" +
                    "      \"url\": \"https://api.layer.com/conversations/123\"\n" +
                    "    },\n" +
                    "    \"parts\": [\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another message.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/1\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another part of message \\n \\n \\n\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"sent_at\": \"2014-09-09T04:44:47+00:00\",\n" +
                    "    \"sender\": {\n" +
                    "      \"id\": \"layer:///identities/1234\",\n" +
                    "      \"url\": \"https://api.layer.com/identities/1234\",\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"One Two Three Four\"\n" +
                    "    },\n" +
                    "    \"is_unread\": true,\n" +
                    "    \"recipient_status\": {\n" +
                    "      \"layer:///identities/777\": \"sent\",\n" +
                    "      \"layer:///identities/999\": \"read\",\n" +
                    "      \"layer:///identities/111\": \"delivered\",\n" +
                    "      \"layer:///identities/1234\": \"read\"\n" +
                    "    }\n" +
                    "  },{\n" +
                    "  \"id\": \"layer:///messages/123\",\n" +
                    "  \"url\": \"https://api.layer.com/messages/123\",\n" +
                    "  \"receipts_url\": \"https://api.layer.com/messages/123/receipts\",\n" +
                    "  \"position\": 15032697020,\n" +
                    "  \"conversation\": {\n" +
                    "    \"id\": \"layer:///conversations/123\",\n" +
                    "    \"url\": \"https://api.layer.com/conversations/123\"\n" +
                    "  },\n" +
                    "  \"parts\": [\n" +
                    "    {\n" +
                    "      \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0\",\n" +
                    "      \"mime_type\": \"text/plain\",\n" +
                    "      \"body\": \"This is the message.\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"sent_at\": \"2014-09-09T04:44:47+00:00\",\n" +
                    "  \"sender\": {\n" +
                    "    \"id\": \"layer:///identities/1234\",\n" +
                    "    \"url\": \"https://api.layer.com/identities/1234\",\n" +
                    "    \"user_id\": \"123\",\n" +
                    "    \"display_name\": \"One Two Three Four\"\n" +
                    "  },\n" +
                    "  \"is_unread\": true,\n" +
                    "  \"recipient_status\": {\n" +
                    "    \"layer:///identities/777\": \"sent\",\n" +
                    "    \"layer:///identities/999\": \"read\",\n" +
                    "    \"layer:///identities/111\": \"delivered\",\n" +
                    "    \"layer:///identities/1234\": \"read\"\n" +
                    "  }\n" +
                    "},\n" +
                    "  {\n" +
                    "    \"id\": \"layer:///messages/1234\",\n" +
                    "    \"url\": \"https://api.layer.com/messages/123\",\n" +
                    "    \"receipts_url\": \"https://api.layer.com/messages/123/receipts\",\n" +
                    "    \"position\": 15032697020,\n" +
                    "    \"conversation\": {\n" +
                    "      \"id\": \"layer:///conversations/123\",\n" +
                    "      \"url\": \"https://api.layer.com/conversations/123\"\n" +
                    "    },\n" +
                    "    \"parts\": [\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another message.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/1\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another part of message\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"sent_at\": \"2014-09-09T04:44:47+00:00\",\n" +
                    "    \"sender\": {\n" +
                    "      \"id\": \"layer:///identities/1234\",\n" +
                    "      \"url\": \"https://api.layer.com/identities/1234\",\n" +
                    "      \"user_id\": \"3\",\n" +
                    "      \"display_name\": \"One Two Three Four\"\n" +
                    "    },\n" +
                    "    \"is_unread\": true,\n" +
                    "    \"recipient_status\": {\n" +
                    "      \"layer:///identities/777\": \"sent\",\n" +
                    "      \"layer:///identities/999\": \"read\",\n" +
                    "      \"layer:///identities/111\": \"delivered\",\n" +
                    "      \"layer:///identities/1234\": \"read\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"id\": \"layer:///messages/134\",\n" +
                    "    \"url\": \"https://api.layer.com/messages/123\",\n" +
                    "    \"receipts_url\": \"https://api.layer.com/messages/123/receipts\",\n" +
                    "    \"position\": 15032697020,\n" +
                    "    \"conversation\": {\n" +
                    "      \"id\": \"layer:///conversations/123\",\n" +
                    "      \"url\": \"https://api.layer.com/conversations/123\"\n" +
                    "    },\n" +
                    "    \"parts\": [\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/0\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another message.\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\": \"layer:///messages/940de862-3c96-11e4-baad-164230d1df67/parts/1\",\n" +
                    "        \"mime_type\": \"text/plain\",\n" +
                    "        \"body\": \"This is another part of message \\n \\n \\n\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"sent_at\": \"2014-09-09T04:44:47+00:00\",\n" +
                    "    \"sender\": {\n" +
                    "      \"id\": \"layer:///identities/1234\",\n" +
                    "      \"url\": \"https://api.layer.com/identities/1234\",\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"One Two Three Four\"\n" +
                    "    },\n" +
                    "    \"is_unread\": true,\n" +
                    "    \"recipient_status\": {\n" +
                    "      \"layer:///identities/777\": \"sent\",\n" +
                    "      \"layer:///identities/999\": \"read\",\n" +
                    "      \"layer:///identities/111\": \"delivered\",\n" +
                    "      \"layer:///identities/1234\": \"read\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "]");

    private final JsonArray conversations = new JsonArray(
            "[\n" +
                    "  {\"participants\": [\n" +
                    "    {\n" +
                    "      \"user_id\": \"123456789\",\n" +
                    "      \"display_name\": \"kakor\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"user_id\": \"789\",\n" +
                    "      \"display_name\": \"lisa\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "    \"distinct\": true,\n" +
                    "    \"id\": \"abc\",\n" +
                    "    \"metadata\": {\"title\": \"conversation\"}\n" +
                    "  },\n" +
                    "  {\"participants\": [\n" +
                    "    {\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"kakor\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"user_id\": \"12345\",\n" +
                    "      \"display_name\": \"maja\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "    \"distinct\": true,\n" +
                    "    \"id\": \"123\",\n" +
                    "    \"metadata\": {\"title\": \"conve conve\"}\n" +
                    "  },\n" +
                    "  {\"participants\": [\n" +
                    "    {\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"kakor\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"user_id\": \"456\",\n" +
                    "      \"display_name\": \"asdasdr\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "    \"distinct\": true,\n" +
                    "    \"id\": \"abc123\",\n" +
                    "    \"metadata\": {\"title\": \"asdasdasdasd\"}\n" +
                    "  },\n" +
                    "  {\"participants\": [\n" +
                    "    {\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"kakor\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"user_id\": \"78fgh2349\",\n" +
                    "      \"display_name\": \"lfghisa\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "    \"distinct\": true,\n" +
                    "    \"id\": \"abc\",\n" +
                    "    \"metadata\": {\"title\": \"converfghfghfghsation\"}\n" +
                    "  },\n" +
                    "  {\"participants\": [\n" +
                    "    {\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"kakfghfghor\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"user_id\": \"12312312312345\",\n" +
                    "      \"display_name\": \"majfgha\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "    \"distinct\": true,\n" +
                    "    \"id\": \"abc3333\",\n" +
                    "    \"metadata\": {\"title\": \"conve conertertertertertertve\"}\n" +
                    "  },\n" +
                    "  {\"participants\": [\n" +
                    "    {\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"kakor\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"user_id\": \"456\",\n" +
                    "      \"display_name\": \"asdaertertertsdr\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "    \"distinct\": true,\n" +
                    "    \"id\": \"abc224453452\",\n" +
                    "    \"metadata\": {\"title\": \"asdasdasertertertertertertdasd\"}\n" +
                    "  },\n" +
                    "  {\"participants\": [\n" +
                    "    {\n" +
                    "      \"user_id\": \"123we\",\n" +
                    "      \"display_name\": \"kakor\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"user_id\": \"456\",\n" +
                    "      \"display_name\": \"asdaedfsartertertsdr\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "    \"distinct\": true,\n" +
                    "    \"id\": \"abc2244534asdf52\",\n" +
                    "    \"metadata\": {\"title\": \"asdasdfasdasertertertertertertdasd\"}\n" +
                    "  },\n" +
                    "  {\"participants\": [\n" +
                    "    {\n" +
                    "      \"user_id\": \"asas123we\",\n" +
                    "      \"display_name\": \"kakor\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"user_id\": \"456\",\n" +
                    "      \"display_name\": \"asdaedfsartertertsdr\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "    \"distinct\": true,\n" +
                    "    \"id\": \"abc2244534asdf52\",\n" +
                    "    \"metadata\": {\"title\": \"asdasdfasdasertertertertertertdasd\"}\n" +
                    "  },\n" +
                    "  {\"participants\": [\n" +
                    "    {\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"kakfghfghor\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"user_id\": \"12312312312345\",\n" +
                    "      \"display_name\": \"majfgha\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "    \"distinct\": true,\n" +
                    "    \"id\": \"abc3333\",\n" +
                    "    \"metadata\": {\"title\": \"conve conertertertertertertve\"}\n" +
                    "  },\n" +
                    "  {\"participants\": [\n" +
                    "    {\n" +
                    "      \"user_id\": \"123\",\n" +
                    "      \"display_name\": \"kakor\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"user_id\": \"456\",\n" +
                    "      \"display_name\": \"asdaertertertsdr\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "    \"distinct\": true,\n" +
                    "    \"id\": \"abc224453452\",\n" +
                    "    \"metadata\": {\"title\": \"asdasdasertertertertertertdasd\"}\n" +
                    "  },\n" +
                    "  {\"participants\": [\n" +
                    "    {\n" +
                    "      \"user_id\": \"123we\",\n" +
                    "      \"display_name\": \"kakor\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"user_id\": \"456\",\n" +
                    "      \"display_name\": \"asdaedfsartertertsdr\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "    \"distinct\": true,\n" +
                    "    \"id\": \"abc2244534asdf52\",\n" +
                    "    \"metadata\": {\"title\": \"asdasdfasdasertertertertertertdasd\"}\n" +
                    "  },\n" +
                    "  {\"participants\": [\n" +
                    "    {\n" +
                    "      \"user_id\": \"asas123we\",\n" +
                    "      \"display_name\": \"kakor\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"user_id\": \"456\",\n" +
                    "      \"display_name\": \"asdaedfsartertertsdr\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "    \"distinct\": true,\n" +
                    "    \"id\": \"abc2244534asdf52\",\n" +
                    "    \"metadata\": {\"title\": \"asdasdfasdasertertertertertertdasd\"}\n" +
                    "  }\n" +
                    "]\n");

}