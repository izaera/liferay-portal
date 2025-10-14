/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';
import path from 'path';

import {dataApiHelpersTest} from '../../fixtures/dataApiHelpersTest';
import {formsPagesTest} from '../../fixtures/formsPagesTest';
import {loginTest} from '../../fixtures/loginTest';
import {getRandomInt} from '../../utils/getRandomInt';
import {deleteItems} from './utils/deleteItems';

export const test = mergeTests(dataApiHelpersTest, loginTest(), formsPagesTest);

test.afterEach(async ({formsPage}) => {
	await formsPage.goTo();

	await deleteItems(formsPage);
});

test.describe('Manage fields through Form Preview page', () => {
	test('assert that it is possible to delete the predefined value of a text field', async ({
		formBuilderPage,
		formBuilderSidePanelPage,
	}) => {
		await formBuilderPage.goToNew();

		await formBuilderPage.fillFormTitle('Form' + getRandomInt());

		await formBuilderSidePanelPage.addFieldByDoubleClick('Text');

		await formBuilderSidePanelPage.advancedTab.click();

		await formBuilderSidePanelPage.predefinedValueField.fill(
			'predefined value for text field.'
		);

		const newTabPage = await formBuilderPage.openPreviewForm();

		await newTabPage.getByLabel('Text').click();

		await newTabPage.keyboard.press('Control+A');

		await newTabPage.keyboard.press('Backspace');

		// Wait a little bit before doing the assertion since useSyncValue hook takes a few miliseconds to set the value on the text field
		// Otherwise the test would always pass, even with the bug still present

		await newTabPage.waitForTimeout(1000);

		await expect(newTabPage.getByLabel('Text')).toHaveValue('');

		await newTabPage.close();
	});

	test('can move the last field of a child group into the parent group field', async ({
		formBuilderPage,
		formBuilderSidePanelPage,
		page,
	}) => {
		await test.step('go to form builder and create structure with two levels of nesting and one field in each', async () => {
			await formBuilderPage.goToNew();

			await formBuilderSidePanelPage.addFieldByDoubleClick('Text');

			await formBuilderSidePanelPage.backButton.click();

			await formBuilderSidePanelPage.addFieldToFieldGroup('Text', 0);

			await formBuilderSidePanelPage.backButton.click();

			await formBuilderSidePanelPage.addFieldToFieldGroup('Text', 2);

			await page.getByLabel('Actions').nth(4).click();

			await page.getByRole('menuitem', {name: 'Delete'}).click();
		});

		await test.step('drag field from child into the parent one to create new fieldGroup', async () => {
			await page
				.locator('.ddm-drag')
				.nth(3)
				.dragTo(page.locator('.ddm-drag').nth(1));
		});

		await expect(
			page.getByLabel('Fields Group', {exact: true})
		).toHaveCount(2);
	});

	test('duplicating field with evaluation rules has correct behavior', async ({
		formBuilderPage,
		formBuilderSidePanelPage,
		page,
	}) => {
		await formBuilderPage.goToNew();

		await formBuilderPage.fillFormTitle('Form' + getRandomInt());

		await formBuilderSidePanelPage.addFieldByDoubleClick('Text');

		await formBuilderSidePanelPage.label.fill('Text Field');

		await formBuilderSidePanelPage.requiredFieldToggleSwitch.click();

		await formBuilderSidePanelPage.clickAdvancedTab();

		await formBuilderSidePanelPage.repeatableFieldToggleSwitch.click();

		await page.getByLabel('Add Duplicate Field').waitFor();

		const newTabPage = await formBuilderPage.openPreviewForm();

		await newTabPage.getByLabel('Text Field', {exact: true}).click();

		await newTabPage
			.getByRole('button', {
				name: 'Add Duplicate Field Text Field',
			})
			.click();

		await expect(
			newTabPage.getByText('This field is required.')
		).toBeVisible();

		await expect(
			newTabPage.getByLabel('Text Field', {exact: true})
		).toHaveCount(2);
	});

	test('LPD-12824 HTML autocomplete attribute is rendered and has the configured value limited to 20 non-special characters in Date, Numeric and Text field types', async ({
		formBuilderPage,
		formBuilderSidePanelPage,
	}) => {
		const testData: {
			expectedValue: string;
			fieldTitle: FormFieldTypeTitle;
			inputValue: string;
		}[] = [
			{
				expectedValue: 'bday',
				fieldTitle: 'Date',
				inputValue: '+)(*&^%$#@ bday$__%  ',
			},
			{
				expectedValue: 'one-time-code',
				fieldTitle: 'Numeric',
				inputValue: '****[][one-time-code&&#()',
			},
			{
				expectedValue: 'transaction-currency',
				fieldTitle: 'Text',
				inputValue: 'transaction-currencyextracharacters',
			},
		];

		await formBuilderPage.goToNew();

		await expect(formBuilderPage.newFormHeading).toBeVisible();

		await formBuilderPage.fillFormTitle('Form' + getRandomInt());

		for (const data of testData) {
			await formBuilderSidePanelPage.addFieldByDoubleClick(
				data.fieldTitle
			);

			await formBuilderSidePanelPage.clickAdvancedTab();

			await expect(
				formBuilderSidePanelPage.htmlAutocompleteAttributeField
			).toBeVisible();

			await formBuilderSidePanelPage.htmlAutocompleteAttributeField.fill(
				data.inputValue
			);

			await formBuilderSidePanelPage.clickBackButton();
		}

		const newTabPage = await formBuilderPage.openPreviewForm();

		for (const data of testData) {
			if (data.fieldTitle === 'Date') {
				await expect(
					newTabPage.getByPlaceholder('__/__/____')
				).toHaveAttribute('autocomplete', data.expectedValue);

				continue;
			}

			await expect(
				newTabPage.getByLabel(data.fieldTitle)
			).toHaveAttribute('autocomplete', data.expectedValue);
		}

		await newTabPage.close();
	});

	test('make sure the aria-labelledby reference is present in the captcha form view', async ({
		formBuilderPage,
		formBuilderSidePanelPage,
	}) => {
		await formBuilderPage.goToNew();

		await formBuilderPage.fillFormTitle('Form' + getRandomInt());

		await formBuilderSidePanelPage.addFieldByDoubleClick('Text');

		await formBuilderPage.formSettingsButton.click();

		await formBuilderPage.requireCaptchaToggle.click();

		await formBuilderPage.formSettingsDoneButton.click();

		const newTabPage = await formBuilderPage.openPreviewForm();

		const captchaContainer = newTabPage.locator(
			"[data-field-reference='_CAPTCHA_']"
		);

		await expect(captchaContainer).toBeVisible();

		const captchaContainerAriaLabelledby =
			await captchaContainer.getAttribute('aria-labelledby');

		const screenReaderOnlyCaptchaSpan = newTabPage.locator(
			`span[id='${captchaContainerAriaLabelledby}']`
		);

		await expect(screenReaderOnlyCaptchaSpan).toHaveClass('sr-only');

		await expect(screenReaderOnlyCaptchaSpan).toContainText('captcha');

		await newTabPage.close();
	});

	test('verify boolean field aria-labelledby is only created when there is corresponding label rendered', async ({
		formBuilderPage,
		formBuilderSidePanelPage,
	}) => {
		await formBuilderPage.goToNew();

		await formBuilderPage.fillFormTitle('Form' + getRandomInt());

		await formBuilderSidePanelPage.addFieldByDoubleClick('Boolean');

		await formBuilderSidePanelPage.label.fill('Boolean without helptext');

		await formBuilderSidePanelPage.backButton.click();

		await formBuilderSidePanelPage.addFieldByDoubleClick('Boolean');

		await formBuilderSidePanelPage.label.fill('Boolean with helptext');

		await formBuilderSidePanelPage.helpText.fill('Help text');

		await formBuilderSidePanelPage.backButton.click();

		const newTabPage = await formBuilderPage.openPreviewForm();

		const elementWithoutHelpText = newTabPage
			.locator('.form-group')
			.first();

		await expect(elementWithoutHelpText).not.toHaveAttribute(
			'aria-labelledby'
		);

		const elementWithHelpText = newTabPage.locator('.form-group').last();

		await expect(elementWithHelpText).toHaveAttribute('aria-labelledby');

		const helpTextLabelId =
			await elementWithHelpText.getAttribute('aria-labelledby');

		await expect(
			newTabPage.locator(`[id="${helpTextLabelId}"]`)
		).toBeVisible();

		await newTabPage.close();
	});
});

test.describe('Manage fields through Form Builder page', () => {
	test('assert edition of a rich text field predefined value that contains a rule', async ({
		formBuilderPage,
		formsPage,
		page,
	}) => {
		await formsPage.goTo();

		await formsPage.importForm(
			path.join(
				__dirname,
				'dependencies',
				'form-with-rich-text.portlet.lar'
			)
		);

		await formsPage.openForm('Form with rich text field');

		await expect(
			page.getByRole('textbox', {name: 'Rich Text'})
		).toBeVisible();

		await formBuilderPage.openFieldSettings('Rich Text');

		await formBuilderPage.settingsAdvancedTab.click();

		const richTextPredefinedValueIframe = page
			.getByRole('textbox', {name: 'Predefined Value'})
			.frameLocator('iframe');

		await richTextPredefinedValueIframe
			.getByText("Rich's text predefined value")
			.click();

		await page.keyboard.press('Control+A');

		await page.keyboard.press('Backspace');

		await page.keyboard.type(
			'Typing a new predefined value for the rich text field.'
		);

		await expect(
			richTextPredefinedValueIframe.getByText(
				'Typing a new predefined value for the rich text field.'
			)
		).toBeVisible();
	});
});
