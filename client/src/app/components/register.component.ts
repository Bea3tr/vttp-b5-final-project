import { Component, inject, OnInit, signal } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { HttpErrorResponse } from '@angular/common/http';
import { timer } from 'rxjs';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit {
    hide = signal(true)
    clickEvent(event: MouseEvent) {
      this.hide.set(!this.hide())
      event.stopPropagation()
    }
  
    constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer){
      this.matIconRegistry.addSvgIcon(
        "google",
        this.domSanitizer.bypassSecurityTrustResourceUrl("icons/google-icon.svg")
      )
    }

  private authSvc = inject(AuthService)
  private fb = inject(FormBuilder)

  protected step = 1
  protected form !: FormGroup
  protected emailForm !: FormGroup
  protected codeForm !: FormGroup
  protected codeTimeout = true
  protected userEmail = ''
  protected countdown = 45


  ngOnInit(): void {
      this.form = this.createForm()
      this.emailForm = this.fb.group({
        email: this.fb.control<string>('', 
          [ Validators.required, Validators.email, Validators.maxLength(256) ])
      })
      this.codeForm = this.fb.group({
        code: this.fb.control<string>('', 
          [ Validators.required, Validators.minLength(6), Validators.maxLength(6) ])
      })
  }

  protected isCtrlInvalid(ctrlName: string): boolean {
    return !!this.form.get(ctrlName)?.invalid
  }

 // Step 1: Send Verification Code
  sendVerificationCode() {
    this.userEmail = this.emailForm.value['email']
    this.authSvc.sendVerificationCode(this.userEmail)
      .then((resp) => {
        console.info(resp)
        this.step = 2 // Move to next step
        timer(0, 1000).subscribe((seconds) => {
          this.countdown = 45 - seconds
          if (this.countdown <= 0) {
            this.codeTimeout = false
          }
        })
      })
      .catch((err: HttpErrorResponse) => {
        alert(err.error.message)
      })
      this.emailForm.reset()
  }

  // Step 2: Verify Code
  verifyCode() {
    this.authSvc.verifyCode(this.userEmail, this.codeForm.value['code'])
      .then((resp) => {
        console.info(resp)
        this.step = 3
      })
      .catch((err: HttpErrorResponse) => {
        alert(err.error.message)
      })
      this.codeForm.reset()
  }

  googleLogin() {
    this.authSvc.googleLogin()
  }

  // Step 3: Register user
  register() {
    const value = this.form.value
    const userInfo = {
      name: value.name,
      email: this.userEmail,
      password: value.password
    }
    this.authSvc.register(userInfo)
  }

  private createForm() {
    return this.fb.group({
      name: this.fb.control<string>('', 
        [ Validators.required, Validators.minLength(3), Validators.maxLength(32) ]),
      password: this.fb.control<string>('', 
        [ Validators.required, Validators.minLength(8), Validators.maxLength(64) ])
    })
  }

}
