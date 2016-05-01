package org.intellimate.izou.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.intellimate.izou.config.AddOn;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.IzouModule;
import org.intellimate.server.proto.HttpRequest;
import org.intellimate.server.proto.HttpResponse;
import org.intellimate.server.proto.IzouInstanceStatus;

import java.util.List;

/**
 * @author LeanderK
 * @version 1.0
 */
public class RequestHandler extends IzouModule {
    private static JsonFormat.Printer PRINTER = JsonFormat.printer().includingDefaultValueFields();
    private final List<AddOn> selectedWithoutDependencies;

    public RequestHandler(Main main, List<AddOn> selectedWithoutDependencies) {
        super(main);
        this.selectedWithoutDependencies = selectedWithoutDependencies;
    }

    HttpResponse handleRequests(HttpRequest httpRequest) {
        if (httpRequest.getUrl().startsWith("/apps")) {
            return handleApps(httpRequest);
        } else if (httpRequest.getUrl().equals("/stats")) {

        }
    }

    HttpResponse handleApps(HttpRequest httpRequest) {
        if (httpRequest.getUrl().equals("/apps")) {

        }
    }

    HttpResponse handleStatus(HttpRequest httpRequest) {
        //TODO support POST of status & implement the other statuses
        IzouInstanceStatus status = IzouInstanceStatus.newBuilder().setStatus(IzouInstanceStatus.Status.RUNNING).build();
        return messageHelper(status, 200);
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
            return HttpResponse.newBuilder().setStatus(400).build();
        }
    }
}
