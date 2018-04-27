var fs = require('fs');
var pkgJson = require('./classes/META-INF/resources/node_modules/frontend-js-metal-web$metal-uri@2.4.0/package.json');

pkgJson.main = pkgJson.module;

fs.writeFileSync('./classes/META-INF/resources/node_modules/frontend-js-metal-web$metal-uri@2.4.0/package.json', JSON.stringify(pkgJson,null,2));