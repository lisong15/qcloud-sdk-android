package com.tencent.qcloud.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * </p>
 * Created by wjielai on 2017/11/29.
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class QCloudHttpUtils {
    // 对非 '/' 字符进行URLEncoder编码
    public static String urlEncodeWithSlash(String fileId) {

        if (fileId != null && fileId.length() > 0 && !fileId.equals("/")) {
            String[] paras = fileId.split("/");
            for (int i = 0; i < paras.length; i++) {
                paras[i] = urlEncodeString(paras[i]);
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < paras.length; i++) {
                stringBuilder.append(paras[i]);
                stringBuilder.append("/");
            }
            if (!fileId.endsWith("/")) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            fileId = stringBuilder.toString();
        }

        return fileId;
    }

    public static Map<String, List<String>> getDecodedQueryPair(URL url) {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        if (url.getQuery() != null) {
            final String[] pairs = url.getQuery().split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? urlDecodeString(pair.substring(0, idx)) : pair;
                if (!query_pairs.containsKey(key)) {
                    query_pairs.put(key, new LinkedList<String>());
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? urlDecodeString(pair.substring(idx + 1)) : null;
                query_pairs.get(key).add(value);
            }
        }
        return query_pairs;
    }

    public static long[] parseContentRange(String contentRange) {
        if (QCloudStringUtils.isEmpty(contentRange)) {
            return null;
        }
        int lastBlankIndex = contentRange.lastIndexOf(" ");
        int acrossIndex = contentRange.indexOf("-");
        int slashIndex = contentRange.indexOf("/");
        if (lastBlankIndex == -1 || acrossIndex == -1 || slashIndex == -1) {
            return null;
        }

        long start = Long.parseLong(contentRange.substring(lastBlankIndex + 1, acrossIndex));
        long end = Long.parseLong(contentRange.substring(acrossIndex + 1, slashIndex));
        long max = Long.parseLong(contentRange.substring(slashIndex + 1));

        return new long[] {start, end, max};
    }

    public static String urlEncodeString(String source) {
        try {
            return URLEncoder.encode(source, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String urlDecodeString(String source) {
        try {
            return URLDecoder.decode(source, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String queryParametersString(Map<String, String> keyValues) {

        if (keyValues == null) {
            return null;
        }
        StringBuilder source = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : keyValues.entrySet()) {

            if (!first) {
                source.append("&");
            }
            source.append(entry.getKey()+"="+entry.getValue());
            first = false;
        }
        return source.toString();
    }
}
