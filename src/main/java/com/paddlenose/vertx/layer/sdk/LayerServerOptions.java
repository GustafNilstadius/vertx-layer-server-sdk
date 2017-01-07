package com.paddlenose.vertx.layer.sdk;

import io.vertx.core.json.JsonObject;

/**
 * Options for LayerServerClient.
 * @author Gustaf Nilstadius
 *         Created by Gustaf Nilstadius ( hipernx ) on 2017-01-07.
 */
@SuppressWarnings("WeakerAccess")
public class LayerServerOptions {
    private JsonObject options;

    /**
     * Constructor creates options
     * <p>
     *     Requires String layer_app_id, String layer_app_token as JsonObject
     * </p>
     * @param options the options, contains: String layer_app_id, String layer_app_token
     */
    public LayerServerOptions(JsonObject options){
        this.options = options;
    }

    /**
     * Constructor, initializes options
     */
    public LayerServerOptions(){
        this.options = new JsonObject();
    }

    /**
     * Constructor, creates options from arguments
     * @param layer_app_id Layer application id
     * @param layer_app_token Layer application token
     */
    public LayerServerOptions(String layer_app_id, String layer_app_token){
        this.options = new JsonObject()
                .put("layer_app_id", layer_app_id)
                .put("layer_app_token", layer_app_token);
    }


    /**
     * Set Layer application id in options
     * @param layer_app_id Layer application id
     * @return Current options
     */
    public JsonObject setLayerAppId(String layer_app_id){
        this.options.put("layer_app_id", layer_app_id);
        return options;
    }

    /**
     * Set Layer application token in options
     * @param layer_app_token Layer application token
     * @return Current options
     */
    public JsonObject setLayerAppToken(String layer_app_token){
        this.options.put("layer_app_token", layer_app_token);
        return options;
    }

    /**
     * Returns options
     * @return Options
     */
    public JsonObject getOptions() {
        return options;
    }

    /**
     * Retrieves a string from options with id.
     * @param id Id of field required
     * @return String
     */
    public String getString(String id) {
        return options.getString(id);
    }
}
