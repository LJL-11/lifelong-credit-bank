# API 设计草稿

统一响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

积分增加请求示例：

```http
POST /api/admin/credit-accounts/increase
Content-Type: application/json

{
  "learnerId": 1,
  "amount": 20,
  "sourceType": "COURSE_COMPLETE",
  "sourceNo": "JAVA-BASE",
  "remark": "完成 Java 程序设计基础"
}
```

积分消费请求示例：

```http
POST /api/admin/credit-accounts/consume
Content-Type: application/json

{
  "learnerId": 1,
  "amount": 10,
  "sourceType": "MALL_EXCHANGE",
  "sourceNo": "GIFT-001",
  "remark": "积分商城兑换"
}
```
