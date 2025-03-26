import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { ChatUser, Item, MediaFile, UserInfo } from '../models/models';
import { PostService } from '../services/post.service';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { ProfileService } from '../services/profile.service';
import { map, Observable, startWith, tap } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { ChatService } from '../services/chat.service';
import { ShopService } from '../services/shop.service';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent implements OnInit {
  constructor(
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer,
    private authSvc: AuthService,
    private router: Router,
    private actRoute: ActivatedRoute,
    private postSvc: PostService,
    private fb: FormBuilder,
    private profileSvc: ProfileService,
    private chatSvc: ChatService,
    private shopSvc: ShopService
  ) {
    const icons = [
      'plus',
      'cross',
      'shelter',
      'shop',
      'color-shelter',
      'color-shop',
      'comment',
      'img',
    ];
    icons.forEach((icon) => {
      this.matIconRegistry.addSvgIcon(
        icon,
        this.domSanitizer.bypassSecurityTrustResourceUrl(
          `icons/${icon}-icon.svg`
        )
      );
    });
  }

  protected form!: FormGroup;
  protected listForm!: FormGroup;
  protected id: string = '';
  protected user!: UserInfo;
  protected isPopupOpen = false;
  protected isSearchOpen = false;
  protected isChatOpen = false;
  protected isListOpen = false;
  protected isViewOpen = false;
  protected activeTab = 'post';
  protected selectedFiles!: FileList;
  protected chatUsers: ChatUser[] = [];
  protected currentChat!: ChatUser;
  protected chatItem!: Item;

  // For autocomplete field
  protected users: UserInfo[] = [];
  protected searchControl = new FormControl<string | UserInfo>('');
  protected filteredOptions!: Observable<UserInfo[]>;

  onFileChange(event: any) {
    console.info('onFileChange:', event.target.files);
    this.selectedFiles = event.target.files;
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
  }

  ngOnInit(): void {
    this.form = this.createForm();
    this.actRoute.params.subscribe(async (params) => {
      this.id = params['userId'];
      let r = await this.authSvc.getUserInfo(this.id);
      this.user = r;
    });
    this.chatSvc.reload.subscribe(async (val) => {
      if (val == true) {
        console.info('Reloading chat users')
        this.chatUsers = [];
        await this.loadChatUsers();
        this.chatSvc.reloadChat(false);
      }
    });
    this.authSvc.getAllUsers(this.id).then((resp) => {
      this.users = resp;
      console.info('>>> Users:', this.users);
    });
    this.filteredOptions = this.searchControl.valueChanges.pipe(
      startWith(''),
      map((value) => {
        const name = typeof value === 'string' ? value : value?.name;
        return name ? this._filter(name as string) : this.users.slice();
      }),
      tap((result) => {
        console.info('>>>', result);
      })
    );
    this.loadChatUsers();
  }

  displayFn(user: UserInfo): string {
    return user && user.name ? user.name : '';
  }

  // Forms
  post() {
    this.postSvc
      .uploadPost(this.form, this.selectedFiles, this.id)
      .then((resp) => {
        alert(resp.message);
      })
      .catch((err: HttpErrorResponse) => {
        alert(err.error.message);
      });
    this.form.reset();
    this.isPopupOpen = false;
    this.postSvc.reloadPosts(true);
  }

  searchUser(user: UserInfo) {
    console.info('>>> Selected:', user);
    this.router.navigate(['/user', this.id, user.id]);
  }

  listItem() {
    this.shopSvc
      .listItem(this.listForm, this.selectedFiles, this.id)
      .then((resp) => {
        console.info(resp.message);
        this.shopSvc.reloadItems(true);
      })
      .catch((err: HttpErrorResponse) => {
        console.info(err.error.message);
      });
    this.listForm.reset();
    this.isListOpen = false;
  }

  deleteChat() {
    this.chatSvc
      .removeChat(this.id, this.currentChat.user.id, this.currentChat.type)
      .then(async (resp) => {
        console.info(resp);
        this.chatSvc.reloadChat(true);
      });
    this.isChatOpen = false;
  }

  // Hidden components
  openChat(chatUser: ChatUser) {
    this.isChatOpen = true;
    this.currentChat = chatUser;
    this.shopSvc
      .getItemById(chatUser.type)
      .then((resp) => {
        this.chatItem = resp;
        this.chatItem.currentFileIndex = 0;
        console.info(resp)
      })
      .catch((err: HttpErrorResponse) => {
        console.info(err.error.message);
      });
  }

  closeChat() {
    this.isChatOpen = false;
  }

  openPopup() {
    this.isPopupOpen = true;
  }

  closePopup() {
    this.isPopupOpen = false;
  }

  openSearch() {
    this.isSearchOpen = true;
  }

  closeSearch() {
    this.isSearchOpen = false;
  }

  viewItem() {
    this.isViewOpen = true;
  }

  closeView() {
    this.isViewOpen = false;
  }

  openList() {
    this.isListOpen = true;
    this.listForm = this.fb.group({
      item: this.fb.control<string>('', [
        Validators.required,
        Validators.minLength(3),
      ]),
      description: this.fb.control<string>('', [
        Validators.required,
        Validators.minLength(3),
      ]),
      price: this.fb.control<number>(0, [
        Validators.required,
        Validators.min(0),
      ]),
    });
  }

  closeList() {
    this.isListOpen = false;
    this.listForm.reset();
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

  private createForm() {
    return this.fb.group({
      post: this.fb.control<string>(''),
      status: this.fb.control<string>('', [Validators.required]),
    });
  }

  private _filter(name: string): UserInfo[] {
    const filterValue = name.toLowerCase();

    return this.users.filter((user) =>
      user.name.toLowerCase().includes(filterValue)
    );
  }

  logout() {
    this.authSvc.logout();
    this.router.navigate(['/']);
  }

  private async loadChatUsers() {
    console.info('Loading chat users for', this.id);
    let chats = await this.chatSvc.getChats(this.id);
    console.info('Chats:', chats);
    if (chats && chats.length > 0) {
      chats.forEach(async (chat) => {
        let user = await this.authSvc.getUserInfo(chat.id);
        let chatUser = { user, type: chat.type };
        this.chatUsers = [...this.chatUsers, chatUser];
      });
    }
  }
}
