import { Component, inject, OnInit, signal } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit{
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
  private authSvc = inject(AuthService);
  private fb = inject(FormBuilder)
  protected form !: FormGroup

  ngOnInit(): void {
      this.form = this.createForm();
  }

  googleLogin() {
    this.authSvc.googleLogin();
  }

  userLogin() {
    const value = this.form.value
    const userInfo = {
      email: value.email,
      password: value.password
    }
    this.authSvc.userLogin(userInfo)
  }

  private createForm() {
    return this.fb.group({
      email: this.fb.control<string>('', [ Validators.required, Validators.email ]),
      password: this.fb.control<string>('', [ Validators.required ])
    })
  }
}
