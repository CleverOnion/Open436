#!/bin/bash
# 初始化数据库：首启时导入 M1 与 M7 的基础表结构与数据

set -euo pipefail

DB_NAME="${POSTGRES_DB:-open436}"
DB_USER="${POSTGRES_USER:-open436}"

echo "[db-init] Running migrations for database: ${DB_NAME}"

export PGPASSWORD="${POSTGRES_PASSWORD}"

psql -v ON_ERROR_STOP=1 -U "$DB_USER" -d "$DB_NAME" -c "SELECT 1;" >/dev/null

run_dir() {
  local dir="$1"
  if [ -d "$dir" ]; then
    echo "[db-init] Applying SQLs in $dir"
    # 以文件名排序执行，确保 V1/V2 与 001/002 顺序正确
    for f in $(ls -1 "$dir"/*.sql 2>/dev/null | sort); do
      echo "[db-init] -> $f"
      psql -v ON_ERROR_STOP=1 -U "$DB_USER" -d "$DB_NAME" -f "$f"
    done
  else
    echo "[db-init] Skip, directory not found: $dir"
  fi
}

run_dir "/docker-entrypoint-initdb.d/auth_migrations"
run_dir "/docker-entrypoint-initdb.d/file_migrations"

echo "[db-init] Migrations finished"


