import {Component, OnInit, TemplateRef} from '@angular/core';
import {ModeratorService} from '../Services/ModeratorService';
import {User} from "../Interfaces/User";

import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';
import {NgbModal, ModalDismissReasons} from '@ng-bootstrap/ng-bootstrap';
import {ModalContentComponent} from "./ModalContentComponent";

@Component({
    templateUrl: '../Views/users.html',
    providers: [ModeratorService]
})

export class UsersComponent implements OnInit {

    public errorModal: BsModalRef;

    public page:number = 1;
    public itemsPerPage:number = 10;
    public maxSize:number = 5;
    public numPages:number = 100;
    public length:number = this.itemsPerPage * 100;

    // solver registration modal fields
    login: string = '';
    password: string = '';
    email: string = '';
    phone: string = '';
    rating: string = '';
    education: string = '';
    info: string = '';

    constructor(private service: ModeratorService, private modalService: NgbModal,
                private modalServiceBs: BsModalService) {}

    ngOnInit(): void {
        this.onChangeTable(this.config);
    }

    public buttons: Array<any> = [];

    public columns: Array<any> = [
        {title: '№', name: 'N'},
        {title: 'Name', name: 'name', filtering: {filterString: '', placeholder: 'Filter by name'}},
        {title: 'Phone', name: 'phone', filtering: {filterString: '', placeholder: 'Filter by phone'}},
        {title: 'Code', name: 'code'},
        {title: 'Funds', name: 'funds', sort: ''},
        {title: 'Reserved', name: 'reservedFunds', sort: ''},
        {title: 'Rating', name: 'rating'},
        {title: 'Registration date', name: 'registrationDate', sort: ''},
        {title: 'Last visit', name: 'lastVisit', sort: ''},
        {title: 'Type', name: 'isCustomer', sort: ''},
        {title: 'Promocode', name: 'promocode'},
        {title: 'Status', name: 'enabled'},
    ];

    public config: any = {
        paging: true,
        sorting: {columns: this.columns},
        filtering: {filterString: ''},
        className: ['table-striped', 'table-bordered'],
        columnActions: true,
        api: {
            onShowCode: this.showCode.bind(this),
        }
    };

    private rows: Array<User> = [];

    public saveData(data: User[], isNewData: boolean) {
        console.debug(data);

        this.generateButtons(data);

        this.rows = data;
        for (let i = 0; i < data.length; i++) {
            this.rows[i]['N'] = i + 1;
        }

        this.userFriendlyRows(isNewData);
    }

    public generateButtons(data: Array<any>) {
        this.buttons = Array(data.length);

        //code buttons
        for (let i = 0; i < data.length; i++) {
            this.buttons[i] = new Object();

            this.buttons[i]['code'] = {
                type: 'simple', buttons: [{
                    title: data[i].code ? data[i].code : '',
                    name: 'codeButton',
                    styleClass: 'btn btn-default',
                    styleIcon: 'fa fa-eye',
                    action: 'onShowCode'
                }]
            };
        }

    }

    public userFriendlyRows(isNewData: boolean) {
        this.rows.forEach((user: User) => {

            if (isNewData) {
                var str = new Date(user['registrationDate']).toUTCString();
                user['registrationDate'] = str.slice(0, str.length - 4);

                str = new Date(user['lastVisit']).toUTCString();
                user.lastVisit = str.slice(0, str.length - 4);
            }

            if (user['isCustomer'] == true || user['isCustomer'] == false) {
                user['isCustomer'] = user['isCustomer'] == true ? 'CUSTOMER' : `<span style="color: blue; ">SOLVER</span>`;
            }

            if (user['enabled'] == true) {
                user['enabled'] = `<button class="btn btn-outline-primary"><span style="color: red;">DISABLE</span></button>`
            } else if (user['enabled'] == false) {
                user['enabled'] = `<button class="btn btn-outline-primary"><span style="color: green;">ENABLE</span></button>`
            }
        });
    }

    public onChangeTable(config:any, page:any = {page: this.page, itemsPerPage: this.itemsPerPage}):any {

        if (config.filtering) {
            Object.assign(this.config.filtering, config.filtering);
        }

        if (page && config.paging) {
            this.service.getUsers(page.page, page.itemsPerPage,
                this.columns[1].filtering.filterString,
                this.columns[2].filtering.filterString)
                .subscribe(data => this.saveData(data, true));
        }
    }

    public onCellClick(data: any): any {
        console.debug(data);

        if (data.column == 'enabled') {
            this.service.blockUser(data.row.name)
                .subscribe(data => this.onChangeTable(this.config));
        }
    }

    public open(content: any) {
        console.debug(content);

        this.modalService.open(content).result.then((result) => {
            this.closeResult = `Closed with: ${result}`;
        }, (reason) => {
            this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
        });
    }

    closeResult: string;

    private getDismissReason(reason: any): string {
        if (reason === ModalDismissReasons.ESC) {
            return 'by pressing ESC';
        } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
            return 'by clicking on a backdrop';
        } else {
            return  `with: ${reason}`;
        }
    }

    public onSolverRegistrationSubmit() {
        console.debug('solver registration');

        this.service.registerSolver(this.login, this.password, this.email, this.phone,
            this.rating, this.education, this.info).subscribe(successData => this.openErrorModal('Registration completed'),
            errorData => this.openErrorModal(errorData));
    }

    public cleanRegistrationModal() {
        this.login = '';
        this.password = '';
        this.email = '';
        this.phone = '';
        this.rating = '';
        this.education = '';
        this.info = '';
    }

    public openErrorModal(errorText: string) {
        this.errorModal = this.modalServiceBs.show(ModalContentComponent);
        this.errorModal.content.body = errorText;
    }

    public showCode(data: any) {

        console.debug(this.rows)
        console.debug(data.row)

        this.service.getUserCode(data.row['name']).subscribe(code => {
            this.rows[data.row.N - 1]['code'] = code;
            this.saveData(this.rows, false);
        })
    }
}
