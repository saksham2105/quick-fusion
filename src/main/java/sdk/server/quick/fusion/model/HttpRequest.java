package sdk.server.quick.fusion.model;

import sdk.server.quick.fusion.enums.RequestType;

import java.util.Map;

public class HttpRequest {
   private RequestType type;
   private String urlPattern;
   private Map<String, Object> queryParameters;

   private void setType(RequestType type) {this.type = type;}
   public RequestType getType() { return this.type; }
   private void setUrlPattern(String urlPattern) {this.urlPattern = urlPattern;}
   public String getUrlPattern() {return this.urlPattern;}

   private void setQueryParameters(Map<String, Object> queryParameters) {
      this.queryParameters = queryParameters;
   }

   public Map<String, Object> getQueryParameters() {
      return this.queryParameters;
   }
}
