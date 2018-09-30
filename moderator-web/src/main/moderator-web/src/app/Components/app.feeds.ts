import {Component, OnInit} from '@angular/core';
import {ModeratorService} from "../Services/ModeratorService";

@Component({
    templateUrl: '../Views/feeds.html',
    providers: [ModeratorService]
})

export class FeedsComponent implements OnInit {

    names2Notify = '';
    title2Notify = '';
    message2Notify = '';

    public page:number = 1;
    public itemsPerPage:number = 10;
    public maxSize:number = 5;
    public numPages:number = 100;
    public length:number = this.itemsPerPage * 100;

    public buttons: Array<any> = [];

    public columns: Array<any> = [
        {title: '№', name: 'N'},
        {title: 'Id', name: 'id'},
        {title: 'Name', name: 'user', filtering: {filterString: '', placeholder: 'Filter by name'}},
        {title: 'Date', name: 'date'},
        {title: 'Notification', name: 'notification'},
    ];

    public config: any = {
        paging: true,
        sorting: {columns: this.columns},
        filtering: {filterString: ''},
        className: ['table-striped', 'table-bordered'],
        columnActions: true,
    };

    private rows: Array<any> = [];

    constructor(private service: ModeratorService) {}

    ngOnInit(): void {
        this.onChangeTable(this.config);
    }

    public saveData(data: any[]) {
        console.debug('saveData fired', data);

        this.rows = data;
        this.buttons = Array(data.length).fill({}, 0, data.length);

        for (let i = 0; i < data.length; i++) {
            this.rows[i]['N'] = i + 1;
        }

        this.userFriendlyRows();
    }

    public userFriendlyRows() {
        this.rows.forEach((feed: any) => {
            feed['user'] = feed['user']['name'];

            var str = new Date(feed['date']).toLocaleString();
            feed['date'] = str;
        });
    }

    public onChangeTable(config:any, page:any = {page: this.page, itemsPerPage: this.itemsPerPage}):any {

        if (config.filtering) {
            Object.assign(this.config.filtering, config.filtering);
        }

        if (page && config.paging) {
            this.service.getFeeds(page.page, page.itemsPerPage,
                this.columns[2].filtering.filterString)
                .subscribe(data => this.saveData(data));
        }
    }

    public onSubmit() {
        this.service.notify(this.names2Notify, this.title2Notify, this.message2Notify).subscribe();
    }

    public clearNotification() {
        this.names2Notify = '';
        this.title2Notify = '';
        this.message2Notify = '';
    }
}
