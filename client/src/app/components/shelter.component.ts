import { HttpClient } from '@angular/common/http';
import { Component, inject, OnInit } from '@angular/core';
import { PfResult, UserInfo } from '../models/models';
import { ApiService } from '../services/api.service';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-shelter',
  standalone: false,
  templateUrl: './shelter.component.html',
  styleUrl: './shelter.component.css'
})
export class ShelterComponent implements OnInit {
  
  private http = inject(HttpClient);
  private apiSvc = inject(ApiService);
  private actRoute = inject(ActivatedRoute);
  
  protected pfData: PfResult[] = [];
  protected id = '';

  ngOnInit(): void {
      // Load default data
      this.getAnimals();
      this.actRoute.params.subscribe(
        async (params) => {
          this.id = params['userId'];
      });
  }

  showPicture(result: PfResult): string {
    console.info("Photos:", result.photos[result.currentPhotoIndex]);
    return result.photos[result.currentPhotoIndex];
  }

  nextPicture(result: PfResult) {
    result.currentPhotoIndex = (result.currentPhotoIndex + 1) % result.photos.length;
  }

  loadMore() {
    
  }

  private getAnimals() { 
    this.apiSvc.defaultLoadPf()
      .then((resp) => {
        resp.results.forEach((pf) => {
          const havePic = pf.photos.length > 0;
          console.info("Have Pic, name", pf.name, havePic);
          pf.currentPhotoIndex = 0;
        });
        this.pfData = resp.results;    
    })
  }

}
