README
学号：202330450981
姓名：李岳烁
# DgoShop 项目说明

## 技术栈

### 后端

- - 开发语言：Java 17（提供稳定的语法特性与性能支持，适配Spring Boot最新版本依赖）

- - 框架：Spring Boot（快速构建RESTful API服务，集成自动配置、依赖管理等特性，简化后端开发流程）

- - 持久层：MyBatis（轻量级ORM框架，通过`@Mapper`注解与XML映射文件结合实现数据库交互，支持复杂SQL编写与参数映射）

- - 功能支持：CORS跨域配置（通过`@Configuration`注解配置跨域规则，允许前端域名跨域请求，兼容本地开发调试与线上生产环境）

- - 核心依赖：spring-boot-starter-web（提供Web开发能力，处理HTTP请求与响应）、spring-boot-starter-test（支持单元测试与集成测试）

### 前端

- - 框架：Vue 3（采用组合式API，实现代码逻辑的模块化复用，如`ref`用于响应式数据声明、`computed`处理衍生数据、`useRouter`实现路由跳转，提升代码可维护性）

- - UI工具：Vuetify（基于Material Design设计规范的Vue组件库，提供丰富的预制组件，如布局组件、表单组件、导航组件等，快速搭建美观且适配多端的界面）

- - 代码规范：ESLint（配合`unplugin-auto-import`插件配置自动导入规则，规范Vue、Vue Router等API的使用方式，避免冗余导入代码，提升代码一致性）

- - 路由管理：Vue Router（实现前端页面路由跳转，支持路由懒加载、路由守卫等特性，优化页面加载性能与权限控制）

- - 网络请求：Axios（封装HTTP请求，处理请求拦截、响应拦截、错误捕获等，实现与后端API的稳定交互）

## 代码分布

### 后端代码（`DgoShop/maven/shop/`）

- 核心业务：

- `src/main/java/com/example/shop/controller/`：控制器层，接收前端HTTP请求并返回响应结果。核心文件示例：
  - `OrderController.java`：处理订单相关请求，如订单列表分页查询（GET请求）、订单创建（POST请求）、订单详情查询（GET请求）等，返回JSON格式的业务数据；
  - `MerchantController.java`：负责商户相关操作，包含库存更新接口，通过`StockUpdateDTO`数据传输对象接收前端传入的库存变更参数，实现参数校验与业务逻辑解耦；
  - `UserController.java`：处理用户登录、注册、信息查询等请求，配合权限验证逻辑保障接口安全。

- `src/main/java/com/example/shop/mapper/`：数据访问层，定义数据库操作接口，通过MyBatis映射到数据库表。核心文件示例：
  - `OrderMainMapper.java`：定义订单主表的CRUD操作接口，如`selectById`（根据ID查询订单）、`insert`（插入订单数据）、`selectPage`（分页查询订单）等，接口方法与XML映射文件中的SQL语句一一对应；
  - `ProductMapper.java`：处理商品信息的数据库操作，支持商品列表查询、库存数量更新等功能。

- `src/main/java/com/example/shop/config/`：全局配置类目录，负责框架级配置。

- `src/main/java/com/example/shop/service/`：业务逻辑层。

- `src/main/java/com/example/shop/dto/`：数据传输对象目录（补充），用于前后端数据交互与参数校验。

- `src/main/resources/application.yml`：后端核心配置文件，配置MySQL数据库连接信息、Spring Boot服务端口（默认8080）、MyBatis mapper映射文件路径、日志级别等。

### 前端代码（`DgoShop/vuetify-project/`）

- 核心功能：

- `src/composables/`：Vue 3组合式函数目录，封装可复用的业务逻辑与状态，供多个页面组件调用。核心文件示例：
  - `useAppNavigation.js`：管理全局导航菜单状态（如当前激活菜单、菜单折叠/展开状态），提供路由跳转方法（如`toOrderList`、`toProductDetail`），实现导航逻辑的统一复用；
  - `useRequest.js`：封装Axios请求逻辑，提供GET/POST请求方法，统一处理请求加载状态、错误提示（如网络异常、接口返回错误信息），简化页面组件中的请求代码。

- `src/pages/`：页面组件目录，对应前端各个业务页面，与Vue Router路由规则关联。核心文件示例：
  - `index.vue`：项目首页组件，集成导航栏、轮播图、商品推荐列表等模块，通过调用`useRequest`加载首页数据；
  - `OrderList.vue`：订单列表页面，通过分页组件加载订单数据，支持订单状态筛选、详情查看等操作，调用`useAppNavigation`实现页面跳转；
  - `MerchantStock.vue`：商户库存管理页面，提供库存输入表单，提交数据到后端`MerchantController`的库存更新接口；
  - `Login.vue`：用户登录页面，包含账号密码输入框、登录按钮，验证通过后跳转到首页。

- 配置文件：
  - `vite.config.js`：Vite构建配置文件，配置项目基础路径、插件（Vuetify、Vue Router自动导入、ESLint）、开发服务器端口（默认3000）等；
  - `package.json`：前端项目依赖配置文件，声明Vue、Vuetify、Axios、ESLint等依赖包版本，定义开发（`dev`）、构建（`build`）等脚本命令。

- `src/components/`：公共UI组件目录
