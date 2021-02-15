import mainCssSource from "clay-charts/lib/css/main.css";
import tilesSvgSource from "clay-charts/lib/svg/tiles.svg";

const node = document.createElement('code');

node.innerHTML = `
	<style>${mainCssSource}</style>
	${tilesSvgSource}
`;

document.body.appendChild(node);