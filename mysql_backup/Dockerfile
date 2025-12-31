# Dockerfile 核心配置
# 基于官方MySQL 8.0镜像（和本地MySQL版本尽量一致）
FROM mysql:8.0

# 设置环境变量：root密码（和本地MySQL一致，方便记忆）
ENV MYSQL_ROOT_PASSWORD=123456

# 可选：复制自定义配置文件（解决字符集/大小写问题）
COPY my.cnf /etc/mysql/conf.d/my.cnf

# 复制本地备份的SQL脚本到容器初始化目录（MySQL启动时自动执行）
# 关键：/docker-entrypoint-initdb.d 是MySQL官方指定的初始化脚本目录
COPY all-databases.sql /docker-entrypoint-initdb.d/

# 暴露MySQL默认端口（仅声明，不映射）
EXPOSE 3306