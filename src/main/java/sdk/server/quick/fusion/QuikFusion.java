package sdk.server.quick.fusion;

import sdk.server.quick.fusion.annotations.QuikFusionWebServlet;
import sdk.server.quick.fusion.enums.RequestType;
import sdk.server.quick.fusion.exceptions.QuikFusionException;
import sdk.server.quick.fusion.interfaces.HttpServlet;
import sdk.server.quick.fusion.model.HttpRequest;
import sdk.server.quick.fusion.model.HttpResponse;
import sdk.server.quick.fusion.parser.RequestParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuikFusion {

    private static OutputStreamWriter writer;
    public static BufferedReader reader;
    private static Map<String, HttpServlet> urlToServletMapping;

    public static void main(String[] args) {
       start(9000);
    }

    private static void init() throws Exception {
        urlToServletMapping = new HashMap<>();
        String baseDirectory = "src/main/java";
        String packageToScan = getBasePackage();
        List<Class<?>> classesInPackage = scanClassesInPackage(baseDirectory, packageToScan);

        for (Class<?> clazz : classesInPackage) {
            QuikFusionWebServlet quikFusionWebServlet = clazz.getAnnotation(QuikFusionWebServlet.class);
            String urlPattern = quikFusionWebServlet.value();
            urlToServletMapping.put(urlPattern, (HttpServlet) clazz.newInstance());
        }
    }

    public static List<Class<?>> scanClassesInPackage(String baseDirectory, String packageToScan)
            throws ClassNotFoundException {
        List<Class<?>> foundClasses = new ArrayList<>();
        String packagePath = packageToScan.replace('.', File.separatorChar);
        String basePath = baseDirectory + File.separator + packagePath;

        File packageDir = new File(basePath);
        if (packageDir.exists() && packageDir.isDirectory()) {
            findClassesInDirectory(packageToScan, baseDirectory, packageDir, foundClasses);
        }
        return foundClasses;
    }

    private static void findClassesInDirectory(String packageName, String baseDirectory, File dir, List<Class<?>> foundClasses)
            throws ClassNotFoundException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String newPackageName = packageName + "." + file.getName();
                    String newDirectoryPath = baseDirectory + File.separator + newPackageName.replace('.', File.separatorChar);
                    findClassesInDirectory(newPackageName, baseDirectory, new File(newDirectoryPath), foundClasses);
                } else {
                    String className = packageName + "." + file.getName().substring(0, file.getName().length() - 5);
                    Class<?> clazz = Class.forName(className);
                    if (file.getName().endsWith("java") && clazz.isAnnotationPresent(QuikFusionWebServlet.class) && HttpServlet.class.isAssignableFrom(clazz)) {
                        foundClasses.add(clazz);
                    }
                }
            }
        }
    }
    private static String getBasePackage() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String callingClassName = stackTrace[2].getClassName();
        String[] packageParts = callingClassName.split("\\.");
        return packageParts[0];
    }
    public static void start(int port) {
        try {
            init();
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                InputStream inputStream = clientSocket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder requestContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        break;
                    }
                    requestContent.append(line).append("\n");
                }
                RequestParser requestParser = new RequestParser();
                Class requestParserClass = requestParser.getClass();
                Method parse = requestParserClass.getDeclaredMethod("parse", String.class);
                parse.setAccessible(true);
                HttpRequest httpRequest = (HttpRequest) parse.invoke(requestParser, requestContent.toString());
                HttpServlet servlet = urlToServletMapping.get(httpRequest.getUrlPattern());
                HttpResponse httpResponse = new HttpResponse();
                OutputStream outputStream = clientSocket.getOutputStream();
                Class responseClass = httpResponse.getClass();
                Method setWriter = responseClass.getDeclaredMethod("setWriter", OutputStreamWriter.class);
                writer = new OutputStreamWriter(outputStream);
                setWriter.setAccessible(true);
                setWriter.invoke(httpResponse, writer);
                if (servlet != null && httpRequest.getType().equals(RequestType.GET)) {
                     servlet.get(httpRequest, httpResponse);
                }
                if (servlet != null && httpRequest.getType().equals(RequestType.POST)) {
                    servlet.post(httpRequest, httpResponse);
                }
                writer.flush();
                clientSocket.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new QuikFusionException(exception.getMessage());
        }

    }

}