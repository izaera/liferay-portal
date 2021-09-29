import {State} from '@liferay/frontend-js-state-web/index';

import {counterAtomReact, userAtom} from './sharedState';

const buttonElementJSP = document.getElementById('test-button-jsp');
const buttonElementReact = document.getElementById('test-button-react');
const counterElement = document.getElementById('test-counter-jsp');
const nameElement = document.getElementById('test-name');

const counterAtom = State.atom('test-counter-jsp', 0);

State.subscribe(counterAtom, (newVal) => {
	counterElement.innerText = newVal;
});

State.subscribe(userAtom, (event) => {
	nameElement.innerText = event.name;
});

if (buttonElementJSP) {
	buttonElementJSP.addEventListener('click', () => {
		State.write(counterAtom, State.read(counterAtom) + 1);
	});
}

if (buttonElementReact) {
	buttonElementReact.addEventListener('click', () => {
		State.write(
			counterAtomReact,
			State.read(counterAtomReact) + 1
		);
	});
}
