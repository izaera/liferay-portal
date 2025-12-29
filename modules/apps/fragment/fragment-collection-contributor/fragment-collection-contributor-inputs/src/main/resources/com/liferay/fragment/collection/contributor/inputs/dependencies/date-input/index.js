const error = document.getElementById(`${fragmentNamespace}-date-input-error`);
const inputElement = document.getElementById(`${fragmentNamespace}-date-input`);

if (inputElement) {
	if (input.attributes?.readOnly) {
		inputElement.addEventListener('keydown', (event) => {
			if (event.code === 'Space') {
				event.preventDefault();
			}
		});
	}
	else if (layoutMode === 'edit') {
		inputElement.setAttribute('disabled', true);
	}
	else {
		if (error) {

			// This delay is intentional to give screen readers time to process and
			// accept the focus change. Without this delay, the focus is often
			// ignored by the screen reader even though it works visually.

			setTimeout(() => {
				inputElement.focus();

				inputElement.scrollIntoView({
					block: 'center',
				});
			}, 50);
		}
	}
}
