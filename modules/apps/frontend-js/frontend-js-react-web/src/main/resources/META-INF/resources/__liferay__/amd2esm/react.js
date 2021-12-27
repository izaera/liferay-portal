export default await new Promise((resolve, reject) => {
	Liferay.Loader.require(
		'liferay!frontend-js-react-web$react@16.12.0/index',
		resolve,
		reject
	);
});
