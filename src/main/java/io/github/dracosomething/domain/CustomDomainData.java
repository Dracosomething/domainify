package io.github.dracosomething.domain;

import java.io.File;
import java.util.ArrayList;

public record CustomDomainData(
        ArrayList<String> serverAlias,
        File errorLog,
        File customLog
) {
}
