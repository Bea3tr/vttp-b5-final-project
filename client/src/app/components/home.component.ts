import { Component, inject, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { UserInfo } from '../models/models';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})

export class HomeComponent implements OnInit {

  constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer,
    private authSvc: AuthService, private router: Router, private actRoute: ActivatedRoute) {
    this.matIconRegistry.addSvgIcon(
      "add",
      this.domSanitizer.bypassSecurityTrustResourceUrl("plus.svg")
    )
  }

  protected id: string = ''
  protected user !: UserInfo

  ngOnInit(): void {
        this.actRoute.params.subscribe(
          async(params) => {
            this.id = params['userId'];
            let r = await this.authSvc.getUserInfo(this.id);
            this.user = r;
        });
  }

  logout() {
    this.authSvc.logout()
    this.router.navigate(['/'])
  }
}
