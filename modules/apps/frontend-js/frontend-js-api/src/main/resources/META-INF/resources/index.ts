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

//
// All this would be generated analyzing the ThemeDisplayJSApi interface at
// build time with a Java plugin.
//

declare const Liferay: {
    ThemeDisplay: ThemeDisplay;
};

class ThemeDisplay {
    public constructor(private _impl: ThemeDisplay) {}

    getLayoutId(): number {
        return this._impl.getLayoutId();
    }

    getLayoutURL(): string {
        return this._impl.getLayoutURL();
    }
}

const themeDisplay = new ThemeDisplay(Liferay.ThemeDisplay);

export {
    themeDisplay
};

//
// The underlying implementation in Liferay.* would be generated at runtime with
// the help of JSApiFactory instances that would be leveraged by a servlet to
// generate the same code we now hardcode in top_js.jspf, etc.
//