package com.tencent.qcloud.core.auth;

import com.tencent.qcloud.core.common.QCloudClientException;
import com.tencent.qcloud.core.http.HttpRequest;
import com.tencent.qcloud.core.util.QCloudHttpUtils;
import com.tencent.qcloud.core.util.QCloudStringUtils;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import static com.tencent.qcloud.core.http.HttpConstants.Header.CONTENT_LENGTH;
import static com.tencent.qcloud.core.http.HttpConstants.Header.CONTENT_TYPE;
import static com.tencent.qcloud.core.http.HttpConstants.Header.DATE;
import static com.tencent.qcloud.core.http.HttpConstants.Header.TRANSFER_ENCODING;


/**
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class COSXmlSignSourceProvider implements QCloudSignSourceProvider {

    private Set<String> paras;

    /**
     * 真正用来签名的参数
     */
    private Set<String> realSignParas;

    /**
     * 真正用来签名的头部
     */
    private Set<String> realSignHeader;

    private Set<String> headers;

    private long duration;
    private long beginTime;
    private long expiredTime;

    private String signTime;

    public COSXmlSignSourceProvider() {
        headers = new HashSet<String>();
        paras = new HashSet<String>();
        realSignHeader = new HashSet<String>();
        realSignParas = new HashSet<String>();
    }

    /**
     * 设置请求签名开始时间
     *
     * @param beginTime 单位是秒
     * @return COSXmlSignSourceProvider
     */
    public COSXmlSignSourceProvider setSignBeginTime(long beginTime) {
        this.beginTime = Utils.handleTimeAccuracy(beginTime);
        return this;
    }

    /**
     * 设置请求签名过期时间
     *
     * @param expiredTime 单位是秒
     * @return COSXmlSignSourceProvider
     */
    public COSXmlSignSourceProvider setSignExpiredTime(long expiredTime) {
        this.expiredTime = Utils.handleTimeAccuracy(expiredTime);
        return this;
    }

    /**
     * 设置签名有效时长
     * @param duration 单位是秒
     * @return COSXmlSignSourceProvider
     */
    public COSXmlSignSourceProvider setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    /**
     * 需要加密的parameter
     *
     * @param key
     */
    public void parameter(String key) {
        paras.add(key);
    }

    public void parameters(Set<String> keys) {
        if(keys != null){
            paras.addAll(keys);
        }
    }

    /**
     * 需要加密的header
     *
     * @param key
     */
    public void header(String key) {

        headers.add(key);
    }

    public void headers(Set<String> keys) {
        if(keys != null){
            headers.addAll(keys);
        }
    }

    /**
     * 签名需要的参数
     * <P>
     * 1、q-sign-algorithm : 固定值 sha1
     *
     * 2、q-ak ：
     *</P>
     * @return T
     */
    @Override
    public <T> String source(HttpRequest<T> request) throws QCloudClientException {
        if (request == null) {
            return null;
        }

        if (headers.size() > 0) {

            Set<String> lowerCaseHeaders = toLowerCase(headers);

            // 1、是否存在Content-Type
            if (lowerCaseHeaders != null && lowerCaseHeaders.contains(CONTENT_TYPE.toLowerCase())) {

                String contentType = request.contentType();
                if (contentType != null) {
                    request.addHeader(CONTENT_TYPE, contentType);
                }
            }

            // 2、是否存在Content-Length
            if (lowerCaseHeaders != null && lowerCaseHeaders.contains(CONTENT_LENGTH.toLowerCase())) {

                long contentLength;
                try {
                    contentLength = request.contentLength();
                } catch (IOException e) {
                    throw new QCloudClientException("read content length fails", e);
                }
                if (contentLength != -1) {
                    request.addHeader(CONTENT_LENGTH, Long.toString(contentLength));
                    request.removeHeader(TRANSFER_ENCODING);
                } else {
                    request.addHeader(TRANSFER_ENCODING, "chunked");
                    request.removeHeader(CONTENT_LENGTH);
                }

            }

            // 3、是否存在Date
            if (lowerCaseHeaders != null && lowerCaseHeaders.contains(DATE.toLowerCase())) {

                Date d = new Date();
                DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                format.setTimeZone(TimeZone.getTimeZone("GMT"));
                request.addHeader(DATE, format.format(d));
            }

        }


        // 添加method
        StringBuilder formatString = new StringBuilder(request.method().toLowerCase());
        formatString.append("\n");


        // 添加path
        String path = QCloudHttpUtils.urlDecodeString(request.url().getPath());
        formatString.append(path);
        formatString.append("\n");

        // 添加parameters
        String paraString = queryStringForKeys(request.url(), paras, realSignParas);

        if (paraString == null) {
            paraString = "";
        }
        formatString.append(paraString);
        formatString.append("\n");

        // 添加header，得到最终的formatString
        String headerString = "";
        if (request.headers() != null) {
            headerString = headersStringForKeys(request.headers(), headers, realSignHeader);
        }
        formatString.append(headerString);
        formatString.append("\n");


        StringBuilder stringToSign = new StringBuilder();

        // 追加 q-sign-algorithm
        stringToSign.append(AuthConstants.SHA1);
        stringToSign.append("\n");

        // 追加q-sign-time
        if (beginTime == 0) {
            beginTime = System.currentTimeMillis() / 1000;
        }
        if (expiredTime == 0) {
            expiredTime = beginTime + duration;
        }
        signTime = beginTime + ";" + expiredTime;
        stringToSign.append(signTime);
        stringToSign.append("\n");

        // 追加 sha1Hash(formatString)
        String formatStringSha1 = Utils.encodeHexString(Utils.sha1(formatString.toString()));
        stringToSign.append(formatStringSha1);
        stringToSign.append("\n");

        return stringToSign.toString();
    }

    String getRealHeaderList() {
        return sortAndJoinSemicolon(realSignHeader);
    }

    String getRealParameterList() {
        return sortAndJoinSemicolon(realSignParas);
    }

    String getSignTime() {
        return signTime;
    }

    private String sortAndJoinSemicolon(Set<String> values) {
        if (values == null) {
            return "";
        }

        // 这里也需要先按字典顺序进行排序
        Set<String> set = new TreeSet<>(values);

        StringBuilder str  = new StringBuilder();
        for (String value : set) {
            if (!QCloudStringUtils.isEmpty(str.toString())) {
                str.append(";");
            }
            str.append(value);
        }

        return str.toString();
    }

    /**
     * 将set中所有的值转化为小写
     *
     * @param set 原始集合
     * @return 小写的集合
     */
    private Set<String> toLowerCase(Set<String> set) {
        if (set != null && set.size() > 0) {
            Set<String> lowerSet = new HashSet<>();
            for (String key : set) {
                if (key != null) {
                    lowerSet.add(key.toLowerCase());
                }
            }
            return lowerSet;
        }

        return null;
    }

    private String queryStringForKeys(URL httpUrl, Set<String> keys, Set<String> realKeys) {
        StringBuilder out = new StringBuilder();
        boolean isFirst = true;

        // 1、将所有的key值转化为小写，并进行排序
        List<String> orderKeys = new LinkedList<>();
        for (String key : keys) {
            orderKeys.add(key.toLowerCase());
        }
        Collections.sort(orderKeys, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        // 2、获得query所有的name，并进行小写映射
        Map<String, List<String>> queryNameValues = QCloudHttpUtils.getDecodedQueryPair(httpUrl);
        Set<String> queryNames = queryNameValues.keySet();
        if (queryNames == null) {
            return "";
        }
        Map<String, String> maps = new HashMap<>();
        for (String name : queryNames) {
            maps.put(name.toLowerCase(), name);
        }

        // 3、取出需要的参数
        for (String key : orderKeys) {
            List<String> values = queryNameValues.get(maps.get(key));
            if (values != null) {
                for (String value : values) {
                    if (!isFirst) {
                        out.append('&');
                    }
                    isFirst = false;
                    realKeys.add(key.toLowerCase());
                    out.append(key.toLowerCase());
                    if (value != null) {
                        out.append('=');
                        out.append(value.toLowerCase());
                    }
                }
            }
        }
        return out.toString();
    }

    private String headersStringForKeys(Map<String, List<String>> headers, Set<String> keys, Set<String> realKeys) {
        StringBuilder out = new StringBuilder();
        boolean isFirst = true;

        // 1、将所有的key值转化为小写，并进行排序
        List<String> orderKeys = new LinkedList<>();
        for (String key : keys) {
            orderKeys.add(key.toLowerCase());
        }
        Collections.sort(orderKeys, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        // 2、获得headers所有的name，并进行小写映射
        Set<String> headerNames = headers.keySet();
        if (headerNames == null) {
            return "";
        }
        Map<String, String> maps = new HashMap<>();
        for (String name : headerNames) {
            maps.put(name.toLowerCase(), name);
        }

        // 3、取出需要的参数
        for (String key : orderKeys) {
            List<String> values = headers.get(maps.get(key));
            if (values != null) {
                for (String value : values) {
                    if (!isFirst) {
                        out.append('&');
                    }
                    isFirst = false;
                    out.append(key.toLowerCase());
                    realKeys.add(key);
                    if (value != null) {
                        out.append('=');
                        out.append(QCloudHttpUtils.urlEncodeString(value));
                    }
                }
            }
        }

        return out.toString();
    }
}
