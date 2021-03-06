package com.tencent.cos.xml.model.object;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.tag.CopyObject;
import com.tencent.cos.xml.transfer.XmlSlimParser;
import com.tencent.qcloud.core.http.HttpResponse;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by bradyxiao on 2017/9/20.
 * author bradyxiao
 */
public class CopyObjectResult extends CosXmlResult {

    public CopyObject copyObject;

    @Override
    public void parseResponseBody(HttpResponse response) throws CosXmlServiceException, CosXmlClientException {
        super.parseResponseBody(response);
        copyObject = new CopyObject();
        try {
            XmlSlimParser.parseCopyObjectResult(response.byteStream(), copyObject);
        } catch (XmlPullParserException e) {
           throw new CosXmlClientException(e);
        } catch (IOException e) {
            throw new CosXmlClientException(e);
        }
    }

    @Override
    public String printResult() {
        return copyObject != null ? copyObject.toString() : super.printResult();
    }
}
