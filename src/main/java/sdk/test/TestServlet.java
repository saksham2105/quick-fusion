package sdk.test;

import sdk.server.quick.fusion.annotations.QuikFusionWebServlet;
import sdk.server.quick.fusion.enums.ResponseType;
import sdk.server.quick.fusion.interfaces.HttpServlet;
import sdk.server.quick.fusion.model.HttpRequest;
import sdk.server.quick.fusion.model.HttpResponse;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

@QuikFusionWebServlet("/testMapping")
public class TestServlet implements HttpServlet {

    @Override
    public void get(HttpRequest request, HttpResponse response) throws IOException {
        response.setType(ResponseType.TEXT_HTML);
        OutputStreamWriter writer = response.getWriter();
        writer.write("<!DOCTYPE html><html>\n" +
                "<head>\n" +
                "    <title>Hello, Saksham</title>\n" +
                "</head>");
        writer.write("<body>\n" +
                "    <h1>Hello, Saksham Bhai</h1>\n" +
                "</body>\n" +
                "</html>");
    }
}
