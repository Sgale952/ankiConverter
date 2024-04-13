package github.sgale.cardOperations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import github.sgale.tasks.CardOperator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

import static github.sgale.tasks.PropertyGenerator.getSetting;

public class CardFinder extends CardOperator {
    private final String TAG;

    public CardFinder() {
        super("");
        this.TAG  = getSetting("tag");
    }

    public long[] findByTag() throws IOException {
        HttpURLConnection conn = createConnection("GET");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(buildJson().getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        System.out.println("Find cards: " + conn.getResponseCode());
        return getResponse(conn);
    }

    private String buildJson() {
        JsonObject params = new JsonObject();
        params.addProperty("query", "tag:"+TAG);

        JsonObject request = new JsonObject();
        request.addProperty("action", "findCards");
        request.addProperty("version", 6);
        request.add("params", params);

        return gson.toJson(request);
    }

    private long[] getResponse(HttpURLConnection conn) throws IOException {
        String rawResponse = getRawResponse(conn);
        JsonArray cardIdsJson = gson.fromJson(rawResponse, JsonObject.class).getAsJsonArray("result");

        long[] cardIds = new long[cardIdsJson.size()];
        for (int i = 0; i < cardIdsJson.size(); i++) {
            cardIds[i] = cardIdsJson.get(i).getAsLong();
        }

        return cardIds;
    }
}
