import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { lastValueFrom } from "rxjs";
import { Post, UploadResponse } from "../models/models";

@Injectable({ providedIn: 'root' })
export class FileUploadService {
    
    private http = inject(HttpClient)

    uploadPost(form: any, files: FileList, userId: string) {
        const formData = new FormData();
        formData.set('post', form.value['post']);
        formData.set('status', form.value['status']);
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }
        return lastValueFrom(this.http.post<UploadResponse>(`/api/post/${userId}`, formData))
    }

    getPublicPosts() {
        return lastValueFrom(this.http.get<Post[]>('/api/public-posts'))
    }

    getPosts(userId: string) {
        const params = new HttpParams()
            .set("userId", userId);
        return lastValueFrom(this.http.get<Post[]>('/api/get-posts', { params }))
    }

}