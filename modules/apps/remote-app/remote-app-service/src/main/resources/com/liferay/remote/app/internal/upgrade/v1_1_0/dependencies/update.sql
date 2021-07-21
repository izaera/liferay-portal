-- noinspection SqlDialectInspectionForFile

-- noinspection SqlNoDataSourceInspectionForFile

alter table RemoteAppEntry add type_ VARCHAR(75) NULL;

COMMIT_TRANSACTION;