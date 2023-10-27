package sdk.server.quick.fusion.parser;

import sdk.server.quick.fusion.enums.RequestType;
import sdk.server.quick.fusion.model.HttpRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {

    private HttpRequest parse(final String requestBody) throws Exception {
        String[] lines = requestBody.split("\n");

        HttpRequest httpRequest = new HttpRequest();
        Class clazz = httpRequest.getClass();
        for (String line : lines) {
            if (line.contains(": ")) {
                int delimiterIndex = line.indexOf(": ");
                String name = line.substring(0, delimiterIndex);
                String value = line.substring(delimiterIndex + 2);
            } else {
               String[] requestParameters = line.split(" ");
               RequestType requestType = RequestType.valueOf(requestParameters[0] != null ? requestParameters[0] : RequestType.GET.name());
               String urlPattern = requestParameters[1];
               Method setType = clazz.getDeclaredMethod("setType", RequestType.class);
               Method setUrlPattern = clazz.getDeclaredMethod("setUrlPattern", String.class);
               setType.setAccessible(true);
               setUrlPattern.setAccessible(true);
               setType.invoke(httpRequest, requestType);
               int questionMarkIndex = urlPattern.indexOf("?");
               setUrlPattern.invoke(httpRequest, urlPattern);
               if (questionMarkIndex != -1) {
                   setUrlPattern.invoke(httpRequest, urlPattern.substring(0, questionMarkIndex));
                   //TODO: Extract query params
                   String[] queryStrings = urlPattern.substring(questionMarkIndex + 1).split("&");
                   Method setQueryParameters = clazz.getDeclaredMethod("setQueryParameters", Map.class);
                   setQueryParameters.setAccessible(true);
                   Map<String, Object> queryParametersMap = new HashMap<>();
                   for (String queryString : queryStrings) {
                       String key = queryString.split("=")[0];
                       Object value = queryString.split("=")[1];
                       queryParametersMap.put(key, value);
                   }
                   setQueryParameters.invoke(httpRequest, queryParametersMap);
               }
            }
        }
        return httpRequest;
    }

}
