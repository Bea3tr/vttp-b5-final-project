import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { BehaviorSubject, lastValueFrom } from "rxjs";
import { PfResponse, Post, UserInfo } from "../models/models";

@Injectable()
export class ProfileService {

    private http = inject(HttpClient)
    activeTab = new BehaviorSubject<string>('post')

    reloadActiveTab(tab: string) {
        this.activeTab.next(tab)
    }

    isUser(id: string) {
        return id == sessionStorage.getItem('user')
    }

     getSelfPosts(userId: string) {
        const params = new HttpParams()
            .append("userId", userId)
        return lastValueFrom(this.http.get<Post[]>('/api/post/get', { params }))
    }

    getUserPosts(userId: string) {
        const params = new HttpParams()
            .append("userId", userId)
        return lastValueFrom(this.http.get<Post[]>('/api/post/get-others', { params }))
    }

    getSavedPosts(userId: string) {
        return lastValueFrom(this.http.get<Post[]>(`/api/post/get-posts-saved/${userId}`))
    }

    getSavedPfData(userId: string) {
        return lastValueFrom(this.http.get<PfResponse>(`/api/shelter/get-data-saved/${userId}`))
    }

    getUserInfo(id: string) {
        return lastValueFrom(this.http.get<UserInfo>(`/api/get-user/${id}`))
    }

    updatePic(file: File, userId: string) {
        const formData = new FormData()
        formData.set('file', file)
        return lastValueFrom(this.http.put<any>(`/api/user/edit-pic/${userId}`, formData))
    }

    updateName(name: string, userId: string) {
        const body = {
            name: name
        }
        return lastValueFrom(this.http.put<any>(`/api/user/edit-name/${userId}`, { body }))
    }

    updatePassword(form: any, userId: string) {
        const body = {
            oldPassword: form.value['oldPassword'],
            newPassword: form.value['newPassword']
        }
        return lastValueFrom(this.http.put<any>(`/api/user/edit-password/${userId}`, { body }))
    }
    
}