/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.service.test;

import com.liferay.account.model.AccountEntry;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.product.model.CPConfigurationEntry;
import com.liferay.commerce.product.model.CPConfigurationList;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CPConfigurationEntryLocalService;
import com.liferay.commerce.product.test.util.CPTestUtil;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.repository.LocalRepository;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import java.math.BigDecimal;

import java.util.List;

import org.frutilla.FrutillaRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stefano Motta
 */
@RunWith(Arquillian.class)
public class CommerceOrderLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_user = UserTestUtil.addUser();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getGroupId(), _user.getUserId());

		_accountEntry = CommerceAccountTestUtil.addPersonAccountEntry(
			_user.getUserId(), _serviceContext);

		_commerceCurrency = CommerceCurrencyTestUtil.addCommerceCurrency(
			_group.getCompanyId());

		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			_group.getGroupId(), _commerceCurrency.getCode());
	}

	@Test
	public void testAddCommerceOrderAttachment() throws Exception {
		frutillaRule.scenario(
			"Add an attachment to an order"
		).given(
			"An order"
		).when(
			"I add an attachment"
		).then(
			"I should be able to retrieve it"
		);

		CommerceOrder commerceOrder =
			_commerceOrderLocalService.addCommerceOrder(
				_user.getUserId(), _commerceChannel.getGroupId(),
				_accountEntry.getAccountEntryId(), _commerceCurrency.getCode(),
				0);

		Class<?> clazz = getClass();

		FileEntry fileEntry1 =
			_commerceOrderLocalService.addAttachmentFileEntry(
				RandomTestUtil.randomString(), _user.getUserId(),
				commerceOrder.getCommerceOrderId(),
				RandomTestUtil.randomString(),
				clazz.getResourceAsStream("dependencies/attachment.txt"));

		LocalRepository localRepository = commerceOrder.getLocalRepository();

		Assert.assertNotNull(localRepository);

		Folder folder = commerceOrder.getFolder(localRepository);

		Assert.assertNotNull(folder);

		List<FileEntry> attachmentFileEntries =
			commerceOrder.getAttachmentFileEntries(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		Assert.assertEquals(
			attachmentFileEntries.toString(), 1, attachmentFileEntries.size());

		FileEntry fileEntry2 = attachmentFileEntries.get(0);

		Assert.assertEquals(
			fileEntry1.getExternalReferenceCode(),
			fileEntry2.getExternalReferenceCode());
		Assert.assertEquals(
			fileEntry1.getFileEntryId(), fileEntry2.getFileEntryId());
		Assert.assertEquals(folder.getFolderId(), fileEntry2.getFolderId());
	}

	@Test
	public void testAddCommerceOrderWithCPConfigurationEntryShippable()
		throws Exception {

		_accountEntry = CommerceAccountTestUtil.addBusinessAccountEntry(
			_user.getUserId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString() + "@liferay.com",
			RandomTestUtil.randomString(), new long[] {_user.getUserId()}, null,
			_serviceContext);

		CommerceOrder commerceOrder = CommerceTestUtil.addB2BCommerceOrder(
			_group.getGroupId(), _user.getUserId(),
			_accountEntry.getAccountEntryId(),
			_commerceCurrency.getCommerceCurrencyId());

		CPInstance cpInstance = CPTestUtil.addCPInstanceWithRandomSku(
			_group.getGroupId(), BigDecimal.valueOf(34.90));

		CPDefinition cpDefinition = cpInstance.getCPDefinition();

		CPConfigurationList masterCPConfigurationList =
			cpDefinition.getMasterCPConfigurationList();

		CPConfigurationEntry cpConfigurationEntry =
			_cpConfigurationEntryLocalService.fetchCPConfigurationEntry(
				_portal.getClassNameId(CPDefinition.class),
				cpDefinition.getCPDefinitionId(),
				masterCPConfigurationList.getCPConfigurationListId());

		cpConfigurationEntry.setShippable(false);

		cpConfigurationEntry =
			_cpConfigurationEntryLocalService.updateCPConfigurationEntry(
				cpConfigurationEntry);

		CommerceTestUtil.addCommerceOrderItem(
			commerceOrder.getCommerceOrderId(), cpInstance.getCPInstanceId(),
			BigDecimal.ONE);

		commerceOrder = _commerceOrderLocalService.getCommerceOrder(
			commerceOrder.getCommerceOrderId());

		Assert.assertEquals(
			cpConfigurationEntry.isShippable(), commerceOrder.isShippable());
	}

	@Test
	public void testDeleteCommerceOrderAttachment() throws Exception {
		frutillaRule.scenario(
			"Delete an attachment from an order"
		).given(
			"An order with an attachment"
		).when(
			"I delete the attachment"
		).then(
			"The file entry does not exist anymore"
		);

		CommerceOrder commerceOrder =
			_commerceOrderLocalService.addCommerceOrder(
				_user.getUserId(), _commerceChannel.getGroupId(),
				_accountEntry.getAccountEntryId(), _commerceCurrency.getCode(),
				0);

		Class<?> clazz = getClass();

		FileEntry fileEntry = _commerceOrderLocalService.addAttachmentFileEntry(
			RandomTestUtil.randomString(), _user.getUserId(),
			commerceOrder.getCommerceOrderId(), RandomTestUtil.randomString(),
			clazz.getResourceAsStream("dependencies/attachment.txt"));

		LocalRepository localRepository = commerceOrder.getLocalRepository();

		Assert.assertNotNull(localRepository);

		_commerceOrderLocalService.deleteAttachmentFileEntry(
			fileEntry.getFileEntryId(), commerceOrder.getCommerceOrderId());

		List<FileEntry> attachmentFileEntries =
			commerceOrder.getAttachmentFileEntries(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		Assert.assertEquals(
			attachmentFileEntries.toString(), 0, attachmentFileEntries.size());

		Assert.assertNull(
			localRepository.fetchFileEntryByExternalReferenceCode(
				fileEntry.getExternalReferenceCode()));
	}

	@Rule
	public FrutillaRule frutillaRule = new FrutillaRule();

	private AccountEntry _accountEntry;
	private CommerceChannel _commerceChannel;
	private CommerceCurrency _commerceCurrency;

	@Inject
	private CommerceOrderLocalService _commerceOrderLocalService;

	@Inject
	private CPConfigurationEntryLocalService _cpConfigurationEntryLocalService;

	private Group _group;

	@Inject
	private Portal _portal;

	private ServiceContext _serviceContext;
	private User _user;

}