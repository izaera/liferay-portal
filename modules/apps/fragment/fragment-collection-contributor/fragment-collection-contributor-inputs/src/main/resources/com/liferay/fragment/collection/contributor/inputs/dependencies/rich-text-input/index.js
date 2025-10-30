const inputElement = document.getElementById(
	`${fragmentNamespace}-rich-text-input`
);
const CKEditorRequiredInput = document.getElementById(
	`${fragmentEntryLinkNamespace}-ckeditor-required`
);
const errorMessage = document.getElementById(
	`${fragmentEntryLinkNamespace}-error-message`
);
const errorMessageTextId = `${fragmentEntryLinkNamespace}-error-message-text`;
const errorMessageText = document.getElementById(errorMessageTextId);

const inputLabelElement = document.getElementById(
	`${fragmentEntryLinkNamespace}-rich-text-input-label`
);

const editorName = `${fragmentEntryLinkNamespace}-${input.name}`;

let currentLanguageId = themeDisplay.getDefaultLanguageId();

document.getElementById(editorName).name = input.name;

if (input.attributes?.readOnly) {
	if (inputElement) {
		inputElement.innerHTML = input.value;
	}
}
else if (layoutMode === 'edit') {
	if (inputElement) {
		inputElement.setAttribute('disabled', true);
	}
}
else if (layoutMode !== 'edit' && input.localizable) {
	CKEDITOR.on('instanceReady', (editorEvent) => {
		if (input.required) {
			validateInput();
		}

		if (editorEvent.editor.name === editorName) {
			editorEvent.editor.on('change', () => {
				const value = editorEvent.editor.getData();

				const translationInput =
					getOrCreateTranslationInput(currentLanguageId);

				translationInput.value = value;

				Liferay.fire('localizationSelect:updateTranslationStatus', {
					languageId: currentLanguageId,
				});

				if (currentLanguageId === themeDisplay.getDefaultLanguageId()) {
					updateCKEditorRequired(value);
				}
			});

			Liferay.on('localizationSelect:localeChanged', (event) => {
				currentLanguageId = event.languageId;

				const translationInput =
					getOrCreateTranslationInput(currentLanguageId);

				if (translationInput.getAttribute('value') !== null) {
					editorEvent.editor.setData(translationInput.value);
				}
				else {
					editorEvent.editor.setData(getDefaultLanguageValue());
				}
			});
		}
	});

	if (input.valueI18n) {
		Object.entries(input.valueI18n).forEach(([languageId, value]) => {
			const translationInput = getOrCreateTranslationInput(languageId);

			translationInput.value = Liferay.Util.unescapeHTML(value);
		});
	}
}
else if (Liferay.FeatureFlags['LPD-37927']) {
	CKEDITOR.on('instanceReady', (editorEvent) => {
		if (input.required) {
			validateInput();
		}

		if (editorEvent.editor.name === editorName) {
			editorEvent.editor.on('change', () => {
				updateCKEditorRequired(editorEvent.editor.getData());
			});

			Liferay.on('localizationSelect:localeChanged', (event) => {
				const isDefaultLanguage =
					event.languageId === themeDisplay.getDefaultLanguageId();

				const unlocalizedInfo = document.getElementById(
					`${fragmentNamespace}-unlocalized-info`
				);

				if (isDefaultLanguage) {
					editorEvent.editor.setReadOnly(false);

					unlocalizedInfo?.classList.add('d-none');
				}
				else {
					editorEvent.editor.setReadOnly(true);

					unlocalizedInfo?.classList.remove('d-none');
				}
			});
		}
	});
}

function getDefaultLanguageValue() {
	const defaultLanguageInput = getOrCreateTranslationInput(
		themeDisplay.getDefaultLanguageId()
	);

	return defaultLanguageInput.value;
}

function getOrCreateTranslationInput(languageId) {
	const inputId = `${fragmentNamespace}${input.name}_${languageId}`;

	let translationInput = document.getElementById(inputId);

	if (!translationInput) {
		translationInput = document.createElement('input');
		translationInput.type = 'hidden';
		translationInput.id = inputId;
		translationInput.name = `${input.name}_${languageId}`;

		inputLabelElement.parentElement.appendChild(translationInput);
	}

	return translationInput;
}

// Whenever the field is required, we validate if the CKEditorRequiredInput
// is valid on submit. If it is not valid, the error message will be shown
// and the field will be focused.

function validateInput() {
	CKEditorRequiredInput.addEventListener('invalid', (event) => {
		event.preventDefault();

		errorMessage.classList.remove('d-none');
		errorMessageText.textContent = errorMessageText.dataset.requiredError;

		document
			.getElementById(`cke_${editorName}`)
			.querySelector('iframe')
			.contentDocument.body.focus();
	});
}

function updateCKEditorRequired(value) {
	CKEditorRequiredInput.value = value;

	if (value) {
		errorMessage.classList.add('d-none');
		errorMessageText.textContent = '';
	}
}
