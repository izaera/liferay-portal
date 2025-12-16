/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.service.impl;

import com.liferay.object.internal.sort.SortDSLQueryVisitor;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.sql.dsl.query.DSLQuery;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.lang.reflect.Method;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

/**
 * @author Pedro Leite
 */
public class ObjectEntryLocalServiceImplTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		ReflectionTestUtil.setFieldValue(
			_objectEntryLocalServiceImpl, "_objectFieldLocalService",
			Mockito.mock(ObjectFieldLocalService.class));

		Snapshot<ObjectRelationshipLocalService> snapshot = Mockito.mock(
			Snapshot.class);

		Mockito.when(
			snapshot.get()
		).thenReturn(
			Mockito.mock(ObjectRelationshipLocalService.class)
		);

		ReflectionTestUtil.setFieldValue(
			_objectEntryLocalServiceImpl,
			"_objectRelationshipLocalServiceSnapshot", snapshot);
	}

	@Test
	public void testApplyOrderBy() throws Exception {
		try (MockedConstruction<SortDSLQueryVisitor> mockedConstruction =
				Mockito.mockConstruction(
					SortDSLQueryVisitor.class,
					(mock, context) -> Mockito.when(
						mock.visit(Mockito.any(), Mockito.any())
					).thenReturn(
						Mockito.mock(DSLQuery.class)
					))) {

			_applyOrderBy(-1, -1, null);

			List<SortDSLQueryVisitor> sortDSLQueryVisitors =
				mockedConstruction.constructed();

			Assert.assertEquals(
				sortDSLQueryVisitors.toString(), 0,
				sortDSLQueryVisitors.size());

			_applyOrderBy(0, 100, new Sort[0]);

			sortDSLQueryVisitors = mockedConstruction.constructed();

			Assert.assertEquals(
				sortDSLQueryVisitors.toString(), 1,
				sortDSLQueryVisitors.size());

			_assertSorts(
				new boolean[] {false}, sortDSLQueryVisitors.get(0),
				new String[] {"id"});

			_applyOrderBy(
				0, 100, new Sort[] {new Sort(Field.CREATE_DATE, true)});

			sortDSLQueryVisitors = mockedConstruction.constructed();

			Assert.assertEquals(
				sortDSLQueryVisitors.toString(), 2,
				sortDSLQueryVisitors.size());

			_assertSorts(
				new boolean[] {true, false}, sortDSLQueryVisitors.get(1),
				new String[] {Field.CREATE_DATE, "id"});
		}
	}

	private void _applyOrderBy(int start, int end, Sort[] sorts)
		throws Exception {

		Method method = ObjectEntryLocalServiceImpl.class.getDeclaredMethod(
			"_applyOrderBy", DSLQuery.class, ObjectDefinition.class, int.class,
			int.class, Sort[].class);

		method.setAccessible(true);

		method.invoke(
			_objectEntryLocalServiceImpl, Mockito.mock(DSLQuery.class),
			Mockito.mock(ObjectDefinition.class), start, end, sorts);
	}

	private void _assertSorts(
			boolean[] expectedReverses, SortDSLQueryVisitor sortDSLQueryVisitor,
			String[] expectedFieldNames)
		throws Exception {

		ArgumentCaptor<com.liferay.object.internal.sort.Sort> argumentCaptor =
			ArgumentCaptor.forClass(
				com.liferay.object.internal.sort.Sort.class);

		Mockito.verify(
			sortDSLQueryVisitor, Mockito.times(expectedFieldNames.length)
		).visit(
			Mockito.any(), argumentCaptor.capture()
		);

		List<com.liferay.object.internal.sort.Sort> sorts =
			argumentCaptor.getAllValues();

		Assert.assertEquals(
			sorts.toString(), expectedFieldNames.length, sorts.size());

		for (int i = 0; i < expectedFieldNames.length; i++) {
			com.liferay.object.internal.sort.Sort sort = sorts.get(i);

			Assert.assertEquals(expectedFieldNames[i], sort.getFieldName());
			Assert.assertEquals(expectedReverses[i], sort.isReverse());
		}
	}

	private final ObjectEntryLocalServiceImpl _objectEntryLocalServiceImpl =
		new ObjectEntryLocalServiceImpl();

}