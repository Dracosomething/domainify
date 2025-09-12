package io.github.dracosomething.domain;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class CustomDomain {
    public static final ArrayList<CustomDomain> DOMAINS = new ArrayList<>();
    private static final URI test = URI.create("file:/Windows/System32/drivers/etc/hosts");
    private static final File HOSTS =  new File(test);
    private static final File CONFIG = new File(URI.create("file:/xampp/apache/conf/extra/httpd-vhosts.conf"));

    private String serverAlias;
    private String name;
    private File target;
    private CustomDomainData domainData = null;


    public CustomDomain(String url, String serverAlias, File path) {
        this.name = url;
        this.serverAlias = serverAlias;
        this.target = path;
        DOMAINS.add(this);
        this.registerDomain();
    }

    public CustomDomain(String url, String serverAlias, File path, CustomDomainData domainData) {
        this.name = url;
        this.serverAlias = serverAlias;
        this.target = path;
        this.domainData = domainData;
        DOMAINS.add(this);
        this.registerDomain();
    }

    private void registerDomain() {
        if (!HOSTS.exists()) return;
        if (!CONFIG.exists()) return;
        try {
            Scanner reader = new Scanner(HOSTS);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                if (shouldSkip(data))
                    continue;
                if (containsDomainHosts(data))
                    return;
            }
            FileWriter writer = new FileWriter(HOSTS, true);
            writer.append("127.0.0.1 ").append(name);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Scanner reader = new Scanner(CONFIG);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                if (shouldSkip(data))
                    continue;
                if (containsDomainConfig(data))
                    return;
            }

            FileWriter writer = new FileWriter(CONFIG, true);
            writer.append("\n").append("<VirtualHost 127.0.0.1:80>\n");
            writer.append("\tServerName ").append(this.name).append("\n");
            writer.append("\tServerAlias ").append(this.serverAlias).append("\n");
            writer.append("\tDocumentRoot \"").append(String.valueOf(this.target)).append("\"\n");
            if (domainData != null) {
                if (domainData.serverAdmin() != null)
                    writer.append("\tServerAdmin ").append(domainData.serverAdmin()).append("\n");
                if (domainData.errorLog() != null)
                    writer.append("\tErrorLog \"").append(String.valueOf(domainData.errorLog())).append("\"\n");
                if (domainData.customLog() != null)
                    writer.append("\tCustomLog \"").append(String.valueOf(domainData.customLog())).append("\"\n");
            }
            writer.append("</VirtualHost>");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean shouldSkip(String data) {
        if (data.startsWith("#"))
            return true;
        if (data.startsWith("<") && data.endsWith(">"))
            return true;
        if (data.startsWith(" ") ) {
            for (int i = 0; i < data.length(); i++) {
                if (data.charAt(i) == ' ')
                    continue;
                if (data.charAt(i) == '#')
                    return true;
            }
        }
        return false;
    }

    private boolean containsDomainConfig(String data) {
        String[] kvPair = data.split(" ", 2);
        if (kvPair.length == 1) return false;
        String key = kvPair[0];
        String value = kvPair[1];
        key = key.replace("\t", "");
        boolean retVal = false;
        switch (key) {
            case "ServerName" -> retVal = Objects.equals(value, this.name);
            case "ServerAlias" -> retVal = Objects.equals(value, this.serverAlias);
            case "DocumentRoot" -> retVal = Objects.equals(value, this.target.getPath());
        }
        return retVal;
    }

    private boolean containsDomainHosts(String data) {
        String[] kvPair = data.split(" ");
        if (kvPair.length == 1) return false;
        String key = kvPair[0];
        String value = kvPair[1];

        return Objects.equals(key, "127.0.0.1") && Objects.equals(value, this.name);
    }

    @Override
    public String toString() {
        return this.name;
    }

    static {
        new CustomDomain("test.domain.dummy", "domain.dummy", new File("C:\\Users\\alias\\Documents\\school\\Schooljaar2024-2025\\Module 2\\opdracht 5"));
        new CustomDomain("example.d.cedd", "d.cedd", new File("C:\\Users\\alias\\Documents\\school\\Schooljaar2024-2025\\Module 2\\opdracht 5"));
    }
}
