import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Load, PfResponse } from "../models/models";
import { BehaviorSubject, lastValueFrom } from "rxjs";

@Injectable()
export class ApiService {

    private http = inject(HttpClient);
    reload = new BehaviorSubject<boolean>(false);

    defaultLoadPf() {
        return lastValueFrom(this.http.get<PfResponse>('/api/shelter'));
    }

    loadMorePf(loaded: number[]) {
        const params = new HttpParams()
            .append("loaded", loaded.join())
        return lastValueFrom(this.http.get<PfResponse>('/api/shelter/load', { params }));
    }

    loadMorePfFiltered(load: Load) {
        let params = new HttpParams();
        for (const key of Object.keys(load) as (keyof Load)[]) {
            console.log(`${key}:`, load[key]); // Accessing value dynamically
            let value = load[key];
            // Value not empty
            if(value) {
                if(Array.isArray(value)) {
                    params = params.append(key, value.join());
                } else {
                    params.append(key, value);
                }
            }
        }
        return lastValueFrom(this.http.get<PfResponse>('/api/shelter/load-filtered', { params }))
    }

    getFilteredPf(form: any) {
        const formParams = ["type", "breed", "size", "gender", "age", "name", "location"];
        let params = new HttpParams();
        formParams.forEach((p) => {
            if(form.value[p]) {
                params = params.append(p, form.value[p])
            }
        })
        return lastValueFrom(this.http.get<PfResponse>('/api/shelter/filtered', { params }));
    }

    savePfToUser(userId: string, pfId: number) {
        console.info('UserId, PfId:', userId, pfId);
        const body = {
            userId: userId,
            pfId: pfId
        };
        return lastValueFrom(this.http.put<any>('/api/shelter/save', { body }));
    }

    removeSavedPf(userId: string, pfId: number) {
        console.info('UserId, PfId:', userId, pfId);
        const body = {
            userId: userId,
            pfId: pfId
        };
        return lastValueFrom(this.http.put<any>('/api/shelter/remove', { body }));
    }

    getSavedPf(userId: string) {
        return lastValueFrom(this.http.get<any>(`/api/shelter/get-saved/${userId}`));
    }

    getTypes() {
        return lastValueFrom(this.http.get<any>('/api/shelter/types'));
    }

    getBreeds(type: string) {
        return lastValueFrom(this.http.get<any>(`/api/shelter/breeds/${type}`));
    }

    reloadSavedPfs(boolean: boolean) {
        this.reload.next(boolean);
    }
}