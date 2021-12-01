package co.casterlabs.caffeinated.app.networking.localserver.handlers;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.networking.localserver.RequestError;
import co.casterlabs.caffeinated.app.networking.localserver.RouteHelper;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;

public class RouteWidgetApi implements HttpProvider, RouteHelper {

    @HttpEndpoint(uri = "/api/plugin/:pluginId/widget/:widgetId/html")
    public HttpResponse onGetWidgetHtmlRequest(SoraHttpSession session) {
        try {
            if (authorize(session)) {
                String pluginId = session.getUriParameters().get("pluginId");
                String widgetId = session.getUriParameters().get("widgetId");

                CaffeinatedPlugin owningPlugin = CaffeinatedApp.getInstance().getPlugins().getPlugins().getPluginById(pluginId);

                if (owningPlugin == null) {
                    return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
                }

                for (Widget widget : owningPlugin.getWidgets()) {
                    if (widget.getId().equals(widgetId)) {
                        String html = widget.getWidgetHtml();

                        if (html == null) {
                            return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.RESOURCE_NOT_FOUND);
                        } else {
                            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, html)
                                .setMimeType("text/html");
                        }
                    }
                }

                return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.WIDGET_NOT_FOUND);
            } else {
                return newErrorResponse(StandardHttpStatus.UNAUTHORIZED, RequestError.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

    @SuppressWarnings("deprecation")
    @HttpEndpoint(uri = "/api/plugin/:pluginId/widgets")
    public HttpResponse onGetWidgetsRequest(SoraHttpSession session) {
        try {
            if (authorize(session)) {
                String pluginId = session.getUriParameters().get("pluginId");

                CaffeinatedPlugin owningPlugin = CaffeinatedApp.getInstance().getPlugins().getPlugins().getPluginById(pluginId);

                if (owningPlugin == null) {
                    return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
                }

                JsonArray widgetsJson = new JsonArray();

                for (Widget widget : owningPlugin.getWidgets()) {
                    JsonObject widgetJson = widget.toJson();

                    widgetJson.remove("settings");
                    widgetJson.remove("settingsLayout");

                    widgetsJson.add(widgetJson);
                }

                return newResponse(StandardHttpStatus.OK, new JsonObject().put("widgets", widgetsJson));
            } else {
                return newErrorResponse(StandardHttpStatus.UNAUTHORIZED, RequestError.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

}
