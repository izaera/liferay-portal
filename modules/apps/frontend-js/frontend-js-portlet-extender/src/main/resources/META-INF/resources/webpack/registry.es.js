class Registry {
	constructor() {
		this._portlets = {};
	}

	get registeredPortlets() {
		return Object.values(this._portlets).filter(
			portlet => portlet.main !== undefined
		);
	}

	launchPortlet(portletId, args) {
		const portlet = this._getPortlet(portletId);

		if (portlet.main) {
			portlet.main(args);
		} else {
			portlet.pendingLaunchs.push({
				args
			});
		}
	}

	registerPortlet(portletId, main) {
		const portlet = this._getPortlet(portletId);

		if (portlet.main !== undefined) {
			throw new Error(
				'\n' +
					`Portlet ${portletId} is already registered.\n` +
					'\n' +
					'This can be caused because you have placed more than one\n' +
					'webpack based portlet in the same page or because some\n' +
					'portlet directly exports webpack code (i.e.: it sets the\n' +
					'window.webpackJsonp variable).\n'
			);
		}

		portlet.main = main;

		portlet.pendingLaunchs.forEach(launch => {
			portlet.main(launch.args);
		});
	}

	_getPortlet(portletId) {
		let portlet = this._portlets[portletId];

		if (!portlet) {
			this._portlets[portletId] = portlet = {
				id: portletId,
				main: undefined,
				pendingLaunchs: []
			};
		}

		return portlet;
	}
}

const registry = new Registry();

export default registry;
