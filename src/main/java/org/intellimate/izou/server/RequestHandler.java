package org.intellimate.izou.server;

import com.esotericsoftware.yamlbeans.YamlWriter;
import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.config.AddOn;
import org.intellimate.izou.identification.AddOnInformationManager;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.IzouModule;
import org.intellimate.server.proto.*;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author LeanderK
 * @version 1.0
 */
public class RequestHandler extends IzouModule {
    private static JsonFormat.Printer PRINTER = JsonFormat.printer().includingDefaultValueFields();
    private static JsonFormat.Parser PARSER = JsonFormat.parser();


    public RequestHandler(Main main) {
        super(main);
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
            return handleListApps(httpRequest);
        } else if (url.matches("/apps/\\d+(/.*)?")) {
            Pattern pattern = Pattern.compile("/apps/(?<id>\\d+)(/.*)?");
            return handleAddonHTTPRequest(httpRequest, url, pattern, id -> getMain().getAddOnInformationManager().getAddOn(Integer.parseInt(id)));
        } else if (url.matches("/apps/dev/\\w+(/.*)?")) {
            if (httpRequest.getMethod().equals("POST") && url.matches("/apps/dev/\\w+/\\d+/\\d+/\\d+")) {
                return saveLocalApp(httpRequest, url);
            } else if (httpRequest.getMethod().equals("GET")) {
                Pattern pattern = Pattern.compile("/apps/dev/(?<id>\\w+)(/.*)?");
                return handleAddonHTTPRequest(httpRequest, url, pattern, id -> getMain().getAddOnInformationManager().getAddOn(Integer.parseInt(id)));
            }
        }
        return sendStringMessage("illegal request, no suitable route found", 404);
    }

    private HttpResponse saveLocalApp(HttpRequest httpRequest, String url) {
        Pattern pattern = Pattern.compile("/apps/dev/(?<id>\\w+)/(?<major>\\d+)/(?<minor>\\d+)/(?<patch>\\d+)");
        Matcher matcher = pattern.matcher(url);
        String id = matcher.group("id");
        int major = Integer.parseInt(matcher.group("major"));
        int minor = Integer.parseInt(matcher.group("minor"));
        int patch = Integer.parseInt(matcher.group("patch"));
        AddOn addOn = new AddOn(id, major+"."+minor+"."+patch, -1);
        getMain().getAddOnInformationManager().getInstalledAddOns().add(addOn);
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
            getMain().getAddOnInformationManager().addAddonToInstalledList(addOn);
        } catch (IOException e) {
            error("unable to write to config file", e);
            return sendStringMessage("the file was added but izou was unable to update the config file", 404);
        }
        return sendStringMessage("OK", 201);
    }

    private HttpResponse handleListApps(HttpRequest httpRequest) {
        if (httpRequest.getMethod().equals("GET")) {
            IzouAppList appList = IzouAppList.newBuilder()
                    .addAllSelected(
                            getMain().getAddOnInformationManager().getInstalledAddOns().stream()
                                    .map(this::toApp)
                                    .collect(Collectors.toList())
                    )
                    .addAllToInstall(
                            getMain().getAddOnInformationManager().getInstalledWithDependencies().stream()
                                    .map(this::toApp)
                                    .collect(Collectors.toList())
                    )
                    .addAllToDelete(
                            getMain().getAddOnInformationManager().getAddOnsToDelete().stream()
                                    .map(this::toApp)
                                    .collect(Collectors.toList())
                    )
                    .addAllToInstall(
                            getMain().getAddOnInformationManager().getAddOnsToInstall().stream()
                                    .map(this::toApp)
                                    .collect(Collectors.toList())
                    )
                    .build();
            return messageHelper(appList, 200);
        } else if (httpRequest.getMethod().equals("PATCH")) {
            String json = httpRequest.getBody().toStringUtf8();
            IzouAppList.Builder builder = IzouAppList.newBuilder();
            try {
                PARSER.merge(json, builder);
            } catch (InvalidProtocolBufferException e) {
                error("unable to parse message", e);
                return sendStringMessage("unable to parse message", 400);
            }
            IzouAppList list = builder.build();
            AddOnInformationManager informationManager = getMain().getAddOnInformationManager();

            if (list.getInstalledList().size() != informationManager.getInstalledAddOns().size()) {
                return sendStringMessage("patched installed list is not the same size as the local one", 400);
            }

            Set<Integer> installedIds = informationManager.getInstalledAddOns().stream()
                    .map(AddOn::getId)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());

            Set<String> installedNames = informationManager.getInstalledAddOns().stream()
                    .map(addOn -> addOn.name)
                    .collect(Collectors.toSet());

            List<AddOn> newToInstall = list.getToInstallList().stream()
                    .map(this::toAddon)
                    .collect(Collectors.toList());

            List<AddOn> newToDelete = list.getToDeleteList().stream()
                    .map(this::toAddon)
                    .collect(Collectors.toList());

            boolean deleteWithoutInstalled = newToDelete.stream()
                    .filter(addOn -> addOn.getId().map(id -> !installedIds.contains(id)).orElse(false) || !installedNames.contains(addOn.name))
                    .findAny().isPresent();

            if (deleteWithoutInstalled) {
                return sendStringMessage("delete app list contains apps that are not installed", 400);
            }

            boolean alreadyInstalled = newToInstall.stream()
                    .anyMatch(addOn -> addOn.getId().map(installedIds::contains).orElse(false) || installedNames.contains(addOn.name));

            if (alreadyInstalled) {
                return sendStringMessage("toInstall app list contains installed apps", 400);
            }

            if (newToInstall.stream().anyMatch(addon -> !addon.getId().isPresent())) {
                return sendStringMessage("toInstall app list contains apps without ids", 400);
            }

            try {
                informationManager.setAddonsToDelete(newToDelete);
                informationManager.setAddOnsToInstall(newToInstall);
            } catch (IOException e) {
                error("unable to write to config file", e);
                return sendStringMessage("unable to write to config file", 404);
            }
        }
        return sendStringMessage("illegal request, no suitable route found", 404);
    }

    private App toApp(AddOn addOn) {
        App.Builder builder = App.newBuilder()
                .setName(addOn.name)
                .addVersions(App.AppVersion.newBuilder().setVersion(addOn.version));
        addOn.getId().ifPresent(builder::setId);
        return builder.build();
    }

    private AddOn toAddon(App app) {
        int id = -1;
        if (app.hasField(app.getDescriptorForType().findFieldByNumber(App.ID_FIELD_NUMBER))) {
            id = app.getId();
        }
        return new AddOn(app.getName(), null, id);
    }

    private HttpResponse handleAddonHTTPRequest(HttpRequest httpRequest, String url, Pattern pattern, Function<String, Optional<AddOnModel>> getAddon) {
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
        Optional<AddOnModel> addOnModel = getAddon.apply(id);
        if (!addOnModel.isPresent()) {
            return sendStringMessage("no local app found with id: "+id, 404);
        }
        //TODO wrap in completableFuture to isolate
        return addOnModel.flatMap(addOnModelInstance -> Optional.ofNullable(addOnModelInstance.handleRequest(httpRequest)))
                .orElseGet(() -> HttpResponse.newBuilder().setStatus(404).build());
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
