/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.entry.util.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.service.ExpandoColumnLocalService;
import com.liferay.expando.kernel.service.ExpandoTableLocalService;
import com.liferay.object.entry.util.ObjectEntryDTOConverterUtil;
import com.liferay.object.system.SystemObjectDefinitionManagerRegistry;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;

import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Yuri Monteiro
 */
@RunWith(Arquillian.class)
public class ObjectEntryDTOConverterUtilTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testToValues() throws Exception {
		_user = UserTestUtil.addUser();

		ExpandoTable expandoTable = _expandoTableLocalService.addDefaultTable(
			TestPropsValues.getCompanyId(),
			PortalUtil.getClassNameId(User.class));

		String expandoColumnName = RandomTestUtil.randomString();

		_expandoColumnLocalService.addColumn(
			expandoTable.getTableId(), expandoColumnName,
			ExpandoColumnConstants.STRING_ARRAY);

		_expandoColumnLocalService.getColumn(
			expandoTable.getTableId(), expandoColumnName);

		String[] expectedValues = {
			RandomTestUtil.randomString(), RandomTestUtil.randomString()
		};

		_user.getExpandoBridge(
		).setAttribute(
			expandoColumnName, expectedValues, false
		);

		_user = _userLocalService.getUser(_user.getUserId());

		User contextUser = TestPropsValues.getUser();

		PermissionChecker originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();
		String originalName = PrincipalThreadLocal.getName();

		try {
			PermissionThreadLocal.setPermissionChecker(
				PermissionCheckerFactoryUtil.create(contextUser));
			PrincipalThreadLocal.setName(contextUser.getUserId());

			Map<String, Object> values = ObjectEntryDTOConverterUtil.toValues(
				_user, _dtoConverterRegistry, "User",
				_systemObjectDefinitionManagerRegistry, contextUser);

			Assert.assertEquals(
				_user.getUserId(), GetterUtil.getLong(values.get("id")));

			Object customFields = values.get("customFields");

			Assert.assertNotNull(customFields);

			String valuesJSON = JSONFactoryUtil.serialize(values);

			Assert.assertTrue(valuesJSON.contains(expandoColumnName));
			Assert.assertTrue(valuesJSON.contains(expectedValues[0]));
			Assert.assertTrue(valuesJSON.contains(expectedValues[1]));
		}
		finally {
			PermissionThreadLocal.setPermissionChecker(
				originalPermissionChecker);
			PrincipalThreadLocal.setName(originalName);
		}
	}

	@Inject
	private DTOConverterRegistry _dtoConverterRegistry;

	@Inject
	private ExpandoColumnLocalService _expandoColumnLocalService;

	@Inject
	private ExpandoTableLocalService _expandoTableLocalService;

	@Inject
	private SystemObjectDefinitionManagerRegistry
		_systemObjectDefinitionManagerRegistry;

	@DeleteAfterTestRun
	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}