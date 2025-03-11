import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements OnInit {
    hide = signal(true);
    clickEvent(event: MouseEvent) {
      this.hide.set(!this.hide());
      event.stopPropagation();
    }
  
    constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer){
      this.matIconRegistry.addSvgIcon(
        "google",
        this.domSanitizer.bypassSecurityTrustResourceUrl("google_icon.svg")
      )
    }

  private router = inject(Router)
  private authSvc = inject(AuthService)
  private fb = inject(FormBuilder)

  protected form !: FormGroup

  ngOnInit(): void {
      this.form = this.createForm()
  }

  protected isCtrlInvalid(ctrlName: string): boolean {
    return !!this.form.get(ctrlName)?.invalid
  }

  googleLogin() {
    this.authSvc.googleLogin();
  }

  register() {
    const value = this.form.value
    const userInfo = {
      name: value.name,
      email: value.email,
      password: value.password
    }
    this.authSvc.register(userInfo)
  }

  private createForm() {
    return this.fb.group({
      name: this.fb.control<string>('', [ Validators.required, Validators.minLength(3), Validators.maxLength(32) ]),
      email: this.fb.control<string>('', [ Validators.required, Validators.maxLength(256), Validators.email ]),
      password: this.fb.control<string>('', [ Validators.required, Validators.minLength(8), Validators.maxLength(64) ])
    })
  }

}
