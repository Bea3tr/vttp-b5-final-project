import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';

declare const google: any;

@Injectable({ providedIn: 'root' })
export class AuthService {
    private authSubject = new BehaviorSubject<any>(null);
    private http = inject(HttpClient);
    private router = inject(Router)

    googleLogin() {
        if (typeof google === 'undefined') {
            console.error('Google SDK not loaded!');
            return;
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
            .requestAccessToken();
    }

    private fetchGoogleUserInfo(accessToken: string) {
        this.http.get(`https://www.googleapis.com/oauth2/v3/userinfo`, {
            headers: { Authorization: `Bearer ${accessToken}` }
        })
        .subscribe((userData: any) => {
            const userInfo = {
                email: userData.email,
                name: userData.name,
                profilePicture: userData.picture
            }
            this.sendUserInfoToSB(userInfo)
            localStorage.setItem('access_token', accessToken);
        })
    }

    private sendUserInfoToSB(userInfo: any) {
        this.http.post('/api/user-info', userInfo)
            .subscribe((resp: any) => {
                console.info('User info sent to SB: ', resp)
                this.authSubject.next(resp)
                if(resp.message == 'access authenticated')
                    this.router.navigate(['/home'])
            })
    }

    isLoggedIn(): boolean {
        // Get token from storage
        const token = sessionStorage.getItem('access_token')
        return !!token;
    }

    logout() {
        sessionStorage.removeItem('access_token');
        this.authSubject.next(null);
    }
}

