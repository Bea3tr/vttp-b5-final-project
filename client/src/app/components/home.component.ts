import { Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { UserInfo } from '../models/models';
import { FileUploadService } from '../services/fileupload.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

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

  form !: FormGroup;
  
  protected id: string = '';
  protected user !: UserInfo;
  protected isPopupOpen = false;
  protected showMessage = false;
  protected postMessage = '';
  protected messageType = '';
  protected activeTab = 'post';
  protected selectedFiles !: FileList;
  
  onFileChange(event: any) {
    console.info('onFileChange:', event.target.files);
    this.selectedFiles = event.target.files;
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
  }
  
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
    this.fileUploadSvc.uploadPost(this.form, this.selectedFiles, this.id);
    //   .then((resp) => {
    //     if(resp.success == true) {
    //       console.info('Post successful');
    //       this.postMessage = resp.message;
    //       this.messageType = 'success';
    //     } else {
    //       this.postMessage = 'Upload unsuccessful';
    //       this.messageType = 'failed';
    //     }
    //     this.showMessage = true;
    //     console.info('Show:', this.showMessage, this.messageType, this.postMessage);
    //       // Hide message after 1 second
    //     setTimeout(() => {
    //       this.showMessage = false;
    //     }, 2000);
    // });
    this.form.reset();
    this.isPopupOpen = false;
    this.fileUploadSvc.reloadPosts(true);
  }

  private createForm() {
    return this.fb.group({
      post: this.fb.control<string>(''),
      status: this.fb.control<string>('', [ Validators.required ])
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
