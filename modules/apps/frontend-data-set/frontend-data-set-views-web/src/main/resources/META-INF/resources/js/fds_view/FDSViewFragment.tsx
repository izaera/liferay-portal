/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FrontendDataSet} from '@liferay/frontend-data-set-web';

import PropTypes from "prop-types";
import React, {useEffect, useState} from "react";

function FDSViewFragment({
    id,
    filters,
    views,
    ...otherProps
}: {id: string, filters: any, views: any[]}) {
    const [resolvedFilters, setResolvedFilters] = useState(null);

    useEffect(() => {
        const promises = filters.map((filter: any) => {
            if (!filter.cxFilterImplURL) {
                return Promise.resolve(filter);
            }

            // @ts-ignore
            return import(
                /* webpackIgnore: true */ filter.cxFilterImplURL
            ).then(module => ({
                ...filter,
                cxFilterImpl: module['default']
            }));
        });

        Promise.all(promises).then((resolvedFilters: any) => {
            setResolvedFilters(resolvedFilters);
        })

    }, [filters]);

    return (
        resolvedFilters ?
            <FrontendDataSet
                id={id}
                filters={resolvedFilters}
                views={views}
                {...otherProps}
            /> :
            <></>
    );
}

FDSViewFragment.propTypes = {
    id: PropTypes.string,
    filters: PropTypes.array,
    views: PropTypes.array,
};

export default FDSViewFragment;