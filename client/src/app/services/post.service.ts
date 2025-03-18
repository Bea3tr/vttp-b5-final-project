import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { BehaviorSubject, lastValueFrom } from "rxjs";
import { Post, UploadResponse } from "../models/models";

@Injectable()
export class PostService {
    
    private http = inject(HttpClient)
    reload = new BehaviorSubject<boolean>(false);

    uploadPost(form: any, files: FileList, userId: string) {
        const formData = new FormData();
        formData.set('post', form.value['post']);
        formData.set('status', form.value['status']);
        if(files != undefined) {
            for (let i = 0; i < files.length; i++) {
                formData.append('files', files[i]);
            }
        }
        return lastValueFrom(this.http.post<UploadResponse>(`/api/post/${userId}`, formData));
    }

    deletePost(postId: string) {
        return lastValueFrom(this.http.delete<any>(`/api/post/delete/${postId}`));
    }

    editPost(postId: string, edited: string) {
        const body = {
            postId: postId,
            edited: edited
        };
        return lastValueFrom(this.http.put<any>('/api/post/edit', { body }));
    }

    getPublicPosts() {
        return lastValueFrom(this.http.get<Post[]>('/api/post/public'))
    }

    getPosts(userId: string) {
        const params = new HttpParams()
            .set("userId", userId);
        return lastValueFrom(this.http.get<Post[]>('/api/post/get', { params }))
    }

    savePostToUser(userId: string, postId: string) {
        console.info('UserId, PostId:', userId, postId);
        const body = {
            userId: userId,
            postId: postId
        };
        return lastValueFrom(this.http.put<any>('/api/post/save', { body }));
    }

    removeSavedPost(userId: string, postId: string) {
        console.info('UserId, PostId:', userId, postId);
        const body = {
            userId: userId,
            postId: postId
        };
        return lastValueFrom(this.http.put<any>('/api/post/remove', { body }));
    }

    getSavedPosts(userId: string) {
        return lastValueFrom(this.http.get<any>(`/api/post/get-saved/${userId}`));
    }

    // Comment Endpoints
    postComment(userId: string, postId: string, comment: string) {
        const body = {
            userId: userId,
            postId: postId,
            comment: comment
        };
        return lastValueFrom(this.http.post<any>('/api/comment/post', { body }));
    }

    editComment(cId: string, edited: string) {
        const body = {
            cId: cId,
            edited: edited
        };
        return lastValueFrom(this.http.put<any>('/api/comment/edit', { body }));
    }

    getComments(postId: string) {
        const params = new HttpParams()
            .set("postId", postId);
        return lastValueFrom(this.http.get<any>('/api/comment/get', { params }));
    }

    deleteComment(cId: string) {
        return lastValueFrom(this.http.delete<any>(`/api/comment/delete/${cId}`));
    }


    reloadSavedPosts(boolean: boolean) {
        this.reload.next(boolean);
    }
    
    reloadPosts(boolean: boolean) {
        this.reload.next(boolean);
    }

}