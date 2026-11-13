FROM liquibase/liquibase:latest
LABEL org.opencontainers.image.source=https://github.com/kestra-io/plugin-liquibase
LABEL org.opencontainers.image.description="Liquibase OSS image with common JDBC drivers pre-installed (Postgres, MySQL, MariaDB, MSSQL, DB2, Sybase, SQLite)."

# The base image ships an lpm built with an older Go, so replace it with a patched release.
ARG LPM_VERSION=0.3.8
ARG LPM_SHA256=2ad35eac660043579c97fb5dfe65a2c64ae0b32932b2deb6804b88bfd348b73e
ARG LPM_SHA256_ARM=07ae0027b1f51bcad939d451ebed5ba112335f2429274343ce99382a4494e8ae
USER root
RUN apt-get update && apt-get upgrade -y && apt-get install -y --no-install-recommends unzip && \
    arch="$(dpkg --print-architecture)" && \
    case "$arch" in \
    amd64) DOWNLOAD_ARCH="" ;; \
    arm64) DOWNLOAD_ARCH="-arm64" && LPM_SHA256=$LPM_SHA256_ARM ;; \
    *) echo >&2 "error: unsupported architecture '$arch'" && exit 1 ;; \
    esac && \
    wget -q -O /tmp/lpm.zip "https://github.com/liquibase/liquibase-package-manager/releases/download/v${LPM_VERSION}/lpm-${LPM_VERSION}-linux${DOWNLOAD_ARCH}.zip" && \
    echo "$LPM_SHA256 */tmp/lpm.zip" | sha256sum -c - && \
    unzip -o /tmp/lpm.zip -d /liquibase/bin/ && \
    rm /tmp/lpm.zip && \
    apt-get purge -y --auto-remove unzip && rm -rf /var/lib/apt/lists/* && \
    lpm --version
USER liquibase:liquibase

# we add common drivers with Liquibase Package Manager (LPM)
RUN lpm add postgresql --global \
    && lpm add mysql --global \
    && lpm add mariadb@3.5.11 --global \
    && lpm add mssql --global \
    && lpm add db2 --global \
    && lpm add sybase --global \
    && lpm add sqlite --global
