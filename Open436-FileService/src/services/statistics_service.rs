use anyhow::Result;
use serde::Serialize;
use sqlx::PgPool;

use crate::utils::path_generator::format_size;

#[derive(Debug, Serialize)]
pub struct StorageStatistics {
    pub total_files: i64,
    pub total_size: i64,
    pub total_size_pretty: String,
    pub by_status: StatusStatistics,
    pub by_type: TypeStatistics,
}

#[derive(Debug, Serialize)]
pub struct StatusStatistics {
    pub unused: FileStat,
    pub used: FileStat,
    pub deleted: FileStat,
}

#[derive(Debug, Serialize)]
pub struct TypeStatistics {
    pub avatar: FileStat,
    pub post: FileStat,
    pub reply: FileStat,
    pub section_icon: FileStat,
}

#[derive(Debug, Serialize)]
pub struct FileStat {
    pub count: i64,
    pub size: i64,
    pub size_pretty: String,
}

pub struct StatisticsService;

impl StatisticsService {
    /// 获取存储统计
    pub async fn get_statistics(pool: &PgPool) -> Result<StorageStatistics> {
        // 总统计
        let total = sqlx::query!(
            r#"
            SELECT 
                COUNT(*)::bigint as count,
                COALESCE(SUM(file_size), 0)::bigint as size
            FROM files
            WHERE status != 'deleted'
            "#
        )
        .fetch_one(pool)
        .await?;

        // 按状态统计
        let by_status_rows = sqlx::query!(
            r#"
            SELECT 
                status::TEXT as status,
                COUNT(*)::bigint as count,
                COALESCE(SUM(file_size), 0)::bigint as size
            FROM files
            GROUP BY status
            "#
        )
        .fetch_all(pool)
        .await?;

        let mut unused = FileStat {
            count: 0,
            size: 0,
            size_pretty: "0 bytes".to_string(),
        };
        let mut used = FileStat {
            count: 0,
            size: 0,
            size_pretty: "0 bytes".to_string(),
        };
        let mut deleted = FileStat {
            count: 0,
            size: 0,
            size_pretty: "0 bytes".to_string(),
        };

        for row in by_status_rows {
            let count = row.count.unwrap_or(0);
            let size = row.size.unwrap_or(0);
            let stat = FileStat {
                count,
                size,
                size_pretty: format_size(size),
            };

            match row.status.as_deref() {
                Some("unused") => unused = stat,
                Some("used") => used = stat,
                Some("deleted") => deleted = stat,
                _ => {}
            }
        }

        // 按类型统计
        let by_type_rows = sqlx::query!(
            r#"
            SELECT 
                file_type::TEXT as file_type,
                COUNT(*)::bigint as count,
                COALESCE(SUM(file_size), 0)::bigint as size
            FROM files
            WHERE status != 'deleted'
            GROUP BY file_type
            "#
        )
        .fetch_all(pool)
        .await?;

        let mut avatar = FileStat {
            count: 0,
            size: 0,
            size_pretty: "0 bytes".to_string(),
        };
        let mut post = FileStat {
            count: 0,
            size: 0,
            size_pretty: "0 bytes".to_string(),
        };
        let mut reply = FileStat {
            count: 0,
            size: 0,
            size_pretty: "0 bytes".to_string(),
        };
        let mut section_icon = FileStat {
            count: 0,
            size: 0,
            size_pretty: "0 bytes".to_string(),
        };

        for row in by_type_rows {
            let count = row.count.unwrap_or(0);
            let size = row.size.unwrap_or(0);
            let stat = FileStat {
                count,
                size,
                size_pretty: format_size(size),
            };

            match row.file_type.as_deref() {
                Some("avatar") => avatar = stat,
                Some("post") => post = stat,
                Some("reply") => reply = stat,
                Some("section_icon") => section_icon = stat,
                _ => {}
            }
        }

        Ok(StorageStatistics {
            total_files: total.count.unwrap_or(0),
            total_size: total.size.unwrap_or(0),
            total_size_pretty: format_size(total.size.unwrap_or(0)),
            by_status: StatusStatistics {
                unused,
                used,
                deleted,
            },
            by_type: TypeStatistics {
                avatar,
                post,
                reply,
                section_icon,
            },
        })
    }
}

