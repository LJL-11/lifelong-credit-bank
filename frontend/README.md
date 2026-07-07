# Lifelong Credit Bank Frontend

这是独立的 Vue 3 + Vite 前端工程。

## 启动顺序

1. 先启动 Spring Boot 后端，默认端口是 `8080`。
2. 再进入本目录启动前端：

```powershell
cd frontend
npm install
npm run dev
```

前端地址：

```text
http://localhost:5173/
```

后端接口地址：

```text
http://localhost:8080/api
```

Vite 已在 `vite.config.js` 中配置代理，前端请求 `/api` 会自动转发到 `http://localhost:8080`。

## 打包

```powershell
npm run build
```

打包产物会生成到：

```text
frontend/dist/
```
