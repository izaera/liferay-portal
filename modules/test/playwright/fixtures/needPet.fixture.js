/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {ApiHelpers} from '../helpers/ApiHelpers';

exports.test = (petType) => 
	test.extend({
		_needPet: async ({page}, use) => {
			// This 👇 simulates creating a pet in the DB 
			let pet = {
				type: petType,
				name: petType == 'cat' ? 'Garfield' : 'Tony'
			};

			try {
				await use(pet);
			}
			finally {
				// Here we would clean up the pet from the DB
			}
		},
	});
