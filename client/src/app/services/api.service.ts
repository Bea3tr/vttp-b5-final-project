import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { PfResponse } from "../models/models";
import { BehaviorSubject, lastValueFrom } from "rxjs";

@Injectable({ 'providedIn': 'root' })
export class ApiService {

    private http = inject(HttpClient);
    reload = new BehaviorSubject<boolean>(false);

    defaultLoadPf() {
        return lastValueFrom(this.http.get<PfResponse>('/api/shelter'));
    }

    savePfToUser(userId: string, pfId: number) {
        console.info('UserId, PfId:', userId, pfId);
        const body = {
            userId: userId,
            pfId: pfId
        };
        return lastValueFrom(this.http.put<any>('/api/save-pf', { body }));
    }

    removeSavedPf(userId: string, pfId: number) {
        console.info('UserId, PfId:', userId, pfId);
        const body = {
            userId: userId,
            pfId: pfId
        };
        return lastValueFrom(this.http.put<any>('/api/remove-pf', { body }));
    }

    getSavedPf(userId: string) {
        return lastValueFrom(this.http.get<any>(`/api/get-saved-pf/${userId}`));
    }

    reloadSavedPfs(boolean: boolean) {
        this.reload.next(boolean);
    }
}