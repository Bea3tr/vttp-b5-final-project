import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Router } from "@angular/router";

@Injectable({ providedIn: 'root' })
export class FileUploadService {
    
    private http = inject(HttpClient)
    private router = inject(Router)

    uploadPost(formData: FormData, userId: string) {
        this.http.post(`/api/post/${userId}`, formData)
            .subscribe((resp: any) => {
                console.info('Post sent to SB:', resp)
                if(resp.message == 'posted') {
                    this.router.navigate(['/home', userId])
                }
            })
    }

}