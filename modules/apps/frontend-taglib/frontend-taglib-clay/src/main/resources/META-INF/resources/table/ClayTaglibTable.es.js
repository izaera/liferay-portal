import 'clay-table';
import 'clay-pagination-bar';

import ClayComponent from 'clay-component';
import Soy from 'metal-soy';

import {Config} from 'metal-state';

import templates from './ClayTaglibTable.soy';

class ClayTaglibTable extends ClayComponent {

	_handleItemToggled(event) {

	}

	_handleItemsPerPageClicked(event) {
		if (this.disableAJAX) {
			return;
		}

		event.preventDefault();

		if (this.pageSize == event.data.item.label) {
			return;
		}

		this.pageSize = event.data.item.label;

		this.paginationSelectedEntry = this.paginationEntries.map((x) => x.label).indexOf(this.pageSize);

		this._loadData();
	}

	_handlePageClicked(event) {
		if (this.disableAJAX) {
			return;
		}

		let newPage = parseInt(event.data.page, 10);

		if (this.currentPage == newPage) {
			return;
		}

		this.currentPage = newPage;

		this._loadData();
	}

	_loadData() {
		fetch(
			this._getApiURL(),
			{
				method: 'GET'
			}
		)
			.then(response => response.json())
			.then(
				updatedItems => {
					this.items = updatedItems;
				}
			)
			.catch(
				err => {}
			);
	}

	_getApiURL() {
		let url = this.dataContributorAPI;

		url = url + '&pageSize=' + this.pageSize;

		url = url + '&page=' + this.currentPage;

		return url;
	}

}

Soy.register(ClayTaglibTable, templates);

ClayTaglibTable.STATE = {
	currentPage: Config.number(),
	dataContributorAPI: Config.string(),
	disableAJAX: Config.bool(),
	pageSize: Config.number(),
	paginationBaseHref: Config.string(),
	paginationEntries: Config.array(),
	paginationSelectedEntry: Config.number(),
	items: Config.array()
};

export {ClayTaglibTable};
export default ClayTaglibTable;