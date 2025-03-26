import { AfterViewChecked, AfterViewInit, Component, ElementRef, inject, Input, OnInit, ViewChild } from '@angular/core';
import { ChatService } from '../services/chat.service';
import { ChatMessage } from '../models/models';

@Component({
  selector: 'app-chat',
  standalone: false,
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit, AfterViewInit{

  @ViewChild('scrollContainer') 
  private scrollContainer!: ElementRef;

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
    this.chatSvc.saveChat(this.senderId, this.receiverId, this.type)
      .then((resp) => {
        console.info(resp.message)
      })
    this.chatSvc.saveChat(this.receiverId, this.senderId, this.type)
      .then((resp) => {
        console.info(resp.message)
      })

    this.chatSvc.getMessages().subscribe((message) => {
      console.info('Real time message:', message)
      this.messages.push(message);
    });

    this.loadChatHistory();
  }

  ngAfterViewInit(): void {
    console.info('View initialized')
    this.scrollToBottom();
  }

  loadChatHistory() {
    this.chatSvc.getChatHistory(this.senderId, this.receiverId, this.type).subscribe((messages) => {
      this.messages = messages;
      console.info('Message history:', this.messages)
    });
  }

  sendMessage() {
    this.scrollToBottom()
    console.info('Sending message:', this.messageContent.trim())
    if (this.messageContent.trim()) {
      this.chatSvc.sendMessage(this.senderId, this.receiverId, this.messageContent, this.type);
      this.messageContent = '';
    }
  }

  private scrollToBottom(): void {
    try {
      setTimeout(() => { 
        this.scrollContainer.nativeElement.scrollTop =
          this.scrollContainer.nativeElement.scrollHeight;
      }, 100);
    } catch (err) {
      console.error('Scroll error:', err);
    }
  }
}
