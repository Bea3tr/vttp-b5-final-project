import { Component, inject, OnInit } from '@angular/core';
import { PostService } from '../services/post.service';
import { ActivatedRoute } from '@angular/router';
import { Comment, MediaFile, Post, UserInfo } from '../models/models';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-post',
  standalone: false,
  templateUrl: './post.component.html',
  styleUrl: './post.component.css'
})
export class PostComponent implements OnInit {

  constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer,
    private actRoute: ActivatedRoute, private postSvc: PostService, private authSvc: AuthService,
    private fb: FormBuilder) {
    const icons = ["comment", "thumbsup", "thumbsdown", "send"];
    icons.forEach((icon) => {
      this.matIconRegistry.addSvgIcon(
        icon,
        this.domSanitizer.bypassSecurityTrustResourceUrl(`icons/${icon}-icon.svg`)
      );
    });
  }

  protected id = ''
  protected isPopupOpen = false;
  protected isEditOpen = false;
  protected isEditCommentOpen = false;
  protected posts: Post[] = [];
  protected savedPosts: string[] = [];
  protected form !: FormGroup;
  protected editForm !: FormGroup;
  protected activePostId = '';
  protected editPostId = '';
  protected editCommentId = '';
  protected displayedComments: Comment[] = []

  ngOnInit(): void {
    this.form = this.fb.group({
      comment: this.fb.control<string>('', [Validators.required, Validators.minLength(3)])
    });
    this.actRoute.params.subscribe(
      async (params) => {
        this.id = params['userId'];
      });
    this.loadPublicPosts();
    this.getSavedPosts(this.id);
    this.postSvc.reload.subscribe(async (val) => {
      console.info('Post val:', val)
      if (val == true) {
        await this.loadPublicPosts();
        this.getSavedPosts(this.id);
        this.postSvc.reloadPosts(false);
      }
    });
  }

  postComment() {
    const comment = this.form.value['comment'];
    this.postSvc.postComment(this.id, this.activePostId, comment)
      .then((resp) => {
        console.info(resp.message);
        this.postSvc.reloadPosts(true);
      })
    this.form.reset();
    this.isPopupOpen = false;
  }

  editPost() {
    const edited = this.editForm.value['edited'];
    this.postSvc.editPost(this.editPostId, edited)
      .then((resp) => {
        console.info(resp.message);
        this.postSvc.reloadPosts(true);
      })
      .catch((err) => {
        console.info(err.message);
      })
    this.editForm.reset();
    this.isEditOpen = false;
  }

  editComment() {
    const edited = this.editForm.value['edited'];
    this.postSvc.editComment(this.editCommentId, edited)
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

  deletePost(postId: string) {
    this.postSvc.deletePost(postId)
      .then((resp) => {
        console.info(resp.message);
        // Reload posts
        this.postSvc.reloadPosts(true);
      })
  }

  deleteComment(cId: string) {
    this.postSvc.deleteComment(cId)
      .then(async (resp) => {
        console.info(resp.comment, resp.timestamp);
        this.displayedComments = await this.getComments(this.activePostId);
      })
  }

  savePostToUser(postId: string) {
    this.postSvc.savePostToUser(this.id, postId);
    if (this.savedPosts.includes(postId)) {
      this.removeSavedPost(postId);
    }
    this.postSvc.reloadSavedPosts(true);
  }

  removeSavedPost(postId: string) {
    this.postSvc.removeSavedPost(this.id, postId);
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
      edited: this.fb.control<string>(`${content}`, [ Validators.minLength(3) ])
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
      edited: this.fb.control<string>(`${content}`, [ Validators.minLength(3) ])
    });
    this.isEditCommentOpen = true;
  }

  closeEditComment() {
    this.isEditCommentOpen = false;
    this.editForm.reset();
  }

  showFile(post: Post): MediaFile {
    // console.info('MediaFile:', post.files[post.currentFileIndex])
    return post.files[post.currentFileIndex];
  }

  nextFile(post: Post) {
    if (post.files && post.files.length > 0) {
      post.currentFileIndex = (post.currentFileIndex + 1) % post.files.length;
    }
  }

  prevFile(post: Post) {
    if (post.files && post.files.length > 0) {
      post.currentFileIndex = (post.currentFileIndex - 1 + post.files.length) % post.files.length;
    }
  }

  /////////////// Private methods ////////////////
  private async loadPublicPosts() {
    try {
      const posts = await this.postSvc.getPublicPosts();

      // Assign default index for file navigation
      posts.forEach(post => {
        post.currentFileIndex = 0;
      });

      this.displayedComments = await this.getComments(this.activePostId);

      // Now assign posts after all comments are fetched
      this.posts = posts;
      console.info('Posts:', this.posts);

    } catch (error) {
      console.error("Error loading public posts:", error);
    }
  }

  private getSavedPosts(userId: string) {
    this.postSvc.getSavedPosts(userId)
      .then((resp) => {
        this.savedPosts = resp.result;
        console.info('Saved posts:', this.savedPosts);
      })
  }

  private async getComments(postId: string): Promise<Comment[]> {
    let comments: Comment[] = [];
    try {
      const resp = await this.postSvc.getComments(postId);
      console.info(resp.message);
      comments = resp.comments;

      if (comments) {
        // Fetch user info for each comment in parallel
        await Promise.all(comments.map(async (comment) => {
          const userInfo = await this.authSvc.getUserInfo(comment.user_id);
          comment.name = userInfo.name;
          comment.picture = userInfo.picture;
        }));
      }
      return comments;

    } catch (error) {
      console.error('Error fetching comments:', error);
      return []; 
    }
  }

}

