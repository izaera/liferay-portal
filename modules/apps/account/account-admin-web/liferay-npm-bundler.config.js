/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

const defaults = require('liferay-npm-bundler/lib/liferay-npm-bundler.config.js');

module.exports = {
	...defaults,
	exports: {
		AccountEntriesManagementToolbarDefaultEventHandler:
			'./account_entries_admin/js/AccountEntriesManagementToolbarDefaultEventHandler.es.js',
		AccountOrganizationsManagementToolbarDefaultEventHandler:
			'./account_entries_admin/js/AccountOrganizationsManagementToolbarDefaultEventHandler.es.js',
		AccountRolesManagementToolbarDefaultEventHandler:
			'./account_entries_admin/js/AccountRolesManagementToolbarDefaultEventHandler.es.js',
		AccountUsersManagementToolbarDefaultEventHandler:
			'./account_entries_admin/js/AccountUsersManagementToolbarDefaultEventHandler.es.js',
		ManagementToolbarDefaultEventHandler:
			'./account_users_admin/js/ManagementToolbarDefaultEventHandler.es.js',
	},
	imports: {
		...defaults.imports,
	},
};
