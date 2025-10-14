package io.github.dracosomething.domain;

import io.github.dracosomething.util.FileUtils;
import io.github.dracosomething.util.Util;
import io.github.dracosomething.util.comparator.ReverseFileOrder;
import io.github.dracosomething.util.Pair;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

public class CustomDomain {
    public static final ArrayList<CustomDomain> DOMAINS = new ArrayList<>();
    private static final File HOSTS;
    private static final File CONFIG = new File(FileUtils.PROJECT, "/apache/conf/extra/httpd-vhosts.conf");

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
        registerConfig(CONFIG);
    }

    private void registerConfig(final File const_) {
        if (!const_.exists()) return;
        try {
            Scanner reader = new Scanner(const_);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                if (shouldSkip(data))
                    continue;
                if (containsDomainConfig(data))
                    return;
            }

            FileWriter writer = new FileWriter(const_, true);
            writer.append(System.lineSeparator()).append("<VirtualHost 127.0.0.1:80>").append(System.lineSeparator());
            writer.append("\tServerName ").append(this.name).append(System.lineSeparator());
            writer.append("\tServerAdmin ").append(this.serverAdmin).append(System.lineSeparator());
            writer.append("\tDocumentRoot \"").append(String.valueOf(this.target)).append("\"").append(System.lineSeparator());
            if (domainData != null) {
                if (domainData.serverAlias() != null) {
                    for (String alias : domainData.serverAlias()) {
                        writer.append("\tServerAlias ").append(alias).append(System.lineSeparator());
                    }
                }
                if (domainData.errorLog() != null)
                    writer.append("\tErrorLog \"").append(String.valueOf(domainData.errorLog())).append("\"").append(System.lineSeparator());
                if (domainData.customLog() != null)
                    writer.append("\tCustomLog \"").append(String.valueOf(domainData.customLog())).append("\"").append(System.lineSeparator());
            }
            writer.append("</VirtualHost>");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean shouldSkip(String data) {
        return shouldSkip(data, true);
    }

    private static boolean shouldSkip(String data, boolean header) {
        if (data.startsWith("#"))
            return true;
        if ((data.startsWith("<") && data.endsWith(">")) && header)
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

    public static void readDomainXML(final File const_) {
        if (!const_.exists()) return;
        try {
            Scanner reader = new Scanner(const_);
            CustomDomain[] arr = new CustomDomain[1];
            int index = 0;
            while (reader.hasNextLine()) {
                String data = reader.nextLine().trim();
                if ((data.startsWith("<") && !data.startsWith("</")) && data.endsWith(">")) {
                    arr[index] = new CustomDomain();
                }
                if (shouldSkip(data))
                    continue;
                if (arr[index] == null)
                    continue;
                CustomDomain domain = getConfData(reader, data, arr[index]);
                arr[index] = domain;
                index++;
                arr = Arrays.copyOf(arr, index+1);
            }

            DOMAINS.clear();
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
        DummyCustomDomain domain = new DummyCustomDomain();
        writeDataToDummyDomain(domain, currentLine);

        while(reader.hasNextLine()) {
            String data = reader.nextLine().trim();
            if (data.startsWith("</") && data.endsWith(">"))
                break;
            writeDataToDummyDomain(domain, data);
        }
        if (domain.getName() == null || domain.getServerAdmin() == null || domain.getTarget() == null)
            throw new RuntimeException("One of required fields is null. fields: " + domain.getName() + ", " +
                    domain.getServerAdmin() + ", " + domain.getTarget());

        fillIn.name = domain.getName();
        fillIn.serverAdmin = domain.getServerAdmin();
        fillIn.target = domain.getTarget();

        if (!domain.getServerAlias().isEmpty() || domain.getErrorLog() != null || domain.getCustomLog() != null) {
            fillIn.domainData = new CustomDomainData(domain.getServerAlias(), domain.getErrorLog(), domain.getCustomLog());
        }

        return fillIn;
    }

    private static void writeDataToDummyDomain(DummyCustomDomain dummy, String line) {
        Pair<String, String> value = getValue(line);
        switch (value.getKey()) {
            case "ServerName" -> dummy.setName(value.getValue());
            case "ServerAlias" -> dummy.addServerAlias(value.getValue());
            case "ServerAdmin" -> dummy.setServerAdmin(value.getValue());
            case "DocumentRoot" -> {
                if (!FileUtils.isProperPath(value.getValue()))
                    throw new RuntimeException("string is not a proper file path");
                String val = value.getValue();
                if (val.startsWith("${SRVROOT}")) {
                    val = val.replace("${SRVROOT}", FileUtils.SRVROOT);
                }
                dummy.setTarget(new File(val));
            }
            case "ErrorLog" -> {
                if (!FileUtils.isProperPath(value.getValue()))
                    throw new RuntimeException("string is not a proper file path");
                String val = value.getValue();
                if (val.startsWith("${SRVROOT}")) {
                    val = val.replace("${SRVROOT}", FileUtils.SRVROOT);
                }
                dummy.setErrorLog(new File(val));
            }
            case "CustomLog" -> {
                if (!FileUtils.isProperPath(value.getValue()))
                    throw new RuntimeException("string is not a proper file path");
                String val = value.getValue();
                if (val.startsWith("${SRVROOT}")) {
                    val = val.replace("${SRVROOT}", FileUtils.SRVROOT);
                }
                dummy.setCustomLog(new File(val));
            }
        }
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

    public CustomDomainData getDomainData() {
        return domainData;
    }

    public String getName() {
        return name;
    }

    public File getTarget() {
        return target;
    }

    public String getServerAdmin() {
        return serverAdmin;
    }

    public String[] toArray() {
        int i = 0;
        String[] arr = new String[6];
        arr[i++] = this.name;
        arr[i++] = this.serverAdmin;
        arr[i++] = this.target.getPath();
        if (this.domainData == null)
            return arr;
        if (this.domainData.serverAlias() != null) {
            StringBuilder builder = new StringBuilder();
            Iterator<String> itr = this.domainData.serverAlias().iterator();
            while(itr.hasNext()) {
                String str = itr.next();
                builder.append(str);
                if (itr.hasNext()) {
                    builder.append(", ");
                }
            }
            arr[i++] = builder.toString();
        }

        if (this.domainData.customLog() != null)
            arr[i++] = this.domainData.customLog().getPath();
        if (this.domainData.errorLog() != null)
            arr[i++] = this.domainData.errorLog().getPath();
        return arr;
    }

    public void updateDomain(DummyCustomDomain updated) {
        if (!HOSTS.exists()) return;
        try {
            Scanner scanner = new Scanner(HOSTS);
            StringBuilder fileData = new StringBuilder();
            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();
                if (data.equals("127.0.0.1 " + this.name)) {
                    fileData.append("127.0.0.1 ").append(updated.getName()).append(System.lineSeparator());
                    continue;
                }
                fileData.append(data).append(System.lineSeparator());
            }

            FileWriter writer = new FileWriter(HOSTS);
            writer.append(fileData);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateConfig(CONFIG, updated);
    }

    private void updateConfig(final File const_, DummyCustomDomain updated) {
        if (!const_.exists()) return;
        try {
            Scanner scanner = new Scanner(const_);
            StringBuilder fileData = new StringBuilder();
            int i = 0;
            int iCache = -1;
            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();
                i++;
                if (iCache >= i)
                    continue;
                if ((data.startsWith("<") && !data.startsWith("</")) && data.endsWith(">")) {
                    Pair<String, Integer> pair = gatherData(i, const_);
                    String gathered = pair.getKey();
                    if (stringEquals(gathered)) {
                        fileData.append(modifyXMLFormat(data, gathered, updated));
                        iCache = i + pair.getValue();
                        continue;
                    }
                }
                fileData.append(data).append(System.lineSeparator());
            }

            FileWriter writer = new FileWriter(const_);
            writer.append(fileData);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Pair<String, Integer> gatherData(int index, final File const_) {
        StringBuilder builder = new StringBuilder();
        int newLines = 1;
        try {
            Scanner scanner = new Scanner(const_);
            for (int i = 0; i < index; i++) {
                scanner.nextLine();
            }
            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();
                if (data.startsWith("ServerAlias"))
                    continue;
                if (data.startsWith("</") && data.endsWith(">"))
                    break;
                builder.append(data).append(System.lineSeparator());
                newLines++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Pair<>(builder.toString(), newLines);
    }

    public void removeDomain() {
        if (!HOSTS.exists()) return;
        try {
            Scanner scanner = new Scanner(HOSTS);
            StringBuilder fileData = new StringBuilder();
            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();
                if (data.equals("127.0.0.1 " + this.name)) {
                    continue;
                }
                fileData.append(data).append(System.lineSeparator());
            }

            FileWriter writer = new FileWriter(HOSTS);
            writer.append(fileData);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        removeConfig(CONFIG);
    }

    private void removeConfig(final File const_) {
        if (!const_.exists()) return;
        try {
            Scanner scanner = new Scanner(const_);
            StringBuilder fileData = new StringBuilder();
            int i = 0;
            int iCache = -1;
            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();
                i++;
                if (iCache >= i)
                    continue;
                if ((data.startsWith("<") && !data.startsWith("</")) && data.endsWith(">")) {
                    Pair<String, Integer> pair = gatherData(i, const_);
                    String gathered = pair.getKey();
                    if (stringEquals(gathered)) {
                        iCache = i + pair.getValue();
                        continue;
                    }
                }
                fileData.append(data).append(System.lineSeparator());
            }

            FileWriter writer = new FileWriter(const_);
            writer.append(fileData);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String modifyXMLFormat(String header, String str, DummyCustomDomain updated) {
        Stream<String> stringStream = Arrays.stream(str.split(System.lineSeparator()));
        Iterator<String> itr = stringStream.iterator();
        StringBuilder retVal = new StringBuilder(header).append(System.lineSeparator());
        itr.forEachRemaining(data -> {
            Pair<String, String> pair = getValue(data);
            retVal.append("\t").append(pair.getKey()).append(" ");
            switch (pair.getKey()) {
                case "ServerName" -> retVal.append(updated.getName());
                case "ServerAdmin" -> retVal.append(updated.getServerAdmin());
                case "DocumentRoot" -> {
                    retVal.append("\"").append(updated.getTarget().getPath()).append("\"");
                }
                case "ErrorLog" -> {
                    retVal.append("\"").append(updated.getErrorLog().getPath()).append("\"");
                }
                case "CustomLog" -> {
                    retVal.append("\"").append(updated.getCustomLog().getPath()).append("\"");
                }
            }
            retVal.append(System.lineSeparator());
        });
        if (updated.getServerAlias() != null) {
            for (String alias : updated.getServerAlias()) {
                retVal.append("\t").append("ServerAlias ").append(alias).append(System.lineSeparator());
            }
        }
        retVal.append("</VirtualHost>").append(System.lineSeparator());
        return retVal.toString();
    }

    private boolean stringEquals(String str) {
        Stream<String> stream = Arrays.stream(str.split(System.lineSeparator()));
        boolean retVal = false;
        for (String string : stream.toList()) {
            if (string.endsWith("ServerName " + this.name)) {
                retVal = true;
                break;
            }
        }
        return retVal;
    }

    @Override
    public String toString() {
        return "{" + "name: " + this.name + ", " +
                "target: " + this.target.getPath() + ", " +
                "server admin: " + this.serverAdmin + ", " +
                "domain data: " + this.domainData +
                "}";
    }

    public static File getConfig() {
        return CONFIG;
    }

    static {
        if (Util.IS_WINDOWS) {
            HOSTS =  new File(FileUtils.ROOT, "Windows/System32/drivers/etc/hosts");
        } else {
            HOSTS = new File(FileUtils.ROOT, "etc/hosts");
        }
    }
}
