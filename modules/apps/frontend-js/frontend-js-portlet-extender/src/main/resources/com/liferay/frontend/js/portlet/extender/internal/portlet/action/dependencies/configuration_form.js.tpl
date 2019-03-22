var Lang = A.Lang;

alert("hola");

debugger;
/*
var liferayDDMForm = Liferay.component(
	'[$PORTLET_NAMESPACE$][$ESCAPED_FIELDS_NAMESPACE$]ddmForm',
	new Liferay.DDM.Form(
		{
//			container: '#<%= randomNamespace %>',
//			ddmFormValuesInput: '#[$PORTLET_NAMESPACE$]<%= HtmlUtil.getAUICompatibleId(ddmFormValuesInputName) %>',
//			defaultEditLocale: '<%= (defaultEditLocale == null) ? StringPool.BLANK : HtmlUtil.escapeJS(defaultEditLocale.toString()) %>',
//			documentLibrarySelectorURL: '<%= documentLibrarySelectorURL %>',
			definition: [$DEFINITION$],
//			doAsGroupId: <%= scopeGroupId %>,
			fieldsNamespace: '[$ESCAPED_FIELDS_NAMESPACE$]',
//			imageSelectorURL: '<%= imageSelectorURL %>',
//			mode: '<%= HtmlUtil.escapeJS(mode) %>',
			p_l_id: [$PLID$],
			portletNamespace: '[$PORTLET_NAMESPACE$]',
//			repeatable: <%= repeatable %>,
//			requestedLocale: '<%= (requestedLocale == null) ? StringPool.BLANK : HtmlUtil.escapeJS(requestedLocale.toString()) %>',
//			synchronousFormSubmission: <%= synchronousFormSubmission %>

//			<c:if test="<%= ddmFormValues != null %>">
//				, values: <%= DDMUtil.getDDMFormValuesJSONString(ddmFormValues) %>
//			</c:if>
		}
	)
);

var onDestroyPortlet = function(event) {
	if (event.portletId === '[$PORTLET_DISPLAY_ID$]') {
		liferayDDMForm.destroy();

		Liferay.detach('inputLocalized:localeChanged', onLocaleChange);
		Liferay.detach('destroyPortlet', onDestroyPortlet);
	}
};

Liferay.on('destroyPortlet', onDestroyPortlet);

//var onLocaleChange = function(event) {
//	var languageId = event.item.getAttribute('data-value');
//
//	languageId = languageId.replace('_', '-');
//
//	var triggerContent = Lang.sub(
//		'<span class="inline-item">{flag}</span><span class="btn-section">{languageId}</span>',
//		{
//			flag: Liferay.Util.getLexiconIconTpl(languageId.toLowerCase()),
//			languageId: languageId
//		}
//	);
//
//	var trigger = A.one('#[$PORTLET_NAMESPACE$][$FIELDS_NAMESPACE$]Menu');
//
//	trigger.setHTML(triggerContent);
//};

//Liferay.on('inputLocalized:localeChanged', onLocaleChange);

//window.fireLocaleChanged = function(event) {
//	Liferay.fire(
//		'inputLocalized:localeChanged',
//		{
//			item: event.currentTarget
//		}
//	);
//};
*/