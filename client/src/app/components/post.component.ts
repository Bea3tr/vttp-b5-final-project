import { Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { FileUploadService } from '../services/fileupload.service';

@Component({
  selector: 'app-post',
  standalone: false,
  templateUrl: './post.component.html',
  styleUrl: './post.component.css'
})
export class PostComponent implements OnInit {

  private actRoute = inject(ActivatedRoute)
  private fileUploadSvc = inject(FileUploadService)
  private fb = inject(FormBuilder)

  @ViewChild('file')
  imageFile !: ElementRef
  form !: FormGroup

  protected id = ''

  ngOnInit(): void {
    this.actRoute.params.subscribe(
      params => this.id = params['userId']
    )
    this.form = this.createForm()
  }

  post() {
    console.info('Uploading post...')
    const formData = new FormData()
    formData.set("post", this.form.value['post'])
    formData.set('file', this.imageFile.nativeElement.files[0])
    this.fileUploadSvc.uploadPost(formData, this.id)
  }

  private createForm() {
    return this.fb.group({
      post: this.fb.control<string>('')
    })

  }

}
