package com.fitsoft.conn;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.fitsoft.entity.CommonFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OSSConnection {

    //创建的bucket名字
    private static final String bucketName = "bucket-joker";
    //设置URL过期时间为10年  3600L* 1000*24*365*10
    private static final Date expiration = new Date(new Date().getTime() + 3600L * 1000 * 24 * 365 * 10);

    //简单上传文件
    public static boolean upload(MultipartFile file) {
        OSS singleOss = Connection.SINGLE_OSS;
        try {
            InputStream is = file.getInputStream();
            String name = file.getOriginalFilename();
            singleOss.putObject(bucketName, name, is);
            // 上传回调。
        /*    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, name, new ByteArrayInputStream(file.getBytes()));
            Callback callback = new Callback();
            String callbackUrl = "";
            callback.setCallbackUrl(callbackUrl);
            //（可选）设置回调请求消息头中Host的值，即您的服务器配置Host的值。
            // callback.setCallbackHost("yourCallbackHost");
            // 设置发起回调时请求body的值。
            callback.setCallbackBody("{\\\"mimeType\\\":${mimeType},\\\"size\\\":${size}}");
            // 设置发起回调请求的Content-Type。
            callback.setCalbackBodyType(Callback.CalbackBodyType.JSON);
            // 设置发起回调请求的自定义参数，由Key和Value组成，Key必须以x:开始。
            callback.addCallbackVar("x:var1", "value1");
            callback.addCallbackVar("x:var2", "value2");
            putObjectRequest.setCallback(callback);*/
            is.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //断点续传上传文件  用不到
    public static boolean uploadFile(MultipartFile file) {
        String name = file.getOriginalFilename();
        OSS ossClient = Connection.SINGLE_OSS;
        ObjectMetadata meta = new ObjectMetadata();
        // 指定上传的内容类型。
        meta.setContentType("text/plain");
        // 通过UploadFileRequest设置多个参数。
        UploadFileRequest uploadFileRequest = new UploadFileRequest(bucketName, name);
        // 通过UploadFileRequest设置单个参数。
        // 设置存储空间名称。
        //uploadFileRequest.setBucketName("<yourBucketName>");
        // 设置文件名称。
        //uploadFileRequest.setKey("<yourObjectName>");
        // 指定上传的本地文件。
        uploadFileRequest.setUploadFile("<yourLocalFile>");
        // 指定上传并发线程数，默认为1。
        uploadFileRequest.setTaskNum(5);
        // 指定上传的分片大小，范围为100KB~5GB，默认为文件大小/10000。
        uploadFileRequest.setPartSize(1024 * 1024);
        // 开启断点续传，默认关闭。
        uploadFileRequest.setEnableCheckpoint(true);
        // 记录本地分片上传结果的文件。开启断点续传功能时需要设置此参数，上传过程中的进度信息会保存在该文件中，如果某一分片上传失败，再次上传时会根据文件中记录的点继续上传。上传完成后，该文件会被删除。默认与待上传的本地文件同目录，为uploadFile.ucp。
        uploadFileRequest.setCheckpointFile("1");
        //   uploadFileRequest.setCheckpointFile("<yourCheckpointFile>");
        // 文件的元数据。
        uploadFileRequest.setObjectMetadata(meta);
        // 设置上传成功回调，参数为Callback类型。
        //    uploadFileRequest.setCallback("<yourCallbackEvent>");
        uploadFileRequest.setCallback(new Callback());
        // 断点续传上传。
        try {
            ossClient.uploadFile(uploadFileRequest);
            return true;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
    }

    //分片上传文件
    public static boolean uploadMultiple(MultipartFile file) throws IOException {
        OSS ossClient = Connection.SINGLE_OSS;
        // 创建InitiateMultipartUploadRequest对象。
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest("<yourBucketName>", "<yourObjectName>");

        // 如果需要在初始化分片时设置文件存储类型，请参考以下示例代码。
        // ObjectMetadata metadata = new ObjectMetadata();
        // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
        // request.setObjectMetadata(metadata);

        // 初始化分片。
        InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
        // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个ID来发起相关的操作，如取消分片上传、查询分片上传等。
        String uploadId = upresult.getUploadId();

        // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
        List<PartETag> partETags =  new ArrayList<PartETag>();
        // 计算文件有多少个分片。
        final long partSize = 1024 * 1024L;   // 1MB
        final File sampleFile = new File("<localFile>");
        long fileLength = sampleFile.length();
        int partCount = (int) (fileLength / partSize);
        if (fileLength % partSize != 0) {
            partCount++;
        }
        // 遍历分片上传。
        for (int i = 0; i < partCount; i++) {
            long startPos = i * partSize;
            long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
            InputStream instream = new FileInputStream(sampleFile);
            // 跳过已经上传的分片。
            instream.skip(startPos);
            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(bucketName);
            uploadPartRequest.setKey(file.getOriginalFilename());
            uploadPartRequest.setUploadId(uploadId);
            uploadPartRequest.setInputStream(instream);
            // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100KB。
            uploadPartRequest.setPartSize(curPartSize);
            // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
            uploadPartRequest.setPartNumber( i + 1);
            // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
            UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
            // 每次上传分片之后，OSS的返回结果会包含一个PartETag。PartETag将被保存到partETags中。
            partETags.add(uploadPartResult.getPartETag());
        }

        // 创建CompleteMultipartUploadRequest对象。
        // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                new CompleteMultipartUploadRequest(bucketName, file.getOriginalFilename(), uploadId, partETags);
        // 如果需要在完成文件上传的同时设置文件访问权限，请参考以下示例代码。
        // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.PublicRead);
        // 完成上传。
        //   CompleteMultipartUploadResult completeMultipartUploadResult =
        ossClient.completeMultipartUpload(completeMultipartUploadRequest);
        return true;
    }

    //删除文件
    public static void delete(String name) {
        Connection.SINGLE_OSS.deleteObject(bucketName, name);
    }

    //查询所有文件
    public static List<CommonFile> queryAll() {
        ObjectListing objectListing = Connection.SINGLE_OSS.listObjects(bucketName);
        List<OSSObjectSummary> objectSummary = objectListing.getObjectSummaries();
        List<CommonFile> files = new ArrayList<>();
        for (OSSObjectSummary object : objectSummary) {
            final String name = object.getKey();
            files.add(new CommonFile(name,
                    Connection.SINGLE_OSS.generatePresignedUrl(bucketName, name, expiration).toString()));
        }
        return files;
    }

    /**
     * 创建单例连接
     */
    private static final class Connection {

        private static final OSS SINGLE_OSS = getSingleOss();

        private static OSS getSingleOss() {
            //外网访问
            final String endpoint1 = "oss-cn-beijing.aliyuncs.com";
            //ECS 的经典网络访问（内网）
            //    final String endpoint2 = "oss-cn-beijing-internal.aliyuncs.com";
            //ECS 的 VPC 网络访问（内网）
            //    final String endpoint3 = "oss-cn-beijing-internal.aliyuncs.com";

            // accessKeyId和accessKeySecret是OSS的访问密钥，您可以在控制台上创建和查看，
            // 创建和查看访问密钥的链接地址是：https://ak-console.aliyun.com/#/。
            // 注意：accessKeyId和accessKeySecret前后都没有空格，从控制台复制时请检查并去除多余的空格。

            //控制台可查看
            final String accessKeyId = "";
            final String accessKeySecret = "";
            return new OSSClientBuilder().build(endpoint1, accessKeyId, accessKeySecret);
        }
    }
}
