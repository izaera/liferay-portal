/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import ClayColorPicker from '@clayui/color-picker';
import React, {useState} from 'react';

const DEFAULT_COLORS = [
	'000000',
	'5F5F5F',
	'9A9A9A',
	'CBCBCB',
	'E1E1E1',
	'FFFFFF',
	'FF0D0D',
	'FF8A1C',
	'2BA676',
	'006EF8',
	'7F26FF',
	'FF21A0',
	'FF5F5F',
	'FFB46E',
	'50D2A0',
	'4B9BFF',
	'AF78FF',
	'FF73C3',
	'FFB1B1',
	'FFDEC0',
	'91E3C3',
	'9DC8FF',
	'DFCAFF',
	'FFC5E6',
	'FFD9D9',
	'FFF3E8',
	'B1EBD5',
	'C5DFFF',
	'F8F2FF',
	'FFEDF7',
];

function normalizeColor(color) {
	if (color === null || color === '') {
		color = '808080';
	}

	return color.startsWith('#') ? color.substring(1) : color;
}

const ColorPicker = ({color, label, name}) => {
	const [colorValue, setColorValue] = useState(normalizeColor(color));
	const [customColors, setCustoms] = useState(DEFAULT_COLORS);

	return (
		<div className="form-group">
			<input name={name} type="hidden" value={`#${colorValue}`} />

			<ClayColorPicker
				colors={customColors}
				label={label}
				name={`${name}ColorPicker`}
				onColorsChange={setCustoms}
				onValueChange={setColorValue}
				showHex={true}
				title={label}
				value={colorValue}
			/>
		</div>
	);
};

export default ColorPicker;
