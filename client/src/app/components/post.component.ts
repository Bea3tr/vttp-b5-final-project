import { Component, inject, OnInit} from '@angular/core';
import { FileUploadService } from '../services/fileupload.service';
import { ActivatedRoute } from '@angular/router';
import { Post } from '../models/models';

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
    this.loadPosts();
  }

  private loadPosts() {
    this.fileUploadSvc.getPosts(this.id)
      .then((resp) => {
        this.posts = resp
    });
  }

}
