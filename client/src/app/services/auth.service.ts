import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, lastValueFrom, Subject } from 'rxjs';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';
import { UserInfo } from '../models/models';

declare const google: any

@Injectable()
export class AuthService {
    private authSubject = new BehaviorSubject<any>(null)
    private http = inject(HttpClient)
    private router = inject(Router)

    userInfo = new Subject<any>()

    googleLogin() {
        if (typeof google === 'undefined') {
            console.error('Google SDK not loaded!')
            return
        }
        google.accounts.oauth2
            .initTokenClient({
                client_id: environment.googleClientId,
                scope: 'profile email',
                callback: (resp: any) => {
                    console.info('Response: ', resp)
                    if (resp.access_token) {
                        this.fetchGoogleUserInfo(resp.access_token)
                    }
                }
            })
            .requestAccessToken()
    }

    userLogin(userInfo: any) { 
        this.sendUserInfoToSB(userInfo, '/api/user-login')
    }

    register(userInfo: any) {
        this.sendUserInfoToSB(userInfo, '/api/register')
    }

    // Authenticate login with access token attribute
    isLoggedIn(): boolean {
        // Get token from storage
        const token = sessionStorage.getItem('authenticated')
        return token == 'true'
    }

    logout() {
        sessionStorage.removeItem('authenticated')
        this.authSubject.next(null)
    }

    getUserInfo(id: string) {
        return lastValueFrom(this.http.get<UserInfo>(`/api/get-user/${id}`))
    }

    getAllUsers(id: string) {
        return lastValueFrom(this.http.get<UserInfo[]>(`/api/users/${id}`))
    }

    sendVerificationCode(email: string) {
        const body = {
            email: email
        }
        return lastValueFrom(this.http.post<any>('/api/mail/send-reset-code', { body }))
    }

    verifyCode(email: string, code: string) {
        const body = {
            email: email,
            code: code
        }
        return lastValueFrom(this.http.post<any>('/api/mail/verify-code', { body }))
    }

    resetPassword(email: string, newPassword: string) {
        const body = {
            email: email,
            newPassword: newPassword
        }
        return lastValueFrom(this.http.put<any>('/api/auth/reset-password', { body }))
    }

    // Fetch Google user data from Google API with access token
    private fetchGoogleUserInfo(accessToken: string) {
        this.http.get(`https://www.googleapis.com/oauth2/v3/userinfo`, {
            headers: { Authorization: `Bearer ${accessToken}` }
        })
        .subscribe((userData: any) => {
            const userInfo = {
                email: userData.email,
                name: userData.name
            }
            this.sendUserInfoToSB(userInfo, '/api/google-user')
        })
    }

    // Store authentication in session storage - true if user is authenticated
    private sendUserInfoToSB(userInfo: any, endpoint: string) {
        this.http.post(endpoint, userInfo)
            .subscribe({
                next: (resp: any) => {
                    console.info('User info sent to SB:', resp)
                    this.authSubject.next(resp)
                    if(resp.message == 'authenticated') {
                        this.router.navigate(['/home', resp.id])
                        sessionStorage.setItem('authenticated', 'true')
                    }
                },
                error: (err: HttpErrorResponse) => {alert(err.error.message)}
            })
    }
}

