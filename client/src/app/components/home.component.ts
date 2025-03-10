import { Component, inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})

export class HomeComponent {
  private authSvc = inject(AuthService)
  private router = inject(Router)

  logout() {
    this.authSvc.logout()
    this.router.navigate(['/'])
  }
}
