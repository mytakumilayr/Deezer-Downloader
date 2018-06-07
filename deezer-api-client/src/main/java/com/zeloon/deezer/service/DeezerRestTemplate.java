/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zeloon.deezer.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeloon.deezer.io.CachedResourceConnection;
import com.zeloon.deezer.io.ResourceConnection;

import java.io.IOException;

public class DeezerRestTemplate {

    private static final String DEEZER_URI = "http://api.deezer.com/";

    private ResourceConnection resourceConnection;

    private boolean retryOnQuotaLimitReached = false;

    public DeezerRestTemplate(ResourceConnection resourceConnection) {
        this.resourceConnection = resourceConnection;
    }

    public <T> T get(final String prefix, Class<T> targetClass) {
        return getData(prefix, targetClass, false);
    }

    public  <T> T get(final String prefix, final Long id, Class<T> targetClass) {
        final String requestedUri = prefix + "/" + id;
        return getData(requestedUri, targetClass, false);
    }

    public <T> T get(final String prefix, final Long id, final String postfix, Class<T> targetClass) {
        final String requestedUri = prefix + "/" + id + "/" + postfix;
        return getData(requestedUri, targetClass, false);
    }

    private <T> T getData(final String uri, Class<T> targetClass, Boolean ignoreCache) {
        final String requestedUrl = uri.startsWith(DEEZER_URI) ? uri : DEEZER_URI + uri;
        final String response;
        try {
            if (resourceConnection instanceof CachedResourceConnection) {
                response = ((CachedResourceConnection) resourceConnection).getData(requestedUrl, ignoreCache);
            } else {
                response = resourceConnection.getData(requestedUrl);resourceConnection.getData(requestedUrl);
            }
            if (containsError(response)) {
                if (response.contains("Quota limit exceeded")) {
                    if (!retryOnQuotaLimitReached) {
                        throw new QuotaLimitExceededException();
                    }
                    try {
                        System.out.println("Quota limit reached, waiting 500ms");
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {}
                    return getData(uri, targetClass, true);
                } else {
                    throw new RestClientException("Error " + response);
                }
            } else {
                return convertJson(response, targetClass);
            }
        } catch (IOException ex) {
            throw new RestClientException("There is an exception for url " + requestedUrl, ex);
        }
    }

    private <T> T convertJson(final String content, Class<T> targetClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.ANY);
        //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(content, targetClass);
    }

    private Boolean containsError(final String response) {
        return response.startsWith("{\"error");
    }

    public boolean isRetryOnQuotaLimitReached() {
        return retryOnQuotaLimitReached;
    }

    public void setRetryOnQuotaLimitReached(boolean retryOnQuotaLimitReached) {
        this.retryOnQuotaLimitReached = retryOnQuotaLimitReached;
    }
}
