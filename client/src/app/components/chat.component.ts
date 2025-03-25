import { Component, inject, Input } from '@angular/core';
import { ChatService } from '../services/chat.service';
import { ChatMessage } from '../models/models';

@Component({
  selector: 'app-chat',
  standalone: false,
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent {

  messages: ChatMessage[] = [];
  messageContent = '';

  @Input()
  senderId = ''
  @Input()
  receiverId = ''
  @Input()
  type = ''

  private chatSvc = inject(ChatService)

  ngOnInit() {
    this.chatSvc.connect(this.senderId);
    this.chatSvc.saveChat(this.senderId, this.receiverId)
      .then((resp) => {
        console.info(resp.message)
      })
    this.chatSvc.saveChat(this.receiverId, this.senderId)
      .then((resp) => {
        console.info(resp.message)
      })

    this.chatSvc.getMessages().subscribe((message) => {
      this.messages.push(message);
    });

    this.loadChatHistory();
  }

  loadChatHistory() {
    this.chatSvc.getChatHistory(this.senderId, this.receiverId, this.type).subscribe((messages) => {
      this.messages = messages;
      console.info('Message history:', this.messages)
    });
  }

  sendMessage() {
    console.info('Sending message:', this.messageContent.trim())
    if (this.messageContent.trim()) {
      this.chatSvc.sendMessage(this.senderId, this.receiverId, this.messageContent, this.type);
      this.messageContent = '';
    }
  }

}
