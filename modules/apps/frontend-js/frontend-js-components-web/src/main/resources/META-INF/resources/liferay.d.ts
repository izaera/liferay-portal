/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

declare module Liferay {
	namespace Portlet {
		export function openModal(...args: any[]): void;

		export function openWindow(...args: any[]): void;
	}

	namespace Util {
		export function openAlertModal(...args: any[]): void;

		export function openConfirmModal(...args: any[]): void;

		export function openModal(props: Object): void;

		export function openPortletModal(
			containerProps: Object,
			footerCssClass: string,
			headerCssClass: string,
			iframeBodyCssClass: string,
			onClose: () => void,
			portletSelector: string,
			subTitle: string,
			title: string,
			url: string
		): void;

		export function openSelectionModal(
			buttonAddLabel: string,
			buttonCancelLabel: string,
			containerProps: Object,
			customSelectEvent: boolean,
			height: string,
			id: string,
			iframeBodyCssClass: string,
			multiple: boolean,
			onClose: () => void,
			onSelect: () => void,
			selectEventName: string,
			selectedData: any,
			size: 'full-screen' | 'lg' | 'md' | 'sm',
			title: string,
			url: string,
			zIndex: number
		): void;

		export function openSimpleInputModal(...args: any[]): void;

		export function openToast(...args: any[]): void;
	}
}
