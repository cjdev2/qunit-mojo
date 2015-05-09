package com.cj.qunit.mojo.jetty;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.jetty.util.log.Logger;

public class JettyMavenLogger implements org.eclipse.jetty.util.log.Logger {
    
    private final String name;
    private final Log log;
    
    public JettyMavenLogger(String name, Log log) {
        super();
        this.name = name;
        this.log = log;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void warn(String msg, Object... args) {
        log.warn(msg);
    }

    @Override
    public void warn(Throwable thrown) {
        log.warn(thrown);
    }

    @Override
    public void warn(String msg, Throwable thrown) {
        if(looksLikeAJettyPortConflictMessage(msg)){
            log.debug(msg, thrown);
        }else{
            log.warn(msg, thrown);
        }
    }

    private boolean looksLikeAJettyPortConflictMessage(String msg) {
        return (msg.startsWith("FAILED SelectChannelConnector@") || msg.startsWith("FAILED org.eclipse.jetty.server.Server@")) &&  msg.endsWith("java.net.BindException: Address already in use");
    }

    @Override
    public void info(String msg, Object... args) {
        log.info(msg);
    }

    @Override
    public void info(Throwable thrown) {
        log.info(thrown);
    }

    @Override
    public void info(String msg, Throwable thrown) {
        log.info(msg, thrown);
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
    }

    @Override
    public void debug(String msg, Object... args) {
        log.debug(msg);
    }

    @Override
    public void debug(String msg, long stuff) {
        log.debug(msg);
    }

    @Override
    public void debug(Throwable thrown) {
        log.debug(thrown);
    }

    @Override
    public void debug(String msg, Throwable thrown) {
        log.debug(msg, thrown);
    }

    @Override
    public Logger getLogger(String name) {
        return new JettyMavenLogger(name, log);
    }

    @Override
    public void ignore(Throwable ignored) {
    }
    
}
