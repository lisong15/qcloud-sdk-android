package com.tencent.cos.xml.model.bucket;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.RequestXmlBodySerializer;
import com.tencent.cos.xml.model.ResponseXmlS3BodySerializer;
import com.tencent.qcloud.core.network.QCloudNetWorkConstants;
import com.tencent.qcloud.core.network.QCloudRequestPriority;

import java.util.Map;

/**
 * Created by bradyxiao on 2017/11/6.
 * <p>
 *     实现读取存储桶中用户跨区域复制配置信息。
 * </p>
 *
 * @see com.tencent.cos.xml.CosXml#getBucketReplication(GetBucketReplicationRequest)
 * @see com.tencent.cos.xml.CosXml#getBucketReplicationAsync(GetBucketReplicationRequest, CosXmlResultListener)
 */

public class GetBucketReplicationRequest extends CosXmlRequest {

    public GetBucketReplicationRequest(String bucket){
        setBucket(bucket);
        contentType = QCloudNetWorkConstants.ContentType.X_WWW_FORM_URLENCODED;
        requestHeaders.put(QCloudNetWorkConstants.HttpHeader.CONTENT_TYPE,contentType);
    }

    @Override
    protected void build() throws CosXmlClientException{
        super.build();

        priority = QCloudRequestPriority.Q_CLOUD_REQUEST_PRIORITY_NORMAL;

        setRequestMethod();
        requestOriginBuilder.method(requestMethod);

        setRequestPath();
        requestOriginBuilder.pathAddRear(requestPath);

        requestOriginBuilder.hostAddFront(bucket);

        setRequestQueryParams();
        if(requestQueryParams.size() > 0){
            for(Object object : requestQueryParams.entrySet()){
                Map.Entry<String,String> entry = (Map.Entry<String, String>) object;
                requestOriginBuilder.query(entry.getKey(),entry.getValue());
            }
        }

        if(requestHeaders.size() > 0){
            for(Object object : requestHeaders.entrySet()){
                Map.Entry<String,String> entry = (Map.Entry<String, String>) object;
                requestOriginBuilder.header(entry.getKey(),entry.getValue());
            }
        }

        responseBodySerializer = new ResponseXmlS3BodySerializer(GetBucketReplicationResult.class);
    }

    @Override
    protected void setRequestMethod() {
        requestMethod = QCloudNetWorkConstants.RequestMethod.GET;
    }

    @Override
    protected void setRequestPath() {
        requestPath = "/";
    }

    @Override
    protected void setRequestQueryParams() {
        requestQueryParams.put("replication", null);
    }

    @Override
    protected void checkParameters() throws CosXmlClientException {
        if(bucket == null){
            throw new CosXmlClientException("bucket must not be null");
        }
    }
}