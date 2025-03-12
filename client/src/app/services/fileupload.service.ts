import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Router } from "@angular/router";
import { lastValueFrom } from "rxjs";
import { Post, UploadResponse } from "../models/models";

@Injectable({ providedIn: 'root' })
export class FileUploadService {
    
    private http = inject(HttpClient)
    private router = inject(Router)

    uploadPost(formData: FormData, userId: string) {
        return lastValueFrom(this.http.post<UploadResponse>(`/api/post/${userId}`, formData))
    }

    getPosts(userId: string) {
        const params = new HttpParams()
            .set("userId", userId);
        return lastValueFrom(this.http.get<Post[]>('/api/get-posts', { params }))
    }

}