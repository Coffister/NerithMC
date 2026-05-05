package gg.nerith.core.metrics;

import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;
import gg.nerith.core.NerithCore;
import gg.nerith.core.island.Island;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class WebApiServer extends NanoHTTPD {

    private final NerithCore plugin;
    private final Gson gson = new Gson();

    public WebApiServer(NerithCore plugin, int port) {
        super(port);
        this.plugin = plugin;
    }

    public void startServer() {
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
            plugin.getLogger().info("[NerithCore] Web API started on port " + getListeningPort());
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "[NerithCore] Failed to start Web API", e);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Map<String, String> params = session.getParms();

        try {
            if (uri.startsWith("/api/leaderboard")) {
                int limit = Integer.parseInt(params.getOrDefault("limit", "10"));
                limit = Math.min(limit, 100);
                return jsonResponse(plugin.getMetricsCollector().getLeaderboard(limit));
            }

            if (uri.startsWith("/api/island/")) {
                String uuidStr = uri.substring("/api/island/".length());
                UUID uuid = UUID.fromString(uuidStr);
                Optional<Island> islandOpt = plugin.getIslandManager().getIslandById(uuid);
                if (islandOpt.isEmpty()) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json",
                            "{\"error\":\"Island not found\"}");
                }
                return jsonResponse(plugin.getMetricsCollector().getIslandData(islandOpt.get()));
            }

            if (uri.equals("/api/stats/global")) {
                return jsonResponse(plugin.getMetricsCollector().getGlobalStats());
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json",
                    "{\"error\":\"Unknown endpoint\"}");

        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    "{\"error\":\"Internal server error\"}");
        }
    }

    private Response jsonResponse(Object data) {
        String json = gson.toJson(data);
        Response resp = newFixedLengthResponse(Response.Status.OK, "application/json", json);
        resp.addHeader("Access-Control-Allow-Origin", "*");
        return resp;
    }
}
