import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { UserInfo } from '../models/models';
import { PostService } from '../services/post.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProfileService } from '../services/profile.service';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})

export class HomeComponent implements OnInit {

  constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer,
    private authSvc: AuthService, private router: Router, private actRoute: ActivatedRoute,
    private postSvc: PostService, private fb: FormBuilder, private profileSvc: ProfileService) {
    const icons = ["plus", "cross", "shelter", "shop", "color-shelter", "color-shop"]
    icons.forEach((icon) => {
      this.matIconRegistry.addSvgIcon(
        icon,
        this.domSanitizer.bypassSecurityTrustResourceUrl(`icons/${icon}-icon.svg`)
      )
    })
  }

  form !: FormGroup

  protected id: string = ''
  protected user !: UserInfo
  protected isPopupOpen = false
  protected activeTab = ''
  protected selectedFiles !: FileList

  onFileChange(event: any) {
    console.info('onFileChange:', event.target.files)
    this.selectedFiles = event.target.files
  }

  setActiveTab(tab: string) {
    this.activeTab = tab
  }

  ngOnInit(): void {
    this.form = this.createForm()
    this.actRoute.params.subscribe(
      async (params) => {
        this.id = params['userId']
        let r = await this.authSvc.getUserInfo(this.id)
        this.user = r
      })
    this.profileSvc.activeTab.subscribe(async (tab) => {
      console.info('Current:', tab)
      this.activeTab = tab
    })
  }

  post() {
    this.postSvc.uploadPost(this.form, this.selectedFiles, this.id)
      .then((resp) => {
        if(resp.success == true) {
          alert('Upload successful')
        } else {
          alert('Error uploading post')
        }
    })
    this.form.reset()
    this.isPopupOpen = false
    this.postSvc.reloadPosts(true)
  }

  private createForm() {
    return this.fb.group({
      post: this.fb.control<string>(''),
      status: this.fb.control<string>('', [Validators.required])
    })
  }

  logout() {
    this.authSvc.logout()
    this.router.navigate(['/'])
  }

  openPopup() {
    this.isPopupOpen = true
  }

  closePopup() {
    this.isPopupOpen = false
  }
}
function then(arg0: (resp: any) => void) {
  throw new Error('Function not implemented.')
}

