# Ubuntu 22.04（原生glibc，兼容所有Linux JRE，无需手动装glibc）
FROM ubuntu:22.04

# 关键：替换为阿里云Ubuntu国内镜像源（解决官方源502）
RUN sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list && \
    sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list && \
    # 仅安装必要依赖（bash Ubuntu默认已存在，可省略，此处保留仅为兼容）
    apt update -y && apt install -y --no-install-recommends bash && \
    # 清理缓存，减小镜像体积
    rm -rf /var/lib/apt/lists/*

# 复制你本地的Linux x64版本JRE 17（必须确认是Linux版本，非Windows）
COPY jre17 /usr/local/jre17

# 强制赋予java执行权限
RUN chmod +x /usr/local/jre17/bin/java

# 配置环境变量（确保java命令可被找到）
ENV JAVA_HOME=/usr/local/jre17
ENV PATH=${JAVA_HOME}/bin:${PATH}

# 验证java是否可执行（关键步骤，提前发现问题）
RUN java -version

# 设置工作目录
WORKDIR /app

# 复制Spring Boot Jar包
COPY target/shop-0.0.1-SNAPSHOT.jar /app/shop-backend.jar

# 暴露后端端口
EXPOSE 8080

# 启动命令（直接用java，Ubuntu原生glibc无需额外解释器）
ENTRYPOINT ["java", "-Xms256M", "-Xmx512M", "-jar", "/app/shop-backend.jar"]