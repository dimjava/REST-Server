import { Routes, RouterModule } from '@angular/router';
import { UsersComponent } from './Components/app.users';
import { OrdersComponent } from './Components/app.orders';
import { PaymentsComponent } from './Components/app.payments';
import {FeedsComponent} from './Components/app.feeds';
import {ReportsComponent} from "./Components/app.reports";

const appRoutes: Routes = [
    {path: "Users", component: UsersComponent},
    {path: "Orders", component: OrdersComponent},
    {path: "Payments", component: PaymentsComponent},
    {path: "Feeds", component: FeedsComponent},
    {path: "Reports", component: ReportsComponent},
];

export const appRoutingProviders: any[] = [];
export const routing = RouterModule.forRoot(appRoutes);
