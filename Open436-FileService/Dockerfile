FROM rust:1.70 as builder

WORKDIR /app

# 复制依赖文件
COPY Cargo.toml Cargo.lock ./

# 复制源代码
COPY src ./src
COPY migrations ./migrations

# 构建 Release 版本
RUN cargo build --release

# 运行时镜像
FROM debian:bookworm-slim

# 安装运行时依赖
RUN apt-get update && apt-get install -y \
    libssl3 \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# 从构建阶段复制二进制文件
COPY --from=builder /app/target/release/file-service /usr/local/bin/file-service

# 暴露端口
EXPOSE 8007

# 启动服务
CMD ["file-service"]

