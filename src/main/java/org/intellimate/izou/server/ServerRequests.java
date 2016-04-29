package org.intellimate.izou.server;

import com.google.protobuf.util.JsonFormat;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import java.io.InputStream;

/**
 * used for all the qhttp-requests to the server
 * @author LeanderK
 * @version 1.0
 */
public class ServerRequests {
    private final String url;
    private final String refreshToken;
    private String authToken = null;
    private final JsonFormat.Parser parser = JsonFormat.parser();

    public ServerRequests(String url, String refreshToken) {
        this.url = url;
        this.refreshToken = refreshToken;
    }

    public void init() throws UnirestException {
        if (authToken != null) {
            return;
        }
        HttpResponse<String> response = Unirest.get(url + "/authentication/izou")
                .queryString("Authorization", "Bearer " + refreshToken)
                .asString();

        if (response.getStatus() != 200) {
            throw new IllegalStateException("server answered with " + response.getStatus());
        } else {
            authToken = response.getBody();
        }
    }

    public String getNewestVersion() throws UnirestException {
        HttpResponse<JsonNode> jsonResponse = doGet("/izou").asJson();
        if (jsonResponse.getStatus() != 200) {
            throw new IllegalStateException("server answered with " + jsonResponse.getStatus());
        }
        //TODO: protobuf builder
        parser.merge(jsonResponse.getBody().toString(), null);
    }

    public InputStream downloadIzouVersion(String url) throws UnirestException {
        assureInit();
        HttpResponse<InputStream> response = Unirest.get(url)
                .queryString("Authorization", "Bearer " + authToken)
                .asBinary();
        if (response.getStatus() != 200) {
            throw new IllegalStateException("server answered with " + response.getStatus());
        }
        return response.getBody();
    }

    private HttpRequest doGet(String route) {
        assureInit();
        return Unirest.get(url + route)
                .header("Accept", "application/json")
                .queryString("Authorization", "Bearer " + authToken);
    }

    private void assureInit() {
        if (authToken == null) {
            throw new IllegalStateException("not initialized");
        }
    }
}
