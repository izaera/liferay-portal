const defaults = require('liferay-npm-scripts/src/config/liferay-npm-bundler.config.js');

module.exports = {
	...defaults,
	exports: {
	},
	imports: {
		...defaults.imports
	}
}


