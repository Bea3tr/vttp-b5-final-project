import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormGroup, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { timer } from 'rxjs';

@Component({
  selector: 'app-reset',
  standalone: false,
  templateUrl: './reset.component.html',
  styleUrl: './reset.component.css'
})
export class ResetComponent implements OnInit {
  step: number = 1
  emailForm!: FormGroup
  codeForm!: FormGroup
  passwordForm!: FormGroup
  userEmail: string = ''
  codeTimeout = true
  countdown = 45

  private fb = inject(FormBuilder)
  private authSvc = inject(AuthService)
  private router = inject(Router)

  ngOnInit(): void {
    this.emailForm = this.fb.group({
      email: this.fb.control<string>('', [Validators.required, Validators.email])
    })
    this.codeForm = this.fb.group({
      code: this.fb.control<string>('', [Validators.required, Validators.minLength(6), Validators.maxLength(6)])
    })
    this.passwordForm = this.fb.group({
      newPassword: this.fb.control<string>('', [Validators.required, Validators.minLength(8), Validators.maxLength(64)]),
      confirmPassword: this.fb.control<string>('', [Validators.required])
    }, { validators: this.passwordMatch })

  }

    hide1 = signal(true)
    clickEvent1(event: MouseEvent) {
      this.hide1.set(!this.hide1())
      event.stopPropagation()
    }
  
    hide2 = signal(true)
    clickEvent2(event: MouseEvent) {
      this.hide2.set(!this.hide2())
      event.stopPropagation()
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
        });
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

  // Step 3: Reset Password
  resetPassword() {
    this.authSvc.resetPassword(this.userEmail, this.passwordForm.value['newPassword'])
      .then((resp) => {
        alert(resp.message)
        this.router.navigate(['/'])
      })
      .catch((err) => {
        alert(err.error.message)
      })
    this.passwordForm.reset();
  }

  private passwordMatch = (ctrl: AbstractControl) => {
    const values = Object.values(ctrl.value)
    if (values[0] == values[1]) {
      return null
    }
    return { passwordMatch: true } as ValidationErrors
  }
}
