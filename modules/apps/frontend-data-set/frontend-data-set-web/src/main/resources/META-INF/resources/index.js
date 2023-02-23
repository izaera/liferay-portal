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

// Temporary hack to avoid needing to migrate more things from AMD to ESM

import perico from './FrontendDataSet';
export default perico;

// Frontend Data Set API

export {default as FrontendDataSet} from './FrontendDataSet';

// Renderers API

export {
	dataRenderers as DataRenderers,
	inputRenderers as InputRenderers,
} from './data_renderers/index';
export {default as DateTimeRenderer} from './data_renderers/DateTimeRenderer';
export {default as StatusRenderer} from './data_renderers/StatusRenderer';

// Data Set Events API

export {default as DATA_SET_EVENT} from './utils/eventsDefinitions';
