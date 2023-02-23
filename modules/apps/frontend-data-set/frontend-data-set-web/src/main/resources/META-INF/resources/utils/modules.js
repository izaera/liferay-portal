/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import React from 'react';

const moduleURLPromises = {};

export function getLiferayJsModule(moduleURL) {
	if (moduleURL.includes('|')) {
		if (!moduleURLPromises[moduleURL]) {
			moduleURLPromises[moduleURL] = new Promise((resolve, reject) => {
				// GreenAppleFDSCellRenderer|/o/ce-sample/index.js
				const parts = moduleURL.split('|');

				// TODO: We may want to create a npm-scripts webpack plugin to add /*webpackIgnore: true*/ to every dynamic import
				import(/*webpackIgnore: true*/ parts[1])
					.then((module) => {
						let renderer = module[parts[0]];

						resolve((props) => {
							return React.createElement(
								class extends React.Component {
									constructor(props) {
										super(props);

										this.props = props;
										this.ref = React.createRef();
									}

									render() {
										// Sandbox passed-in args
										const {value} = this.props;

										setTimeout(
											() => {
												renderer({
													element: this.ref.current,
													value
												});
											},
											0
										);

										return <div ref={this.ref}></div>;
									}
								},
								props
							);
						});
					})
					.catch((err) => {
						console.error(err);

						resolve(() => (
							<div className="custom-component">
								component load failed
							</div>
						));

						// this enters an infinite loop -> reject(err);
					});
			});
		}

		return moduleURLPromises[moduleURL];
	}
	else {
		return new Promise((resolve, reject) => {
				Liferay.Loader.require(
					moduleURL,
					(jsModule) => resolve(jsModule.default || jsModule),
					(error) => reject(error)
				);
		});
	}
}

export function getFakeJsModule() {
	return new Promise((resolve) => {
		setTimeout(
			() =>
				resolve(() => (
					<div className="custom-component">
						fakely fetched component
					</div>
				)),
			3000
		);
	});
}

export const getJsModule = Liferay.Loader?.require
	? getLiferayJsModule
	: getFakeJsModule;

export const fetchedJsModules = [];

export function getComponentByModuleURL(url) {
	return new Promise((resolve, reject) => {
		const foundModule = fetchedJsModules.find((cr) => cr.url === url);
		if (foundModule) {
			resolve(foundModule.component);
		}

		return getJsModule(url)
			.then((fetchedComponent) => {
				fetchedJsModules.push({
					component: fetchedComponent,
					url,
				});

				return resolve(fetchedComponent);
			})
			.catch(reject);
	});
}
