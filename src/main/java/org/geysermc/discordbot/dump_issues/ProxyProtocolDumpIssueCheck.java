package org.geysermc.discordbot.dump_issues;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProxyProtocolDumpIssueCheck extends AbstractDumpIssueCheck {

    @NotNull
    @Override
    public List<String> checkIssues(JSONObject dump) {
        JSONObject configRemote = dump.getJSONObject("config").getJSONObject("remote");
        JSONObject configBedrock = dump.getJSONObject("config").getJSONObject("bedrock");

        List<String> warnings = new ArrayList<>();

        if (configBedrock.getBoolean("enable-proxy-protocol")) {
            warnings.add("- `enable-proxy-protocol` should ONLY be enabled if you run a reverse UDP proxy in front of Geyser.");
        }
        if (configRemote.getBoolean("use-proxy-protocol")) {
            warnings.add("- `use-proxy-protocol` should ONLY be enabled if your server supports PROXY protocol (it probably doesn't), or you have the same option enabled in your BungeeCord/Velocity config (it is off by default).");
        }

        return warnings;
    }
}
