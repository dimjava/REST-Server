import {Component, OnInit, Input} from '@angular/core';
import {ModeratorService} from "../Services/ModeratorService";
import {User} from "../Interfaces/User";
import {BsModalRef, BsModalService} from "ngx-bootstrap";
import {ModalContentComponent} from "./ModalContentComponent";

@Component ({
    templateUrl: '../Views/orders.html',
    providers: [ModeratorService]
})

export class OrdersComponent implements OnInit {

    constructor(private service: ModeratorService, private modalServiceBs: BsModalService) {}

    ngOnInit(): void {
        this.onChangeTable(this.config);

    }

    public errorModal: BsModalRef;

    public page:number = 1;
    public itemsPerPage:number = 10;
    public maxSize:number = 5;
    public numPages:number = 100;
    public length:number = this.itemsPerPage * 100;

    public orderIdClicked = -1;

    public modalText = 'Loading...';
    public modalTitle = 'Loading...';

    public buttons: Array<any> = [];
    public modalButtons: Array<any> = [];

    public formLabel: string;
    public formValue: string;

    public columns: Array<any> = [
        {title: '№', name: 'N'},
        {title: 'Id', name: 'id'},
        {title: 'User name', name: 'user', filtering: {filterString: '', placeholder: 'Filter by name'}},
        {title: 'Status', name: 'status', filtering: {filterString: '', placeholder: 'Filter by status'}},
        {title: 'Solver name', name: 'solver', filtering: {filterString: '', placeholder: 'Filter by name'}},
        {title: 'Chat', name: 'chat'},
        {title: 'Date', name: 'date'},
        {title: 'Maturity', name: 'maturityDate'},
        {title: 'Price', name: 'price'},
        {title: 'Photo', name: 'pictures'},
        {title: 'Comment', name: 'comment'},
        {title: 'Review', name: 'review'}
    ];

    public config: any = {
        paging: true,
        sorting: {columns: this.columns},
        filtering: {filterString: ''},
        className: ['table-striped', 'table-bordered'],
        api: {
            onStatusChange: this.onStatusChange.bind(this),
            onGetPictures: this.onGetPictures.bind(this),
            onGetChat: this.onGetChat.bind(this),
            onGetReviewComment: this.onGetReviewComment.bind(this),
            actOnReview: this.actOnReview.bind(this),
            onGetComment: this.onGetComment.bind(this),
            onChangePrice: this.onChangePrice.bind(this),
        }
    };

    private rows: Array<User> = [];

    public userFriendlyRows() {
        this.rows.forEach((order: any) => {
            order['chatId'] = order['chat']['id'];
            order['chat'] = order['chat']['status'];
            order['user'] = order['user']['name'];

            if (order['solver'] != null) {
                order['solver'] = order['solver']['name'];
            } else {
                order['solver'] = '';
            }

            var str = new Date(order['date']).toLocaleString();
            order['date'] = str;

            str = new Date(order['maturityDate']).toLocaleString();
            order.maturityDate = str;
        });
    }

    public generateButtons(data: Array<any>) {
        this.buttons = Array(data.length);

        // buttons for status
        for (let i = 0; i < data.length; i++) {
            this.buttons[i] = new Object();

            switch (data[i]['status']) {
                case 'AVAILABLE':
                    this.buttons[i]['status'] = this.availableButton;
                    break;
                case 'MODERATION':
                    this.buttons[i]['status'] = this.moderationButton;
                    break;
                case 'REJECTED_APPEAL':
                    this.buttons[i]['status'] = this.rejectedAppealButton;
                    break;
                case 'REJECTED':
                    this.buttons[i]['status'] = this.rejectedButton;
                    break;
                default:
                    this.buttons[i] = {};
                    break;
            }


            // buttons for order's pictures
            this.buttons[i]['pictures'] = {
                type: 'simple',
                buttons: Array(data[i]['pictures'].length)
            };

            for (let j = 0; j < data[i]['pictures'].length; j++) {
                this.buttons[i]['pictures'].buttons[j] = {
                    title: '',
                    name: `${data[i]['pictures'][j].id}`,
                    styleClass: 'btn btn-default',
                    styleIcon: 'fa fa-camera',
                    action: 'onGetPictures'
                };
            }

            //buttons for chat messages
            if (data[i].chat.status !== 'DISABLED') {

                this.buttons[i]['chat'] = {
                    type: 'simple', buttons: [{
                        title: data[i].chat.status,
                        name: 'chatButton',
                        styleClass: 'btn btn-default',
                        styleIcon: 'fa fa-envelope',
                        action: 'onGetChat'
                    }]
                };
            }

            //buttons for price change
            if (data[i]['status'] == 'RESERVED' || data[i]['stauts'] == 'SOLVED') {
                this.buttons[i]['price'] = {
                    type: 'simple', buttons: [{
                        title: data[i]['price'],
                        name: 'priceChangeButton',
                        styleClass: 'btn btn-default',
                        styleIcon: 'fa fa-sort',
                        action: 'onChangePrice'
                    }]
                };
            }

            //buttons for review
            if (data[i].review) {
                this.buttons[i]['review'] = {
                    type: 'simple', buttons: [{
                        title: data[i].review.rating.toString(),
                        name: 'reviewButton',
                        styleClass: 'btn btn-default',
                        styleIcon: 'fa fa-star-o',
                        action: 'onGetReviewComment'
                    }]
                };
            } else {
                this.buttons[i]['review'] = {
                    type: 'simple', buttons: [{
                        title: '',
                        name: 'reviewButton',
                        styleClass: 'btn btn-default',
                        styleIcon: 'fa fa-star',
                        action: 'onGetReviewComment'
                    }]
                };
            }

            //show comment buttons
            this.buttons[i]['comment'] = {
                type: 'simple', buttons: [{
                    title: '',
                    name: 'commentButton',
                    styleClass: 'btn btn-default',
                    styleIcon: 'fa fa-comment-o',
                    action: 'onGetComment'
                }]
            }
        }
    }

    public saveData(data: User[]) {
        console.debug('saveData fired', data);

        this.rows = data;
        this.generateButtons(data);

        for (let i = 0; i < data.length; i++) {
            this.rows[i]['N'] = i + 1;
        }

        this.userFriendlyRows();
    }

    public onChangeTable(config:any, page:any = {page: this.page, itemsPerPage: this.itemsPerPage}):any {
        console.debug('Table change fired');

        if (config.filtering) {
            Object.assign(this.config.filtering, config.filtering);
        }

        if (page && config.paging) {
            this.service.getOrders(page.page, page.itemsPerPage,
                this.columns.find(col => col.name == 'user').filtering.filterString,
                this.columns.find(col => col.name == 'solver').filtering.filterString,
                this.columns.find(col => col.name == 'status').filtering.filterString
            ).subscribe(data => this.saveData(data));
        }
    }

    public onCellClick(data: any): any {
        console.debug('onCellClick', data);
    }

    public onStatusChange(data: any):void {
        console.debug('edited', data);

        this.service.updateOrderStatus(data.row.id, data.buttonName)
            .subscribe(data => this.onChangeTable(this.config));
    }

    public onGetPictures(data: any) {
        console.debug('get pictures', data);

        this.service.getPicture(data.row.id, data.buttonName);
    }

    public onGetChat(data: any) {
        console.debug('Getting chat info', data);

        let result = [];

        this.modalTitle = `Chat of order ${data.row.id}`;

        this.service.getMessages(data.row.chatId)
            .subscribe(data => this.updateModalText(data));

        document.getElementById('chatModalButton').click();
    }

    public actOnReview(data: any) {
        this.service.actOnReview(this.orderIdClicked).subscribe(data => this.onChangeTable(this.config));
    }

    public onGetReviewComment(data: any) {

        if (!data.row['review']) {
            return;
        }

        this.orderIdClicked = data.row['id'];

        this.modalTitle = `Review comment of order ${data.row.id}`;
        this.modalText = `${data.row['review']['comment']}`;

        if (data.row['review']['hide']) {
            let b = {
                title: 'Show',
                name: 'showReviewButton',
                styleClass: 'btn btn-success',
                action: 'actOnReview'
            };

            this.modalButtons = [b];
        } else {
            let b = {
                title: 'Hide',
                name: 'hideReviewButton',
                styleClass: 'btn btn-danger',
                action: 'actOnReview'
            };

            this.modalButtons = [b];
        }

        document.getElementById('chatModalButton').click();
    }

    public onGetComment(data: any) {
        let comment = 'There is no comment for this order';
        if (data.row['comment'] && data.row['comment'] != '') {
            comment = this.service.decodeB64(data.row['comment']);
        }

        this.modalTitle = `Comment of order ${data.row.id}`;
        this.modalText = comment;

        document.getElementById('chatModalButton').click();
    }

    public updateModalText(data: any) {
        console.debug(data);

        let name = '';

        if (data.length != 0) {
            name = data[0].user.name;
        }

        let text = ``;

        for (let i = 0; i < data.length; i++) {

            let align = "right";

            if (data[i].user.name === name) {
                align = "left";
            }

            let dt = new Date(data[i].date).toLocaleString();
            text += `<div align="center"><b>(${dt})</b></div>`;

            if (data[i].messageType.id === 0) {
                text += `<div align=${align}><b>${data[i].user.name}: </b>${this.service.decodeB64(data[i].data)}</div>`;
            } else {
                text += `<div align=${align}><b>${data[i].user.name}: </b><a href="chats/messages/${data[i].id}/v2" target="_blank">Photo</a></div>`
            }
        }

        this.modalText = text;
    }

    public onChangePrice(data: any) {
        this.formLabel = 'Price';
        this.formValue = data.row.price;
        this.modalTitle = `Change price of order ${data.row.id}`;
        this.modalText = '';
        this.orderIdClicked = data.row.id;

        document.getElementById('chatModalButton').click();
    }

    public cleanModal() {
        this.modalTitle = 'Loading...';
        this.modalText = 'Loading...';
        this.modalButtons = [];

        this.formLabel = null;
        this.formValue = null;
    }

    public onSubmit() {
        this.service.updatePrice(this.orderIdClicked.toString(), this.formValue)
            .subscribe(successData => this.openErrorModal('Сompleted'),
                errorData => this.openErrorModal(errorData));
    }

    public openErrorModal(errorText: string) {
        this.errorModal = this.modalServiceBs.show(ModalContentComponent);
        this.errorModal.content.body = errorText;
    }

    private availableButton = {
        type: 'dropdown',
        dropdownTitle: 'AVAILABLE',
        dropdownStyleClass: 'btn btn-success dropdown-toggle',
        buttons: [
            { name: 'MODERATION', title: 'MODERATION', action: 'onStatusChange' }
        ]};
    private moderationButton = {
        type: 'dropdown',
        dropdownTitle: 'MODERATION',
        dropdownStyleClass: 'btn btn-warning dropdown-toggle',
        buttons: [
            { name: 'AVAILABLE', title: 'AVAILABLE', action: 'onStatusChange' },
            { name: 'UNACCEPTABLE', title: 'UNACCEPTABLE', action: 'onStatusChange' },
        ]};
    private rejectedAppealButton = {
        type: 'dropdown',
        dropdownTitle: 'REJECTED_APPEAL',
        dropdownStyleClass: 'btn btn-danger dropdown-toggle',
        buttons: [
            { name: 'DONE_MODER', title: 'DONE_MODER', action: 'onStatusChange' },
            { name: 'REJECTED_MODER', title: 'REJECTED_MODER', action: 'onStatusChange' },
        ]};
    private rejectedButton = {
        type: 'dropdown',
        dropdownTitle: 'REJECTED',
        dropdownStyleClass: 'btn btn-warning dropdown-toggle',
        buttons: [
            { name: 'DONE_MODER', title: 'DONE_MODER', action: 'onStatusChange' },
            { name: 'REJECTED_MODER', title: 'REJECTED_MODER', action: 'onStatusChange' },
        ]};
}
