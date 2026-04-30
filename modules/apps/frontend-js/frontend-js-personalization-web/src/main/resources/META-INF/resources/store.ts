/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Retention} from './index';
import {log} from './log';

const SESSION_STORAGE_KEY = 'com.liferay.segments';
const SESSION_SEGMENTS_SEPARATOR = '|';

class Store {
	private pageSegmentIds: Set<string> = new Set();

	clear(retention?: Retention): void {
		log(`Clearing segments for scope: ${retention ?? 'ALL'}`);

		switch (retention) {
			case 'PAGE':
				this.pageSegmentIds = new Set();
				break;

			case 'SESSION':
				localStorage.removeItem(SESSION_STORAGE_KEY);
				break;

			default:
				this.pageSegmentIds = new Set();
				localStorage.removeItem(SESSION_STORAGE_KEY);
				break;
		}
	}

	getSegmentIds(): Set<string> {
		const set: Set<string> = new Set();

		for (const segmentId of this.getSessionSegmentIds()) {
			set.add(segmentId);
		}

		for (const segmentId of this.getPageSegmentIds()) {
			set.add(segmentId);
		}

		return set;
	}

	getPageSegmentIds(): Set<string> {
		return this.pageSegmentIds;
	}

	getSessionSegmentIds(): Set<string> {
		const sessionItem = localStorage.getItem(SESSION_STORAGE_KEY);

		if (!sessionItem) {
			return new Set();
		}

		return new Set(sessionItem.split(SESSION_SEGMENTS_SEPARATOR));
	}

	setPageSegmentIds(segmentIds: Set<string>) {
		this.pageSegmentIds = segmentIds;
	}

	setSessionSegmentIds(segmentIds: Set<string>) {
		localStorage.setItem(
			SESSION_STORAGE_KEY,
			[...segmentIds].join(SESSION_SEGMENTS_SEPARATOR)
		);
	}
}

export const store = new Store();
