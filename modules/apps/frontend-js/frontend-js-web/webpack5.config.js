const path = require('path');
const {
	container: { ModuleFederationPlugin },
} = require('webpack');

module.exports = {
	entry: './src/main/resources/META-INF/resources/null.js',
	mode: 'development',
	devtool: 'source-map',
	module: {
		rules: [
			{
				test: /\.js$/,
				exclude: /node_modules/,
				use: {
					loader: 'babel-loader',
					options: {
						presets: ['@babel/preset-react']
					}
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
			'webpack',
		),
		publicPath: '/o/frontend-js-web/webpack/',
	},
	plugins: [
		new ModuleFederationPlugin({
			name: 'frontendJsWeb',
			filename: 'remoteEntry.js',
			exposes: {
				'.': './src/main/resources/META-INF/resources/test.index.es.js',
			}
		}),
	]
};
