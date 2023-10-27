package sdk.server.quick.fusion.model;

import sdk.server.quick.fusion.enums.ResponseType;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class ResponseWriter {
    private OutputStreamWriter outputStreamWriter;
    private int statusCode;
    private ResponseType responseType;

    private void setOutputStreamWriter(OutputStreamWriter outputStreamWriter) {
        this.outputStreamWriter = outputStreamWriter;
    }
    private void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    private void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    private void setBasicHeader() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("HTTP/1.1 %d OK\r\n", this.statusCode));
        stringBuilder.append(String.format("Content-Type: %s\r\n\r\n", this.responseType.getMimeType()));
        this.outputStreamWriter.write(stringBuilder.toString());
    }

    public void write(String content) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(content);
        this.outputStreamWriter.write(stringBuilder.toString());
        this.outputStreamWriter.flush();
    }
}
