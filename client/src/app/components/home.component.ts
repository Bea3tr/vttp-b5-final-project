import { Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { UserInfo } from '../models/models';
import { FileUploadService } from '../services/fileupload.service';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})

export class HomeComponent implements OnInit {

  constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer,
    private authSvc: AuthService, private router: Router, private actRoute: ActivatedRoute,
    private fileUploadSvc: FileUploadService, private fb: FormBuilder) {
      const icons = ["plus", "cross", "shelter", "shop", "color-shelter", "color-shop"];
      icons.forEach((icon) => {
        this.matIconRegistry.addSvgIcon(
          icon,
          this.domSanitizer.bypassSecurityTrustResourceUrl(`icons/${icon}-icon.svg`)
        );
      });
  }

  @ViewChild('file')
  imageFile !: ElementRef;
  form !: FormGroup;
  
  protected id: string = '';
  protected user !: UserInfo;
  protected isPopupOpen = false;
  protected showMessage = false;
  protected postMessage = '';
  protected messageType = '';
  
  ngOnInit(): void {
    this.form = this.createForm();
    this.actRoute.params.subscribe(
      async (params) => {
        this.id = params['userId'];
        let r = await this.authSvc.getUserInfo(this.id);
        this.user = r;
      });
  }

  post() {
    console.info('Uploading post...');
    const formData = new FormData();
    formData.set("post", this.form.value['post']);
    if(this.imageFile.nativeElement.files[0]){
      formData.set('file', this.imageFile.nativeElement.files[0]);
    } else {
      formData.set('file', new Blob(), '');
    }
    this.fileUploadSvc.uploadPost(formData, this.id)
      .then((resp) => {
        if(resp.success == true) {
          this.postMessage = resp.message;
          this.messageType = 'success';
        } else {
          this.postMessage = 'Upload unsuccessful';
          this.messageType = 'failed';
        }
        this.showMessage = true;
          // Hide message after 1 second
        setTimeout(() => {
          this.showMessage = false;
        }, 2000);
    });
    this.isPopupOpen = false;
  }

  private createForm() {
    return this.fb.group({
      post: this.fb.control<string>('')
    });
  }

  logout() {
    this.authSvc.logout();
    this.router.navigate(['/']);
  }

  openPopup() {
    this.isPopupOpen = true;
  }

  closePopup() {
    this.isPopupOpen = false;
  }
}
