package sdk.server.quick.fusion.model;

import sdk.server.quick.fusion.enums.ResponseType;

import java.io.OutputStreamWriter;

public class HttpResponse {
    private ResponseType type;
    private int statusCode;
    private OutputStreamWriter writer;

    public void setType(ResponseType type) {
        this.type = type;
    }
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    public int getCode() {
        return this.statusCode;
    }
    public ResponseType getType() {return this.type;}
    protected void setWriter(OutputStreamWriter writer) {this.writer = writer;}
    public OutputStreamWriter getWriter() {
        if (this.statusCode == 0) {
            this.statusCode = 200;
        }
        try {
            if (this.type == null) {
                this.setType(ResponseType.TEXT_HTML);
                this.writer.write(String.format("HTTP/1.1 %d OK\n", statusCode));
                this.writer.write(String.format("Content-Type: %s\r\n\r\n", type.getMimeType()));
            } else {
                this.writer.write(String.format("HTTP/1.1 %d OK\n", statusCode));
                this.writer.write(String.format("Content-Type: %s\r\n\r\n", type.getMimeType()));
            }
        } catch (Exception e) {
            e.printStackTrace();;
            return null;
        }
        return this.writer;
    }
}
