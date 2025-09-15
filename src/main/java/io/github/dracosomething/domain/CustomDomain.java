package io.github.dracosomething.domain;

import io.github.dracosomething.Pair;
import io.github.dracosomething.Util;

import java.io.*;
import java.net.URI;
import java.util.*;

public class CustomDomain {
    public static final ArrayList<CustomDomain> DOMAINS = new ArrayList<>();
    public static final CustomDomain EMPTY = new CustomDomain();
    private static final File HOSTS =  new File(URI.create("file:/Windows/System32/drivers/etc/hosts"));
    private static final File CONFIG = new File(URI.create("file:/xampp/apache/conf/extra/httpd-vhosts.conf"));

    private String serverAdmin;
    private String name;
    private File target;
    private CustomDomainData domainData = null;

    private CustomDomain() {}

    public CustomDomain(String url, String serverAlias, File path) {
        this.name = url;
        this.serverAdmin = serverAlias;
        this.target = path;
        this.registerDomain();
    }

    public CustomDomain(String url, String serverAlias, File path, CustomDomainData domainData) {
        this.name = url;
        this.serverAdmin = serverAlias;
        this.target = path;
        this.domainData = domainData;
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
            writer.append("\tServerAdmin ").append(this.serverAdmin).append("\n");
            writer.append("\tDocumentRoot \"").append(String.valueOf(this.target)).append("\"\n");
            if (domainData != null) {
                if (domainData.serverAlias() != null) {
                    for (String alias : domainData.serverAlias()) {
                        writer.append("\tServerAlias ").append(alias).append("\n");
                    }
                }
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

    private static boolean shouldSkip(String data) {
        if (data.startsWith("#"))
            return true;
        if (data.startsWith("<") && data.endsWith(">"))
            return true;
        if (data.isBlank() || data.isEmpty())
            return true;
        if (data.startsWith(" ") ) {
            for (int i = 0; i < data.length(); i++) {
                if (data.charAt(i) == ' ')
                    continue;
                if (data.charAt(i) == '#')
                    return true;
                else
                    break;
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
            case "ServerAdmin" -> retVal = Objects.equals(value, this.serverAdmin);
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

    private static void readDomainXML() {
        if (!CONFIG.exists()) return;
        try {
            Scanner reader = new Scanner(CONFIG);
            CustomDomain[] arr = new CustomDomain[1];
            int index = 0;
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                if ((data.startsWith("<") && !data.startsWith("</")) && data.endsWith(">")) {
                    arr[index] = new CustomDomain();
                }
                if (shouldSkip(data))
                    continue;
                CustomDomain domain = getConfData(reader, data, arr[index]);
                arr[index] = domain;
                index++;
                arr = Arrays.copyOf(arr, index+1);
            }

            for (CustomDomain domain : arr) {
                if (domain == null || domain.isEmpty())
                    continue;
                DOMAINS.add(domain);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CustomDomain getConfData(Scanner reader, String currentLine, CustomDomain fillIn) {
        ArrayList<String> serverAlias = new ArrayList<>();
        String name = null;
        File target = null;
        String serverAdmin = null;
        File errorLog = null;
        File customLog = null;
        Pair<String, String> value = getValue(currentLine);
        switch (value.getKey()) {
            case "ServerName" -> name = value.getValue();
            case "ServerAlias" -> serverAlias.add(value.getValue());
            case "ServerAdmin" -> serverAdmin = value.getValue();
            case "DocumentRoot" -> {
                if (!Util.isProperPath(value.getValue()))
                    throw new RuntimeException("string is not a proper file path");
                target = new File(value.getValue());
            }
            case "ErrorLog" -> {
                if (!Util.isProperPath(value.getValue()))
                    throw new RuntimeException("string is not a proper file path");
                errorLog = new File(value.getValue());
            }
            case "CustomLog" -> {
                if (!Util.isProperPath(value.getValue()))
                    throw new RuntimeException("string is not a proper file path");
                customLog = new File(value.getValue());
            }
        }

        while(reader.hasNextLine()) {
            String data = reader.nextLine();
            if (data.startsWith("</") && data.endsWith(">"))
                break;
            value = getValue(data);
            switch (value.getKey()) {
                case "ServerName" -> name = value.getValue();
                case "ServerAlias" -> serverAlias.add(value.getValue());
                case "ServerAdmin" -> serverAdmin = value.getValue();
                case "DocumentRoot" -> {
                    if (!Util.isProperPath(value.getValue()))
                        throw new RuntimeException("string is not a proper file path");
                    target = new File(value.getValue());
                }
                case "ErrorLog" -> {
                    if (!Util.isProperPath(value.getValue()))
                        throw new RuntimeException("string is not a proper file path");
                    errorLog = new File(value.getValue());
                }
                case "CustomLog" -> {
                    if (!Util.isProperPath(value.getValue()))
                        throw new RuntimeException("string is not a proper file path");
                    customLog = new File(value.getValue());
                }
            }
        }
        if (name == null || serverAdmin == null || target == null)
            throw new RuntimeException("One of required field isn't null.");

        fillIn.name = name;
        fillIn.serverAdmin = serverAdmin;
        fillIn.target = target;

        if (!serverAlias.isEmpty() || errorLog != null || customLog != null) {
            fillIn.domainData = new CustomDomainData(serverAlias, errorLog, customLog);
        }

        return fillIn;
    }

    private static Pair<String, String> getValue(String data) {
        Pair<String, String> retVal = new Pair<>("", "");
        String[] pair = data.split(" ", 2);
        if (pair.length <= 1)
            return retVal;
        String key = pair[0].replace("\t", "");
        retVal = new Pair<>(key, "");
        String value = pair[1];
        switch (key) {
            case "ServerName", "ServerAlias", "ServerAdmin" -> retVal.setValue(value);
            case "DocumentRoot", "ErrorLog", "CustomLog" -> {
                value = value.replace("\"", "");
                retVal.setValue(value.replace("\\", "/"));
            }
        }

        return retVal;
    }

    public boolean isEmpty() {
        return this.name == null && this.target == null && this.serverAdmin == null;
    }

    @Override
    public String toString() {
        return this.name;
    }

    static {
        new CustomDomain("test.domain.dummy", "webmaster@test.domain.dummy", new File("C:\\Users\\alias\\Documents\\school\\Schooljaar2024-2025\\Module 2\\opdracht 5"));
        new CustomDomain("example.d.cedd", "webmaster@example.d.cedd", new File("C:\\Users\\alias\\Documents\\school\\Schooljaar2024-2025\\Module 2\\opdracht 5"));
        readDomainXML();
    }
}
