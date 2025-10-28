pub mod batch;
pub mod cleanup;
pub mod delete;
pub mod health;
pub mod info;
pub mod statistics;
pub mod upload;
pub mod usage;

use actix_web::web;

/// 配置所有路由
pub fn configure_routes(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::scope("/api/files")
            .route("/upload", web::post().to(upload::upload_handler))
            .route("/{id}", web::get().to(info::get_file_info_handler))
            .route("/{id}/url", web::get().to(info::get_file_url_handler))
            .route("/{id}/mark-used", web::post().to(usage::mark_used_handler))
            .route(
                "/{id}/mark-unused",
                web::post().to(usage::mark_unused_handler),
            )
            .route("/{id}", web::delete().to(delete::delete_file_handler))
            .route("/batch-info", web::post().to(batch::batch_info_handler))
            .route("/statistics", web::get().to(statistics::statistics_handler))
            .route("/cleanup", web::post().to(cleanup::cleanup_handler)),
    )
    .route("/health", web::get().to(health::health_handler));
}

