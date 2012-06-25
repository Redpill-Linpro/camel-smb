package com.redpill_linpro.component.smb;

import java.net.URI;
import java.util.Map;

import jcifs.smb.SmbFile;

import org.apache.camel.CamelContext;
import org.apache.camel.component.file.GenericFileComponent;
import org.apache.camel.component.file.GenericFileEndpoint;

public class SmbComponent extends GenericFileComponent<SmbFile> {

    public SmbComponent() {

    }

    public SmbComponent(CamelContext context) {
        super(context);
    }

    @Override
    protected SmbEndpoint buildFileEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        if (log.isDebugEnabled())
            log.debug("buildFileEndpoint() uri[" + uri + "] remaining[" + remaining + "] parameters[" + parameters + "]");

        uri = fixSpaces(uri);
        SmbConfiguration config = new SmbConfiguration(new URI(uri));
        SmbEndpoint endpoint = new SmbEndpoint(uri, this, config);
        return endpoint;
    }

    @Override
    protected void afterPropertiesSet(GenericFileEndpoint<SmbFile> endpoint) throws Exception {
        if (log.isDebugEnabled())
            log.debug("afterPropertiesSet()");
    }

    private String fixSpaces(String input) {
        return input.replace(" ", "%20");
    }


}
