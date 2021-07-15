(function() {
	const define = function(name, dependencies, factory) {
		Liferay.Loader.define('[$PACKAGE$]/[$MODULE$]', ['module'], function (module) {
			module.exports = factory();
		});
	}

	define.amd = true;

	fetch("[$URL$]")
		.then(response => response.text())
		.then(text => eval(text));
})();