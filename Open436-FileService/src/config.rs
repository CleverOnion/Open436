use std::env;

#[derive(Debug, Clone)]
pub struct Config {
    pub server: ServerConfig,
    pub database_url: String,
    pub s3: S3Config,
    pub kong_gateway_url: String,
    pub file_limits: FileLimits,
    pub cleanup: CleanupConfig,
}

#[derive(Debug, Clone)]
pub struct ServerConfig {
    pub host: String,
    pub port: u16,
}

#[derive(Debug, Clone)]
pub struct S3Config {
    pub endpoint: Option<String>,
    pub region: String,
    pub bucket: String,
    pub access_key: String,
    pub secret_key: String,
    pub public_url: String,
    pub path_style: bool,
}

#[derive(Debug, Clone)]
pub struct FileLimits {
    pub avatar: usize,
    pub post: usize,
    pub reply: usize,
    pub section_icon: usize,
}

#[derive(Debug, Clone)]
pub struct CleanupConfig {
    pub enabled: bool,
    pub cron_expression: String,
    pub days_threshold: i32,
    pub batch_size: usize,
}

impl Config {
    pub fn from_env() -> Self {
        dotenvy::dotenv().ok();

        let server = ServerConfig {
            host: env::var("SERVER_HOST").unwrap_or_else(|_| "0.0.0.0".to_string()),
            port: env::var("SERVER_PORT")
                .unwrap_or_else(|_| "8007".to_string())
                .parse()
                .expect("SERVER_PORT must be a number"),
        };

        let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");

        let s3 = S3Config {
            endpoint: env::var("S3_ENDPOINT").ok(),
            region: env::var("S3_REGION").unwrap_or_else(|_| "us-east-1".to_string()),
            bucket: env::var("S3_BUCKET").expect("S3_BUCKET must be set"),
            access_key: env::var("S3_ACCESS_KEY").expect("S3_ACCESS_KEY must be set"),
            secret_key: env::var("S3_SECRET_KEY").expect("S3_SECRET_KEY must be set"),
            public_url: env::var("S3_PUBLIC_URL").expect("S3_PUBLIC_URL must be set"),
            path_style: env::var("S3_PATH_STYLE")
                .unwrap_or_else(|_| "true".to_string())
                .parse()
                .unwrap_or(true),
        };

        let kong_gateway_url =
            env::var("KONG_GATEWAY_URL").unwrap_or_else(|_| "http://localhost:8000".to_string());

        let file_limits = FileLimits {
            avatar: env::var("MAX_FILE_SIZE_AVATAR")
                .unwrap_or_else(|_| "2097152".to_string())
                .parse()
                .unwrap_or(2_097_152),
            post: env::var("MAX_FILE_SIZE_POST")
                .unwrap_or_else(|_| "5242880".to_string())
                .parse()
                .unwrap_or(5_242_880),
            reply: env::var("MAX_FILE_SIZE_REPLY")
                .unwrap_or_else(|_| "5242880".to_string())
                .parse()
                .unwrap_or(5_242_880),
            section_icon: env::var("MAX_FILE_SIZE_SECTION_ICON")
                .unwrap_or_else(|_| "512000".to_string())
                .parse()
                .unwrap_or(512_000),
        };

        let cleanup = CleanupConfig {
            enabled: env::var("CLEANUP_ENABLED")
                .unwrap_or_else(|_| "true".to_string())
                .parse()
                .unwrap_or(true),
            cron_expression: env::var("CLEANUP_CRON")
                .unwrap_or_else(|_| "0 0 2 * * *".to_string()),
            days_threshold: env::var("CLEANUP_DAYS_THRESHOLD")
                .unwrap_or_else(|_| "30".to_string())
                .parse()
                .unwrap_or(30),
            batch_size: env::var("CLEANUP_BATCH_SIZE")
                .unwrap_or_else(|_| "100".to_string())
                .parse()
                .unwrap_or(100),
        };

        Self {
            server,
            database_url,
            s3,
            kong_gateway_url,
            file_limits,
            cleanup,
        }
    }
}
