/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom/extend-expect';
import {render, screen} from '@testing-library/react';
import {renderHook} from '@testing-library/react-hooks';
import userEvent from '@testing-library/user-event';
import React from 'react';

import {ConfigurationContainer} from '../../components/ObjectDetails/ConfigurationContainer';
import {useObjectDetailsForm} from '../../components/ObjectDetails/useObjectDetailsForm';

jest.mock('frontend-js-web', () => ({
	sub: jest.fn((langKey, arg) => langKey.replace('x', arg)),
}));

describe('The ConfigurationContainer', () => {
    const initialValues: Partial<ObjectDefinition> = {
		active: true,
		defaultLanguageId: 'en_US',
		externalReferenceCode: 'erc',
		id: 1,
		label: {en_US: 'label'},
		name: 'name',
		pluralLabel: {en_US: 'pluralLabel'},
	};

    const renderConfigurationContainer = (
		customValues: Partial<ObjectDefinition> = {},
		props: Partial<React.ComponentProps<typeof ConfigurationContainer>> = {}
	) => {
		const {result} = renderHook(useObjectDetailsForm, {
			initialProps: {
				initialValues: {...initialValues, ...customValues},
				onSubmit: () => {},
			},
		});

		render(
			<ConfigurationContainer
				hasUpdateObjectDefinitionPermission
				isApproved={true}
				isLinkedObjectDefinition={false}
                isRootDescendantNode={false}
				setValues={result.current.setValues}
				values={result.current.values}
				{...props}
			/>
		);
	};

	it('allows enableIndexSearch toggle to be checked and unchecked when definition is not published', async () => {
		renderConfigurationContainer(initialValues, {
			isApproved: false,
		});

		const toggle = screen.getByRole('switch', {
			name: 'enable-indexed-search',
		});

		await userEvent.click(toggle);
		expect(toggle).toBeChecked();

		await userEvent.click(toggle);
		expect(toggle).not.toBeChecked();
	});

	it('disables enableIndexSearch toggle when definition is published', () => {
		renderConfigurationContainer();

		expect(
			screen.getByRole('switch', {name: 'enable-indexed-search'})
		).toBeDisabled();
	});

	it('disables enableIndexSearch toggle when definition is published and inactive', () => {
		renderConfigurationContainer({active: false});

		expect(
			screen.getByRole('switch', {name: 'enable-indexed-search'})
		).toBeDisabled();
	});
});