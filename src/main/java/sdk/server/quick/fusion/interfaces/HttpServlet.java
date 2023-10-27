package sdk.server.quick.fusion.interfaces;

import sdk.server.quick.fusion.model.HttpRequest;
import sdk.server.quick.fusion.model.HttpResponse;

import java.io.IOException;

public interface HttpServlet {
    default void get(HttpRequest request, HttpResponse response) throws IOException {}

    default void post(HttpRequest request, HttpResponse response) {}
}
