const amdModule = await new Promise((resolve, reject) => {
	Liferay.Loader.require(
		'liferay!frontend-js-react-web$react-dom@16.12.0/index',
		resolve,
		reject
	);
});

const {
	createPortal, findDOMNode, flushSync, hydrate, render,
	unmountComponentAtNode, unstable_batchedUpdates, unstable_createPortal,
	unstable_renderSubtreeIntoContainer,
	__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED,
} = amdModule;

export {
	createPortal, findDOMNode, flushSync, hydrate, render,
	unmountComponentAtNode, unstable_batchedUpdates, unstable_createPortal,
	unstable_renderSubtreeIntoContainer,
	__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED,
};

export default amdModule;