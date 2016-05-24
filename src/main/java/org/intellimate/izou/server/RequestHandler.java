package org.intellimate.izou.server;

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
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author LeanderK
 * @version 1.0
 */
//TODO status add update errored?
class RequestHandler extends IzouModule {
    private static JsonFormat.Printer PRINTER = JsonFormat.printer().includingDefaultValueFields();
    private static JsonFormat.Parser PARSER = JsonFormat.parser();
    private CompletableFuture<Void> updateFuture;


    public RequestHandler(Main main) {
        super(main);
    }

    Response handleRequests(Request request) {
        if (request.getUrl().startsWith("/apps")) {
            return handleApps(request);
        } else if (request.getUrl().equals("/status")) {
            return handleStatus(request);
        }
        return sendStringMessage("illegal request, no suitable route found", 404);
    }

    private Response handleApps(Request request) {
        String url = request.getUrl();
        if (url.equals("/apps")) {
            return handleListApps(request);
        } else if (url.matches("/apps/\\d+(/.*)?")) {
            Pattern pattern = Pattern.compile("/apps/(?<id>\\d+)(/.*)?");
            return handleAddonHTTPRequest(request, url, pattern, id -> getMain().getAddOnInformationManager().getAddOn(Integer.parseInt(id)));
        } else if (url.matches("/apps/dev/\\w+(/.*)?")) {
            if (request.getMethod().equals("POST") && url.matches("/apps/dev/\\w+/\\d+/\\d+/\\d+")) {
                return saveLocalApp(request, url);
            } else if (request.getMethod().equals("GET")) {
                Pattern pattern = Pattern.compile("/apps/dev/(?<id>\\w+)(/.*)?");
                return handleAddonHTTPRequest(request, url, pattern, id -> getMain().getAddOnInformationManager().getAddOn(Integer.parseInt(id)));
            }
        }
        return sendStringMessage("illegal request, no suitable route found", 404);
    }

    private Response saveLocalApp(Request request, String url) {
        Pattern pattern = Pattern.compile("/apps/dev/(?<id>\\w+)/(?<major>\\d+)/(?<minor>\\d+)/(?<patch>\\d+)");
        Matcher matcher = pattern.matcher(url);
        String id = matcher.group("id");
        int major = Integer.parseInt(matcher.group("major"));
        int minor = Integer.parseInt(matcher.group("minor"));
        int patch = Integer.parseInt(matcher.group("patch"));
        AddOn addOn = new AddOn(id, major+"."+minor+"."+patch, -1);
        try {
            getMain().getAddOnInformationManager().addAddonToInstalledList(addOn);
        } catch (IOException e) {
            error("unable to write to config file", e);
            return sendStringMessage("izou was unable to update the config file", 404);
        }
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
            fos.write(request.getData());
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
        return sendStringMessage("OK", 201);
    }

    private Response handleListApps(Request request) {
        if (request.getMethod().equals("GET")) {
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
        } else if (request.getMethod().equals("PATCH")) {
            return sendStringMessage("not implemented yet", 404);
            /*
            String json = request.getDataAsUTF8();
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
            }*/
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

    private Response handleAddonHTTPRequest(Request httpRequest, String url, Pattern pattern, Function<String, Optional<AddOnModel>> getAddon) {
        Matcher matcher = pattern.matcher(url);
        String id = matcher.group("id");
        Boolean authorized = httpRequest.getParams().entrySet().stream()
                .filter(param -> param.getKey().equals("app"))
                .map(Map.Entry::getValue)
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
                .orElseGet(() -> sendStringMessage("illegal request, no suitable route found", 404));
    }

    private Response handleStatus(Request request) {
        if (request.getMethod().equals("GET")) {
            IzouInstanceStatus.Status status = IzouInstanceStatus.Status.RUNNING;
            if (getMain().getCommunicationManager().isPresent() && getMain().getCommunicationManager().get().isSynchronizing()) {
                status = IzouInstanceStatus.Status.UPDATING;
            }
            IzouInstanceStatus statusMessage = IzouInstanceStatus.newBuilder().setStatus(status).build();
            return messageHelper(statusMessage, 200);
        } else if (request.getMethod().equals("PATCH")) {
            String json = request.getDataAsUTF8();
            IzouInstanceStatus.Builder builder = IzouInstanceStatus.newBuilder();
            try {
                PARSER.merge(json, builder);
            } catch (InvalidProtocolBufferException e) {
                error("unable to parse message", e);
                return sendStringMessage("unable to parse message", 400);
            }
            //TODO implement (beware of concurrency!)
            switch (builder.getStatus()) {
                case UNRECOGNIZED: return sendStringMessage("unable to parse message", 404);
                case UPDATING: return handleUpdateRequest();
                case RESTARTING: return sendStringMessage("not implemented yet", 404);
                case RUNNING: IzouInstanceStatus status = IzouInstanceStatus.newBuilder().setStatus(IzouInstanceStatus.Status.RUNNING).build();
                    return messageHelper(status, 200);
                case DISABLED: return sendStringMessage("not implemented yet", 404);
            }
        }
        return sendStringMessage("illegal request, no suitable route found", 404);
    }

    private Response handleUpdateRequest() {
        Optional<CommunicationManager> communicationManager = getMain().getCommunicationManager();
        if (communicationManager.isPresent()) {
            IzouInstanceStatus statusMessage = IzouInstanceStatus.newBuilder().setStatus(IzouInstanceStatus.Status.UPDATING).build();
            if (!communicationManager.get().isSynchronizing()) {
                updateFuture = CompletableFuture.runAsync(() -> {
                    try {
                        communicationManager.get().checkForUpdates();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            return messageHelper(statusMessage, 200);
        } else {
            return sendStringMessage("communication to server is not active", 404);
        }
    }

    private Response messageHelper(Message message, int status) {
        try {
            return new ResponseImpl(status, new HashMap<>(), "application/json", PRINTER.print(message).getBytes(Charset.forName("UTF-8")));
        } catch (InvalidProtocolBufferException e) {
            error("unable to print message", e);
            return sendStringMessage("Izou: unable to print message in messageHelper", 500);
        }
    }

    private Response sendStringMessage(String message, int status) {
        return new ResponseImpl(status, new HashMap<>(), "text", message.getBytes(Charset.forName("UTF-8")));
    }
}
