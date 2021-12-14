<script type="module">
	import initializer from '[$CONTEXT_PATH$]/[$PACKAGE_MODULE$]';

	initializer(
		{
			configuration: {
				portletInstance: JSON.parse('[$PORTLET_INSTANCE_CONFIGURATION$]'),
				system: JSON.parse('[$SYSTEM_CONFIGURATION$]')
			},
			contextPath: '[$CONTEXT_PATH$]',
			portletElementId: '[$PORTLET_ELEMENT_ID$]',
			portletNamespace: '[$PORTLET_NAMESPACE$]'
		}
	);
</script>