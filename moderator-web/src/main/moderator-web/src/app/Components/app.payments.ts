import {Component, OnInit} from '@angular/core';
import {ModeratorService} from "../Services/ModeratorService";
import {ModalContentComponent} from "./ModalContentComponent";
import {BsModalRef, BsModalService} from "ngx-bootstrap";

@Component({
    templateUrl: '../Views/payments.html',
    providers: [ModeratorService]
})

export class PaymentsComponent implements OnInit {

    public errorModal: BsModalRef;

    public page:number = 1;
    public itemsPerPage:number = 10;
    public maxSize:number = 5;
    public numPages:number = 100;
    public length:number = this.itemsPerPage * 100;

    name2Add: string;
    amount2Add: number;
    commission: string;
    comment2Add: string;

    public buttons: Array<any> = [];

    public columns: Array<any> = [
        {title: '№', name: 'N'},
        {title: 'Id', name: 'id'},
        {title: 'Name', name: 'user', filtering: {filterString: '', placeholder: 'Filter by name'}},
        {title: 'Date', name: 'date'},
        {title: 'Amount', name: 'amount'},
        {title: 'Commission', name: 'commission'},
        {title: 'Comment', name: 'comment'}
    ];

    public config: any = {
        paging: true,
        sorting: {columns: this.columns},
        filtering: {filterString: ''},
        className: ['table-striped', 'table-bordered'],
        columnActions: true,
    };

    private rows: Array<any> = [];

    constructor(private service: ModeratorService, private modalServiceBs: BsModalService) {}

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
        this.rows.forEach((payment: any) => {
            payment['user'] = payment['user']['name'];

            var str = new Date(payment['date']).toLocaleString();
            payment['date'] = str;
        });
    }

    public onChangeTable(config:any, page:any = {page: this.page, itemsPerPage: this.itemsPerPage}):any {

        if (config.filtering) {
            Object.assign(this.config.filtering, config.filtering);
        }

        if (page && config.paging) {
            this.service.getPayments(page.page, page.itemsPerPage,
                this.columns[2].filtering.filterString)
                .subscribe(data => this.saveData(data));
        }
    }

    public clearFundsModal() {
        this.amount2Add = null;
        this.comment2Add = '';
        this.name2Add = '';
    }

    public onSubmit() {
        this.service.manageFunds(this.name2Add, this.amount2Add, this.commission, this.comment2Add)
            .subscribe(successData => this.openErrorModal('Сompleted'),
            errorData => this.openErrorModal(errorData));
    }

    public openErrorModal(errorText: string) {
        this.errorModal = this.modalServiceBs.show(ModalContentComponent);
        this.errorModal.content.body = errorText;
    }
}
