import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {routing} from './app.routing';
import {FormsModule} from '@angular/forms';
import {HttpModule, JsonpModule} from '@angular/http';

import { NgTableComponent } from './Components/ng2-table/ng-table.component';
import { NgTableFilteringDirective } from './Components/ng2-table/ng-table-filtering.directive';
import { NgTablePagingDirective } from './Components/ng2-table/ng-table-paging.directive';
import { NgTableSortingDirective } from './Components/ng2-table/ng-table-sorting.directive';

import {App} from './app.component';

import { ModalModule, BsDropdownModule, PaginationModule, TabsModule, BsDatepickerModule } from 'ngx-bootstrap';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import {NgbdTabsetBasic} from './main-tabs';
import { UsersComponent } from './Components/app.users';
import { OrdersComponent } from './Components/app.orders';
import { PaymentsComponent } from './Components/app.payments';
import {FeedsComponent} from './Components/app.feeds';
import {ModalContentComponent} from "./Components/ModalContentComponent";
import {ReportsComponent} from "./Components/app.reports";

@NgModule({
    imports: [
        BrowserModule, routing, FormsModule, HttpModule, JsonpModule,
        NgbModule.forRoot(), PaginationModule.forRoot(), NgbModule.forRoot(),
        TabsModule, BsDropdownModule.forRoot(), ModalModule.forRoot(), BsDatepickerModule.forRoot(),
    ],
    entryComponents: [ModalContentComponent],
    declarations: [App, NgbdTabsetBasic, UsersComponent, OrdersComponent, PaymentsComponent, FeedsComponent,
        ModalContentComponent, ReportsComponent,
        NgTableComponent, NgTableFilteringDirective, NgTablePagingDirective, NgTableSortingDirective],
    bootstrap: [App]
})
export class AppModule {
}
