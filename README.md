# InsuranceAPI

一个使用 Spring Boot 构建的全栈保险购买 Demo，包含 H2 Console 风格的购买前端，以及从产品查询、创建投保订单、模拟支付到生成和查询保单的完整后端流程。

> 本项目用于学习和功能演示。支付为本地模拟，不会产生真实扣款。


## 已实现功能

- H2 Console 风格的响应式保险购买前端
- 四步可视化购买流程：选产品、填资料、确认支付、查看保单
- 页面状态提示、前端表单校验、错误反馈和重新购买
- 查询保险产品列表
- 根据产品 ID 查询产品详情
- 创建投保订单
- 校验投保人和被保险人信息
- 根据订单号查询订单
- 模拟订单支付
- 支付成功后自动生成保单
- 根据保单号查询保单
- 重复支付时返回已经生成的保单，避免重复出单
- 保存下单时的保费、保额和保障天数快照
- 统一处理资源不存在、参数校验失败和 JSON 格式错误
- 使用自动化测试覆盖完整购买流程和常见错误响应
- 对前端展示的用户输入进行 HTML 转义
- 使用 H2 内存数据库保存 Demo 数据
- 应用启动时自动初始化两款旅行意外险产品

## 业务流程

```text
打开保险购买控制台
      ↓
查询并选择保险产品
      ↓
填写投保人和被保险人信息
      ↓
创建待支付订单（PENDING_PAYMENT）
      ↓
模拟支付
      ↓
订单变为已支付（PAID）
      ↓
生成并展示保单
```

## 技术栈

- Java 21
- Spring Boot 4.1.2
- Spring Web MVC
- Spring Data JPA
- Jakarta Bean Validation
- H2 Database
- Lombok
- Maven
- JUnit 5
- HTML5
- CSS3
- 原生 JavaScript（无前端构建工具）

## 项目结构

```text
src/main/java/com/example/insuranceapi
├── common       # 通用错误响应和全局异常处理
├── config       # 初始化产品数据
├── order        # 投保订单、下单请求及订单查询
├── payment      # 模拟支付
├── policy       # 保单实体、响应及数据库访问
└── product      # 保险产品查询及数据库访问

src/main/resources
├── application.properties
└── static
    ├── index.html                    # 保险购买控制台
    ├── css/insurance-console.css     # H2 Console 风格及响应式布局
    └── js/insurance-console.js       # 前端状态和 API 交互

src/test/java/com/example/insuranceapi
├── InsuranceApiApplicationTests.java
└── InsurancePurchaseFlowTests.java  # 页面入口和完整购买流程测试
```

项目采用常见的分层结构：

- `Controller`：接收和返回 HTTP 请求
- `Service`：处理业务规则及事务
- `Repository`：通过 Spring Data JPA 访问数据库
- `Entity`：映射数据库表
- `Request/Response`：定义 API 的输入和输出

## 环境要求

- JDK 21
- Maven 3.9 或更高版本

检查本地环境：

```bash
java -version
mvn -version
```

两条命令都应该显示 Java 21。如果 macOS 上 Maven 使用了其他 Java 版本，可以先执行：

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

## 启动项目

进入项目根目录后执行测试：

```bash
mvn test
```

启动应用：

```bash
mvn spring-boot:run
```

应用默认运行在：

```text
http://localhost:8080
```

打开该地址会进入保险购买前端。页面参考 H2 Console 的工具界面风格，提供以下四步购买流程：

1. 选择保险产品
2. 填写投保人与被保险人资料
3. 核对订单并模拟支付
4. 查看生成的保单

前端直接由 Spring Boot 提供，无需另外安装 Node.js 或启动前端开发服务器。

## 前端页面

页面入口：

```text
http://localhost:8080/
```

界面参考 H2 Console 的工具页面风格，使用蓝色标题栏、米灰色工作区、原生表单控件和实时状态带。主要交互包括：

- 从后端动态读取保险产品
- 展示产品保额、保费和保障期限
- 收集并校验投保人、被保险人资料
- 创建订单后显示订单摘要
- 要求用户二次确认后执行模拟支付
- 支付成功后展示保单号和保障日期
- 支持清空当前状态并开始新的购买流程
- 在窄屏设备上自动调整为移动布局
- 支持键盘焦点和“减少动态效果”系统设置

前端没有保存身份证号等信息到浏览器本地存储。刷新页面会清除当前页面状态，但已写入 H2 的订单在应用停止前仍可通过 API 查询。

## API 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| `GET` | `/api/products` | 查询全部保险产品 |
| `GET` | `/api/products/{id}` | 查询指定产品 |
| `POST` | `/api/orders` | 创建投保订单 |
| `GET` | `/api/orders/{orderNo}` | 查询指定订单 |
| `POST` | `/api/orders/{orderNo}/pay` | 模拟支付并生成保单 |
| `GET` | `/api/policies/{policyNo}` | 根据保单号查询保单 |

常用 HTTP 状态码：

| 状态码 | 含义 |
| --- | --- |
| `200 OK` | 查询成功，或重复支付时返回原保单 |
| `201 Created` | 订单或保单首次创建成功 |
| `400 Bad Request` | JSON 格式或请求参数不正确 |
| `404 Not Found` | 产品、订单或保单不存在 |
| `409 Conflict` | 当前订单状态不允许执行该操作 |

## 使用示例

### 1. 查询产品列表

```bash
curl http://localhost:8080/api/products
```

应用启动时会自动创建两款产品：

- `TRAVEL_BASIC`：基础旅行意外险
- `TRAVEL_PLUS`：升级旅行意外险

### 2. 查询产品详情

```bash
curl http://localhost:8080/api/products/1
```

### 3. 创建投保订单

```bash
curl -i -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "applicant": {
      "name": "张三",
      "idCard": "110101199001011234",
      "phone": "13800138000"
    },
    "insuredPerson": {
      "name": "李四",
      "idCard": "110101199502021234",
      "relationship": "SPOUSE"
    }
  }'
```

创建成功后返回 `201 Created`。订单初始状态为：

```text
PENDING_PAYMENT
```

`relationship` 支持以下值：

- `SELF`：本人
- `SPOUSE`：配偶
- `PARENT`：父母
- `CHILD`：子女

### 4. 查询订单

将下面的订单号替换为创建订单接口实际返回的 `orderNo`：

```bash
curl http://localhost:8080/api/orders/ORD-替换为实际订单号
```

订单响应不会返回投保人和被保险人的身份证号，避免直接暴露敏感信息。

### 5. 模拟支付并生成保单

```bash
curl -i -X POST \
  http://localhost:8080/api/orders/ORD-替换为实际订单号/pay
```

支付成功后：

- 订单状态从 `PENDING_PAYMENT` 变为 `PAID`
- 系统生成以 `POL-` 开头的保单号
- 保单生效日为支付当天
- 保单到期日根据产品保障天数计算
- 对同一个订单重复调用支付接口，不会重复生成保单

首次支付成功返回 `201 Created`；对已经支付的订单重复请求时返回 `200 OK`，并返回原保单。

### 6. 查询保单

将保单号替换为支付接口返回的 `policyNo`：

```bash
curl http://localhost:8080/api/policies/POL-替换为实际保单号
```

如果保单不存在，接口返回统一格式的 `404 Not Found`。

## 参数校验

创建订单时会校验：

- 产品 ID 不能为空且必须大于 0
- 姓名不能为空，最长 50 个字符
- 身份证号必须为 18 位，最后一位可以为数字或 `X`
- 手机号必须符合当前 Demo 使用的中国大陆手机号格式
- 与投保人关系必须为支持的枚举值

校验失败时返回 `400 Bad Request`，例如：

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "applicant.phone: 投保人手机号格式不正确",
  "path": "/api/orders"
}
```

请求不存在的产品或订单时返回 `404 Not Found`。

## 自动化测试

运行全部测试：

```bash
mvn test
```

当前测试共 4 项，覆盖：

- Spring Boot 应用上下文启动
- 首页和静态前端入口
- 产品查询、创建订单、查询订单、模拟支付和查询保单的完整流程
- 重复支付的幂等行为
- 产品不存在和参数校验失败等错误响应

## 数据库说明

当前项目使用 H2 内存数据库：

```text
jdbc:h2:mem:insurance_db
```

H2 控制台地址：

```text
http://localhost:8080/h2-console
```

连接信息：

```text
JDBC URL: jdbc:h2:mem:insurance_db
User Name: sa
Password: 留空
```

当前配置使用 `create-drop`：应用启动时创建表，应用停止时删除表。因此，重启应用后订单和保单会消失，产品初始数据会重新创建。

## 当前数据模型

### 保险产品 `InsuranceProduct`

保存产品编码、名称、介绍、保额、保费和保障天数。

### 投保订单 `InsuranceOrder`

保存订单号、所选产品、投保人信息、被保险人信息、购买时的保费/保额/保障天数快照、订单状态和创建时间。即使产品后续调价，历史订单和保单也不会随之变化。

订单状态包括：

- `PENDING_PAYMENT`
- `PAID`
- `CANCELLED`

### 保单 `Policy`

每个已支付订单对应一张保单，保存保单号、保额、生效日期、到期日期和创建时间。

## Demo 限制

当前版本有以下限制：

- 支付为本地模拟，不会连接真实支付渠道
- 没有用户登录、身份认证和权限控制
- 没有真实核保及保险规则引擎
- 身份证号只做格式校验，没有校验出生日期和校验码
- 敏感字段尚未进行数据库加密或脱敏存储
- 使用内存数据库，数据不会永久保存
- 暂未生成 OpenAPI/Swagger 接口文档
- 暂未处理高并发支付、数据库锁和支付回调签名
- 前端没有独立登录态、订单列表或历史保单中心

## 后续开发目标

1. 接入 Swagger UI
2. 将 H2 替换为 MySQL，并使用数据库迁移工具管理表结构
3. 增加 Spring Security 和用户登录
4. 增加订单取消、支付回调及并发幂等控制
5. 加密保存身份证号等敏感信息
6. 增加用户订单列表和历史保单页面

