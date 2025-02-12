import compareBundleImports from "./compareBundleImports.mjs";
import compareBundleSizes from "./compareBundleSizes.mjs";
import compareJavaImports from "./compareJavaImports.mjs";
import printHeader from "./printHeader.mjs";

const {argv} = process;

if (argv.length < 4) {
	console.error('Usage: compare.sh <left dir> <right dir> [--detailed]');
	process.exit(2);
}

const leftDir = argv[2];
const rightDir = argv[3];
const detailed = argv[4] === '--detailed';

printHeader('📦️ BUNDLE SIZES CHANGES');
compareBundleSizes(leftDir, rightDir, detailed);
console.log('');

printHeader('🔗 BUNDLE IMPORTS CHANGES');
compareBundleImports(leftDir, rightDir);
console.log('');

printHeader('☕️ JAVA IMPORTS CHANGES');
compareJavaImports(leftDir, rightDir);
console.log('');
