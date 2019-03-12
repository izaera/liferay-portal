import {Config} from 'metal-state';
import ClayComponent from 'clay-component';
import Soy from 'metal-soy';

import 'clay-table';
import 'clay-pagination-bar';

import templates from './Table.soy';

class Table extends ClayComponent {
	_handlePageSizeClicked(event) {
		event.preventDefault();

		this.pageSize = event.data.item.label;

		this.paginationSelectedEntry = this.paginationEntries.map(
			x => x.label
		).indexOf(this.pageSize);

		const maxPage = Math.floor((this.totalItems + this.pageSize - 1) / this.pageSize);

		console.log('page', this.page, 'maxPage', maxPage);

		if (this.page > maxPage) {
			this.page = maxPage;
		}

		this._loadData();
	}

	_handlePageClicked(event) {
		event.preventDefault();

		this.page = parseInt(event.data.page, 10);

		this._loadData();
	}

	_loadData() {
		fetch(this._getDataSourceURL())
			.then(response => response.json())
			.then(
				result => {
					this.items = result.items;
				}
			);
	}

	_getDataSourceURL() {
		const separator = (this.dataSourceURL.indexOf('?') != -1) ? '&' : '?';

		return `${this.dataSourceURL}${separator}${this.pageSizeParamName}=${this.pageSize}&${this.pageParamName}=${this.page}`;
	}
}

Soy.register(Table, templates);

Table.STATE = {
	dataSourceURL: Config.string(),
	items: Config.array(),
	page: Config.number(),
	pageParamName: Config.string(),
	pageSize: Config.number(),
	pageSizeParamName: Config.string(),
	paginationEntries: Config.array(),
	paginationSelectedEntry: Config.number(),
	totalItems: Config.number()
};

export {Table};
export default Table;