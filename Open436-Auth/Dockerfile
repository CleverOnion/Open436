FROM openjdk:21-slim

WORKDIR /app

# 复制 JAR 文件
COPY target/Open436-Auth-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8001

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]

