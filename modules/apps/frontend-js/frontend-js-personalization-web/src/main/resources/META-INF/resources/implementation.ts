/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Detection} from './detection';
import {log} from './log';
import {store} from './store';

import type {Handler, Retention, Rules} from './index';

interface HandlersMap {
	[segmentId: string]: Handler[];
}

log('Loading Liferay Audiences API v0.1.0...');

const handlers: HandlersMap = {};

export function clear(retention?: Retention): void {
	store.clear(retention);
}

export function get(): Set<string> {
	return store.getSegmentIds();
}

export async function runDetection(rulesURL: string): Promise<void> {

	// eslint-disable-next-line @liferay/portal/no-global-fetch
	const result = await fetch(rulesURL);

	const rules: Rules = await result.json();

	const detection = new Detection(rules);

	const matches = await detection.run();

	const pageSegmentIds = store.getPageSegmentIds();
	const sessionSegmentIds = store.getSessionSegmentIds();

	for (const match of matches) {
		switch (match.retention) {
			case 'PAGE': {
				pageSegmentIds.add(match.id);
				break;
			}

			case 'SESSION': {
				sessionSegmentIds.add(match.id);
				break;
			}

			default: {
				throw new Error(`Unsupported retention: ${match.retention}`);
			}
		}
	}

	store.setPageSegmentIds(pageSegmentIds);
	store.setSessionSegmentIds(sessionSegmentIds);
}

export function on(segmentId: string, handler: Handler): void {
	log(
		`Adding handler '${handler.name ?? 'anonymous'}' for segment '${segmentId}'`
	);

	if (!handlers[segmentId]) {
		handlers[segmentId] = [];
	}

	handlers[segmentId].push(handler);
}

export async function runHandlers(): Promise<void> {
	const segmentIds = get();

	for (const segmentId of segmentIds) {
		if (!handlers[segmentId]) {
			continue;
		}

		for (const handler of handlers[segmentId]) {
			log(
				`Running handler '${handler.name ?? 'anonymous'}' for segment '${segmentId}'`
			);

			await handler.apply(handler);
		}
	}
}
