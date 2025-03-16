import { Component, inject, OnInit } from '@angular/core';
import { FileUploadService } from '../services/fileupload.service';
import { ActivatedRoute } from '@angular/router';
import { MediaFile, Post } from '../models/models';

@Component({
  selector: 'app-post',
  standalone: false,
  templateUrl: './post.component.html',
  styleUrl: './post.component.css'
})
export class PostComponent implements OnInit {

  private actRoute = inject(ActivatedRoute)
  private fileUploadSvc = inject(FileUploadService)

  protected id = ''
  protected posts: Post[] = []

  ngOnInit(): void {
    this.actRoute.params.subscribe(
      async (params) => {
        this.id = params['userId'];
      });
    this.loadPublicPosts();
    this.fileUploadSvc.reload.subscribe(val => {
      console.info('Post val:', val)
      if(val == true) {
        setTimeout(() => {
          console.info('Refetching posts...');
          this.loadPublicPosts();
        }, 1500);
        this.fileUploadSvc.reloadPosts(false);
      }
    });
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

  deletePost(postId: string) {
    this.fileUploadSvc.deletePost(postId)
      .then((resp) => {
        console.info(resp.message);
        // Reload posts
        this.fileUploadSvc.reloadPosts(true);
      })
  }

  private loadPublicPosts() {
    this.fileUploadSvc.getPublicPosts()
      .then((resp) => {
        resp.forEach(post => {
          post.currentFileIndex = 0;
        })
        this.posts = resp;
        console.info('Posts:', this.posts);
      });
  }

  private loadPosts() {
    this.fileUploadSvc.getPosts(this.id)
      .then((resp) => {
        this.posts = resp
      });
  }

}
