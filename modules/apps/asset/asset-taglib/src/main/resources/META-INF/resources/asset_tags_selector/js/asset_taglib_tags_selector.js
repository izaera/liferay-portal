AUI.add(
	'liferay-asset-taglib-tags-selector',
	function(A) {
		var Lang = A.Lang;

		var AArray = A.Array;

		var MAP_INVALID_CHARACTERS = AArray.hash(
			[
				'"',
				'#',
				'%',
				'&',
				'*',
				'+',
				',',
				'/',
				':',
				';',
				'<',
				'=',
				'>',
				'?',
				'@',
				'[',
				'\'',
				'\\',
				'\n',
				'\r',
				']',
				'`',
				'{',
				'|',
				'}',
				'~'
			]
		);

		var TPL_DUPLICATE_ALERT = '<div class="help-block">{duplicate} {tag}: {tagName}</div>';

		var TPL_MAX_LENGTH_ALERT = '<div class="help-block">{message}</div>';

		/**
		 * OPTIONS
		 *
		 * Required
		 * hiddenInput {string}: The hidden input used to pass in the current tags.
		 * tagNames (string): The current tags.
		 *
		 */

		var AssetTaglibTagsSelector = A.Component.create(
			{
				ATTRS: {
					allowAddEntry: {
						value: true
					},

					allowAnyEntry: {
						value: true
					},

					autoHighlight: {
						value: false
					},

					dataSource: {
						valueFn: function() {
							var instance = this;

							return instance._getTagsDataSource();
						}
					},

					eventName: {
						validator: Lang.isString
					},

					groupIds: {
						setter: '_setGroupIds',
						validator: Lang.isString
					},

					guid: {
						value: ''
					},

					hiddenInput: {
						setter: function(value) {
							var instance = this;

							return A.one(value + instance.get('guid'));
						}
					},

					matchKey: {
						value: 'value'
					},

					maxLength: {
						value: 75
					},

					portletURL: {
						validator: Lang.isString
					},

					schema: {
						value: {
							resultFields: ['text', 'value']
						}
					},

					tagNames: {
						setter: function(value) {
							var instance = this;

							if (Lang.isString(value)) {
								value = value.split(',');
							}

							return value;
						},
						value: ''
					}
				},

				EXTENDS: A.TextboxList,

				NAME: 'tagselector',

				prototype: {
					renderUI: function() {
						var instance = this;

						instance.initMultiSelectComponent();
},

					bindUI: function() {
						var instance = this;
					},

					syncUI: function() {
						var instance = this;
					},

					initMultiSelectComponent: function() {
						var instance = this;

						AssetTaglibTagsSelector.superclass.renderUI.apply(instance, arguments);

						var tagNames = instance.get('tagNames');
						tagNames.forEach(instance.add, instance);

						var form = instance.inputNode.get('form');
					},

					addEntries: function() {
						var instance = this;

						instance._addEntries();
					},

					_addEntries: function() {
						var instance = this;

						var text = Lang.String.escapeHTML(instance.inputNode.val()).trim();

						if (text) {
							if (text.indexOf(',') > -1) {
								var items = text.split(',');

								items.forEach(
									function(item, index) {
										instance.entries.add(item, {});
									}
								);
							}
							else {
								instance.entries.add(text, {});
							}
						}

						Liferay.Util.focusFormField(instance.inputNode);
					},

					_bindTagsSelector: function() {
						var instance = this;

						var form = instance.inputNode.get('form');

						instance._submitFormListener = A.Do.before(instance._addEntries, form, 'submit', instance);

						instance.get('boundingBox').on('keypress', instance._onKeyPress, instance);

						instance.get('boundingBox').after('paste', instance._onPaste, instance);
					},

					_checkDuplicateTag: function(object) {
						var instance = this;

						var tag = !object.value ? object : object.value;

						if (!instance.entries.containsKey(tag)) {
							return;
						}

						var message = Lang.sub(
							TPL_DUPLICATE_ALERT,
							{
								duplicate: Liferay.Language.get('duplicate'),
								tag: Liferay.Language.get('tag'),
								tagName: tag
							}
						);

						instance._showError(message);
					},

					_checkMaxLengthTag: function(object) {
						var instance = this;

						var tag = !object.value ? object : object.value;

						var maxLength = instance.get('maxLength');

						if (!tag.length || (tag.length <= maxLength)) {
							return;
						}

						var message = Lang.sub(
							TPL_MAX_LENGTH_ALERT,
							{
								message: Lang.sub(Liferay.Language.get('please-enter-no-more-than-x-characters'), [maxLength])
							}
						);

						instance._showError(message);

						return new A.Do.Halt();
					},

					_getTagsDataSource: function() {
						var instance = this;

						var AssetTagSearch = Liferay.Service.bind('/assettag/search');

						AssetTagSearch._serviceQueryCache = {};

						var serviceQueryCache = AssetTagSearch._serviceQueryCache;

						var dataSource = new Liferay.Service.DataSource(
							{
								on: {
									request: function(event) {
										var term = decodeURIComponent(event.request);

										var key = term;

										if (term == '*') {
											term = '';
										}

										var serviceQueryObj = serviceQueryCache[key];

										if (!serviceQueryObj) {
											serviceQueryObj = {
												end: 20,
												groupIds: instance.get('groupIds'),
												name: '%' + term + '%',
												start: 0,
												tagProperties: ''
											};

											serviceQueryCache[key] = serviceQueryObj;
										}

										event.request = serviceQueryObj;
									}
								},
								source: AssetTagSearch
							}
						).plug(
							A.Plugin.DataSourceCache,
							{
								max: 500
							}
						);

						return dataSource;
					},

					_onAddEntryClick: function(event) {
						var instance = this;

						event.domEvent.preventDefault();

						instance._addEntries();
					},

					_onKeyPress: function(event) {
						var instance = this;

						var charCode = event.charCode;

						if (!A.UA.gecko || event._event.charCode) {
							if (charCode == '44') {
								event.preventDefault();

								instance._addEntries();
							}
							else if (MAP_INVALID_CHARACTERS[String.fromCharCode(charCode)]) {
								event.halt();
							}
						}
					},

					_onPaste: function(event) {
						var instance = this;

						var pastedText = (event._event.clipboardData || window.clipboardData).getData('text');

						if (pastedText.indexOf(',') !== -1) {
							requestAnimationFrame(
								function() {
									instance.addEntries();
								}
							);
						}
					},

					_renderIcons: function() {
						var instance = this;

						var contentBox = instance.get('contentBox');

						var buttonGroup = [];

						if (instance.get('portletURL')) {
							buttonGroup.unshift(
								{
									label: Liferay.Language.get('select'),
									on: {
										click: A.bind('_showSelectPopup', instance)
									},
									title: Liferay.Language.get('select-tags')
								}
							);
						}

						if (instance.get('allowAddEntry')) {
							buttonGroup.unshift(
								{
									label: Liferay.Language.get('add'),
									on: {
										click: A.bind('_onAddEntryClick', instance)
									},
									title: Liferay.Language.get('add-tags')
								}
							);
						}

						instance.icons = new A.Toolbar(
							{
								children: [buttonGroup]
							}
						).render(contentBox);

						var iconsBoundingBox = instance.icons.get('boundingBox');

						instance.entryHolder.placeAfter(iconsBoundingBox);
					},

					_setGroupIds: function(value) {
						return value.split(',');
					},

					_showError: function(message) {
						var instance = this;

						var contentBox = instance.get('contentBox');

						var toolbar = instance.icons.get('contentBox');

						contentBox.addClass('has-error');

						var alertNode = toolbar.insertBefore(message, toolbar);

						A.later(
							5000,
							instance,
							function() {
								alertNode.remove();

								contentBox.removeClass('has-error');
							},
							{},
							false
						);
					},

					showMultiSelectPopUp: function(event, callback) {
						var instance = this;

						event.preventDefault();

						var uri = Lang.sub(
							decodeURIComponent(instance.get('portletURL')),
							{
								selectedTagNames: instance.entries.keys.join()
							}
						);

						var itemSelectorDialog = new A.LiferayItemSelectorDialog(
							{
								eventName: instance.get('eventName'),
								on: {
									selectedItemChange: function(event) {
										var selectedItem = event.newVal;

										if (selectedItem) {
											instance.entries.each(
												function(item) {
													instance.entries.remove(item);
												}
											);

											event.selectedItems = [];
											AArray.each(
												selectedItem.items.split(','),
												function(value) {
													instance.add(value);
													event.selectedItems.push({label: value,
														value: value});
												}
											);
											if (callback) {
												callback(event);
											}
										}
									}
								},
								'strings.add': Liferay.Language.get('done'),
								title: Liferay.Language.get('tags'),
								url: uri
							}
						);

						itemSelectorDialog.open();
					},

					multiSelectRemoveTag: function(event) {
						var instance = this;
						const item = event.data.item.value;
						instance.entries.remove(item);
						console.log(instance.entries.keys);
},

					getSelectedItems: function() {
						var instance = this;
						return instance.entries.keys.map(key => {
							return {label: key,
								value: key};
						});
					},

					multiSelectAddEntries: function(selectedItems) {
						var instance = this;

						instance._multiSelectAddEntries(selectedItems);
					},

					_multiSelectAddEntries: function(selectedItems) {
						var instance = this;
						selectedItems.forEach(item => {
							instance.entries.add(item.value, item.label);
						});
						instance._updateHiddenInput();
						Liferay.Util.focusFormField(instance.inputNode);
					},

					_showSelectPopup: function(event) {
						var instance = this;

						event.domEvent.preventDefault();

						var uri = Lang.sub(
							decodeURIComponent(instance.get('portletURL')),
							{
								selectedTagNames: instance.entries.keys.join()
							}
						);

						var itemSelectorDialog = new A.LiferayItemSelectorDialog(
							{
								eventName: instance.get('eventName'),
								on: {
									selectedItemChange: function(event) {
										var selectedItem = event.newVal;

										if (selectedItem) {
											instance.entries.each(
												function(item) {
													instance.entries.remove(item);
												}
											);

											AArray.each(
												selectedItem.items.split(','),
												function(value) {
													instance.add(value);
												}
											);
										}
									}
								},
								'strings.add': Liferay.Language.get('done'),
								title: Liferay.Language.get('tags'),
								url: uri
							}
						);

						itemSelectorDialog.open();
					},

					_updateHiddenInput: function(event) {
						var instance = this;

						var hiddenInput = instance.get('hiddenInput');

						hiddenInput.val(instance.entries.keys.join());
					}
				}
			}
		);

		Liferay.AssetTaglibTagsSelector = AssetTaglibTagsSelector;
	},
	'',
	{
		requires: ['aui-io-plugin-deprecated', 'aui-live-search-deprecated', 'aui-template-deprecated', 'aui-textboxlist-deprecated', 'datasource-cache', 'liferay-item-selector-dialog', 'liferay-service-datasource']
	}
);