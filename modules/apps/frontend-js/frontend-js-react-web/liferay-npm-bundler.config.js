module.exports = {
	'create-jar': false,
	exports: {
		classnames: 'classnames',
		react: 'react',
		react_dom: 'react-dom',
		prop_types: 'prop-types'
	},
	imports: {
		"frontend-taglib-clay": {
			"@clayui/icon": ">=3.0.0-alpha.1"
		}
	},
	source: 'src/main/resources/META-INF/resources',
	output: 'build/node/packageRunBuild/resources',
	webpack: {
		module: {
			rules: [
				{
					test: /\.js$/,
					exclude: /node_modules/,
					use: ['babel-loader']
				}
			]
		},
	},
	workdir: 'build/node/bundler',
	"log-level": "debug"
}
