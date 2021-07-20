(function() {
	fetch("[$URL$]")
		.then(response => response.text())
		.then((source) => {
			Liferay.Loader.define(
				'[$PACKAGE$]/[$MODULE$]',
				['module'],
				function (module) {
					eval(source.replace('export default', 'module.exports='));
				}
			);
		});
})();