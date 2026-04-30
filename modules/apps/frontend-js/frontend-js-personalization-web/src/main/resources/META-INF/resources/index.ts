/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {clear, get, on, runDetection, runHandlers} from './implementation';

// JSON API

export interface Rules {
	segments: Segment[];
}

export interface Segment {
	combinator: Combinator;
	id: string;
	retention: Retention;
	rules: Rule[];
}

export type Combinator = 'and' | 'or';

export type Retention = 'PAGE' | 'SESSION';

export type Rule = LeafRule | RuleGroup;

export interface LeafRule {
	attr: Attribute;
	op: Operator;
	val: any;
}

export interface RuleGroup {
	combinator: Combinator;
	rules: Rule[];
}

export type Attribute =
	| 'browser_language'
	| 'browser_name'
	| 'browser_version'
	| `cookie:${string}`
	| 'hostname'
	| 'local_date'
	| 'local_hour'
	| 'pathname'
	| 'referrer'
	| `search_param:${string}`
	| 'url'
	| 'user_agent';
export type Operator = 'between' | 'eq' | 'include' | 'matches';

// JavaScript API

export interface Handler {
	name: string | undefined;
	(): Promise<void> | void;
}

export interface PersonalizationAPI {
	clear(retention?: Retention): void;
	get(): Set<string>;
	on(segmentId: string, handler: Handler): void;
	runDetection(rulesURL: string): Promise<void>;
	runHandlers(): Promise<void>;
}

export const personalization: PersonalizationAPI = {
	clear,
	get,
	on,
	runDetection,
	runHandlers,
};
