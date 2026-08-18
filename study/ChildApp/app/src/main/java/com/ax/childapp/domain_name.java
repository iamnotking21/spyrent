package com.ax.childapp;

import java.net.URI;
import java.net.URISyntaxException;

public class domain_name {
    public CharSequence getDomain(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.contains("www.") ? domain.substring(4) : domain;
    }

    public CharSequence getAuthority(String url) throws URISyntaxException{
        URI uri = new URI(url);
        String la = uri.getAuthority();
        return la;
    }
}
