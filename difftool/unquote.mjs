export default function unquote(string) {
	if (string.startsWith('"')) {
		string = string.substring(1);
	}

	if (string.endsWith('"')) {
		string = string.substring(0, string.length -1);
	}

	return string;
}
