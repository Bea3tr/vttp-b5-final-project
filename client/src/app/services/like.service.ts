import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { lastValueFrom, Observable } from "rxjs";

@Injectable()
export class LikeService {

  private http = inject(HttpClient)

  likePost(postId: string) {
    return this.http.post<any>(`/api/likes/${postId}`, {});
  }

  unlikePost(postId: string) {
    return this.http.delete<any>(`/api/likes/${postId}`);
  }

  getLikeCount(postId: string): Promise<any> {
    return lastValueFrom(this.http.get<any>(`/api/likes/${postId}/count`));
  }
}