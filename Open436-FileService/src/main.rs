use actix_cors::Cors;
use actix_web::{middleware::Logger, web, App, HttpResponse, HttpServer};
use std::env;
use std::sync::Arc;
use tracing_subscriber;

mod config;
mod consul;
mod db;
mod handlers;
mod middleware;
mod models;
mod scheduler;
mod services;
mod storage;
mod utils;

use consul::{ConsulClient, HealthCheck, ServiceRegistration};

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    // 1. 加载环境变量
    dotenvy::dotenv().ok();

    // 2. 初始化日志
    tracing_subscriber::fmt::init();

    tracing::info!("Starting File Storage Service...");

    // 3. 加载配置
    let config = config::Config::from_env();
    tracing::info!(
        "Server configuration loaded: {}:{}",
        config.server.host,
        config.server.port
    );

    // 3.1 Consul 配置
    let consul_url = env::var("CONSUL_URL").unwrap_or_else(|_| "http://localhost:8500".to_string());
    let service_port: u16 = config.server.port;
    let service_host = config.server.host.clone();
    
    let consul_client = ConsulClient::new(consul_url);
    let service_id = format!("file-service-{}", uuid::Uuid::new_v4());

    // 3.2 注册服务到 Consul
    let registration = ServiceRegistration {
        id: service_id.clone(),
        name: "file-service".to_string(),
        port: service_port,
        address: service_host.clone(),
        check: HealthCheck {
            http: format!("http://{}:{}/health", service_host, service_port),
            interval: "10s".to_string(),
            timeout: "5s".to_string(),
        },
    };

    if let Err(e) = consul_client.register_service(registration).await {
        tracing::warn!("Failed to register service to Consul: {}", e);
    } else {
        tracing::info!("Service registered to Consul: {}", service_id);
    }

    // 4. 初始化数据库连接池
    let db_pool = db::create_pool(&config.database_url).await;
    tracing::info!("Database connection pool created");

    // 5. 初始化存储后端（Minio/S3）
    let storage = storage::create_storage(&config.s3).await;
    tracing::info!("Storage backend initialized: S3 (Minio)");

    // 6. 启动定时任务
    if let Err(e) = scheduler::start_cleanup_job(
        Arc::new(db_pool.clone()),
        storage.clone(),
        &config.cleanup,
    )
    .await
    {
        tracing::error!("Failed to start cleanup job: {}", e);
    }

    // 7. 准备共享数据
    let db_data = web::Data::new(db_pool);
    let storage_data = web::Data::new(storage);
    let cleanup_config_data = web::Data::new(config.cleanup.clone());

    // 8. 启动 HTTP 服务器
    tracing::info!(
        "Starting HTTP server at http://{}:{}",
        service_host,
        service_port
    );

    let consul_for_shutdown = consul_client.clone();
    let service_id_for_shutdown = service_id.clone();

    let server = HttpServer::new(move || {
        App::new()
            // 注入依赖
            .app_data(db_data.clone())
            .app_data(storage_data.clone())
            .app_data(cleanup_config_data.clone())
            // 设置请求体大小限制（10 MB）
            .app_data(web::PayloadConfig::new(10 * 1024 * 1024))
            // 中间件
            .wrap(Logger::default())
            .wrap(
                Cors::default()
                    .allow_any_origin()
                    .allowed_methods(vec!["GET", "POST", "PUT", "DELETE"])
                    .allowed_headers(vec![
                        actix_web::http::header::AUTHORIZATION,
                        actix_web::http::header::CONTENT_TYPE,
                    ])
                    .max_age(3600),
            )
            // 健康检查端点
            .route("/health", web::get().to(health_check))
            // API 路由
            .configure(handlers::configure_routes)
    })
    .bind((service_host.as_str(), service_port))?
    .run();

    // 9. 优雅关闭处理
    tokio::spawn(async move {
        tokio::signal::ctrl_c().await.ok();
        tracing::info!("Shutting down...");
        if let Err(e) = consul_for_shutdown.deregister_service(&service_id_for_shutdown).await {
            tracing::warn!("Failed to deregister service: {}", e);
        }
    });

    server.await
}

/// 健康检查端点
async fn health_check() -> HttpResponse {
    HttpResponse::Ok().json(serde_json::json!({
        "status": "healthy",
        "service": "file-service"
    }))
}
