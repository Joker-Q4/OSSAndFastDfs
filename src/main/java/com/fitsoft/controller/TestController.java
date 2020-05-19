package com.fitsoft.controller;

import com.fitsoft.config.FastDFSClient;
import com.fitsoft.conn.OSSConnection;
import com.fitsoft.entity.CommonFile;
import com.fitsoft.utils.CommonResult;
import org.apache.http.entity.ContentType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    FastDFSClient fastDFSClient;

    @PostMapping("/upload")
    public Map<String, Object> upload(
            HttpServletRequest request,
            MultipartFile file){

        if(OSSConnection.upload(file)){
            return CommonResult.toResult(0,
                    "上传成功，表单参数为"+request.getParameter("title"),
                    0 ,
                    null);
        }
        return CommonResult.toResult(1, "上传失败",0 , null);
    }

    @PostMapping("/uploadFastDfs")
    public Map<String, Object> uploadFastDfs(MultipartFile file){

        try {
            String url = fastDFSClient.uploadFile(file, true);
            System.out.println("------------------------------------------------------------");
            System.out.println("上传的文件路径为：   " + url);
            System.out.println("------------------------------------------------------------");
            return CommonResult.toResult(0, url, 0, null);
        } catch (IOException e) {
            e.printStackTrace();
            return CommonResult.toResult(1, e.getMessage(), 0 , null);
        }
    }

    /**
     * 测试上传50个22M左右的apk，需要10秒钟
     * @return 结果
     * @throws IOException 读取文件异常
     */
    @GetMapping("/testUpload")
    public Map<String, Object> testUpload() throws IOException {
        final int testSize = 50;
        final CyclicBarrier barrier = new CyclicBarrier(testSize);
        final List<String> list = new ArrayList<>();
        final List<MultipartFile> files = new ArrayList<>();
        for(int i=1; i<=testSize; i++){
            File apkFile = new File("C:/test/test/小丑影视 v1.6.2 - 副本 ("+i+").apk");
            FileInputStream fileInputStream = new FileInputStream(apkFile);
            files.add(new MockMultipartFile(apkFile.getName(), apkFile.getName(),
                    ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream));
            fileInputStream.close();
        }
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("==========================================================");
            System.out.println("开始上传时间：           " + df.format(new Date()));
            System.out.println("==========================================================");
            for (MultipartFile file: files) {
                String url = fastDFSClient.uploadFile(file, true);
                list.add(url);
                System.out.println("------------------------------------------------------------");
                System.out.println("已上传的文件个数为:  "+list.size()+"      上传的文件路径为：   " + url);
                System.out.println("------------------------------------------------------------");
            }
            if(list.size() != files.size()){
                barrier.await();
            }
            System.out.println("==========================================================");
            System.out.println("结束上传时间：           " + df.format(new Date()));
            System.out.println("==========================================================");
            return CommonResult.toResult(0, "成功", list.size(), list);
        } catch (InterruptedException | BrokenBarrierException | IOException e) {
            e.printStackTrace();
            return CommonResult.toResult(1, e.getMessage(), 0 , null);
        }
    }

    @GetMapping("/delFileFromFastDfs")
    public Map<String, Object> delFileFromFastDfs(@RequestParam String fileUrl){
        System.out.println("*********************************************************");
        System.out.println("删除的文件路径为：   " + fileUrl);
        System.out.println("*********************************************************");
        fastDFSClient.deleteFile(fileUrl);
        return CommonResult.toResult(0, "", 0, null);
    }

    @GetMapping("/delete")
    public Map<String, Object> delete(@RequestParam(defaultValue = "") String name){
        if(name.isEmpty())
            return CommonResult.toResult(1, "删除失败，文件名不能为空",0 , null);
        OSSConnection.delete(name);
        return CommonResult.toResult(0, "删除成功",0 , null);
    }

    /**
     * 两种方式
     * 1. 通过inputStream处理
     * 2. 通过url获取， 好处就是服务器无压力
     * @return Map
     */
    @GetMapping("/selectAll")
    public Map<String, Object> selectAll(){

        final List<CommonFile> list = OSSConnection.queryAll();
        final int size = list.size();
        if(size == 0){
            return CommonResult.toResult(0, "无数据", 0 , null);
        }
        return CommonResult.toResult(0, "查询成功", size, list);
    }
}
