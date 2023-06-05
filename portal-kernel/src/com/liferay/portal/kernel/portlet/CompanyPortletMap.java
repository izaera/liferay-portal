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

package com.liferay.portal.kernel.portlet;

import com.liferay.portal.kernel.model.CompanyConstants;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Iván Zaera Avellon
 */
public class CompanyPortletMap<T> {

	public void clear() {
		_map.clear();
	}

	public void clear(long companyId) {
		ConcurrentMap<String, T> companyMap = _getCompanyMap(companyId);

		companyMap.clear();
	}

	public T get(long companyId, String portletId) {
		ConcurrentMap<String, T> companyMap = _getCompanyMap(companyId);

		T value = companyMap.get(portletId);

		if (value != null) {
			return value;
		}

		companyMap = _getCompanyMap(CompanyConstants.SYSTEM);

		return companyMap.get(portletId);
	}

	public boolean isEmpty() {
		return _map.isEmpty();
	}

	public Set<String> keySet(long companyId) {
		ConcurrentMap<String, T> systemCompanyMap = _getCompanyMap(
			CompanyConstants.SYSTEM);

		Set<String> systemKeySet = systemCompanyMap.keySet();

		if (companyId == CompanyConstants.SYSTEM) {
			return systemKeySet;
		}

		Iterator<String> systemIterator = systemKeySet.iterator();

		ConcurrentMap<String, T> companyMap = _getCompanyMap(companyId);

		Set<String> companyKeySet = companyMap.keySet();

		Iterator<String> companyIterator = companyKeySet.iterator();

		return new AbstractSet<String>() {

			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {

					@Override
					public boolean hasNext() {
						if (systemIterator.hasNext() ||
							companyIterator.hasNext()) {

							return true;
						}

						return false;
					}

					@Override
					public String next() {
						if (systemIterator.hasNext()) {
							return systemIterator.next();
						}

						return companyIterator.next();
					}

				};
			}

			@Override
			public int size() {
				return systemKeySet.size() + companyKeySet.size();
			}

		};
	}

	public void put(long companyId, String portletId, T value) {
		ConcurrentMap<String, T> companyMap = _getCompanyMap(companyId);

		companyMap.put(portletId, value);
	}

	public T remove(long companyId, String portletId) {
		ConcurrentMap<String, T> companyMap = _getCompanyMap(companyId);

		return companyMap.remove(portletId);
	}

	public Collection<T> values(long companyId) {
		ConcurrentMap<String, T> systemCompanyMap = _getCompanyMap(
			CompanyConstants.SYSTEM);

		Collection<T> systemValues = systemCompanyMap.values();

		if (companyId == CompanyConstants.SYSTEM) {
			return systemValues;
		}

		Iterator<T> systemIterator = systemValues.iterator();

		ConcurrentMap<String, T> companyMap = _getCompanyMap(companyId);

		Collection<T> companyValues = companyMap.values();

		Iterator<T> companyIterator = companyValues.iterator();

		return new AbstractCollection<T>() {

			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {

					@Override
					public boolean hasNext() {
						if (systemIterator.hasNext() ||
							companyIterator.hasNext()) {

							return true;
						}

						return false;
					}

					@Override
					public T next() {
						if (systemIterator.hasNext()) {
							return systemIterator.next();
						}

						return companyIterator.next();
					}

				};
			}

			@Override
			public int size() {
				return systemValues.size() + companyValues.size();
			}

		};
	}

	private ConcurrentMap<String, T> _getCompanyMap(long companyId) {
		ConcurrentMap<String, T> companyMap = _map.get(companyId);

		// TODO: only create CompanyMap on put (not get)

		if (companyMap == null) {
			_map.putIfAbsent(companyId, new ConcurrentHashMap<>());

			companyMap = _map.get(companyId);
		}

		return companyMap;
	}

	private final ConcurrentMap<Long, ConcurrentMap<String, T>> _map =
		new ConcurrentHashMap<>();

}