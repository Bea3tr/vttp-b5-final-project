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

  private apiSvc = inject(ApiService);
  private actRoute = inject(ActivatedRoute);

  protected pfData: PfResult[] = [];
  protected id = '';
  protected savedPfs: number[] = [];
  protected breeds: string[] = [];

  ngOnInit(): void {
    // Load default data
    this.getAnimals();
    this.actRoute.params.subscribe(
      async (params) => {
        this.id = params['userId'];
      });
    this.getSavedPf(this.id);
    this.apiSvc.reload.subscribe(val => {
      if(val == true) {
        setTimeout(() => {
          console.info('Refetching saved...');
          this.getSavedPf(this.id);
        }, 100);
        this.apiSvc.reloadSavedPfs(false);
      }
    });
  }

  showPicture(result: PfResult): string {
    return result.photos[result.currentPhotoIndex];
  }

  nextPic(result: PfResult) {
    if (result.photos && result.photos.length > 0) {
      result.currentPhotoIndex = (result.currentPhotoIndex + 1) % result.photos.length;
    }
  }

  prevPic(result: PfResult) {
    if (result.photos && result.photos.length > 0) {
      result.currentPhotoIndex = (result.currentPhotoIndex - 1 + result.photos.length) % result.photos.length;
    }
  }

  savePfToUser(pfId: number) {
    this.apiSvc.savePfToUser(this.id, pfId);
    if(this.savedPfs.includes(pfId)) {
      this.removeSavedPf(pfId);
    }
    this.apiSvc.reloadSavedPfs(true);
  }

  removeSavedPf(pfId: number) {
    this.apiSvc.removeSavedPf(this.id, pfId);
  }

  getSavedPf(userId: string) {
    this.apiSvc.getSavedPf(userId)
      .then((resp) => {
        this.savedPfs = resp.result;
        console.info('Saved pfs:', this.savedPfs);
      })
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
