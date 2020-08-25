package com.alivc.base;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.vod.upload.impl.UploadAttachedMediaImpl;
import com.aliyun.vod.upload.impl.UploadImageImpl;
import com.aliyun.vod.upload.impl.UploadVideoImpl;
import com.aliyun.vod.upload.req.UploadAttachedMediaRequest;
import com.aliyun.vod.upload.req.UploadImageRequest;
import com.aliyun.vod.upload.req.UploadStreamRequest;
import com.aliyun.vod.upload.req.UploadVideoRequest;
import com.aliyun.vod.upload.resp.UploadAttachedMediaResponse;
import com.aliyun.vod.upload.resp.UploadImageResponse;
import com.aliyun.vod.upload.resp.UploadStreamResponse;
import com.aliyun.vod.upload.resp.UploadVideoResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.live.model.v20161101.DescribeLiveStreamsOnlineListRequest;
import com.aliyuncs.live.model.v20161101.DescribeLiveStreamsOnlineListResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.vod.model.v20170321.CreateAuditRequest;
import com.aliyuncs.vod.model.v20170321.CreateAuditResponse;
import com.aliyuncs.vod.model.v20170321.CreateUploadAttachedMediaRequest;
import com.aliyuncs.vod.model.v20170321.CreateUploadAttachedMediaResponse;
import com.aliyuncs.vod.model.v20170321.CreateUploadImageRequest;
import com.aliyuncs.vod.model.v20170321.CreateUploadImageResponse;
import com.aliyuncs.vod.model.v20170321.DeleteStreamRequest;
import com.aliyuncs.vod.model.v20170321.DeleteStreamResponse;
import com.aliyuncs.vod.model.v20170321.GetPlayInfoRequest;
import com.aliyuncs.vod.model.v20170321.GetPlayInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetTranscodeSummaryRequest;
import com.aliyuncs.vod.model.v20170321.GetTranscodeSummaryResponse;
import com.aliyuncs.vod.model.v20170321.GetTranscodeTaskRequest;
import com.aliyuncs.vod.model.v20170321.GetTranscodeTaskResponse;
import com.aliyuncs.vod.model.v20170321.GetTranscodeTemplateGroupRequest;
import com.aliyuncs.vod.model.v20170321.GetTranscodeTemplateGroupResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoRequest;
import com.aliyuncs.vod.model.v20170321.GetVideoInfoResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthRequest;
import com.aliyuncs.vod.model.v20170321.GetVideoPlayAuthResponse;
import com.aliyuncs.vod.model.v20170321.ListAppInfoRequest;
import com.aliyuncs.vod.model.v20170321.ListAppInfoResponse;
import com.aliyuncs.vod.model.v20170321.ListSnapshotsRequest;
import com.aliyuncs.vod.model.v20170321.ListSnapshotsResponse;
import com.aliyuncs.vod.model.v20170321.ListVodTemplateRequest;
import com.aliyuncs.vod.model.v20170321.ListVodTemplateResponse;
import com.aliyuncs.vod.model.v20170321.PreloadVodObjectCachesRequest;
import com.aliyuncs.vod.model.v20170321.PreloadVodObjectCachesResponse;
import com.aliyuncs.vod.model.v20170321.SubmitAIJobRequest;
import com.aliyuncs.vod.model.v20170321.SubmitAIJobResponse;
import com.aliyuncs.vod.model.v20170321.SubmitSnapshotJobRequest;
import com.aliyuncs.vod.model.v20170321.SubmitSnapshotJobResponse;
import com.aliyuncs.vod.model.v20170321.SubmitTranscodeJobsRequest;
import com.aliyuncs.vod.model.v20170321.SubmitTranscodeJobsResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: VodOpenAPI <br/>
 * Function: VOD 相关API调用类 <br/>
 * Date:     2019年1月10日  <br/>
 *
 * @author haihua.whh
 * @version v0.0.1
 */
@Slf4j
public class VodOpenAPI {

    /**
     * sts方式初始化
     *
     * @param accessKeyId accessKeySecret securityToken
     * @return DefaultAcsClient
     * @throws Exception
     */
    public static DefaultAcsClient initVodClient(String accessKeyId, String accessKeySecret, String securityToken) throws ClientException {
        DefaultProfile profile = DefaultProfile.getProfile(ConfigMapUtil.getValueByKey("VOD_REGIONID"), accessKeyId, accessKeySecret, securityToken);
        DefaultAcsClient client = new DefaultAcsClient(profile);
        return client;
    }

    public static DefaultAcsClient initVodClient() throws Exception {
        JSONObject akInfoJson = AkUtil.getAkInfo();
        DefaultProfile profile = DefaultProfile.getProfile(ConfigMapUtil.getValueByKey("VOD_REGIONID"), akInfoJson.getString("AccessKeyId"), akInfoJson.getString("AccessKeySecret"), akInfoJson.getString("SecurityToken"));
        DefaultAcsClient client = new DefaultAcsClient(profile);
        return client;
    }


    /*
    *
    DefaultAcsClient defaultAcsClient = VodOpenAPI.initVodClient(
            AkUtil.getAkInfo().getString("AccessKeyId"),
            AkUtil.getAkInfo().getString("AccessKeySecret"),
            AkUtil.getAkInfo().getString("SecurityToken"));
    response = defaultAcsClient.getAcsResponse(request);
            videosMap.put("total", response.getTotal());



     */

    /**
     * 构建审核内容
     *
     * @param videoId 视频ID
     *                status 视频审核状态，取值范围：Blocked(屏蔽)，Normal(正常)。
     *                reason 若审核状态为屏蔽时，需给出屏蔽的理由，最长支持128字节。
     *                comment 备注
     * @return 审核内容Json字符串：
     * [{"VideoId":"93ab850b4f6f44eab54b6e91d24d81d4","Status":"Normal"},
     * {"VideoId":"43ab850b4f6f44eab54b6e91d24d81d3","Status":"Blocked","Reason":"色情视频","Comment":"有露点镜"}]
     */
    private static String buildAuditContent(String videoId, String status, String reason, String comment)
            throws Exception {
        List<JSONObject> auditContents = new ArrayList<JSONObject>();
        JSONObject auditContent = new JSONObject();
        auditContent.put("VideoId", videoId);
        auditContent.put("Status", status);
        auditContent.put("Reason", reason);
        auditContent.put("Comment", comment);
        auditContents.add(auditContent);
        return auditContents.toString();
    }

    /**
     * 人工审核
     *
     * @param videoId 视频ID
     *                status 视频审核状态，取值范围：Blocked(屏蔽)，Normal(正常)。
     *                reason 若审核状态为屏蔽时，需给出屏蔽的理由，最长支持128字节。
     *                comment 备注
     * @return 审核结果：Json: { “RequestId”: “25818875-5F78-4A13-BEF6-D7393642CA58” }
     */
    public static CreateAuditResponse createAudit(String videoId, String status, String reason, String comment)
            throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        CreateAuditRequest request = new CreateAuditRequest();
        request.setAuditContent(buildAuditContent(videoId, status, reason, comment));
        return client.getAcsResponse(request);
    }

    /**
     * 提交转码作业
     *
     * @param videoId 视频ID
     *                templateGroupId  转码模板id
     * @return 转码结果：Json:  {
     * "RequestId": "25818875-5F78-4A13-BEF6-D7393642CA58",
     * "TranscodeJobs": [{
     * "JobId": "ad90a501b1b94ba6afb72374ad005046"
     * }
     * ...
     * ]
     * }
     */
    public static SubmitTranscodeJobsResponse submitTranscodeJobs(String videoId, String templateGroupId, String content) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        SubmitTranscodeJobsRequest request = new SubmitTranscodeJobsRequest();
        request.setVideoId(videoId);
        request.setTemplateGroupId(templateGroupId);
        //构建需要替换的水印参数(只有需要替换水印相关信息才需要构建)
        JSONObject overrideParams = buildOverrideParams(content);
        if (StringUtils.isNotEmpty(content)) {
            request.setOverrideParams(overrideParams.toString());
        }
        return client.getAcsResponse(request);
    }

    public static JSONObject buildOverrideParams(String content) {
        JSONObject overrideParams = new JSONObject();
        JSONArray watermarks = new JSONArray();
        //文字水印内容替换
        JSONObject watermark2 = new JSONObject();
        //模板上面关联需要替换内容的文字水印ID
        watermark2.put("WatermarkId", "4a303c57f1c67e35c0b230bdfc2896e3");
        //需要替换成对应的内容
        watermark2.put("Content", "用户ID：" + content);
        watermarks.add(watermark2);
        overrideParams.put("Watermarks", watermarks);
        return overrideParams;
    }

    /**
     * 提交截图作业
     *
     * @param videoId 视频ID
     *                specifiedOffsetTime   截图指定时间的起始点，单位：毫秒
     *                interval              截图的间隔时间，必须大于等于0，单位：秒。其中Interval为0表示按照Count数根据视频时长平均截图
     * @return 截图结果：Json:  {
     * "RequestId": "25818875-5F78-4A13-BEF6-D7393642CA58",
     * "SnapshotJob": {
     * "JobId": "ad90a501b1b94ba6afb72374ad005046"
     * }
     * }
     */
    public static SubmitSnapshotJobResponse submitSnapshotJobs(String videoId, Long specifiedOffsetTime, Long interval, Long count) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        SubmitSnapshotJobRequest request = new SubmitSnapshotJobRequest();
        request.setVideoId(videoId);
        request.setSpecifiedOffsetTime(specifiedOffsetTime);
        request.setInterval(interval);
        request.setCount(count);
        return client.getAcsResponse(request);
    }

    public static ListSnapshotsResponse listSnapshots(String videoId) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        ListSnapshotsRequest request = new ListSnapshotsRequest();
        request.setVideoId(videoId);
        request.setSnapshotType("SpriteOriginSnapshot");
        return client.getAcsResponse(request);
    }


//	SnapshotType	String	否	返回的截图类型。默认值：CoverSnapshot。
//	取值范围：
//	CoverSnapshot（封面截图）
//	NormalSnapshot（SubmitSnapshotJob接口发起的普通截图)
//	SpriteSnapshot（SubmitSnapshotJob接口发起的雪碧截图)
//	SpriteOriginSnapshot（组成雪碧图的原始小图)


    /**
     * 提交AI作业(智能审核，智能DNA)
     *
     * @param videoId 视频ID
     *                types   AI作业类型，多个用逗号分隔。取值范围：AIVideoCensor(智能审核)、AIMediaDNA(智能DNA)
     * @return AI结果：Json: {
     * "RequestId": "25818875-5F78-4A13-BEF6-D7393642CA58",
     * "AIJobList": {
     * "AIJob": [
     * {
     * "JobId": "88c6ca184c0e47098a5b665e2a126797",
     * "MediaId":"3D3D12340d92c641401fab46a0b847fdff97",
     * "Type":"AIVideoCensor"
     * },
     * {
     * "JobId": "9e82640c85114bf5af23edfaf8933272",
     * "MediaId":"3D3D12340d92c641401fab46a0b847fdff97",
     * "Type":"AIMediaDNA"
     * }
     * ]
     * }
     * }
     */
    public static SubmitAIJobResponse submitAIJob(String videoId, String types) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        SubmitAIJobRequest request = new SubmitAIJobRequest();
        request.setMediaId(videoId);
        request.setTypes(types);
        return client.getAcsResponse(request);
    }

    /**
     * 获取视频播放地址
     *
     * @param videoId 视频ID
     * @return 视频地址信息：Json:  {
     * "RequestId": "25818875-5F78-4A13-BEF6-D7393642CA58",
     * "VideoBase": {
     * "VideoId": "93ab850b4f6f44eab54b6e91d24d81d4",
     * "Title": "阿里云VOD",
     * "Duration": "15.0",
     * "CoverURL": "http://image.example.com/sample.jpg?auth_key=2333232-atbb667",
     * "Status": "Normal",
     * "MediaType": "video",
     * "OutputType": "cdn",
     * "CreationTime": "2017-06-26T05:38:48Z"
     * },
     * "PlayInfoList": {
     * "PlayInfo":[
     * {
     * "Bitrate": "1575.0",
     * "Definition": "SD",
     * "Duration": "15.0",
     * "Encrypt": 0,
     * "PlayURL": "http://vod.aliyunsample.com/ABEBDE15CC479FD4D1329/52a53151eba5226f8e2da3b55bc57c49.m3u8?auth_key=abdf2123-6783232accc",
     * "Format": "m3u8",
     * "StreamType": "video",
     * "Fps": "30.0",
     * "Height": 960,
     * "Size": 3090951,
     * "Width": 540,
     * "JobId":"23ef850b4f6f44eab54b6e91d24d8456",
     * "WatermarkId": "c169d97d995040d6af5b815dfff14e3f",
     * "Status":"Invisible",
     * "CreationTime":"2018-07-04T06:36:56Z",
     * "ModificationTime":"2018-07-04T06:37:13Z"
     * }
     * ]
     * }
     * }
     */
    public static GetPlayInfoResponse getPlayInfo(String videoId) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        GetPlayInfoRequest request = new GetPlayInfoRequest();
        request.setResultType("Multiple");
        request.setVideoId(videoId);


        return client.getAcsResponse(request);
    }


    public static DeleteStreamResponse deleteStream(String videoId, String jobId) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        DeleteStreamRequest request = new DeleteStreamRequest();
        request.setVideoId(videoId);
        request.setJobIds(jobId);
        return client.getAcsResponse(request);
    }

    public static GetTranscodeTaskResponse getTranscodeTask(String jobId) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        GetTranscodeTaskRequest request = new GetTranscodeTaskRequest();
        request.setTranscodeTaskId(jobId);
        return client.getAcsResponse(request);
    }

    public static GetTranscodeSummaryResponse getTranscodeSummary(String videoId) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        GetTranscodeSummaryRequest request = new GetTranscodeSummaryRequest();
        request.setVideoIds(videoId);
        return client.getAcsResponse(request);
    }

    public static GetTranscodeTemplateGroupResponse getTranscodeTemplateGroup(String transcodeTemplateGroupId) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        GetTranscodeTemplateGroupRequest request = new GetTranscodeTemplateGroupRequest();
        request.setTranscodeTemplateGroupId(transcodeTemplateGroupId);
        return client.getAcsResponse(request);
    }

    public static ListVodTemplateResponse listVodTemplate() throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        ListVodTemplateRequest request = new ListVodTemplateRequest();

        return client.getAcsResponse(request);
    }

    /**
     * 获取视频播放凭证
     *
     * @param videoId 视频ID
     * @return GetVideoPlayAuthResponse
     * @throws Exception
     */
    public static GetVideoPlayAuthResponse getVideoPlayAuth(String videoId) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        GetVideoPlayAuthRequest request = new GetVideoPlayAuthRequest();
        request.setVideoId(videoId);
        return client.getAcsResponse(request);
    }

    /**
     * 获取在线的推送流
     *
     * @param domainName
     * @return
     * @throws Exception
     */
    public static DescribeLiveStreamsOnlineListResponse describeLiveStreamsOnlineListResponse(String domainName) throws Exception {
        JSONObject akInfoJson = AkUtil.getAkInfo();
        DefaultAcsClient client = initVodClient(
                akInfoJson.getString("AccessKeyId"),
                akInfoJson.getString("AccessKeySecret"),
                akInfoJson.getString("SecurityToken"));

        DescribeLiveStreamsOnlineListRequest request = new DescribeLiveStreamsOnlineListRequest();
        request.setDomainName(domainName);

        return client.getAcsResponse(request);
    }

    /**
     * 获取视频信息
     *
     * @param videoId 视频ID
     * @return GetVideoInfoResponse
     * @throws Exception
     */
    public static GetVideoInfoResponse getVideoInfo(String videoId) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        GetVideoInfoRequest request = new GetVideoInfoRequest();
        request.setVideoId(videoId);
        return client.getAcsResponse(request);
    }

    /**
     * 预热缓存
     *
     * @param objectPath
     * @return PushObjectCacheResponse
     * @throws Exception
     */
    public static PreloadVodObjectCachesResponse pushObjectCache(String objectPath) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        PreloadVodObjectCachesRequest request = new PreloadVodObjectCachesRequest();
        request.setObjectPath(objectPath);
        return client.getAcsResponse(request);
    }

    /**
     * 获取辅助媒资上传地址和凭证
     *
     * @param objectPath
     * @return PushObjectCacheResponse
     * @throws Exception
     */
    public static CreateUploadAttachedMediaResponse createUploadAttachedMedia(String objectPath) throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        CreateUploadAttachedMediaRequest request = new CreateUploadAttachedMediaRequest();


        return client.getAcsResponse(request);
    }

    /**
     * 获取图片上传地址和凭证
     *
     * @param
     * @return CreateUploadImageResponse
     * @throws Exception
     */
    public static CreateUploadImageResponse createUploadImage() throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        CreateUploadImageRequest request = new CreateUploadImageRequest();
        request.setImageType("default");
        return client.getAcsResponse(request);
    }


    /**
     * 图片上传接口，流式上传示例
     * 参数参考文档 https://help.aliyun.com/document_detail/55619.html
     *
     * @param imageExt
     * @param url
     * @throws Exception
     */
    public static String uploadImageLocalFile(String imageExt, String url) throws Exception {
        // 图片类型（必选）取值范围：default（默认)，cover（封面），watermark(水印)
        String imageType = "default";
        UploadImageRequest request = new UploadImageRequest(AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"), imageType);
        /* 图片文件扩展名（可选）取值范围：png，jpg，jpeg */
        request.setImageExt(imageExt);
        /* 图片标题（可选）长度不超过128个字节，UTF8编码 */
        //request.setTitle("图片标题");
        /* 图片标签（可选）单个标签不超过32字节，最多不超过16个标签，多个用逗号分隔，UTF8编码 */
        //request.setTags("标签1,标签2");
        /* 存储区域（可选）*/
        //request.setStorageLocation("out-4f3952f78c0211e8b3020013e7.oss-cn-shanghai.aliyuncs.com");
        /* 流式上传时，InputStream为必选，fileName为源文件名称，如:文件名称.png(可选)*/
        //request.setFileName("测试文件名称.png");
        /* 开启默认上传进度回调 */
        // request.setPrintProgress(true);
        /* 设置自定义上传进度回调 (必须继承 ProgressListener) */
        // request.setProgressListener(new PutObjectProgressListener());
        InputStream urlStream = getUrlStream(url);
        request.setInputStream(urlStream);
        UploadImageImpl uploadImage = new UploadImageImpl();
        UploadImageResponse response = uploadImage.upload(request);
        return response.getImageURL();
    }

    /**
     * 辅助媒资上传接口，流式上传示例（支持文件流和网络流）
     * 参数参考文档 https://help.aliyun.com/document_detail/98467.html
     *
     * @param businessType mediaExt url
     * @throws Exception
     */
    public static String uploadAttachedMediaStream(String businessType, String mediaExt, String url) throws Exception {
        UploadAttachedMediaRequest request;
        InputStream urlStream = getUrlStream(url);
        request = new UploadAttachedMediaRequest(AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"), businessType, mediaExt);
        request.setInputStream(urlStream);
        UploadAttachedMediaImpl uploader = new UploadAttachedMediaImpl();
        UploadAttachedMediaResponse response = uploader.upload(request);
        return response.getMediaURL();
    }

    public static UploadAttachedMediaResponse uploadAttachedMedia(String businessType, String mediaExt, String title, String fileName) throws Exception {
        JSONObject akInfo = AkUtil.getAkInfo();

        UploadAttachedMediaRequest request = new UploadAttachedMediaRequest(akInfo.getString("AccessKeyId"), akInfo.getString("AccessKeySecret"), businessType, mediaExt);
        request.setSecurityToken(akInfo.getString("SecurityToken"));
        request.setTitle(title);
        request.setInputStream(new FileInputStream(fileName));

        UploadAttachedMediaImpl uploadAttachedMedia = new UploadAttachedMediaImpl();

        UploadAttachedMediaResponse response = uploadAttachedMedia.upload(request);

        return response;
    }

//    /**
//     * 本地文件上传接口
//     *
//     * @param title
//     * @param fileName
//     */
//    public static String uploadVideo(String title, String fileName) throws Exception {
//        UploadVideoRequest request = new UploadVideoRequest(AkUtil.getAkInfo().getString("AccessKeyId"),
//                AkUtil.getAkInfo().getString("AccessKeySecret"), title, fileName);
//        /* 可指定分片上传时每个分片的大小，默认为2M字节 */
//        request.setPartSize(2 * 1024 * 1024L);
//        /* 可指定分片上传时的并发线程数，默认为1，(注：该配置会占用服务器CPU资源，需根据服务器情况指定）*/
//        request.setTaskNum(1);
//        /* 是否开启断点续传, 默认断点续传功能关闭。当网络不稳定或者程序崩溃时，再次发起相同上传请求，可以继续未完成的上传任务，适用于超时3000秒仍不能上传完成的大文件。
//        注意: 断点续传开启后，会在上传过程中将上传位置写入本地磁盘文件，影响文件上传速度，请您根据实际情况选择是否开启*/
//        //request.setEnableCheckpoint(false);
//        /* OSS慢请求日志打印超时时间，是指每个分片上传时间超过该阈值时会打印debug日志，如果想屏蔽此日志，请调整该阈值。单位: 毫秒，默认为300000毫秒*/
//        //request.setSlowRequestsThreshold(300000L);
//        /* 可指定每个分片慢请求时打印日志的时间阈值，默认为300s*/
//        //request.setSlowRequestsThreshold(300000L);
//        /* 是否显示水印(可选)，指定模板组ID时，根据模板组配置确定是否显示水印*/
//        //request.setIsShowWaterMark(true);
//        /* 设置上传完成后的回调URL(可选)，建议您通过点播控制台配置事件通知，参见文档 https://help.aliyun.com/document_detail/55627.html */
//        //request.setCallback("http://callback.sample.com");
//        /* 自定义消息回调设置(可选)，参数说明参考文档 https://help.aliyun.com/document_detail/86952.html#UserData */
//        // request.setUserData("{\"Extend\":{\"test\":\"www\",\"localId\":\"xxxx\"},\"MessageCallback\":{\"CallbackURL\":\"http://test.test.com\"}}");
//        /* 视频分类ID(可选) */
//        //request.setCateId(0);
//        /* 视频标签,多个用逗号分隔(可选) */
//        //request.setTags("标签1,标签2");
//        /* 视频描述(可选) */
//        //request.setDescription("视频描述");
//        /* 封面图片(可选) */
//        //request.setCoverURL("http://cover.sample.com/sample.jpg");
//        /* 模板组ID(可选) */
//        //request.setTemplateGroupId("8c4792cbc8694e7084fd5330e56a33d");
//        /* 工作流ID(可选) */
//        //request.setWorkflowId("d4430d07361f0*be1339577859b0177b");
//        /* 存储区域(可选) */
//        //request.setStorageLocation("in-201703232118266-5sejdln9o.oss-cn-shanghai.aliyuncs.com");
//        /* 开启默认上传进度回调 */
//        //request.setPrintProgress(false);
//        /* 设置自定义上传进度回调 (必须继承 VoDProgressListener) */
//        //request.setProgressListener(new PutObjectProgressListener());
//        /* 设置应用ID*/
//        request.setAppId(ConfigMapUtil.getValueByKey("APP_ID"));
//        UploadVideoImpl uploader = new UploadVideoImpl();
//        UploadVideoResponse response = uploader.uploadVideo(request);
//        System.out.print("RequestId=" + response.getRequestId() + "\n");  //请求视频点播服务的请求ID
//        String videoId = response.getVideoId();
//        if (response.isSuccess()) {
//            System.out.print("VideoId=" + response.getVideoId() + "\n");
//        } else {
//            /* 如果设置回调URL无效，不影响视频上传，可以返回VideoId同时会返回错误码。其他情况上传失败时，VideoId为空，此时需要根据返回错误码分析具体错误原因 */
//            System.out.print("VideoId=" + response.getVideoId() + "\n");
//            System.out.print("ErrorCode=" + response.getCode() + "\n");
//            System.out.print("ErrorMessage=" + response.getMessage() + "\n");
//        }
//        return videoId;
//    }

    /**
     * 流式上传接口
     *
     * @param
     * @param
     * @param title
     * @param fileName
     * @param inputStream
     */
    public static String uploadStream(String title, String fileName, InputStream inputStream) throws Exception {
        UploadStreamRequest request = new UploadStreamRequest(AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"), title, fileName, inputStream);
        request.setSecurityToken(AkUtil.getAkInfo().getString("SecurityToken"));
        /* 是否使用默认水印(可选)，指定模板组ID时，根据模板组配置确定是否使用默认水印*/
        //request.setShowWaterMark(true);
        /* 设置上传完成后的回调URL(可选)，建议通过点播控制台配置消息监听事件，参见文档 https://help.aliyun.com/document_detail/57029.html */
        //request.setCallback("http://callback.sample.com");
        /* 自定义消息回调设置，参数说明参考文档 https://help.aliyun.com/document_detail/86952.html#UserData */
        //request.setUserData(""{\"Extend\":{\"test\":\"www\",\"localId\":\"xxxx\"},\"MessageCallback\":{\"CallbackURL\":\"http://test.test.com\"}}"");
        /* 视频分类ID(可选) */
        //request.setCateId(0);
        /* 视频标签,多个用逗号分隔(可选) */
        //request.setTags("标签1,标签2");
        /* 视频描述(可选) */
        //request.setDescription("视频描述");
        /* 封面图片(可选) */
        //request.setCoverURL("http://cover.sample.com/sample.jpg");
        /* 模板组ID(可选) */
        //request.setTemplateGroupId("8c4792cbc8694e7084fd5330e56a33d");
        /* 工作流ID(可选) */
        //request.setWorkflowId("d4430d07361f0*be1339577859b0177b");
        /* 存储区域(可选) */
        //request.setStorageLocation("in-201703232118266-5sejdln9o.oss-cn-shanghai.aliyuncs.com");
        /* 开启默认上传进度回调 */
        // request.setPrintProgress(true);
        /* 设置自定义上传进度回调 (必须继承 VoDProgressListener) */
        // request.setProgressListener(new PutObjectProgressListener());
        /* 设置应用ID*/
        request.setAppId(ConfigMapUtil.getValueByKey("APP_ID"));
        UploadVideoImpl uploader = new UploadVideoImpl();
        UploadStreamResponse response = uploader.uploadStream(request);
        System.out.print("RequestId=" + response.getRequestId() + "\n");  //请求视频点播服务的请求ID
        if (response.isSuccess()) {
            System.out.print("VideoId=" + response.getVideoId() + "\n");
        } else { //如果设置回调URL无效，不影响视频上传，可以返回VideoId同时会返回错误码。其他情况上传失败时，VideoId为空，此时需要根据返回错误码分析具体错误原因
            System.out.print("VideoId=" + response.getVideoId() + "\n");
            System.out.print("ErrorCode=" + response.getCode() + "\n");
            System.out.print("ErrorMessage=" + response.getMessage() + "\n");
        }
        return response.getVideoId();
    }


    /**
     * 图片上传接口，流式上传示例（支持文件流和网络流）
     * 参数参考文档 https://help.aliyun.com/document_detail/55619.html
     *
     * @param imageType
     * @param inputStream
     */
    public static String uploadImageStream(String imageType, InputStream inputStream) throws Exception {
        /* 图片类型（必选）取值范围：default（默认)，cover（封面），watermark（水印）*/
        UploadImageRequest request = new UploadImageRequest(AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"), imageType);

        request.setSecurityToken(AkUtil.getAkInfo().getString("SecurityToken"));
        /* 图片文件扩展名（可选）取值范围：png，jpg，jpeg */
        //request.setImageExt("png");
        /* 图片标题（可选）长度不超过128个字节，UTF8编码 */
        //request.setTitle("图片标题");
        /* 图片标签（可选）单个标签不超过32字节，最多不超过16个标签，多个用逗号分隔，UTF8编码 */
        //request.setTags("标签1,标签2");
        /* 存储区域（可选）*/
        //request.setStorageLocation("out-4f3952f78c0211e8b3020013e7.oss-cn-shanghai.aliyuncs.com");
        /* 流式上传时，InputStream为必选，fileName为源文件名称，如:文件名称.png(可选)*/
        //request.setFileName("测试文件名称.png");
        /* 开启默认上传进度回调 */
        // request.setPrintProgress(true);
        /* 设置自定义上传进度回调 (必须继承 VoDProgressListener) */
        // request.setProgressListener(new PutObjectProgressListener());
        /* 设置应用ID*/
        //request.setAppId("app-1000000");
        // 1.文件流上传
        request.setInputStream(inputStream);
        // 2.网络流上传
        //String url = "http://test.aliyun.com/image/default/test.png";
        //InputStream urlStream = getUrlStream(url);
        //if (urlStream != null) {
        //	request.setInputStream(urlStream);
        //}

        // 开始上传图片
        UploadImageImpl uploadImage = new UploadImageImpl();
        UploadImageResponse response = uploadImage.upload(request);
        System.out.print("RequestId=" + response.getRequestId() + "\n");
        if (response.isSuccess()) {
            System.out.print("ImageId=" + response.getImageId() + "\n");
            System.out.print("ImageURL=" + response.getImageURL() + "\n");
        } else {
            System.out.print("ErrorCode=" + response.getCode() + "\n");
            System.out.print("ErrorMessage=" + response.getMessage() + "\n");
        }
        return response.getImageURL();
    }

    public static ListAppInfoResponse listAppInfoRequest() throws Exception {
        DefaultAcsClient client = initVodClient(
                AkUtil.getAkInfo().getString("AccessKeyId"),
                AkUtil.getAkInfo().getString("AccessKeySecret"),
                AkUtil.getAkInfo().getString("SecurityToken"));
        ListAppInfoRequest request = new ListAppInfoRequest();

        return client.getAcsResponse(request);
    }

    public static UploadImageResponse uploadImage(String imageType, String title, String fileName) throws Exception {
        JSONObject akInfo = AkUtil.getAkInfo();

        UploadImageRequest request = new UploadImageRequest(akInfo.getString("AccessKeyId"), akInfo.getString("AccessKeySecret"), imageType);

        request.setSecurityToken(akInfo.getString("SecurityToken"));
        request.setTitle(title);
        request.setInputStream(new FileInputStream(fileName));
        request.setImageExt(fileName.split("\\.")[1]);


        UploadImageImpl uploadImage = new UploadImageImpl();

        UploadImageResponse response = uploadImage.upload(request);

        return response;
    }


    public static UploadVideoResponse uploadVideo(String title, String fileName) throws Exception {
        JSONObject akInfo = AkUtil.getAkInfo();

        UploadVideoRequest request = new UploadVideoRequest(akInfo.getString("AccessKeyId"), akInfo.getString("AccessKeySecret"), title, fileName);

        request.setSecurityToken(akInfo.getString("SecurityToken"));

        UploadVideoImpl uploadVideo = new UploadVideoImpl();
        UploadVideoResponse response = uploadVideo.uploadVideo(request);

        return response;
    }

    private static InputStream getFileStream(String fileName) {
        try {
            return new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static InputStream getUrlStream(String url) {
        try {
            return new URL(url).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
