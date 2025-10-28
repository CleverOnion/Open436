use actix_cors::Cors;
use actix_web::{middleware::Logger, web, App, HttpServer};
use std::sync::Arc;
use tracing_subscriber;

mod config;
mod db;
mod handlers;
mod middleware;
mod models;
mod scheduler;
mod services;
mod storage;
mod utils;

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

    let server_host = config.server.host.clone();
    let server_port = config.server.port;

    // 8. 启动 HTTP 服务器
    tracing::info!(
        "Starting HTTP server at http://{}:{}",
        server_host,
        server_port
    );

    HttpServer::new(move || {
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
            // API 路由
            .configure(handlers::configure_routes)
    })
    .bind((server_host, server_port))?
    .run()
    .await
}
