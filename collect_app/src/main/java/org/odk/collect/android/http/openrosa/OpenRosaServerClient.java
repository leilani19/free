package org.odk.collect.android.http.openrosa;

import java.io.IOException;
import java.util.Date;

import okhttp3.Request;
import okhttp3.Response;

public interface OpenRosaServerClient {

    Response makeRequest(Request request, Date currentTime) throws IOException;
}
