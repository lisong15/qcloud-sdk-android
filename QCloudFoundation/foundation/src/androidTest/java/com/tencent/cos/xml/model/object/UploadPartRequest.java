package com.tencent.cos.xml.model.object;

import com.tencent.cos.xml.common.RequestMethod;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.utils.FileUtils;
import com.tencent.qcloud.core.http.RequestBodySerializer;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * <p>
 * 上传单个分块。
 * </p>
 *
 * <p>
 * 支持的块的数量为1到10000，块的大小为1 MB 到5 GB。
 * </p>
 * <p>
 * 当传入 uploadId 和 partNumber 都相同的时候，后传入的块将覆盖之前传入的块。当 uploadId 不存在时会返回 404 错误，NoSuchUpload.
 * </p>
 */
final public class UploadPartRequest extends ObjectRequest {
    private int partNumber;
    private String uploadId;
    private String srcPath;
    private byte[] data;
    private long fileLength;
    private long fileOffset = -1L;
    private long fileContentLength = -1L;

    private CosXmlProgressListener progressListener;

    private UploadPartRequest(String bucket, String cosPath){
        super(bucket, cosPath);
    }

    public UploadPartRequest(String bucket, String cosPath, int partNumber, String srcPath, String uploadId){
        this(bucket, cosPath);
        this.partNumber = partNumber;
        this.srcPath = srcPath;
        this.uploadId = uploadId;
        fileOffset = -1L;
        fileContentLength = -1L;
    }

    public UploadPartRequest(String bucket, String cosPath, int partNumber, String srcPath, long offset, long length,
                             String uploadId){
        this(bucket, cosPath);
        this.partNumber = partNumber;
        setSrcPath(srcPath, offset, length);
        this.uploadId = uploadId;
    }

    public UploadPartRequest(String bucket, String cosPath, int partNumber, byte[] data, String uploadId){
        this(bucket, cosPath);
        this.partNumber = partNumber;
        this.data = data;
        this.uploadId = uploadId;
        fileOffset = -1L;
        fileContentLength = -1L;
    }

    public UploadPartRequest(String bucket, String cosPath, int partNumber, InputStream inputStream, String uploadId) throws CosXmlClientException {
        this(bucket, cosPath);
        this.partNumber = partNumber;
        this.srcPath = FileUtils.tempCache(inputStream);
        this.uploadId = uploadId;
        fileOffset = -1L;
        fileContentLength = -1L;
    }

    /**
     * 设置上传的分块数
     *
     * @param partNumber 上传的分块数
     */
    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    /**
     * 获取用户设置的上传分块数
     *
     * @return 上传的分块数
     */
    public int getPartNumber() {
        return partNumber;
    }

    /**
     * 设置分块上传的UploadId号
     *
     * @param uploadId 分块上传的UploadId
     */
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    /**
     * 获取用户设置分块上传的UploadId号
     *
     * @return 分块上传的UploadId
     */
    public String getUploadId() {
        return uploadId;
    }

    @Override
    public String getMethod() {
        return RequestMethod.PUT ;
    }

    @Override
    public Map<String, String> getQueryString() {
        queryParameters.put("partNumber", String.valueOf(partNumber));
        queryParameters.put("uploadID", uploadId);
        return super.getQueryString();
    }

    @Override
    public RequestBodySerializer getRequestBody() throws CosXmlClientException {
        if(srcPath != null){
            if(fileOffset != -1){
                return RequestBodySerializer.file("text/plain", new File(srcPath), fileOffset, fileContentLength);
            }else {
               return RequestBodySerializer.file("text/plain", new File(srcPath));
            }
        }else if(data != null){
            return RequestBodySerializer.bytes("text/plain", data);
        }
        return null;
    }

    @Override
    public void checkParameters() throws CosXmlClientException {
        super.checkParameters();
        if(partNumber <= 0){
            throw new CosXmlClientException("partNumber must be >= 1");
        }
        if(uploadId == null){
            throw new CosXmlClientException("uploadID must not be null");
        }
        if(srcPath == null && data == null){
            throw new CosXmlClientException("Data Source must not be null");
        }
        if(srcPath != null){
            File file = new File(srcPath);
            if(!file.exists()){
                throw new CosXmlClientException("upload file does not exist");
            }
        }
    }

    /**
     * <p>
     * 设置上传的本地文件路径
     * </p>
     * <p>
     * 可以设置上传本地文件、字节数组或者输入流。每次只能上传一种类型，若同时设置，
     * 则优先级为 本地文件&gt;字节数组&gt;输入流
     * </p>
     *
     * @param srcPath 本地文件路径
     * @see UploadPartRequest#setData(byte[])
     */
    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    /**
     * <p>
     * 设置上传的本地文件路径和上传范围
     * </p>
     *
     * @see UploadPartRequest#setSrcPath(String)
     */
    public void setSrcPath(String srcPath, long fileOffset, long contentLength) {
        this.srcPath = srcPath;
        this.fileOffset = fileOffset;
        this.fileContentLength = contentLength;
    }

    /**
     * 获取设置的本地文件路径
     *
     * @return
     */
    public String getSrcPath() {
        return srcPath;
    }

    /**
     * <p>
     * 设置上传的字节数组
     * </p>
     * <p>
     * 可以设置上传本地文件、字节数组或者输入流。每次只能上传一种类型，若同时设置，
     * 则优先级为 本地文件&gt;字节数组&gt;输入流
     * </p>
     *
     * @param data 需要上传的字节数组
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * 获取用户设置的字节数组
     *
     * @return
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 获取用户设置的输入流读取的字节长度
     *
     * @return
     */
    public long getFileLength() {
        if(data != null){
            fileContentLength =  data.length;
        }else if(srcPath != null && fileContentLength == -1L){
            fileContentLength = new File(srcPath).length();
        }
        return fileContentLength;
    }

    /**
     * 获取用户设置的进度监听
     *
     */
    public void setProgressListener(CosXmlProgressListener progressListener){
        this.progressListener = progressListener;
    }

    public CosXmlProgressListener getProgressListener() {
        return progressListener;
    }
}