/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../../fixtures/apiHelpersTest';
import {isolatedSiteTest} from '../../../fixtures/isolatedSiteTest';
import {loginTest} from '../../../fixtures/loginTest';
import getRandomString from '../../../utils/getRandomString';
import getBasicWebContentStructureId from '../../../utils/structured-content/getBasicWebContentStructureId';
import {journalPagesTest} from '../../journal-web/main/fixtures/journalPagesTest';

export const test = mergeTests(
	apiHelpersTest,
	isolatedSiteTest,
	journalPagesTest,
	loginTest()
);

test(
	'Confirm that changing filters does not deselect selected items',
	{tag: '@LPS-172764'},
	async ({apiHelpers, journalPage, page, site}) => {
		const titles = [];

		for (let i = 0; i < 6; i++) {
			titles.push(getRandomString());
		}

		await test.step('Create 6 web content articles', async () => {
			const structuredContentId =
				await getBasicWebContentStructureId(apiHelpers);

			for (const title of titles) {
				await apiHelpers.jsonWebServicesJournal.addWebContent({
					ddmStructureId: structuredContentId,
					descriptionMap: {en_US: getRandomString()},
					groupId: site.id,
					titleMap: {en_US: title},
				});
			}
		});

		await test.step('Filter by recent', async () => {
			await journalPage.goto(site.friendlyUrlPath);

			await page.getByLabel('Filter', {exact: true}).click();
			await page.getByRole('menuitem', {name: 'Recent'}).click();
		});

		await test.step('Select one article per page', async () => {
			await page
				.locator(
					'[id="_com_liferay_journal_web_portlet_JournalPortlet_articles_1"]'
				)
				.locator('input[type=checkbox]')
				.click();
			await page
				.getByLabel('Pagination')
				.getByRole('link')
				.nth(2)
				.click();
			await page
				.locator(
					'[id="_com_liferay_journal_web_portlet_JournalPortlet_articles_1"]'
				)
				.locator('input[type=checkbox]')
				.click();
		});

		await test.step('Clear the filters', async () => {
			await page.getByLabel('Remove Recent Filter').click();
		});

		await test.step('Check that selections are kept', async () => {
			await expect(page.getByText('2 of 6 Items Selected')).toBeVisible();
		});
	}
);
