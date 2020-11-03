import {fetch} from 'frontend-js-web';

export default () => {
	return fetch(
		'http://localhost:8080',
		{
			method: 'GET',
		}
	).then(x => {
		console.log("Fetch finished:", x);
	});
}