import Component from 'metal-component';
import Soy from 'metal-soy';

import componentTemplates from './ClayTableCellTimer.soy';

/**
 * Resize Component
 * @review
 */
class ClayTableCellTimer extends Component {

	/**
	 * @inheritDoc
	 * @review
	 */
	attached() {
		setInterval(
			() => {
				this.value = this.value - 1000;
			},
			Math.random() * 10
		);
	}
}

Soy.register(ClayTableCellTimer, componentTemplates);

export default ClayTableCellTimer;