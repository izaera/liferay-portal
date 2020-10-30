const path = require('path');
const {
	container: { ModuleFederationPlugin },
} = require('webpack');

module.exports = {
	entry: './src/main/resources/META-INF/resources/wiki/js/fetchThing.es.js',
	mode: 'development',
	devtool: 'source-map',
	module: {
		rules: [
			{
				test: /\.js$/,
				exclude: /node_modules/,
				use: {
					loader: 'babel-loader',
				},
			},
		],
	},
	output: {
		path: path.resolve(
			__dirname,
			'build',
			'node',
			'packageRunBuild',
			'resources',
			'js',
		),
		publicPath: '/o/wiki-web/js/',
	},
	plugins: [
		new ModuleFederationPlugin({
			name: 'wiki_web',
			filename: 'remoteEntry.js',
			exposes: {
				'./fetchThing': './src/main/resources/META-INF/resources/wiki/js/fetchThing.es.js',
			}
		}),
	]
};
