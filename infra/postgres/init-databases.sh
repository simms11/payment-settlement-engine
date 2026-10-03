#!/usr/bin/env bash
# Runs once, the first time the container starts with an empty data directory
# (anything in /docker-entrypoint-initdb.d is skipped on later starts).
#
# One PostgreSQL instance hosts three databases, one per service. Each service gets its own
# login role that owns its database, and PUBLIC loses CONNECT, so the gateway role cannot
# even open a connection to the ledger database. "No shared databases" is enforced by
# Postgres, not by convention.
#
# Debezium gets a separate role with REPLICATION (to stream the WAL) and CONNECT on every
# database. Table-level SELECT on each outbox is granted by the services' own migrations.
set -euo pipefail

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
    -v debezium_password="$DEBEZIUM_DB_PASSWORD" <<'EOSQL'
CREATE ROLE debezium WITH LOGIN REPLICATION PASSWORD :'debezium_password';
EOSQL

create_service_database() {
    local database=$1 owner=$2 password=$3

    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
        -v database="$database" -v owner="$owner" -v password="$password" <<'EOSQL'
CREATE ROLE :"owner" WITH LOGIN PASSWORD :'password';
CREATE DATABASE :"database" OWNER :"owner";
REVOKE ALL ON DATABASE :"database" FROM PUBLIC;
GRANT CONNECT ON DATABASE :"database" TO debezium;
EOSQL
}

create_service_database payment_gateway    gateway    "$GATEWAY_DB_PASSWORD"
create_service_database payment_compliance compliance "$COMPLIANCE_DB_PASSWORD"
create_service_database payment_ledger     ledger     "$LEDGER_DB_PASSWORD"
