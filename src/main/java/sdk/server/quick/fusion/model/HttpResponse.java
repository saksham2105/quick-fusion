package sdk.server.quick.fusion.model;

import sdk.server.quick.fusion.enums.ResponseType;

import java.lang.reflect.Method;

public class HttpResponse {
    private ResponseType type;
    private int statusCode;
    private ResponseWriter writer;

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
    private void setWriter(ResponseWriter writer) throws Exception {
        this.writer = writer;
        if (this.statusCode == 0) {
            this.statusCode = 200;
        }
        if (this.type == null) {
            this.setType(ResponseType.TEXT_HTML);
        }
        Class responseWriterClass = this.writer.getClass();

        Method setStatusCode = responseWriterClass.getDeclaredMethod("setStatusCode", int.class);
        setStatusCode.setAccessible(true);
        setStatusCode.invoke(this.writer, this.statusCode);

        Method setResponseType = responseWriterClass.getDeclaredMethod("setResponseType", ResponseType.class);
        setResponseType.setAccessible(true);
        setResponseType.invoke(this.writer, this.type);

        Method setBasicHeader = responseWriterClass.getDeclaredMethod("setBasicHeader");
        setBasicHeader.setAccessible(true);
        setBasicHeader.invoke(this.writer);
    }
    public ResponseWriter getWriter() throws Exception {
        return this.writer;
    }
}
