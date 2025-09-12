package io.github.dracosomething.domain;

import java.io.File;

public record CustomDomainData(
        String serverAdmin,
        File errorLog,
        File customLog
) {
}
