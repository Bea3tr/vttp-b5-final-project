import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Subject, Observable, lastValueFrom, BehaviorSubject } from 'rxjs';
import { ChatMessage, SavedChat } from '../models/models';

@Injectable()
export class ChatService {
  private webSocket!: WebSocket;
  private messageSubject = new Subject<ChatMessage>();
  reload = new BehaviorSubject<boolean>(false)
  private http = inject(HttpClient);

  connect(userId: string) {
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    // const domain = window.location.host;
    // const socketUrl = `${protocol}://${domain}/chat?userId=${userId}`;

    const socketUrl = `${protocol}://localhost:8080/chat?${userId}`
    console.log(`WebSocket connecting to: ${socketUrl}`); 
    this.webSocket = new WebSocket(socketUrl);

    this.webSocket.onmessage = (event) => {
      console.info('Message:', event.data);
      const message = JSON.parse(event.data);
      this.messageSubject.next(message);
    };

    this.webSocket.onclose = () => {
      console.log('WebSocket connection closed.');
    };
  }

  sendMessage(senderId: string, receiverId: string, content: string, type: string) {
    const message = { senderId, receiverId, content, type };
    console.info('Send message:', message);
    this.webSocket.send(JSON.stringify(message));
  }

  getMessages(): Observable<any> {
    return this.messageSubject.asObservable();
  }

  getChatHistory(
    user1: string,
    user2: string,
    type: string
  ): Observable<ChatMessage[]> {
    const params = new HttpParams().append('type', type);
    return this.http.get<ChatMessage[]>(`/api/chat/history/${user1}/${user2}`, {
      params,
    });
  }

  saveChat(userId: string, partyId: string, type:string) {
    console.info('Saving chat to:', userId);
    const body = {
      partyId: partyId,
      type: type
    };
    return lastValueFrom(
      this.http.put<any>(`/api/chat/save/${userId}`, { body })
    );
  }

  removeChat(userId: string, partyId: string, type: string) {
    console.info('Removing chat from:', userId);
    const body = {
      partyId: partyId,
      type: type
    };
    return lastValueFrom(this.http.delete<any>(`/api/chat/remove/${userId}`, { body }));
  }

  getChats(userId: string) {
    return lastValueFrom(this.http.get<SavedChat[]>(`/api/chat/get/${userId}`));
  }

  reloadChat(reload: boolean) {
    this.reload.next(reload);
  }
}
