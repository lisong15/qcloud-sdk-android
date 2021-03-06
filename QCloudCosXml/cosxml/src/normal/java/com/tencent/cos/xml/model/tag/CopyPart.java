package com.tencent.cos.xml.model.tag;

/**
 * Created by bradyxiao on 2017/11/24.
 */

public class CopyPart {
    public String eTag;
    public String lastModified;

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder("{CopyPart:\n");
        stringBuilder.append("ETag:").append(eTag).append("\n");
        stringBuilder.append("LastModified:").append(lastModified).append("\n");
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
