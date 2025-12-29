const error = document.getElementById(
	`${fragmentNamespace}-numeric-input-error`
);
const numericInput = fragmentElement.querySelector(
	`#${fragmentNamespace}-numeric-input`
);

const isInteger = input.attributes.dataType === 'integer';

function handleOnKeydown(event) {
	if (
		(isInteger && (event.key === ',' || event.key === '.')) ||
		event.key === '+'
	) {
		event.preventDefault();
	}
}

function handleOnKeyUp(event) {
	if (!isInteger) {
		event.target.setCustomValidity('');

		if (event.target.checkValidity()) {
			const numDecimals = input.attributes.step.length - 2;
			const [, decimalPart = ''] = event.target.value.split(/[.,]/);

			if (decimalPart.length > numDecimals) {
				event.target.setCustomValidity(
					numericInput.getAttribute('data-validation-message-text')
				);
			}
		}
	}
}

if (layoutMode === 'edit') {
	numericInput.setAttribute('disabled', true);
}
else {
	numericInput.addEventListener('keydown', handleOnKeydown);
	numericInput.addEventListener('keyup', handleOnKeyUp);

	if (error) {

		// This delay is intentional to give screen readers time to process and
		// accept the focus change. Without this delay, the focus is often
		// ignored by the screen reader even though it works visually.

		setTimeout(() => {
			numericInput.focus();

			numericInput.scrollIntoView({
				block: 'center',
			});
		}, 50);
	}
}
