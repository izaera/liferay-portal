<script type="text/javascript">
	Liferay.Loader.require(
		"[$PACKAGE$]/[$MODULE$]",
		function(module) {
			var initializer;

			if (typeof module.default === 'function') {
				initializer = module.default;
			}
			else if (typeof module === 'function') {
				initializer = module;
			}

			if (initializer) {
				initializer(
					{
						configuration: {
							portletInstance: {},
							system: {}
						},
						contextPath: '[$CONTEXT_PATH$]',
						portletElementId: '[$PORTLET_ELEMENT_ID$]',
						portletNamespace: '[$PORTLET_NAMESPACE$]'
					});
			}
			else {
				console.error(
					'Module', '[$PACKAGE$]/[$MODULE$]', 'is not exporting a',
					'function: cannot initialize it.');
			}

		});
</script>