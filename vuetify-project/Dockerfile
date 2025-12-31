# 基础镜像：Alpine Nginx（和你的vuetify-web容器一致）
FROM nginx:alpine


# 1. 删除Nginx默认配置
RUN rm -rf /etc/nginx/conf.d/default.conf

# 2. 拷贝本地修改后的Nginx配置文件到容器
COPY ./nginx/default.conf /etc/nginx/conf.d/

# 3. 拷贝本地打包后的dist目录到Nginx静态文件目录
COPY ./dist/ /usr/share/nginx/html/

# 4. 暴露容器80端口（和Nginx监听端口一致）
EXPOSE 80

# 启动Nginx（Alpine镜像默认启动命令，可省略）
CMD ["nginx", "-g", "daemon off;"]