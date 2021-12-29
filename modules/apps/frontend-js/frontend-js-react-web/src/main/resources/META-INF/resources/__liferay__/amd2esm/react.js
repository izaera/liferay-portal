const amdModule = await new Promise((resolve, reject) => {
	Liferay.Loader.require(
		'liferay!frontend-js-react-web$react@16.12.0/index',
		resolve,
		reject
	);
});

const {
	Children, Component, Fragment, Profiler, PureComponent, StrictMode,
	Suspense, cloneElement, createContext, createElement, createFactory,
	createRef, forwardRef, isValidElement, lazy, memo, useCallback, useContext,
	useDebugValue, useEffect, useImperativeHandle, useLayoutEffect, useMemo,
	useReducer, useRef, useState, version,
	__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED,
} = amdModule;

export {
	Children, Component, Fragment, Profiler, PureComponent, StrictMode,
	Suspense, cloneElement, createContext, createElement, createFactory,
	createRef, forwardRef, isValidElement, lazy, memo, useCallback, useContext,
	useDebugValue, useEffect, useImperativeHandle, useLayoutEffect, useMemo,
	useReducer, useRef, useState, version,
	__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED,
};

export default amdModule;