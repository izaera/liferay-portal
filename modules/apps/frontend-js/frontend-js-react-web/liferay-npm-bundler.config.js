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
		classnames: 'classnames',
		formik: 'formik',
		formik_dist_index: 'formik/dist/index.js',
		prop_types: 'prop-types',
		react: 'react',
		react_dnd: 'react-dnd',
		react_dnd_dist_cjs_index: 'react-dnd/dist/cjs/index.js',
		react_dnd_html5_backend: 'react-dnd-html5-backend',
		react_dnd_html5_backend_dist_cjs_index:
			'react-dnd-html5-backend/dist/cjs/index.js',
		react_dom: 'react-dom',
	},
	imports: {
		'frontend-taglib-clay': {
			'@clayui/icon': '>=3.0.0-alpha.1',
		},
	},
};
