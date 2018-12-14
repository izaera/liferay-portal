import Component from 'metal-component';
import Soy from 'metal-soy';

import componentTemplates from './ClayTableCellEditable.soy';

/**
 * Resize Component
 * @review
 */
class ClayTableCellEditable extends Component {
	_handleClick(event) {
		if (!this.editing) {
			this.editing = true;
		}
	}

	_handleInputCancel(event) {
		setTimeout(
			() => {
				this.editing = false;
			},
			20
		);
	}

	_handleSelectURL(event) {
		const url = 'http://localhost:8080/group/guest/~/control_panel/manage?p_p_id=com_liferay_item_selector_web_portlet_ItemSelectorPortlet&p_p_lifecycle=0&p_p_state=pop_up&p_p_mode=view&_com_liferay_item_selector_web_portlet_ItemSelectorPortlet_0_json=%7B%22desiredItemSelectorReturnTypes%22%3A%22com.liferay.adaptive.media.image.item.selector.AMImageFileEntryItemSelectorReturnType%2Ccom.liferay.item.selector.criteria.FileEntryItemSelectorReturnType%2Ccom.liferay.item.selector.criteria.URLItemSelectorReturnType%22%7D&_com_liferay_item_selector_web_portlet_ItemSelectorPortlet_criteria=com.liferay.blogs.item.selector.criterion.BlogsItemSelectorCriterion%2Ccom.liferay.item.selector.criteria.image.criterion.ImageItemSelectorCriterion%2Ccom.liferay.item.selector.criteria.url.criterion.URLItemSelectorCriterion%2Ccom.liferay.item.selector.criteria.upload.criterion.UploadItemSelectorCriterion&_com_liferay_item_selector_web_portlet_ItemSelectorPortlet_3_json=%7B%22URL%22%3A%22http%3A%5C%2F%5C%2Flocalhost%3A8080%5C%2Fgroup%5C%2Fguest%5C%2F%7E%5C%2Fcontrol_panel%5C%2Fmanage%3Fp_p_id%3Dcom_liferay_blogs_web_portlet_BlogsPortlet%26p_p_lifecycle%3D1%26p_p_state%3Dmaximized%26p_p_mode%3Dview%26_com_liferay_blogs_web_portlet_BlogsPortlet_javax.portlet.action%3D%252Fblogs%252Fupload_image%26p_auth%3DaNuacnhZ%26p_p_auth%3DVaDaxdek%22%2C%22desiredItemSelectorReturnTypes%22%3A%22com.liferay.adaptive.media.image.item.selector.AMImageFileEntryItemSelectorReturnType%2Ccom.liferay.item.selector.criteria.FileEntryItemSelectorReturnType%22%2C%22extensions%22%3A%5B%22.gif%22%2C%22.jpeg%22%2C%22.jpg%22%2C%22.png%22%5D%2C%22maxFileSize%22%3A%225242880%22%2C%22portletId%22%3A%22com_liferay_blogs_web_portlet_BlogsPortlet%22%2C%22repositoryName%22%3A%22Blog+Images%22%7D&_com_liferay_item_selector_web_portlet_ItemSelectorPortlet_itemSelectedEventName=_com_liferay_blogs_web_portlet_BlogsAdminPortlet_contentEditorselectItem&_com_liferay_item_selector_web_portlet_ItemSelectorPortlet_2_json=%7B%22desiredItemSelectorReturnTypes%22%3A%22com.liferay.item.selector.criteria.URLItemSelectorReturnType%22%7D&_com_liferay_item_selector_web_portlet_ItemSelectorPortlet_1_json=%7B%22desiredItemSelectorReturnTypes%22%3A%22com.liferay.adaptive.media.image.item.selector.AMImageFileEntryItemSelectorReturnType%2Ccom.liferay.item.selector.criteria.FileEntryItemSelectorReturnType%2Ccom.liferay.item.selector.criteria.URLItemSelectorReturnType%22%7D&p_p_auth=0eSO4O5u';

		AUI().use(
			'liferay-item-selector-dialog',
			function(A) {
				const itemSelectorDialog = new A.LiferayItemSelectorDialog(
					{
						eventName: '_com_liferay_blogs_web_portlet_BlogsAdminPortlet_contentEditorselectItem',
						url: url,
						zIndex: 1000
					}
				);

				itemSelectorDialog.once(
					'selectedItemChange',
					function(event) {
						this.value = JSON.parse(event.newVal.value).url;
						this.saving = false;
						this.editing = false;
					}.bind(this)
				);

				itemSelectorDialog.open();
			}.bind(this)
		);
	}

	_appendInput(name, value, form) {
		const input = document.createElement('input');
		input.setAttribute('name', name);

		if (value) {
			input.setAttribute('value', value);
		}

		form.appendChild(input);
	}

	_handleInputSave(event) {
		const item = this.initialConfig_.item;

		const formEl = document.createElement('form');

		formEl.setAttribute('action', '/api/jsonws/blogs.blogsentry/update-entry');
		formEl.setAttribute('method', 'POST"');
		formEl.setAttribute('name', 'execute"');

		this._appendInput('allowPingbacks', item.allowPingbacks || false, formEl);
		this._appendInput('allowTrackbacks', item.allowPingbacks || true, formEl);
		this._appendInput('content', item.content, formEl);
		this._appendInput('coverImageCaption', '', formEl);
		this._appendInput('description', '', formEl);
		this._appendInput('displayDateDay', 1, formEl);
		this._appendInput('displayDateHour', 1, formEl);
		this._appendInput('displayDateMinute', 1, formEl);
		this._appendInput('displayDateMonth', 1, formEl);
		this._appendInput('displayDateYear', 1980, formEl);
		this._appendInput('entryId', item.entryId, formEl);
		this._appendInput('subtitle', '', formEl);
		this._appendInput('title', this.element.querySelector('input').value, formEl);
		this._appendInput('trackbacks', '', formEl);
		this._appendInput('-coverImageImageSelector', '', formEl);
		this._appendInput('-smallImageImageSelector', '', formEl);
		this._appendInput('formDate', new Date().getTime(), formEl);

		this.saving = true;

		Liferay.Service.post(
			'/blogs.blogsentry/update-entry',
			formEl,
			function(obj) {
				Liferay.Loader.require(
					'frontend-js-web/liferay/toast/commands/OpenToast.es',
					function(toastCommands) {
						toastCommands.openToast(
							{
								message: 'Cell edited correctly!'
							}
						);

						this.value = obj.title;
						this.saving = false;
						this.editing = false;
					}.bind(this)
				);
			}.bind(this)
		);
	}
}

Soy.register(ClayTableCellEditable, componentTemplates);

export default ClayTableCellEditable;