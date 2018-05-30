
[toc]

### wifi室内定位与人数统计项目介绍
这个项目主要实现通过手机端app根据wifi信号强度的实现室内定位。

#### **app**
语言：java (android)

环境：windows 10

工具：Android Studio

主要功能：实现wifi信号强度的离线采集， 在线定位，步长计算，步数统计，步行轨迹推算。



#### **server**
语言：c#(.net)

环境：windows 10

工具：vs2013

服务：IIS

主要功能：实现与app交互的接口：定位接口，采集数据接口。实现定位算法等核心代码。实现定位精度统计的展示页面。可以展示不同算法定位的结果和误差等。

### 数据库表设计
#### wifi指纹信息表
**tb_wifi_fingerprint**

|字段名  | 含义 |类型|
|------------- | -------------| ------------|
id| 主键|int|
coord| 坐标| varchar(10)|
addtime|添加时间|varchar(50)|
updatetime|更新时间|varchar(50)|
flag|标志位|int|
memory|备注|varchar(MAX)|

#### wifi信号强度表
**tb_wifi_rssi**

|字段名  | 含义 |类型|
|------------- | -------------| ------------|
id| 主键|int|
rssi|wifi信号强度|int|
mac|对应的ap的mac地址|varchar(20)|
mobile_id|手机型号id|int|
room_id|实验房间id|int|
coord_id|参考坐标点id|int|

#### 房间信息表
**tb_room**

|字段名  | 含义 |类型|
|------------- | -------------| ------------|
id| 主键|int|
room_name|房间名称|varchar(20)|
floor_num|楼层号|int|
left_up|房间左上角坐标|varchar(10)|
left_down|房间左下角坐标|varchar(10)|
right_up|房间右上角坐标|varchar(10)|
right_down|房间右下角坐标|varchar(10)|

#### 定位日志表
**tb_location_log**

|字段名  | 含义 |类型|
|------------- | -------------| ------------|
id| 主键|int|
actual_coord|实际坐标|varchar(10)|
location_coord|定位坐标|varchar(10)|
room_id|实验房间id|int|
mobile_id|定位手机型号id|int|
location_algorithm|定位算法id|int|
memory|备注|varchar(MAX)|
flag|标识(关联采集种类表)|int|

#### 手机型号表
**tb_mobile_model**

|字段名  | 含义 |类型|
|------------- | -------------| ------------|
id| 主键|int|
model_name|手机型号名称|varchar(50)|

#### 采集种类表
**tb_collection_flag**

|字段名|含义|类型|
|------------|------------|----------|
id|主键|int|
content|描述|varchar(100)|


#### 定位算法种类表
**tb_collection_flag**

|字段名|含义|类型|
|------------|------------|----------|
id|主键|int|
algorithm_name|算法名称|varchar(50)|

### 接口设计
#### **1.离线采集接口**

**request information**
POST  
api/Fingerprint
application/json

```json
{
"ap":{
	"ap0":{
		"rssi":-66,
		"mac":"0a:69:6c:51:36:34"
		},
	"ap1":{
		"rssi":-56,
		"mac":"0a:69:6c:51:36:eb"
		}
	},
"coord":"0,0",
"memory":"test",
"flag":0,
"addtime":"2017年5月5日 10:08:56",
"mobile_id":3,
"room_id":"1"
}
```

**response information**
HttpResponseMessage
statusCode:200
message:"上传成功"


#### **2.定位接口**

**request information**
POST
api/Location/
application/json

```json

{
	"room_id":"2",
	"mobile_id":"3",
	"actual_coord":"0,0",
	"algorithm":"1",
	"ap":{
		"ap0":{
			"rssi":-56,
			"mac":"0a:69:6c:51:36:80"
		},
		"ap1":{
			"rssi":-53,
			"mac":"0a:69:6c:51:36:7f"
		},
		"ap2":{
			"rssi":-69,
			"mac":"0a:69:6c:51:3d:94"
		},
		"ap3":{
			"rssi":-73,
			"mac":"0a:69:6c:51:35:eb"
		},
		"ap4":{
			"rssi":-83,
			"mac":"06:69:6c:4a:db:9b"
		}
	}
}
```

**response information**
HttpResponseMessage
statusCode :200
message:"100,100"(定位结果)

#### **3.获取定位日志接口**

**request information**
GET
api/LocationLog/?room={room}&mobile={mobile}&algorithm={algorithm}
|参数|描述|
|---|---|
room|实验室id|
mobile|手机型号id|
algorithm|使用算法id|

**request information**
json
```
{"ds":
	[
		{
			"actual_coord":"60,0",
			"location_coord":"108,192"
		},
		{
			"actual_coord":"180,240",
			"location_coord":"96,360"
		},
	]
}
```

#### **4.获取实验房间信息接口**

**request information**
GET
api/Room/id

**response information**
HttpResponseMessage
message:
```
{
	"id":3,
	"room_name":"实验室1"，
	“left_up":"0,720",
	"left_down":"0,0",
	"right_up":"240,720",
	"right_down":"0,240"
}
```

#### **5.获取手机型号接口**

**request information**
GET
api/MobileModel/id

**response information**
HttpResponseMessage
```
{	
	"id":3,
	"model_name":"xiaomi"
}
```




  [1]: ./image/collection.png "collection.png"
  [2]: ./image/location.png "location.png"
  [3]: ./images/log.png "log.png"