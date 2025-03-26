import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { Router, ActivatedRoute } from '@angular/router';
import { UserInfo, Post, MediaFile, Comment, Item } from '../models/models';
import { AuthService } from '../services/auth.service';
import { PostService } from '../services/post.service';
import { ProfileService } from '../services/profile.service';
import { ShopService } from '../services/shop.service';
import { HttpErrorResponse } from '@angular/common/http';
import { LikeService } from '../services/like.service';

@Component({
  selector: 'app-profile-others',
  standalone: false,
  templateUrl: './profile-others.component.html',
  styleUrl: './profile-others.component.css',
})
export class ProfileOthersComponent implements OnInit {
  constructor(
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer,
    private authSvc: AuthService,
    private router: Router,
    private actRoute: ActivatedRoute,
    private postSvc: PostService,
    private fb: FormBuilder,
    private profileSvc: ProfileService,
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

  protected id = '';
  protected userId = '';
  protected user!: UserInfo;
  protected user_other!: UserInfo;
  protected posts: Post[] = [];
  protected isPopupOpen = false;
  protected isChatOpen = false;
  protected isEditCommentOpen = false;
  protected form!: FormGroup;
  protected editForm!: FormGroup;
  protected activePostId = '';
  protected editCommentId = '';
  protected displayedComments: Comment[] = [];
  protected savedPostIds: string[] = [];
  protected listedItems: Item[] = [];

  ngOnInit(): void {
    this.form = this.fb.group({
      comment: this.fb.control<string>('', [
        Validators.required,
        Validators.minLength(3),
      ]),
    });
    this.actRoute.params.subscribe(async (params) => {
      this.id = params['id'];
      this.userId = params['userId'];
      this.user = await this.profileSvc.getUserInfo(this.id);
      this.user_other = await this.profileSvc.getUserInfo(this.userId);
    });
    this.loadPosts();
    this.getSavedPosts(this.id);
    this.getListedItems(this.userId);
    this.postSvc.reload.subscribe(async (val) => {
      if (val == true) {
        console.info('Reloading posts');
        await this.loadPosts();
        this.getSavedPosts(this.id);
        this.postSvc.reloadSavedPosts(false);
      }
    });
  }

  logout() {
    this.authSvc.logout();
    this.router.navigate(['/']);
  }

  /////////////// Post ////////////////
  postComment() {
    const comment = this.form.value['comment'];
    this.postSvc
      .postComment(this.id, this.activePostId, comment)
      .then((resp) => {
        console.info(resp.message);
        this.postSvc.reloadPosts(true);
      });
    this.form.reset();
    this.isPopupOpen = false;
  }

  editComment() {
    const edited = this.editForm.value['edited'];
    this.postSvc
      .editComment(this.editCommentId, edited)
      .then((resp) => {
        console.info(resp.message);
        this.postSvc.reloadPosts(true);
      })
      .catch((err) => {
        console.info(err.message);
      });
    this.editForm.reset();
    this.isEditCommentOpen = false;
  }

  deleteComment(cId: string) {
    this.postSvc.deleteComment(cId).then(async (resp) => {
      console.info(resp.comment, resp.timestamp);
      this.displayedComments = await this.getComments(this.activePostId);
    });
  }

  savePostToUser(postId: string) {
    if (this.savedPostIds.includes(postId)) {
      this.removeSavedPost(postId)
      this.likeSvc.unlikePost(postId)
    } else {
      this.postSvc.savePostToUser(this.id, postId)
      this.likeSvc.likePost(postId)
    }
    this.postSvc.reloadSavedPosts(true)
  }

  removeSavedPost(postId: string) {
    this.postSvc.removeSavedPost(this.id, postId);
    this.postSvc.reloadSavedPosts(true);
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

  openChat() {
    this.isChatOpen = true;
  }

  closeChat() {
    this.isChatOpen = false;
  }

  showFile(obj: any): MediaFile {
    // console.info('MediaFile:', post.files[post.currentFileIndex])
    return obj.files[obj.currentFileIndex]
  }

  nextFile(obj: any) {
    if (obj.files && obj.files.length > 0) {
      obj.currentFileIndex = (obj.currentFileIndex + 1) % obj.files.length
    }
  }

  prevFile(obj: any) {
    if (obj.files && obj.files.length > 0) {
      obj.currentFileIndex = (obj.currentFileIndex - 1 + obj.files.length) % obj.files.length
    }
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

  private async loadPosts() {
    try {
      const posts = await this.profileSvc.getUserPosts(this.userId);
      // Assign default index for file navigation
      posts.forEach((post) => {
        post.currentFileIndex = 0;
        this.likeSvc.getLikeCount(post.id)
            .then((resp) => {
              post.likes = resp.likes
            })
      });
      this.posts = posts;
      console.info('Posts:', this.posts);
    } catch (error) {
      console.error('Error loading public posts:', error);
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
}
