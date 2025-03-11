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
        return token == 'true';
    }

    logout() {
        sessionStorage.removeItem('authenticated');
        this.authSubject.next(null);
    }

    // Fetch Google user data from Google API with access token
    private fetchGoogleUserInfo(accessToken: string) {
        this.http.get(`https://www.googleapis.com/oauth2/v3/userinfo`, {
            headers: { Authorization: `Bearer ${accessToken}` }
        })
        .subscribe((userData: any) => {
            const userInfo = {
                email: userData.email,
                name: userData.name,
                picture: userData.picture
            }
            this.sendUserInfoToSB(userInfo, '/api/google-user')
        })
    }

    // Store authentication in session storage - true if user is authenticated
    private sendUserInfoToSB(userInfo: any, endpoint: string) {
        this.http.post(endpoint, userInfo)
            .subscribe((resp: any) => {
                console.info('User info sent to SB:', resp)
                this.authSubject.next(resp)
                if(resp.message == 'authenticated') {
                    this.router.navigate(['/home', resp.id])
                    sessionStorage.setItem('authenticated', 'true');
                }
            })
    }
}

