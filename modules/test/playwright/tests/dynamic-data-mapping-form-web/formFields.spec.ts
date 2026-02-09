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
import {waitForAlert} from '../../utils/waitForAlert';
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

	test('assert that it is possible to delete the decimal separator of a numeric field without removing the character before it', async ({
		formBuilderPage,
		formBuilderSidePanelPage,
		page,
	}) => {
		await formBuilderPage.goToNew();

		await formBuilderPage.fillFormTitle('Form' + getRandomInt());

		await formBuilderSidePanelPage.addFieldByDoubleClick('Numeric');

		await formBuilderSidePanelPage.numericTypeDecimal.check();

		await expect(formBuilderSidePanelPage.numericTypeDecimal).toBeChecked();

		await formBuilderSidePanelPage.advancedTab.click();

		await formBuilderSidePanelPage.inputMaskToggle.check();

		await expect(page.getByLabel('Thousands Separator')).toBeVisible();

		await formBuilderPage.saveButton.click();

		await waitForAlert(page);

		const newTabPage = await formBuilderPage.openPreviewForm();

		const numericInput = newTabPage.getByLabel('Numeric');

		await numericInput.fill('22.');

		await numericInput.click();

		await newTabPage.keyboard.press('Backspace');

		await expect(numericInput).toHaveValue('22');
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

	test(
		'Deletes fields group when last field is dragged into another field',
		{
			tag: '@LPD-70472',
		},
		async ({formBuilderPage, formBuilderSidePanelPage, page}) => {
			await formBuilderPage.goToNew();

			await formBuilderSidePanelPage.addFieldByDoubleClick('Text');

			await formBuilderPage.openFieldSettings('Text', 0);

			const textFieldReference1 =
				await formBuilderSidePanelPage.getFieldReference();

			await formBuilderSidePanelPage.backButton.click();

			await formBuilderSidePanelPage.addFieldByDoubleClick('Text');

			await formBuilderPage.openFieldSettings('Text', 1);

			const textFieldReference2 =
				await formBuilderSidePanelPage.getFieldReference();

			await formBuilderSidePanelPage.backButton.click();

			await formBuilderSidePanelPage.addFieldByDoubleClick('Text');

			await formBuilderPage.openFieldSettings('Text', 2);

			const textFieldReference3 =
				await formBuilderSidePanelPage.getFieldReference();

			await formBuilderSidePanelPage.backButton.click();

			await formBuilderSidePanelPage.dragAndDropField(
				textFieldReference1,
				textFieldReference2
			);

			await formBuilderSidePanelPage.dragAndDropField(
				textFieldReference3,
				textFieldReference1
			);

			await formBuilderSidePanelPage.dragAndDropField(
				textFieldReference1,
				textFieldReference2
			);

			await formBuilderSidePanelPage.dragAndDropField(
				textFieldReference3,
				textFieldReference2
			);

			const dropZoneTargets = page.locator('.ddm-target');

			await expect(dropZoneTargets).toHaveCount(16);
		}
	);

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

		test('Duplicating fieldset with required fields only takes one click', async ({
		formBuilderPage,
		formBuilderSidePanelPage,
		page,
	}) => {
		await formBuilderPage.goToNew();

		await formBuilderSidePanelPage.addFieldByDoubleClick('Text');

		await formBuilderSidePanelPage.requiredFieldToggleSwitch.click();

		await formBuilderSidePanelPage.clickAdvancedTab();

		const textFieldReference1 =
			await formBuilderSidePanelPage.getFieldReference();

		await formBuilderSidePanelPage.backButton.click();

		await formBuilderSidePanelPage.addFieldByDoubleClick('Text');

		await formBuilderSidePanelPage.requiredFieldToggleSwitch.click();

		await formBuilderSidePanelPage.clickAdvancedTab();

		const textFieldReference2 =
			await formBuilderSidePanelPage.getFieldReference();

		await formBuilderSidePanelPage.dragAndDropField(
			textFieldReference2,
			textFieldReference1
		);

		await page
			.locator('label.text-uppercase', {hasText: 'Fields Group'})
			.click();

		await formBuilderSidePanelPage.clickBasicTab();

		await formBuilderSidePanelPage.repeatableFieldToggleSwitch.click();

		const newTabPage = await formBuilderPage.openPreviewForm();

		await newTabPage.getByRole('textbox').last().click();

		await newTabPage
			.getByRole('button', {
				name: 'Add Duplicate Field Fields Group',
			})
			.click();

		await expect(
			newTabPage.getByText('This field is required.')
		).toBeVisible();

		await expect(
			newTabPage.getByLabel('Fields Group', {exact: true})
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
