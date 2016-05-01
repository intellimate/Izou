package org.intellimate.izou.server;

import com.esotericsoftware.yamlbeans.YamlWriter;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.intellimate.izou.config.AddOn;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.IzouModule;
import org.intellimate.server.proto.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sun.tools.internal.xjc.reader.Ring.add;

/**
 * @author LeanderK
 * @version 1.0
 */
public class RequestHandler extends IzouModule {
    private static JsonFormat.Printer PRINTER = JsonFormat.printer().includingDefaultValueFields();
    private static JsonFormat.Parser PARSER = JsonFormat.parser();
    private final List<AddOn> addOnList;
    private final Map<Integer, AddOn> addonIdMap;
    private final Map<String, AddOn> addonNameMap;
    private final String addonsFile;


    public RequestHandler(Main main, List<AddOn> addOnList, String addonsFile) {
        super(main);
        this.addOnList = addOnList;
        this.addonsFile = addonsFile;
        addonIdMap = addOnList.stream()
                .filter(addOn -> addOn.getId().isPresent())
                .collect(Collectors.toMap(addOn -> addOn.getId().get(), Function.identity(), (u, u2) -> u));
        addonNameMap = addOnList.stream()
                .collect(Collectors.toMap(addOn -> addOn.name, Function.identity(), (u, u2) -> u));
    }

    HttpResponse handleRequests(HttpRequest httpRequest) {
        if (httpRequest.getUrl().startsWith("/apps")) {
            return handleApps(httpRequest);
        } else if (httpRequest.getUrl().equals("/stats")) {
            return handleStatus(httpRequest);
        }
        return HttpResponse.newBuilder().setStatus(404).build();
    }

    private HttpResponse handleApps(HttpRequest httpRequest) {
        String url = httpRequest.getUrl();
        if (url.equals("/apps")) {
            if (httpRequest.getMethod().equals("GET")) {
                List<App> apps = addOnList.stream()
                        .map(addOn -> {
                            App.Builder builder = App.newBuilder();
                            addOn.getId().ifPresent(builder::setId);
                            return builder.setName(addOn.name)
                                    .addVersions(App.AppVersion.newBuilder().setVersion(addOn.version).build())
                                    .build();
                        })
                        .collect(Collectors.toList());
                return messageHelper(AppList.newBuilder().addAllApps(apps).build(), 200);
            } else if (httpRequest.getMethod().equals("PATCH")) {
                String json = httpRequest.getBody().toStringUtf8();
                IzouInstanceStatus.Builder builder = IzouInstanceStatus.newBuilder();
                try {
                    PARSER.merge(json, builder);
                } catch (InvalidProtocolBufferException e) {
                    error("unable to parse message", e);
                    return sendStringMessage("unable to parse message", 400);
                }

                //TODO
            }
        } else if (url.matches("/apps/\\d+(/.*)?")) {
            Pattern pattern = Pattern.compile("/apps/(?<id>\\d+)(/.*)?");
            Matcher matcher = pattern.matcher(url);
            int id = Integer.parseInt(matcher.group("id"));
            Boolean authorized = httpRequest.getParamsList().stream()
                    .filter(param -> param.getKey().equals("app"))
                    .map(HttpRequest.Param::getValueList)
                    .findAny()
                    .map(list -> list.stream().filter(authorizedApp -> authorizedApp.equals(String.valueOf(id))).findAny().isPresent())
                    .orElse(true);

            if (!authorized) {
                return sendStringMessage("App is not authorized to request pages from other apps", 401);
            }
            AddOn addOn = addonIdMap.get(id);
            if (addOn == null) {
                return sendStringMessage("no local app found with id: "+id, 404);
            }
            //TODO code
            return null;
        } else if (url.matches("/apps/dev/\\w+(/.*)?")) {
            if (httpRequest.getMethod().equals("GET")) {
                Pattern pattern = Pattern.compile("/apps/dev/(?<id>\\w+)(/.*)?");
                Matcher matcher = pattern.matcher(url);
                String id = matcher.group("id");
                Boolean authorized = httpRequest.getParamsList().stream()
                        .filter(param -> param.getKey().equals("app"))
                        .map(HttpRequest.Param::getValueList)
                        .findAny()
                        .map(list -> list.stream().filter(authorizedApp -> authorizedApp.equals(id)).findAny().isPresent())
                        .orElse(true);

                if (!authorized) {
                    return sendStringMessage("App is not authorized to request pages from other apps", 401);
                }
                AddOn addOn = addonNameMap.get(id);
                if (addOn == null) {
                    return sendStringMessage("no local app found with name: "+id, 404);
                }
                //TODO code
                return null;
            } else if (httpRequest.getMethod().equals("POST") && url.matches("/apps/dev/\\w+/\\d+/\\d+/\\d+")) {
                Pattern pattern = Pattern.compile("/apps/dev/(?<id>\\w+)/(?<major>\\d+)/(?<minor>\\d+)/(?<patch>\\d+)");
                Matcher matcher = pattern.matcher(url);
                String id = matcher.group("id");
                int major = Integer.parseInt(matcher.group("major"));
                int minor = Integer.parseInt(matcher.group("minor"));
                int patch = Integer.parseInt(matcher.group("patch"));
                AddOn addOn = new AddOn(id, major+"."+minor+"."+patch, -1);
                addOnList.add(addOn);
                addonNameMap.put(id, addOn);
                File file = new File(getMain().getFileSystemManager().getNewLibLocation(), id+"-"+major+"."+minor+"."+patch+".zip");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        error("unable to create File "+ file);
                        return sendStringMessage("an internal error occured", 500);
                    }
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    fos.write(httpRequest.getBody().toByteArray());
                } catch (IOException e) {
                    error("unable to write to file", e);
                    return sendStringMessage("an internal error occured", 500);
                } finally {
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        error("unable to cloe FileOutput", e);
                    }
                }
                try {
                    writeNewYaml(addOnList);
                } catch (IOException e) {
                    error("unable to update apps File", e);
                    return sendStringMessage("an internal error occured", 500);
                }
            }
        }
        return sendStringMessage("illegal request, no suitable route found", 404);
    }

    private void writeNewYaml(List<AddOn> addOns) throws IOException {
        YamlWriter yamlWriter = new YamlWriter(new FileWriter(addonsFile));
        ArrayList<AddOn> tempList = new ArrayList<>(addOns);
        yamlWriter.write(tempList);
    }

    private HttpResponse handleStatus(HttpRequest httpRequest) {
        if (httpRequest.getMethod().equals("GET")) {
            IzouInstanceStatus status = IzouInstanceStatus.newBuilder().setStatus(IzouInstanceStatus.Status.RUNNING).build();
            return messageHelper(status, 200);
        } else if (httpRequest.getMethod().equals("PATCH")) {
            String json = httpRequest.getBody().toStringUtf8();
            IzouInstanceStatus.Builder builder = IzouInstanceStatus.newBuilder();
            try {
                PARSER.merge(json, builder);
            } catch (InvalidProtocolBufferException e) {
                error("unable to parse message", e);
                return sendStringMessage("unable to parse message", 400);
            }
            //TODO implement (beware of concurrency!)
            switch (builder.getStatus()) {
                case UNRECOGNIZED: return sendStringMessage("unable to parse message", 400);
                case UPDATING: return sendStringMessage("not implemented yet", 500);
                case RESTARTING: return sendStringMessage("not implemented yet", 500);
                case RUNNING: IzouInstanceStatus status = IzouInstanceStatus.newBuilder().setStatus(IzouInstanceStatus.Status.RUNNING).build();
                    return messageHelper(status, 200);
                case DISABLED: return sendStringMessage("not implemented yet", 500);
            }
        }
        return sendStringMessage("illegal request, no suitable route found", 404);
    }

    private HttpResponse messageHelper(Message message, int status) {
        try {
            return HttpResponse.newBuilder()
                    .setContentType("application/json")
                    .setBody(ByteString.copyFromUtf8(PRINTER.print(message)))
                    .setStatus(status)
                    .build();
        } catch (InvalidProtocolBufferException e) {
            error("unable to print message", e);
            return HttpResponse.newBuilder().setStatus(500).build();
        }
    }

    private HttpResponse sendStringMessage(String message, int status) {
        return HttpResponse.newBuilder()
                .setStatus(status)
                .setContentType("text")
                .setBody(ByteString.copyFromUtf8(message))
                .build();
    }
}
