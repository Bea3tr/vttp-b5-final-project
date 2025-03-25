import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Subject, Observable, lastValueFrom } from "rxjs";
import { ChatMessage } from "../models/models";

@Injectable()
export class ChatService {
  private webSocket!: WebSocket
  private messageSubject = new Subject<ChatMessage>()

  private http = inject(HttpClient)

  connect(userId: string) {
    this.webSocket = new WebSocket(`wss://pawdiaries-production.up.railway.app/chat?${userId}`)

    this.webSocket.onmessage = (event) => {
      console.info('Message:',event.data)
      const message = JSON.parse(event.data)
      this.messageSubject.next(message)
    }

    this.webSocket.onclose = () => {
      console.log('WebSocket connection closed.')
    }
  }

  sendMessage(senderId: string, receiverId: string, content: string, type: string) {
    const message = { senderId, receiverId, content , type }
    this.webSocket.send(JSON.stringify(message))
  }

  getMessages(): Observable<any> {
    return this.messageSubject.asObservable()
  }

  getChatHistory(user1: string, user2: string, type: string): Observable<ChatMessage[]> {
    const params = new HttpParams()
      .append('type', type)
    return this.http.get<ChatMessage[]>(`/api/chat/history/${user1}/${user2}`, { params })
  }

  saveChat(userId: string, partyId: string) {
    console.info('Saving chat to:', userId)
    const body = {
      partyId: partyId
    }
    return lastValueFrom(this.http.put<any>(`/api/chat/save/${userId}`, { body }))
  }

  removeChat(userId: string, partyId: string) {
    console.info('Removing chat from:', userId)
    const body = {
      partyId: partyId
    }
    return this.http.delete<any>(`/api/chat/remove/${userId}`, { body })
  }

  getChats(userId: string) {
    return lastValueFrom(this.http.get<string[]>(`/api/chat/get/${userId}`))
  }
}