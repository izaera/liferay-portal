/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {test as needPetTest} from '../../fixtures/needPet.fixture';

export const test = mergeTests(
	needPetTest('cat'),
);

test('get a cat pet', async ({
	_needPet,
}) => {
	console.log('======> got cat pet named:', _needPet);
});
