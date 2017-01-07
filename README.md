# Vertx Layer Server SDK 
[![Build Status](https://travis-ci.org/HiPERnx/vertx-layer-server-sdk.svg?branch=master)](https://travis-ci.org/HiPERnx/vertx-layer-server-sdk) [![](https://jitpack.io/v/HiPERnx/vertx-layer-server-sdk.svg)](https://jitpack.io/#HiPERnx/vertx-layer-server-sdk)

This is a SDK to communicate with [Layer](https://github.com/layerhq) in [Vert.X](http://vertx.io/) applications. 

[Java doc can be found here!](http://vertx-layer-server-sdk.paddlenose.com/)  

##Get started 
To get started you simply have to add the dependency to your Maven project  

```XML
        <dependency>  
                <groupId>com.github.HiPERnx</groupId>  
                <artifactId>vertx-layer-server-sdk</artifactId>  
                <version>v1.1</version>  
        </dependency>
```

You will need a Application ID and Token from Layer. [See Layer server documentation.](https://docs.layer.com/reference/server_api/introduction)

###Example 
Create HttpClient, see Vert.X documentation on usage of HttpClient.  
```Java
        HttpClientOptions clientOptions = new HttpClientOptions()
                .setSsl(config().getBoolean("client_ssl", true))
                .setTrustAll(config().getBoolean("client_trust_all", true))
                .setKeepAlive(true)
                .setMaxPoolSize(10)
                .setDefaultPort(config().getInteger("layer_port", 4444))
                .setDefaultHost(config().getString("layer_url", "localhost"));
        HttpClient client = vertx.createHttpClient(clientOptions);
```

Create options and instance of LayerServerClient  
```Java
        LayerServerOptions options = new LayerServerOptions(new JsonObject()
                .put("layer_app_id", app_id)
                .put("layer_app_token", "thisIsAToken"));
        layerClient = new LayerServerClient(httpClient, options);
```

Call Layer and assert true that status code is = 200 and that.
conversation_UUID is the Layer UUID for the conversation that we request.
```Java
        layerClient.getConversation(response -> {
                    //Handle HTTP errors here, like 404. 
                    //Need the body? 
                    response.bodyHandler(body -> {
                        //Handle body
                    });
                }, conversation_UUID);
```


This code is developed by Gustaf Nilstadius at [Paddle Nose Studios](www.paddlenose.com)