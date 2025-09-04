/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';
import {createReadStream} from 'fs';
import moment from 'moment';
import path from 'path';

import {accountSettingsPagesTest} from '../../fixtures/accountSettingsPagesTest';
import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {changeTrackingPagesTest} from '../../fixtures/changeTrackingPagesTest';
import {dataApiHelpersTest} from '../../fixtures/dataApiHelpersTest';
import {pageEditorPagesTest} from '../../fixtures/pageEditorPagesTest';
import {workflowPagesTest} from '../../fixtures/workflowPagesTest';
import {featureFlagsTest} from '../../fixtures/featureFlagsTest';
import getRandomString from '../../utils/getRandomString';
import {PORTLET_URLS} from '../../utils/portletUrls';

export const test = mergeTests(
	accountSettingsPagesTest,
	apiHelpersTest,
	changeTrackingPagesTest,
	dataApiHelpersTest,
	pageEditorPagesTest,
	workflowPagesTest,
	featureFlagsTest({
		'LPD-20131': {enabled: true},
	})
);

test('LPD-28276 Assert tag data persists in parent tab', async ({
	changeTrackingPage,
	ctCollection,
	page,
}) => {
	await page.goto(`/group/guest${PORTLET_URLS.tagsAdmin}`);

	await page.getByRole('link', {name: 'Add Tag'}).click();

	const tagName = getRandomString();

	await page.getByPlaceholder('Name').fill(tagName);

	await page.getByRole('button', {name: 'Save'}).click();

	await changeTrackingPage.goToReviewChanges(ctCollection.body.name);

	await changeTrackingPage.reviewChange(tagName);

	await changeTrackingPage.selectTab('Parents');

	await page.waitForTimeout(3000);

	await expect(page.getByText('Guest', {exact: true})).toBeVisible();
});

test('LPD-29088 Assert Publication Overview panel empty', async ({
	changeTrackingPage,
	ctCollection,
	page,
}) => {
	await changeTrackingPage.goToReviewChanges(ctCollection.body.name);

	const publicationOverviewPanel = page.getByRole('button', {
		name: 'Publication Overview',
	});

	await expect(publicationOverviewPanel).toBeVisible();
	await expect(page.getByText('No changes were found.')).toBeVisible();

	await publicationOverviewPanel.click();
	await expect(page.getByText('No changes were found.')).not.toBeVisible();
});

test('LPD-29088 Assert Publication Overview panel is visible', async ({
	apiHelpers,
	changeTrackingPage,
	ctCollection,
	page,
}) => {
	await changeTrackingPage.workOnProduction();

	const site1 = await apiHelpers.headlessSite.createSite({
		name: getRandomString(),
	});
	apiHelpers.data.push({id: site1.id, type: 'site'});

	const site2 = await apiHelpers.headlessSite.createSite({
		name: getRandomString(),
	});
	apiHelpers.data.push({id: site2.id, type: 'site'});

	await changeTrackingPage.workOnPublication(ctCollection);

	await page.goto(`/group/guest${PORTLET_URLS.tagsAdmin}`);
	await page.getByRole('link', {name: 'Add Tag'}).click();
	await page.getByPlaceholder('Name').fill(getRandomString());
	await page.getByRole('button', {name: 'Save'}).click();

	await apiHelpers.headlessDelivery.postMessageBoardThread({
		articleBody: getRandomString(),
		headline: getRandomString(),
		siteId: site1.id,
	});

	for (let i = 0; i < 3; i++) {
		await apiHelpers.headlessDelivery.postDocument(
			site1.id,
			createReadStream(
				path.join(__dirname, '/dependencies/attachment.txt')
			)
		);

		await apiHelpers.headlessDelivery.postBlog(site2.id);
	}

	await changeTrackingPage.goToReviewChanges(ctCollection.body.name);

	await expect(page.getByText('Liferay DXP (1): Tag (1)')).toBeVisible();
	await expect(
		page.getByText(
			site1.name +
				' (5): Document (3) Message Boards Message (1) Message Boards Thread (1)'
		)
	).toBeVisible();
	await expect(
		page.getByText(site2.name + ' (3):  Blogs Entry (3)')
	).toBeVisible();

	await apiHelpers.headlessChangeTracking.publishCTCollection(
		ctCollection.body.id
	);

	await changeTrackingPage.goToReviewChangesHistory(ctCollection.body.name);

	await expect(page.getByText('Liferay DXP (1): Tag (1)')).toBeVisible();
	await expect(
		page.getByText(
			site1.name +
				' (5): Document (3) Message Boards Message (1) Message Boards Thread (1)'
		)
	).toBeVisible();
	await expect(
		page.getByText(site2.name + ' (3):   Blogs Entry (3)')
	).toBeVisible();
});

test('LPD-29089 Assert Publication Overview filter', async ({
	apiHelpers,
	changeTrackingPage,
	ctCollection,
	page,
}) => {
	const site =
		await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath('guest');

	await apiHelpers.headlessDelivery.postBlog(site.id);

	await apiHelpers.headlessDelivery.postWikiNode(site.id);

	await changeTrackingPage.goToReviewChanges(ctCollection.body.name);

	await page.getByRole('link', {name: 'Blogs Entry (1)'}).click();

	await expect(
		changeTrackingPage.frontendDataSetEntries.getByText('Blogs Entry')
	).toBeVisible();

	await expect(
		changeTrackingPage.frontendDataSetEntries.getByText('Wiki Node')
	).toBeHidden();
});

test('LPD-47743 Assert Publication Score is visible', async ({
	apiHelpers,
	changeTrackingPage,
	ctCollection,
	page,
}) => {
	await changeTrackingPage.workOnPublication(ctCollection);

	const site =
		await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath('guest');

	for (let i = 0; i < 5; i++) {
		await apiHelpers.headlessDelivery.postDocument(
			site.id,
			createReadStream(
				path.join(__dirname, '/dependencies/attachment.txt')
			)
		);
	}

	await changeTrackingPage.goToReviewChanges(ctCollection.body.name);

	await expect(page.getByText('Publication Size:')).toBeVisible();
});

test('LPD-45769 Assert Publication Score description is visible', async ({
	apiHelpers,
	changeTrackingPage,
	ctCollection,
	page,
}) => {
	await changeTrackingPage.workOnPublication(ctCollection);

	const site =
		await apiHelpers.headlessAdminUser.getSiteByFriendlyUrlPath('guest');

	for (let i = 0; i < 5; i++) {
		await apiHelpers.headlessDelivery.postDocument(
			site.id,
			createReadStream(
				path.join(__dirname, '/dependencies/attachment.txt')
			)
		);
	}

	await changeTrackingPage.goToReviewChanges(ctCollection.body.name);

	const publicationSize = page.getByText('Publication Size:');
	await expect(publicationSize).toBeVisible();

	await publicationSize.hover();

	const publicationSizeDescription = page.getByText(
		'The size classification considers both the number of changes and the database size. Please allocate time for the publishing process accordingly.'
	);
	await expect(publicationSizeDescription).toBeVisible();
});

test('User time zone from theme display is applied to publication FDS', async ({
	accountSettingsPage,
	changeTrackingPage,
	page,
}) => {
	await test.step('Check date in different time zone', async () => {
		await accountSettingsPage.goToDisplaySettings();

		await accountSettingsPage.setTimeZone('Europe/Lisbon');

		await changeTrackingPage.goto();

		const utcTime = moment.utc();

		// Add 1 hour offset to the UTC time

		const timeZoneTime = utcTime.add(1, 'hours');

		await expect(
			page
				.getByText(timeZoneTime.format('MMM D, YYYY, h'))
				.first()
		).toBeVisible();
	});

	await test.step('Revert to default UTC time zone', async () => {
		await accountSettingsPage.goToDisplaySettings();

		await accountSettingsPage.setTimeZone('UTC');
	});
});

test('LPD-62112 Cannot Preview Pending Version of Page in a Publication', async ({
	changeTrackingPage,
	ctCollection,
	page,
	pageEditorPage,
	workflowPage,
}) => {
	// Enable Single Approver workflow for Content Pages
	await changeTrackingPage.workOnProduction();

	await workflowPage.goto();

	await workflowPage.changeWorkflow('Content Page', 'Single Approver');

	await changeTrackingPage.workOnPublication(ctCollection);

	await test.step('Go to home edit page', async () => {
		await page.goto(`/web/guest/home?p_l_mode=edit`);
	});

	const headingId = await pageEditorPage.getFragmentId('Paragraph');

	await pageEditorPage.editTextEditable(headingId, 'element-text', 'Edited');

	await pageEditorPage.publishPage();

	await changeTrackingPage.goToReviewChanges(ctCollection.body.name);

	const filtersDropdown = page
		.locator('.filters-dropdown-button');

	await filtersDropdown.waitFor();
	await filtersDropdown.click();

	await page.getByRole('menuitem', {name: 'Status'}).click();

	const pendingCheckbox = page.getByLabel('Pending');

	await pendingCheckbox.check();

	await page
		.getByRole('button', {exact: true, name: 'Add Filter'})
		.click();

	await changeTrackingPage.reviewChange('Home');

	await page
		.locator('.btn-outline-secondary')
		.click();

	await page.getByRole('menuitem', {name: ctCollection.body.name}).click();

	const publicationIFrame = page.frameLocator(
		'iframe[src*="preview"]'
	);

	const newHeading = publicationIFrame.getByText('Edited');

	await expect(newHeading).toBeVisible();
});
