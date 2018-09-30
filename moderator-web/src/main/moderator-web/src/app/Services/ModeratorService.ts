import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { Http, Response, URLSearchParams, Headers, RequestOptions } from '@angular/http'
import { Observable } from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {User} from "../Interfaces/User";

@Injectable()
export class ModeratorService {
    private url = environment.url;

    constructor (private http: Http) {}

    public decodeB64(str: string) {
        return decodeURIComponent(atob(str).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
    }

    getUsers(page: number, perPage: number, nameFilter: string, phoneFilter: string): Observable<User[]> {

        let params: URLSearchParams = new URLSearchParams();
        params.set('page', page.toString());
        params.set('perPage', perPage.toString());
        params.set('nameFilter', nameFilter);
        params.set('phoneFilter', phoneFilter);

        return this.http.get(this.url + "users/", {search: params})
            .map(this.extractData)
            .catch(this.handleError);
    }

    blockUser(name: string): Observable<any[]> {
        let params: URLSearchParams = new URLSearchParams();
        params.set("name", name);

        return this.http.get(this.url + "users/block/", {search: params})
            .map(this.ignoreData)
            .catch(this.handleError);
    }

    getOrders(page: number, perPage: number, nameFilter: string, solverNameFilter: string, status: string): Observable<any[]> {
        let params: URLSearchParams = new URLSearchParams();
        params.set('page', page.toString());
        params.set('perPage', perPage.toString());
        params.set('nameFilter', nameFilter);
        params.set('solverNameFilter', solverNameFilter);
        params.set('status', status);

        return this.http.get(this.url + "orders/", {search: params})
            .map(this.extractData)
            .catch(this.handleError);
    }

    updateOrderStatus(id: number, status: string) {
        let params: URLSearchParams = new URLSearchParams();
        params.set('status', status);

        return this.http.get(this.url + 'orders/' + id.toString(), {search: params})
            .map(this.extractData)
            .catch(this.handleError);
    }

    getPicture(orderId: number, pictureId: number) {
        window.open(this.url + `orders/${orderId}/pictures/${pictureId}`);
    }

    getMessages(chatId: number): Observable<any[]> {
        return this.http.get(this.url + `chats/${chatId}`)
            .map(this.extractData)
            .catch(this.handleError);
    }

    getPayments(page: number, perPage: number, nameFilter: string) {
        let params: URLSearchParams = new URLSearchParams();
        params.set('page', page.toString());
        params.set('perPage', perPage.toString());
        params.set('nameFilter', nameFilter);

        return this.http.get(this.url + "payments/", {search: params})
            .map(this.extractData)
            .catch(this.handleError);
    }

    getFeeds(page: number, perPage: number, nameFilter: string) {
        let params: URLSearchParams = new URLSearchParams();
        params.set('page', page.toString());
        params.set('perPage', perPage.toString());
        params.set('nameFilter', nameFilter);

        return this.http.get(this.url + "feeds/", {search: params})
            .map(this.extractData)
            .catch(this.handleError);
    }

    notify(names: string, title: string, message: string) {
        let body = {names: names, title: title, message: message};
        let headers = new Headers({ 'Content-Type': 'text/plain; charset=UTF-8' });
        let options = new RequestOptions({ headers: headers });

        return this.http.post(this.url + 'notify/', JSON.stringify(body), options)
            .map(this.ignoreData)
            .catch(this.handleError);
    }

    registerSolver(login: string, password: string, email: string, phone: string,
                    rating: string, education: string, info: string) {
        let body = {name: login, password: password, email: email, phone: phone,
                        rating: rating, education: education, info: info
                    };
        let headers = new Headers({ 'Content-Type': 'text/plain; charset=UTF-8' });
        let options = new RequestOptions({ headers: headers });

        return this.http.post(this.url + 'registration/solver/', JSON.stringify(body), options)
            .map(this.ignoreData)
            .catch(this.handleError);
    }

    manageFunds(name: string, amount: number, commission: string, comment: string) {
        let params: URLSearchParams = new URLSearchParams();
        params.set('name', name);
        params.set('amount', amount.toString());
        params.set('comment', comment);
        params.set('commission', commission);

        return this.http.get(this.url + 'funds/release', {search: params})
            .map(this.ignoreData)
            .catch(this.handleError);
    }

    getUserCode(login: string) {
        return this.http.get(this.url + 'users/' + login + '/code')
                    .map(this.extractData)
                    .catch(this.handleError);
    }

    actOnReview(id: number) {
        return this.http.delete(this.url + `orders/${id}/review`)
            .map(this.ignoreData)
            .catch(this.handleError);
    }

    getReport(from: number, until: number) {
        let params: URLSearchParams = new URLSearchParams();
        params.set('from', from.toString());
        params.set('until', until.toString());

        return this.http.get(this.url + 'report/', {search: params})
            .map(this.extractData)
            .catch(this.handleError);
    }

    consistencyCheck() {
        return this.http.get(this.url + 'report/funds/check')
            .map(this.extractData)
            .catch(this.handleError);
    }

    updatePrice(id: string, price: string) {
        return this.http.get(this.url + `orders/${id}/price/${price}`)
            .map(this.ignoreData)
            .catch(this.handleError);
    }

    private ignoreData(res: Response) {
        console.debug('ignoring response');

        return {};
    }

    private extractData(res: Response) {
        let body = res.json();

        return body.result || {};
    }

    private handleError (error: Response | any) {
        // In a real world app, you might use a remote logging infrastructure
        console.debug(error);

        let errMsg: string;
        if (error instanceof Response) {
            const body = error.json() || '';
            const err = body.error || JSON.stringify(body);
            errMsg = `${error.status} - ${error.statusText || ''} ${err}`;
        } else {
            errMsg = error.message ? error.message : error.toString();
        }
        console.error(errMsg);
        return Observable.throw(errMsg);
    }
}
