package org.intellimate.izou.server;

import com.google.common.io.ByteStreams;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.config.AddOn;
import org.intellimate.izou.identification.AddOnInformation;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.main.UpdateManager;
import org.intellimate.izou.util.AddonThreadPoolUser;
import org.intellimate.izou.util.IzouModule;
import org.intellimate.server.proto.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
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
//TODO new status for update errored?
class RequestHandler extends IzouModule implements AddonThreadPoolUser {
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
        Pattern serverIDPattern = Pattern.compile("/apps/(?<id>\\d+)(/.*)?");
        Matcher serverIDMatcher = serverIDPattern.matcher(url);
        if (url.equals("/apps")) {
            return handleListApps(request);
        } else if (serverIDMatcher.matches()) {
            return handleAddonHTTPRequest(request, serverIDMatcher, id -> getMain().getAddOnInformationManager().getAddOn(Integer.parseInt(id)));
        } else if (url.matches("/apps/dev/\\w+(/.*)?")) {
            Pattern pattern = Pattern.compile("/apps/dev/(?<id>\\w+)(/.*)?");
            Matcher devIDMatcher = pattern.matcher(url);
            if (request.getMethod().equals("POST") && url.matches("/apps/dev/\\w+/\\d+/\\d+/\\d+")) {
                return saveLocalApp(request, url);
            } else if (request.getMethod().equals("GET") && devIDMatcher.matches()) {
                return handleAddonHTTPRequest(request, devIDMatcher, id -> getMain().getAddOnInformationManager().getAddOn(id));
            }
        }
        return sendStringMessage("illegal request, no suitable route found", 404);
    }

    private Response saveLocalApp(Request request, String url) {
        Pattern pattern = Pattern.compile("/apps/dev/(?<id>\\w+)/(?<major>\\d+)/(?<minor>\\d+)/(?<patch>\\d+)");
        Matcher matcher = pattern.matcher(url);
        String name = matcher.group("id");
        int major = Integer.parseInt(matcher.group("major"));
        int minor = Integer.parseInt(matcher.group("minor"));
        int patch = Integer.parseInt(matcher.group("patch"));
        AddOn addOn = new AddOn(name, major+"."+minor+"."+patch, -1);
        if (getMain().getAddOnInformationManager().getSelectedAddOns().stream()
                .noneMatch(selected -> selected.name.equals(addOn.name))) {
            try {
                getMain().getAddOnInformationManager().addAddonToSelectedList(addOn);
            } catch (IOException e) {
                error("unable to write to config file", e);
                return sendStringMessage("izou was unable to update the config file", 404);
            }
        }

        try {
            Files.list(getMain().getFileSystemManager().getNewLibLocation().toPath())
                    .map(Path::toFile)
                    .filter(file -> file.getName().matches("\\w+-[\\.\\d]+\\.zip"))
                    .filter(file -> file.getName().startsWith(addOn.name))
                    .forEach(file -> {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            error("unable to delete downloaded version: " + file.toString());
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(getMain().getFileSystemManager().getNewLibLocation(), name+"-"+major+"."+minor+"."+patch+".zip");
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
            long copied = ByteStreams.copy(request.getData(), fos);
            if (copied != request.getContentLength()) {
                file.delete();
                return sendStringMessage("Body size does not equal advertised size", 400);
            }
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
            List<AddOnInformation> installed = null;
            List<AddOn> scheduledToDelete = null;
            List<AddOn> scheduledToInstall = null;
            try {
                installed = getMain().getAddOnManager().getInstalledWithDependencies();
                scheduledToDelete = getMain().getAddOnManager().getScheduledToDelete();
                scheduledToInstall = getMain().getAddOnManager().getScheduledToInstall();
            } catch (IOException e) {
                debug("unable to access App File-Directories", e);
                return sendStringMessage("unable to access App File-Directories", 500);
            }
            IzouAppList appList = IzouAppList.newBuilder()
                    .addAllSelected(
                            getMain().getAddOnInformationManager().getSelectedAddOns().stream()
                                    .map(this::toApp)
                                    .collect(Collectors.toList())
                    )
                    .addAllInstalled(
                            installed.stream()
                                    .map(this::toApp)
                                    .collect(Collectors.toList())
                    )
                    .addAllToDelete(
                            scheduledToDelete.stream()
                                    .map(this::toApp)
                                    .collect(Collectors.toList())
                    )
                    .addAllToInstall(
                            scheduledToInstall.stream()
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

    private App toApp(AddOnInformation addOn) {
        App.Builder builder = App.newBuilder()
                .setName(addOn.getName())
                .addVersions(App.AppVersion.newBuilder().setVersion(addOn.getVersion().toString()));
        addOn.getServerID().ifPresent(builder::setId);
        return builder.build();
    }

    private AddOn toAddon(App app) {
        int id = -1;
        if (app.hasField(app.getDescriptorForType().findFieldByNumber(App.ID_FIELD_NUMBER))) {
            id = app.getId();
        }
        return new AddOn(app.getName(), null, id);
    }

    private Response handleAddonHTTPRequest(Request httpRequest, Matcher matcher, Function<String, Optional<AddOnModel>> getAddon) {
        if (getMain().getState().equals(IzouInstanceStatus.Status.DISABLED)) {
            //TODO: further coding? or another place?
        }
        String id = matcher.group("id");
        Boolean sameApp = httpRequest.getParams().entrySet().stream()
                .filter(param -> param.getKey().equals("app"))
                .map(Map.Entry::getValue)
                .findAny()
                .map(list -> list.stream().filter(authorizedApp -> authorizedApp.equals(id)).findAny().isPresent())
                .orElse(true);

        Request request = httpRequest;
        if (!sameApp) {
            HashMap<String, List<String>> httpParams = new HashMap<>(httpRequest.getParams());
            httpParams.remove("token");
            request = ((RequestImpl) httpRequest).changeParams(httpParams);
        }
        Request finalRequest = request;

        Optional<AddOnModel> addOnModel = getAddon.apply(id);
        if (!addOnModel.isPresent()) {
            return sendStringMessage("no local app found with id: "+id, 404);
        }

        return addOnModel.map(addOnModelInstance -> submit(() -> Optional.ofNullable(addOnModelInstance.handleRequest(finalRequest))))
                .flatMap(future -> {
                    try {
                        return future.join();
                    } catch (Exception e) {
                        return Optional.of(handleException(e, "an internal server error occured", 500));
                    }
                })
                .orElseGet(() -> sendStringMessage("illegal request, no suitable route found", 404));
    }

    private Response handleStatus(Request request) {
        if (request.getMethod().equals("GET")) {
            IzouInstanceStatus.Status status = IzouInstanceStatus.Status.RUNNING;
            if (getMain().getUpdateManager().isPresent() && getMain().getUpdateManager().get().isUpdating()) {
                status = IzouInstanceStatus.Status.UPDATING;
            }
            IzouInstanceStatus statusMessage = IzouInstanceStatus.newBuilder().setStatus(status).build();
            return messageHelper(statusMessage, 200);
        } else if (request.getMethod().equals("PATCH")) {
            String json = null;
            try {
                Optional<String> stringOpt = request.getDataAsUTF8String();
                if (stringOpt.isPresent()) {
                    json = stringOpt.get();
                } else {
                   return sendStringMessage("body-size does not match advertised size", 400);
                }
            } catch (IOException e) {
                return sendStringMessage("unable to read body", 500);
            }
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
        Optional<UpdateManager> updatesManager = getMain().getUpdateManager();
        if (updatesManager.isPresent()) {
            IzouInstanceStatus statusMessage = IzouInstanceStatus.newBuilder().setStatus(IzouInstanceStatus.Status.UPDATING).build();
            if (!updatesManager.get().isUpdating()) {
                updateFuture = CompletableFuture.runAsync(() -> {
                    try {
                        updatesManager.get().checkForUpdates();
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

    private Response handleException(Throwable throwable, String message, int status) {
        ErrorResponse build = ErrorResponse.newBuilder()
                .setCode(message)
                .setDetail(throwable.getMessage())
                .build();
        debug(message, throwable);
        return messageHelper(build, status);
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
        return new ResponseImpl(status, new HashMap<>(), "text/plain", message.getBytes(Charset.forName("UTF-8")));
    }
}
