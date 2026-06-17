package com.pulseops.config;

import java.util.List;

public class CuratedMonitorDefinition {

    public record CuratedMonitor(String name, String url) {}

    public static List<CuratedMonitor> all() {
        return List.of(
            new CuratedMonitor("GitHub API", "https://api.github.com"),
            new CuratedMonitor("OpenAI status", "https://status.openai.com/api/v2/status.json"),
            new CuratedMonitor("Discord status", "https://discordstatus.com/api/v2/status.json"),
            new CuratedMonitor("Cloudflare status", "https://www.cloudflarestatus.com/api/v2/status.json"),
            new CuratedMonitor("Cat Facts", "https://catfact.ninja/fact"),
            new CuratedMonitor("JSONPlaceholder", "https://jsonplaceholder.typicode.com/posts/1"),
            new CuratedMonitor("HTTPBin 500", "https://httpbin.org/status/500")
        );
    }
}
