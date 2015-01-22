package net.somethingdreadful.MAL.api.response;

import java.io.Serializable;

public class OAuth implements Serializable {
    public String access_token;
    public String token_type;
    public String expires;
    public String expires_in;
    public String refresh_token;
}