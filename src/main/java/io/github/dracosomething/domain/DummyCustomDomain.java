package io.github.dracosomething.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class DummyCustomDomain{
    private String serverAdmin;
    private String name;
    private File target;
    private ArrayList<String> serverAlias = null;
    private File errorLog = null;
    private File customLog = null;

    public DummyCustomDomain(CustomDomain domain) {
        this.serverAdmin = domain.getServerAdmin();
        this.name = domain.getName();
        this.target = domain.getTarget();
        if (domain.getDomainData() != null) {
            if (domain.getDomainData().serverAlias() != null)
                this.serverAlias = domain.getDomainData().serverAlias();
            if (domain.getDomainData().errorLog() != null)
                this.errorLog = domain.getDomainData().errorLog();
            if (domain.getDomainData().customLog() != null)
                this.customLog = domain.getDomainData().customLog();
        }
    }

    public DummyCustomDomain(String serverAdmin, String name, File target) {
        this.serverAdmin = serverAdmin;
        this.name = name;
        this.target = target;
    }

    public DummyCustomDomain(String name, File target) {
        this.serverAdmin = "webmaster@" + name;
        this.name = name;
        this.target = target;
    }

    public DummyCustomDomain(String serverAdmin, String name, File target, CustomDomainData data) {
        this.serverAdmin = serverAdmin;
        this.name = name;
        this.target = target;
        if (data.serverAlias() != null)
            this.serverAlias = data.serverAlias();
        if (data.errorLog() != null)
            this.errorLog = data.errorLog();
        if (data.customLog() != null)
            this.customLog = data.customLog();
    }

    public String getServerAdmin() {
        return serverAdmin;
    }

    public File getTarget() {
        return target;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getServerAlias() {
        return serverAlias;
    }

    public File getCustomLog() {
        return customLog;
    }

    public File getErrorLog() {
        return errorLog;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCustomLog(File customLog) {
        this.customLog = customLog;
    }

    public void setErrorLog(File errorLog) {
        this.errorLog = errorLog;
    }

    public void setServerAdmin(String serverAdmin) {
        this.serverAdmin = serverAdmin;
    }

    public void setServerAlias(ArrayList<String> serverAlias) {
        this.serverAlias = serverAlias;
    }

    public void setTarget(File target) {
        this.target = target;
    }

    public String getServerAliasFormatted() {
        if (this.serverAlias == null) return "";
        StringBuilder builder = new StringBuilder();
        Iterator<String> itr = this.serverAlias.iterator();
        while (itr.hasNext()) {
            String alias = itr.next();
            builder.append(alias);
            if (itr.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}
