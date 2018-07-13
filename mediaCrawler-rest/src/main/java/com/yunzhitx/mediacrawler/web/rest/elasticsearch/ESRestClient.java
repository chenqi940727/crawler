package com.yunzhitx.mediacrawler.web.rest.elasticsearch;


import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AUTH;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public class ESRestClient {
    private ESRestClient() {
    }

    public static RestHighLevelClient instance() {
        return ESClientHolder.instance;
    }


    static class ESClientHolder {
        private static RestHighLevelClient instance;

        static {
            Header[] defaultHeaders = {new BasicHeader(AUTH.WWW_AUTH_RESP,"Basic ZWxhc3RpYzpjaGFuZ2VtZQ==")};
            instance = new RestHighLevelClient(
                    RestClient.builder(
                        new HttpHost("192.168.124.129", 9200, "http")).setDefaultHeaders(defaultHeaders));
//                            new HttpHost("60.255.160.15", 9200, "http")).setDefaultHeaders(defaultHeaders));
//                            new HttpHost("60.255.164.45", 9200, "http")).setDefaultHeaders(defaultHeaders));
        }

        public static void close() throws IOException {
            if (instance != null) {
                instance.close();
            }
        }
    }


}
