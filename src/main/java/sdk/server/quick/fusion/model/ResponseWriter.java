package sdk.server.quick.fusion.model;

import sdk.server.quick.fusion.enums.ResponseType;

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

    public void write(String content) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("HTTP/1.1 %d OK\r\n", this.statusCode));
        stringBuilder.append(String.format("Content-Type: %s\r\n\r\n", this.responseType.getMimeType()));
        stringBuilder.append(content);
        this.outputStreamWriter.write(stringBuilder.toString());
        this.outputStreamWriter.flush();
    }
}
