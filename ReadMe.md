# 关于
## 一、OSS配置
### 1. 购买阿里云OSS
链接：https://www.aliyun.com/product/oss?spm=5176.10695662.1112155.2.5f0330bewWGCED&aly_as=zzUN02Vit

### 2. 进入OSS管理控制台
右侧创建 Access Key  
链接：https://oss.console.aliyun.com/overview

找到  
AccessKey ID：  
Access Key Secret：  
替换OSSConnection中的对应项

### 3.进入Bucket 列表，创建Bucket

进入创建好的bucket，然后替换OSSConnection中的bucketName

## 二、fastDfs配置

将application.properties中下面的配置替换成自己的即可  

	fdfs.tracker-list[0]=1.0.0.166:22122
	fdfs.visit.url=1.0.0.166
	fdfs.visit.port=88