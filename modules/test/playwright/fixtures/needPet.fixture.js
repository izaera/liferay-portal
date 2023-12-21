/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {test} from '@playwright/test';

import {ApiHelpers} from '../helpers/ApiHelpers';

let petPool = {
	'dog': {
		type: 'dog',
		name: 'Tony',
		i: 0
	},
	'cat': {
		type: 'cat',
		name: 'Garfield',
		i: 0
	}
}

exports.test = (petType) => 
	test.extend({
		_needPet: async ({page}, use) => {
			// This 👇 simulates grabbing a precreated pet from the DB 
			let pet = petPool[petType];

			// Note that we could even create the pet before playwright is launched (pre-populated DB)
			// and here we would only retrieve it from the DB once per test run.
			//
			// Alternatively we could insert it here on the fly. The possibilities are infinite because
			// we are offloading the strategy to this file, instead of leaving it inside the test.

			try {
				await use(pet);
			}
			finally {
				// Here we would free up the pet from the pool (if necessary)

				// Note that for read-only objects we may simply share them across tests. For read-write
				// we would need to make sure we don't reuse the same object from the pool for two tests
				// but I believe we can do it easily since  the call to `use()` (which is what makes the
				// test run) is asynchronous, thus we can hold a test execution from the fixture for as 
				// long as we want until the pooled object is ready.
				
				// BTW, this can be used to synchronize tests too (think of tests that declare a 
				// dependency on a certain fixture and then that fixture makes them run serially) 😱.				
			}
		},
	});
