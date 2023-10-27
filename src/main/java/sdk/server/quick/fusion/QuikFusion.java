package sdk.server.quick.fusion;

import sdk.server.quick.fusion.annotations.QuikFusionWebServlet;
import sdk.server.quick.fusion.enums.RequestType;
import sdk.server.quick.fusion.exceptions.QuikFusionException;
import sdk.server.quick.fusion.interfaces.HttpServlet;
import sdk.server.quick.fusion.model.HttpRequest;
import sdk.server.quick.fusion.model.HttpResponse;
import sdk.server.quick.fusion.model.ResponseWriter;
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

    private static Map<String, HttpServlet> urlToServletMapping = new HashMap<>();

    private static void init(String basePackage) throws Exception {
        String baseDirectory = "src/main/java";
        List<Class<?>> classesInPackage = scanClassesInPackage(baseDirectory, basePackage);

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
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String basePackage = "";
            if (stackTrace.length >= 3) {
                String callingClassName = stackTrace[2].getClassName();
                int lastDotIndex = callingClassName.lastIndexOf('.');
                if (lastDotIndex >= 0) {
                    basePackage = callingClassName.substring(0, lastDotIndex);
                    basePackage = basePackage.substring(0, basePackage.indexOf("."));
                }
            }

            System.out.println("Quick Fusion has Started on the port number : " + port);
            init(basePackage);
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                InputStream inputStream = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
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
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);

                ResponseWriter responseWriter = new ResponseWriter();

                Class responseWriterClass = responseWriter.getClass();

                Method setOutputStreamWriter = responseWriterClass.getDeclaredMethod("setOutputStreamWriter", OutputStreamWriter.class);
                setOutputStreamWriter.setAccessible(true);
                setOutputStreamWriter.invoke(responseWriter, writer);

                Method setWriter = responseClass.getDeclaredMethod("setWriter", ResponseWriter.class);
                setWriter.setAccessible(true);
                setWriter.invoke(httpResponse, responseWriter);

                if (servlet != null && httpRequest.getType().equals(RequestType.GET)) {
                     servlet.get(httpRequest, httpResponse);
                }
                if (servlet != null && httpRequest.getType().equals(RequestType.POST)) {
                    servlet.post(httpRequest, httpResponse);
                }
                clientSocket.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new QuikFusionException(exception.getMessage());
        }

    }

}