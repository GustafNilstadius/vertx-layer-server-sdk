package com.paddlenose.vertx.layer.sdk;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * @author Gustaf Nilstadius
 *         Created by Gustaf Nilstadius ( hipernx ) on 2017-01-07.
 */
@RunWith(VertxUnitRunner.class)
public class LayerServerOptionsTest {
    @Test
    public void setLayerAppId(TestContext context) throws Exception {
        String appid = "app_id";
        LayerServerOptions options = new LayerServerOptions();
        context.assertEquals(options.setLayerAppId(appid).getString("layer_app_id"), appid);
    }

    @Test
    public void setLayerAppToken(TestContext context) throws Exception {
        String apptoken = "app_token";
        LayerServerOptions options = new LayerServerOptions();
        context.assertEquals(options.setLayerAppToken(apptoken).getString("layer_app_token"), apptoken);
    }

    @Test
    public void getOptions(TestContext context) throws Exception {
        LayerServerOptions options = new LayerServerOptions("app_id", "app_token");
        context.assertEquals(options.getOptions().getString("layer_app_token"), "app_token");
    }

    @Test
    public void getString(TestContext context) throws Exception {
        LayerServerOptions options = new LayerServerOptions(new JsonObject().put("layer_app_id", "app_id").put("layer_app_token", "app_token"));
        context.assertEquals(options.getString("layer_app_id"), "app_id");
    }

}