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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Iván Zaera Avellon
 */
public class CompanyPortletMap<T> {

	public CompanyPortletMap() {
		this(null);
	}

	public CompanyPortletMap(
		CreateCompanyMapProcessor createCompanyMapProcessor) {

		_createCompanyMapProcessor = createCompanyMapProcessor;

		_map.put(CompanyConstants.SYSTEM, new HashMap<>());
	}

	public void clear() {
		synchronized (_map) {
			_map.clear();
			_map.put(CompanyConstants.SYSTEM, new HashMap<>());
		}
	}

	public void clear(long companyId) {
		synchronized (_map) {
			_map.remove(companyId);
		}
	}

	public T get(long companyId, String portletId) {
		synchronized (_map) {
			Map<String, T> map = _map.get(companyId);

			if (map == null) {
				map = _createCompanyMap(companyId);
			}

			return map.get(portletId);
		}
	}

	public boolean isEmpty() {
		synchronized (_map) {
			for (Map<String, T> map : _map.values()) {
				if (!map.isEmpty()) {
					return false;
				}
			}

			return true;
		}
	}

	public Set<String> keySet(long companyId) {
		synchronized (_map) {
			Map<String, T> map = _map.get(companyId);

			if (map == null) {
				map = _createCompanyMap(companyId);
			}

			return map.keySet();
		}
	}

	public void put(long companyId, String portletId, T value) {
		synchronized (_map) {
			Map<String, T> map = _map.get(companyId);

			if (map == null) {
				map = _createCompanyMap(companyId);
			}

			map.put(portletId, value);

			if (companyId == CompanyConstants.SYSTEM) {
				for (Map.Entry<Long, Map<String, T>> entry : _map.entrySet()) {
					if (Objects.equals(
							entry.getKey(), CompanyConstants.SYSTEM)) {

						continue;
					}

					map = entry.getValue();

					map.put(portletId, value);
				}
			}
		}
	}

	public T remove(long companyId, String portletId) {
		synchronized (_map) {
			Map<String, T> map = _map.get(companyId);

			if (map == null) {
				return null;
			}

			T value = map.remove(portletId);

			if (companyId == CompanyConstants.SYSTEM) {
				for (long key : _map.keySet()) {
					if (key == CompanyConstants.SYSTEM) {
						continue;
					}

					_map.remove(key);
				}
			}

			return value;
		}
	}

	public Collection<T> values(long companyId) {
		synchronized (_map) {
			Map<String, T> map = _map.get(companyId);

			if (map == null) {
				map = _createCompanyMap(companyId);
			}

			return map.values();
		}
	}

	public interface CreateCompanyMapProcessor<T> {

		public T process(long companyId, T value);

	}

	private Map<String, T> _createCompanyMap(long companyId) {
		synchronized (_map) {
			Map<String, T> map;

			Map<String, T> systemMap = _map.get(CompanyConstants.SYSTEM);

			if (_createCompanyMapProcessor == null) {
				map = new HashMap<>(systemMap);
			}
			else {
				map = new HashMap<>();

				for (Map.Entry<String, T> entry : systemMap.entrySet()) {
					map.put(
						entry.getKey(),
						_createCompanyMapProcessor.process(
							companyId, entry.getValue()));
				}
			}

			_map.put(companyId, map);

			return map;
		}
	}

	private final CreateCompanyMapProcessor<T> _createCompanyMapProcessor;
	private final Map<Long, Map<String, T>> _map = new HashMap<>();

}