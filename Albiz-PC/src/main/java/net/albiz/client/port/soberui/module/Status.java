// Adapted from SoberClient, Violet, GPL-3.0-only. See THIRD_PARTY_PORTS.md.
package net.albiz.client.port.soberui.module;

import java.util.List;

public interface Status {
    public String statusTitle();

    public List<Row> statusRows();

    public String statusDescription();

    public record Row(String title, String detail, String badge, int badgeColor, String link) {
    }
}

