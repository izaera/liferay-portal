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

import 'cropperjs/dist/cropper.css';
import x from 'isarray/index.js';


console.log('>>>>>>>> if you see this message it means frontend-js-state-web loaded both "cropperjs/dist/cropper.css" and "isarray/index.js"');

console.log('>>>>>>>> checking imports are OK...');
console.log('>>>>>>>> isarray/index.js :', x);
console.log('>>>>>>>> cropperjs/dist/cropper.css :', document.querySelector('link[href="/o/frontend-js-web/__liferay__/css/cropperjs$dist$cropper.css"]'));

console.log('>>>>>>>> now checking that the CSS imports works in AMD...');
(Liferay as any).Loader.require("item-selector-taglib@5.3.1/image_editor/ImageEditor", () => {
	console.log('>>>>>>>> item-selector-taglib@5.3.1/image_editor/ImageEditor correctly loaded!');
});

export {default as State} from './State';

export type {Atom, Selector} from './State';
export type {Immutable} from './types';
