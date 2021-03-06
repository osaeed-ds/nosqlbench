package io.nosqlbench.engine.clients.grafana;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.engine.clients.grafana.transfer.Annotation;
import io.nosqlbench.engine.clients.grafana.transfer.Annotations;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

/**
 * @see <a href="https://grafana.com/docs/grafana/latest/http_api/annotations/">Grafana Annotations API Docs</a>
 */
public class GrafanaClient {

    private final URI baseuri;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    Authenticator auth = null;
    private String username;
    private String password;

    public GrafanaClient(String baseurl) {
        this.baseuri = initURI(baseurl);
    }

    public void basicAuth(String username, String password) {
        this.username = username;
        this.password = password;
        this.auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        };
    }

    private URI initURI(String baseurl) {
        try {
            URI uri = new URI(baseurl);
            String userinfo = uri.getRawUserInfo();
            if (userinfo != null) {
                String[] unpw = userinfo.split(":");
                this.username = unpw[0];
                this.password = unpw[1];
                uri = new URI(baseurl.replace(userinfo + "@", ""));
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpClient getClient() {
        HttpClient.Builder cb = HttpClient.newBuilder();
        if (this.auth != null) {
            cb.authenticator(auth);
        }
        HttpClient client = cb.build();
        return client;
    }

    private URI makeUri(String pathAndQuery) {
        try {
            return new URI(this.baseuri.toString() + pathAndQuery);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <pre>{@code
     * GET /api/annotations?from=1506676478816&to=1507281278816&tags=tag1&tags=tag2&limit=100
     *
     * Example Request:
     *
     * GET /api/annotations?from=1506676478816&to=1507281278816&tags=tag1&tags=tag2&limit=100 HTTP/1.1
     * Accept: application/json
     * Content-Type: application/json
     * Authorization: Basic YWRtaW46YWRtaW4=
     * Query Parameters:
     *
     * from: epoch datetime in milliseconds. Optional.
     * to: epoch datetime in milliseconds. Optional.
     * limit: number. Optional - default is 100. Max limit for results returned.
     * alertId: number. Optional. Find annotations for a specified alert.
     * dashboardId: number. Optional. Find annotations that are scoped to a specific dashboard
     * panelId: number. Optional. Find annotations that are scoped to a specific panel
     * userId: number. Optional. Find annotations created by a specific user
     * type: string. Optional. alert|annotation Return alerts or user created annotations
     * tags: string. Optional. Use this to filter global annotations. Global annotations are annotations from an annotation data source that are not connected specifically to a dashboard or panel. To do an “AND” filtering with multiple tags, specify the tags parameter multiple times e.g. tags=tag1&tags=tag2.
     * Example Response:
     *
     * HTTP/1.1 200
     * Content-Type: application/json
     * [
     *     {
     *         "id": 1124,
     *         "alertId": 0,
     *         "dashboardId": 468,
     *         "panelId": 2,
     *         "userId": 1,
     *         "userName": "",
     *         "newState": "",
     *         "prevState": "",
     *         "time": 1507266395000,
     *         "timeEnd": 1507266395000,
     *         "text": "test",
     *         "metric": "",
     *         "type": "event",
     *         "tags": [
     *             "tag1",
     *             "tag2"
     *         ],
     *         "data": {}
     *     },
     *     {
     *         "id": 1123,
     *         "alertId": 0,
     *         "dashboardId": 468,
     *         "panelId": 2,
     *         "userId": 1,
     *         "userName": "",
     *         "newState": "",
     *         "prevState": "",
     *         "time": 1507265111000,
     *         "text": "test",
     *         "metric": "",
     *         "type": "event",
     *         "tags": [
     *             "tag1",
     *             "tag2"
     *         ],
     *         "data": {}
     *     }
     * ]
     * }</pre>
     *
     * @param by
     * @return
     */
    public Annotations findAnnotations(By... by) {

        String query = By.fields(by);
        HttpRequest.Builder rqb = HttpRequest.newBuilder(makeUri("api/annotations?" + query));
        rqb = addAuth(rqb);
        rqb.setHeader("Content-Type", "application/json");
        HttpRequest request = rqb.build();

        HttpClient client = getClient();
        HttpResponse<String> response = null;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String body = response.body();
        Annotations annotations = gson.fromJson(body, Annotations.class);
        return annotations;
    }

    /**
     * <pre>{@code
     * POST /api/annotations
     *
     * Example Request:
     *
     * POST /api/annotations HTTP/1.1
     * Accept: application/json
     * Content-Type: application/json
     *
     * {
     *   "dashboardId":468,
     *   "panelId":1,
     *   "time":1507037197339,
     *   "timeEnd":1507180805056,
     *   "tags":["tag1","tag2"],
     *   "text":"Annotation Description"
     * }
     * Example Response:
     *
     * HTTP/1.1 200
     * Content-Type: application/json
     *
     * {
     *     "message":"Annotation added",
     *     "id": 1,
     * }
     * }</pre>
     *
     * @return
     */
    public Annotation createAnnotation(Annotation annotation) {
        HttpClient client = getClient();
        HttpRequest.Builder rqb = HttpRequest.newBuilder(makeUri("api/annotations"));
        rqb = addAuth(rqb);
        rqb.setHeader("Content-Type", "application/json");
        String rqBody = gson.toJson(annotation);
        rqb = rqb.POST(HttpRequest.BodyPublishers.ofString(rqBody));
        addAuth(rqb);

        HttpResponse<String> response = null;
        try {
            response = client.send(rqb.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            if (e.getMessage().contains("WWW-Authenticate header missing")) {
                throw new RuntimeException("Java HttpClient was not authorized, and it saw no WWW-Authenticate header" +
                        " in the response, so this is probably Grafana telling you that the auth scheme failed. Normally " +
                        "this error would be thrown by Java HttpClient:" + e.getMessage());
            }
            throw new RuntimeException(e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Creating annotation failed with status code " + response.statusCode() + " at " +
                    "baseurl " + baseuri + ": " + response.body());
        }
        String body = response.body();
        Annotation savedAnnotation = gson.fromJson(body, Annotation.class);
        return savedAnnotation;
    }

    private HttpRequest.Builder addAuth(HttpRequest.Builder rqb) {
        if (this.username != null && this.password != null) {
            rqb = rqb.setHeader("Authorization", encodeBasicAuth(username, password));
        }
        return rqb;
    }

    private static String encodeBasicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    /**
     * <pre>{@code
     * POST /api/annotations/graphite
     *
     * Example Request:
     *
     * POST /api/annotations/graphite HTTP/1.1
     * Accept: application/json
     * Content-Type: application/json
     *
     * {
     *   "what": "Event - deploy",
     *   "tags": ["deploy", "production"],
     *   "when": 1467844481,
     *   "data": "deploy of master branch happened at Wed Jul 6 22:34:41 UTC 2016"
     * }
     * Example Response:
     *
     * HTTP/1.1 200
     * Content-Type: application/json
     *
     * {
     *     "message":"Graphite annotation added",
     *     "id": 1
     * }
     * }</pre>
     *
     * @return
     */
    public Annotation createGraphiteAnnotation() {
        return null;
    }

    /**
     * <pre>{@code
     * PUT /api/annotations/:id
     *
     * Updates all properties of an annotation that matches the specified id. To only update certain property, consider using the Patch Annotation operation.
     *
     * Example Request:
     *
     * PUT /api/annotations/1141 HTTP/1.1
     * Accept: application/json
     * Authorization: Bearer eyJrIjoiT0tTcG1pUlY2RnVKZTFVaDFsNFZXdE9ZWmNrMkZYbk
     * Content-Type: application/json
     *
     * {
     *   "time":1507037197339,
     *   "timeEnd":1507180805056,
     *   "text":"Annotation Description",
     *   "tags":["tag3","tag4","tag5"]
     * }
     * Example Response:
     *
     * HTTP/1.1 200
     * Content-Type: application/json
     *
     * {
     *     "message":"Annotation updated"
     * }
     * }</pre>
     */
    public void updateAnnotation() {

    }

    /**
     * <pre>{@code
     * PATCH /api/annotations/:id
     *
     * Updates one or more properties of an annotation that matches the specified id.
     *
     * This operation currently supports updating of the text, tags, time and timeEnd properties.
     *
     * Example Request:
     *
     * PATCH /api/annotations/1145 HTTP/1.1
     * Accept: application/json
     * Authorization: Bearer eyJrIjoiT0tTcG1pUlY2RnVKZTFVaDFsNFZXdE9ZWmNrMkZYbk
     * Content-Type: application/json
     *
     * {
     *   "text":"New Annotation Description",
     *   "tags":["tag6","tag7","tag8"]
     * }
     * Example Response:
     *
     * HTTP/1.1 200
     * Content-Type: application/json
     *
     * {
     *     "message":"Annotation patched"
     * }
     * }</pre>
     */
    public void patchAnnotation() {

    }

    /**
     * <pre>{@code
     * Example Request:
     *
     * DELETE /api/annotations/1 HTTP/1.1
     * Accept: application/json
     * Content-Type: application/json
     * Authorization: Bearer eyJrIjoiT0tTcG1pUlY2RnVKZTFVaDFsNFZXdE9ZWmNrMkZYbk
     * Example Response:
     *
     * HTTP/1.1 200
     * Content-Type: application/json
     *
     * {
     *     "message":"Annotation deleted"
     * }
     * }</pre>
     *
     * @param id
     */
    public void deleteAnnotation(long id) {

    }

}
