import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { PfResponse } from "../models/models";
import { lastValueFrom } from "rxjs";

@Injectable({ 'providedIn': 'root' })
export class ApiService {

    private http = inject(HttpClient);

    defaultLoadPf() {
        return lastValueFrom(this.http.get<PfResponse>('/api/shelter'));
    }
}