create index IX_CA514799 on ClientExtensionEntry (companyId, externalReferenceCode[$COLUMN_LENGTH:75$]);
create unique index IX_7D891292 on ClientExtensionEntry (companyId, name[$COLUMN_LENGTH:75$], type_[$COLUMN_LENGTH:75$]);
create index IX_32C1FC31 on ClientExtensionEntry (companyId, type_[$COLUMN_LENGTH:75$]);
create index IX_F8DF9578 on ClientExtensionEntry (uuid_[$COLUMN_LENGTH:75$], companyId);