import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ProfileService } from '../services/profile.service';
import { ChatUser, Item, MediaFile, UserInfo } from '../models/models';
import { ChatService } from '../services/chat.service';
import { ShopService } from '../services/shop.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-marketplace',
  standalone: false,
  templateUrl: './marketplace.component.html',
  styleUrl: './marketplace.component.css',
})
export class MarketplaceComponent implements OnInit {
  constructor(
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer,
    private authSvc: AuthService,
    private fb: FormBuilder,
    private chatSvc: ChatService,
    private shopSvc: ShopService
  ) {
    const icons = ['cross', 'refresh'];
    icons.forEach((icon) => {
      this.matIconRegistry.addSvgIcon(
        icon,
        this.domSanitizer.bypassSecurityTrustResourceUrl(
          `icons/${icon}-icon.svg`
        )
      );
    });
  }

  @Input()
  id = '';
  @Input()
  user!: UserInfo;
  protected currentChat!: ChatUser;
  protected items: Item[] = [];
  protected savedItems : string[] = []
  protected chatItem !: Item

  protected isChatOpen = false;
  protected isListOpen = false;

  protected listForm!: FormGroup;
  protected selectedFiles!: FileList;

  ngOnInit(): void {
    this.shopSvc.reload.subscribe(val => {
      if (val == true) {
        setTimeout(() => {
          console.info('Refetching saved...')
          this.loadSavedIds()
          this.loadListedItems();
        }, 100)
        this.shopSvc.reloadSavedItems(false)
      }
    })
    this.listForm = this.fb.group({
      filter: this.fb.control<string>('', [ Validators.required ]) 
    })
    this.loadListedItems();
    this.loadSavedIds();
  }

  saveItemToUser(itemId: string) {
    if (this.savedItems.includes(itemId)) {
      this.removeItemFromUser(itemId)
    } else {
      this.shopSvc.saveItem(this.id, itemId)
      .then((resp) => {
        console.info(resp.message)
      })
      .catch((err: HttpErrorResponse) => {
        console.info(err.error.message);
      });
    }
    this.shopSvc.reloadSavedItems(true)
  }

  removeItemFromUser(itemId: string) {
    this.shopSvc.removeItem(this.id, itemId)
      .then((resp) => {
        console.info(resp.message)
      })
      .catch((err: HttpErrorResponse) => {
        console.info(err.error.message);
      });
  }

  async filter() {
    const filter = this.listForm.value['filter']
    const items = await this.shopSvc.getItemsFiltered(filter, this.id)
    items.forEach(item => {
      item.currentFileIndex = 0
    })
    this.items = items;
    console.info('Items:', this.items)
  }

  getAllItems() {
    this.loadListedItems();
  }

  async openChatSeller(item: Item) {
    this.isChatOpen = true;
    let user = await this.authSvc.getUserInfo(item.user_id)
    this.currentChat = { user, type: item.id }
    this.shopSvc.getItemById(item.id)
      .then((resp) => {
        this.chatItem = resp
      })
      .catch((err: HttpErrorResponse) => {
        console.info(err.error.message)
      })
    this.chatSvc.reloadChat(true)
  }

  closeChat() {
    this.isChatOpen = false;
  }

  showFile(item: Item): MediaFile {
    // console.info('MediaFile:', post.files[post.currentFileIndex])
    return item.files[item.currentFileIndex];
  }

  nextFile(item: Item) {
    if (item.files && item.files.length > 0) {
      item.currentFileIndex = (item.currentFileIndex + 1) % item.files.length;
    }
  }

  prevFile(item: Item) {
    if (item.files && item.files.length > 0) {
      item.currentFileIndex =
        (item.currentFileIndex - 1 + item.files.length) % item.files.length;
    }
  }

  deleteChat() {
    this.chatSvc.removeChat(this.id, this.currentChat.user.id, this.currentChat.type)
      .then((resp) => {
        console.info(resp)
      })
    this.isChatOpen = false;
    this.chatSvc.reloadChat(true);
  }

  private async loadListedItems() {
    console.info('Loading shop items:', this.id);
    const items = await this.shopSvc.getItems(this.id);
    items.forEach(item => {
      item.currentFileIndex = 0
    })
    this.items = items;
    console.info('Items:', this.items)
  }

  private async loadSavedIds() {
    console.info('Loading saved items')
    this.shopSvc.getSaved(this.id)
      .then((resp) => {
        this.savedItems = resp.result;
        console.info('Saved items:', this.savedItems)
      })
      .catch((err: HttpErrorResponse) => {
        console.info(err.error.message)
      })
    
  }
}
