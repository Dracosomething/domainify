package io.github.dracosomething.domain;

import java.io.File;
import java.util.ArrayList;

public record CustomDomainData(
        ArrayList<String> serverAlias,
        File errorLog,
        File customLog
) {
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        builder.append("server alias': ").append(serverAlias.toString()).append(", ");
        builder.append("error log: ").append(errorLog.getPath()).append(", ");
        builder.append("custom log: ").append(customLog.getPath());
        builder.append("}");
        return builder.toString();
    }
}
