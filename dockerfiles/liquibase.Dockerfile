FROM liquibase/liquibase:latest
LABEL org.opencontainers.image.source=https://github.com/kestra-io/plugin-liquibase
LABEL org.opencontainers.image.description="Liquibase OSS image with common JDBC drivers pre-installed (Postgres, MySQL, MariaDB, MSSQL, DB2, Sybase, SQLite)."

# we add common drivers with Liquibase Package Manager (LPM)
RUN lpm add postgresql --global \
    && lpm add mysql --global \
    && lpm add mariadb --global \
    && lpm add mssql --global \
    && lpm add db2 --global \
    && lpm add sybase --global \
    && lpm add sqlite --global
