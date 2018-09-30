import {Component, OnInit, TemplateRef} from '@angular/core';
import {ModeratorService} from '../Services/ModeratorService';
import {BsModalRef, BsModalService} from "ngx-bootstrap";
import {ModalContentComponent} from "./ModalContentComponent";

@Component({
    templateUrl: '../Views/reports.html',
    providers: [ModeratorService],
})

export class ReportsComponent implements OnInit {

    public errorModal: BsModalRef;

    minDate = new Date(2017, 5, 10);
    maxDate = new Date(2018, 9, 15);

    bsValue: Date = new Date();
    // default period is last month
    bsRangeValue: any = [new Date(this.bsValue.getTime() - 1000 * 60 * 60 * 24 * 30), this.bsValue];

    public receivedBonFunds = 0;
    public receivedRealFunds = 0;
    public payedBonNoComm = 0;
    public payedRealNoComm = 0;
    public payedBonCom = 0;
    public payedRealCom = 0;

    constructor(private service: ModeratorService, private modalServiceBs: BsModalService) {}

    ngOnInit(): void {
        this.getReport();
    }

    public getReport() {
        this.service.getReport(this.bsRangeValue[0].getTime(), this.bsRangeValue[1].getTime())
            .subscribe(data => this.updateTable(data));
    }

    public updateTable(data) {
        this.receivedBonFunds = data[0];
        this.receivedRealFunds = data[1];
        this.payedBonNoComm = data[2];
        this.payedRealNoComm = data[3];
        this.payedBonCom = data[4];
        this.payedRealCom = data[5];
    }

    public consistencyCheck() {
        this.service.consistencyCheck()
            .subscribe(data => this.openErrorModal(data));
    }

    public openErrorModal(data: any) {
        let arr: Array<any> = data;

        if (arr.length == 0) {
            this.errorModal = this.modalServiceBs.show(ModalContentComponent);
            this.errorModal.content.body = 'Everything is OK baby, sleep well\n';

            return;
        }

        this.errorModal = this.modalServiceBs.show(ModalContentComponent);
        let body = 'Problem with these users: ';
        for (let i = 0; i < arr.length; i++) {
            body += (arr[i] + ', ');
        }

        this.errorModal.content.body = body;
    }
}
