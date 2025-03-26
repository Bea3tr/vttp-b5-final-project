import { Component, OnInit, signal } from '@angular/core';
import { ProfileService } from '../services/profile.service';
import {
  Comment,
  Item,
  MediaFile,
  PfResult,
  Post,
  UserInfo,
} from '../models/models';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { PostService } from '../services/post.service';
import { ApiService } from '../services/api.service';
import { ShopService } from '../services/shop.service';
import { HttpErrorResponse } from '@angular/common/http';
import { LikeService } from '../services/like.service';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css',
})
export class ProfileComponent implements OnInit {
  constructor(
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer,
    private authSvc: AuthService,
    private router: Router,
    private actRoute: ActivatedRoute,
    private postSvc: PostService,
    private fb: FormBuilder,
    private profileSvc: ProfileService,
    private apiSvc: ApiService,
    private shopSvc: ShopService,
    private likeSvc: LikeService
  ) {
    const icons = [
      'shelter',
      'shop',
      'comment',
      'cross',
      'thumbsup',
      'thumbsdown',
      'send',
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

  hide1 = signal(true);
  clickEvent1(event: MouseEvent) {
    this.hide1.set(!this.hide1());
    event.stopPropagation();
  }

  hide2 = signal(true);
  clickEvent2(event: MouseEvent) {
    this.hide2.set(!this.hide2());
    event.stopPropagation();
  }

  protected id = '';
  protected name = '';
  protected user!: UserInfo;
  protected posts: Post[] = [];
  protected savedPosts: Post[] = [];
  protected savedPf: PfResult[] = [];
  protected listedItems: Item[] = [];

  protected isPopupOpen = false;
  protected isEditOpen = false;
  protected isEditCommentOpen = false;
  protected isEditProfileOpen = false;

  protected form!: FormGroup;
  protected editForm!: FormGroup;
  protected userForm!: FormGroup;
  protected pwForm!: FormGroup;

  protected activePostId = '';
  protected editPostId = '';
  protected editCommentId = '';
  protected displayedComments: Comment[] = [];
  protected toReload = '';
  protected savedPostIds: string[] = [];
  protected selectedFile!: File;

  ngOnInit(): void {
    this.form = this.fb.group({
      comment: this.fb.control<string>('', [
        Validators.required,
        Validators.minLength(3),
      ]),
    });
    this.actRoute.params.subscribe(async (params) => {
      this.id = params['userId'];
      let r = await this.profileSvc.getUserInfo(this.id);
      this.user = r;
      this.name = this.user.name;
    });
    this.loadMyPosts();
    this.loadSavedPosts();
    this.loadSavedPfs();
    this.getSavedPosts(this.id);
    this.getListedItems(this.id);
    this.apiSvc.reload.subscribe(async (val) => {
      if (val == true && this.toReload == 'shelter') {
        this.apiSvc.reloadSavedPfs(false);
        setTimeout(async () => {
          await this.loadSavedPfs();
        }, 500);
      }
    });
    this.postSvc.reload.subscribe(async (val) => {
      if (val == true) {
        if (this.toReload == 'post') {
          console.info('Reloading saved posts');
          await this.loadSavedPosts();
          this.postSvc.reloadSavedPosts(false);
        } else if (this.toReload == 'post-self') {
          console.info('Reloading posts');
          await this.loadMyPosts();
          await this.getSavedPosts(this.id);
          await this.loadSavedPosts();
          this.postSvc.reloadSavedPosts(false);
        } else if (this.toReload == 'user') {
          console.info('Reloading user');
          setTimeout(async () => {
            this.user = await this.profileSvc.getUserInfo(this.id);
          }, 500);
          this.postSvc.reloadSavedPosts(false);
        }
      }
    });
    this.shopSvc.reload.subscribe(async (val) => {
      if (val == true) {
        await this.getListedItems(this.id);
        this.shopSvc.reloadItems(false);
      }
    });
  }

  logout() {
    this.authSvc.logout();
    this.router.navigate(['/']);
  }

  /////////////// Shelter ////////////////
  showPicture(result: PfResult): string {
    return result.photos[result.currentPhotoIndex];
  }

  nextPic(result: PfResult) {
    if (result.photos && result.photos.length > 0) {
      result.currentPhotoIndex =
        (result.currentPhotoIndex + 1) % result.photos.length;
    }
  }

  prevPic(result: PfResult) {
    if (result.photos && result.photos.length > 0) {
      result.currentPhotoIndex =
        (result.currentPhotoIndex - 1 + result.photos.length) %
        result.photos.length;
    }
  }

  removeSavedPf(pfId: number) {
    this.apiSvc.removeSavedPf(this.id, pfId);
    this.toReload = 'shelter';
    this.apiSvc.reloadSavedPfs(true);
  }

  /////////////// Post ////////////////
  postComment() {
    const comment = this.form.value['comment'];
    this.postSvc
      .postComment(this.id, this.activePostId, comment)
      .then((resp) => {
        console.info(resp.message);
        this.toReload = 'post';
        this.postSvc.reloadPosts(true);
      });
    this.form.reset();
    this.isPopupOpen = false;
  }

  editPost() {
    const edited = this.editForm.value['edited'];
    this.postSvc
      .editPost(this.editPostId, edited)
      .then((resp) => {
        console.info(resp.message);
        this.toReload = 'post';
        this.postSvc.reloadPosts(true);
      })
      .catch((err) => {
        console.info(err.message);
      });
    this.editForm.reset();
    this.isEditOpen = false;
  }

  editComment() {
    const edited = this.editForm.value['edited'];
    this.postSvc
      .editComment(this.editCommentId, edited)
      .then((resp) => {
        console.info(resp.message);
        this.toReload = 'post';
        this.postSvc.reloadPosts(true);
      })
      .catch((err) => {
        console.info(err.message);
      });
    this.editForm.reset();
    this.isEditCommentOpen = false;
  }

  deletePost(postId: string) {
    this.postSvc.deletePost(postId).then((resp) => {
      console.info(resp.message);
      // Reload posts
      this.toReload = 'post';
      this.postSvc.reloadPosts(true);
    });
  }

  deleteComment(cId: string) {
    this.postSvc.deleteComment(cId).then(async (resp) => {
      console.info(resp.comment, resp.timestamp);
      this.displayedComments = await this.getComments(this.activePostId);
    });
  }

  savePostToUser(postId: string) {
    if (this.savedPostIds.includes(postId)) {
      this.removeSavedPost(postId);
      this.likeSvc.unlikePost(postId);
    } else {
      this.postSvc.savePostToUser(this.id, postId);
      this.likeSvc.likePost(postId);
    }
    this.toReload = 'post-self';
    this.postSvc.reloadSavedPosts(true);
  }

  removeSavedPost(postId: string) {
    this.postSvc.removeSavedPost(this.id, postId);
    this.toReload = 'post';
    this.postSvc.reloadSavedPosts(true);
  }

  /////////////// Shop controls ////////////////

  deleteItem(itemId: string) {
    this.shopSvc
      .deleteItem(itemId)
      .then((resp) => {
        console.info(resp.message);
      })
      .catch((err: HttpErrorResponse) => {
        console.info(err.error.message);
      });
    this.shopSvc.reloadItems(true);
  }

  purchaseItem(itemId: string) {
    this.shopSvc
      .purchaseItem(itemId)
      .then((resp) => {
        console.info(resp.message);
      })
      .catch((err: HttpErrorResponse) => {
        console.info(err.error.message);
      });
    this.shopSvc.reloadItems(true);
  }

  /////////////// Page controls ////////////////

  async openPopup(postId: string) {
    this.activePostId = postId;
    console.info('Active post:', this.activePostId);
    this.displayedComments = await this.getComments(this.activePostId);
    this.isPopupOpen = true;
  }

  closePopup() {
    this.isPopupOpen = false;
    this.form.reset();
  }

  openEdit(postId: string, content: string) {
    this.editPostId = postId;
    this.editForm = this.fb.group({
      edited: this.fb.control<string>(`${content}`, [Validators.minLength(3)]),
    });
    this.isEditOpen = true;
  }

  closeEdit() {
    this.isEditOpen = false;
    this.editForm.reset();
  }

  openEditComment(cId: string, content: string) {
    this.editCommentId = cId;
    this.editForm = this.fb.group({
      edited: this.fb.control<string>(`${content}`, [Validators.minLength(3)]),
    });
    this.isEditCommentOpen = true;
  }

  closeEditComment() {
    this.isEditCommentOpen = false;
    this.editForm.reset();
  }

  openEditProfile() {
    this.isEditProfileOpen = true;
    this.userForm = this.createUserForm();
    this.pwForm = this.createPasswordForm();
  }

  closeEditProfile() {
    this.isEditProfileOpen = false;
  }

  showFile(obj: any): MediaFile {
    // console.info('MediaFile:', post.files[post.currentFileIndex])
    return obj.files[obj.currentFileIndex];
  }

  nextFile(obj: any) {
    if (obj.files && obj.files.length > 0) {
      obj.currentFileIndex = (obj.currentFileIndex + 1) % obj.files.length;
    }
  }

  prevFile(obj: any) {
    if (obj.files && obj.files.length > 0) {
      obj.currentFileIndex =
        (obj.currentFileIndex - 1 + obj.files.length) % obj.files.length;
    }
  }

  /////////////// Update user profile ////////////////
  onFileChange(event: any) {
    console.info('onFileChange:', event.target.files[0]);
    this.selectedFile = event.target.files[0];
    this.profileSvc
      .updatePic(this.selectedFile, this.id)
      .then((resp) => {
        alert(resp.message);
      })
      .catch((err) => {
        alert('Error updating profile picture');
      });
    this.toReload = 'user';
    this.postSvc.reloadSavedPosts(true);
    this.isEditProfileOpen = false;
  }

  editUsername() {
    const name = this.userForm.value['name'];
    this.profileSvc
      .updateName(name, this.id)
      .then((resp) => {
        alert(resp.message);
      })
      .catch((err) => {
        alert('Error updating username');
      });
    this.toReload = 'user';
    this.postSvc.reloadSavedPosts(true);
    this.isEditProfileOpen = false;
  }

  editPassword() {
    this.profileSvc
      .updatePassword(this.pwForm, this.id)
      .then((resp) => {
        alert(resp.message);
      })
      .catch((err) => {
        console.info(err);
        alert('Error updating password');
      });
    this.isEditProfileOpen = false;
    this.pwForm.reset();
  }

  /////////////// Private Methods ////////////////
  private async getComments(postId: string): Promise<Comment[]> {
    let comments: Comment[] = [];
    try {
      const resp = await this.postSvc.getComments(postId);
      console.info(resp.message);
      comments = resp.comments;

      if (comments) {
        // Fetch user info for each comment in parallel
        await Promise.all(
          comments.map(async (comment) => {
            const userInfo = await this.authSvc.getUserInfo(comment.user_id);
            comment.name = userInfo.name;
            comment.picture = userInfo.picture;
          })
        );
      }
      return comments;
    } catch (error) {
      console.error('Error fetching comments:', error);
      return [];
    }
  }

  private async loadMyPosts() {
    try {
      const posts = await this.profileSvc.getSelfPosts(this.id);

      // Assign default index for file navigation
      posts.forEach((post) => {
        post.currentFileIndex = 0;
        this.likeSvc.getLikeCount(post.id).then((resp) => {
          post.likes = resp.likes;
        });
      });

      this.posts = posts;
      console.info('Posts:', this.posts);
    } catch (error) {
      console.error('Error loading public posts:', error);
    }
  }

  private async loadSavedPosts() {
    try {
      const posts = await this.profileSvc.getSavedPosts(this.id);

      // Assign default index for file navigation
      if (posts) {
        posts.forEach((post) => {
          post.currentFileIndex = 0;
        });
      }

      this.savedPosts = posts;
      console.info('Posts:', this.posts);
    } catch (error) {
      console.error('Error loading public posts:', error);
    }
  }

  private async loadSavedPfs() {
    try {
      const pfs = await this.profileSvc.getSavedPfData(this.id);
      // Assign default index for file navigation
      if (pfs.results) {
        pfs.results.forEach((pf) => {
          pf.currentPhotoIndex = 0;
        });
      }
      this.savedPf = pfs.results;
      console.info('Pfs:', this.savedPf);
    } catch (error) {
      console.error('Error loading saved pfs:', error);
    }
  }

  private getSavedPosts(userId: string) {
    this.postSvc.getSavedPosts(userId).then((resp) => {
      this.savedPostIds = resp.result;
      console.info('Saved posts:', this.savedPostIds);
    });
  }

  private getListedItems(userId: string) {
    this.shopSvc
      .getListedItems(userId)
      .then((resp) => {
        let listedItems = resp;
        listedItems.forEach((item) => {
          item.currentFileIndex = 0;
        });
        this.listedItems = listedItems;
        console.info('Listed items:', this.listedItems);
      })
      .catch((err: HttpErrorResponse) => {
        console.info(err.error.message);
      });
  }

  private createUserForm(): FormGroup {
    console.info('Username:', this.name);
    return this.fb.group({
      name: this.fb.control<string>(this.name, [
        Validators.required,
        Validators.minLength(3),
      ]),
    });
  }

  private createPasswordForm(): FormGroup {
    return this.fb.group({
      oldPassword: this.fb.control<string>(''),
      newPassword: this.fb.control<string>('', [
        Validators.required,
        Validators.minLength(8),
      ]),
    });
  }
}
