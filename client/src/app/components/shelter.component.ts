import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Load, PfResult } from '../models/models';
import { ApiService } from '../services/api.service';
import { ActivatedRoute } from '@angular/router';
import { AbstractControl, FormArray, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { ApiStore } from '../api.store';
import { Observable, tap } from 'rxjs';

@Component({
  selector: 'app-shelter',
  standalone: false,
  templateUrl: './shelter.component.html',
  styleUrl: './shelter.component.css'
})

export class ShelterComponent implements OnInit {

  constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer,
    private apiSvc: ApiService, private actRoute: ActivatedRoute, private fb: FormBuilder,
    private apiStore: ApiStore
  ) {
    this.matIconRegistry.addSvgIcon(
      "refresh",
      this.domSanitizer.bypassSecurityTrustResourceUrl("icons/refresh-icon.svg")
    )
  }

  protected ageList = ['Baby', 'Young', 'Adult', 'Senior'];
  protected sizeList = ['Small', 'Medium', 'Large', 'Extra Large'];
  protected genderList = ['Male', 'Female', 'Unknown'];

  protected load$!: Observable<Load>

  protected pfData: PfResult[] = [];
  protected pfTypes: string[] = [];
  protected pfBreeds: string[] = [];
  protected selectedType = '';
  protected id = '';
  protected savedPfs: number[] = [];
  protected breeds: string[] = [];
  protected form!: FormGroup;
  protected isAtBottom = false;
  protected isFiltered = false;
  protected pf_ids: number[] = []
  protected isNoData = false;

  @ViewChild('scrollContainer', { static: false })
  scrollContainer!: ElementRef;

  onScroll(event: Event): void {
    const target = event.target as HTMLElement;
    const scrollPosition = target.scrollTop + target.clientHeight;
    const scrollHeight = target.scrollHeight;

    this.isAtBottom = scrollPosition >= scrollHeight - 50;
  }

  ngOnInit(): void {
    // Load default data
    console.info('Filtered:', this.isFiltered);
    this.getAnimals();
    this.getTypes();
    this.form = this.createForm();
    this.actRoute.params.subscribe(
      async (params) => {
        this.id = params['userId'];
      });
    this.getSavedPf(this.id);
    this.apiSvc.reload.subscribe(val => {
      if (val == true) {
        setTimeout(() => {
          console.info('Refetching saved...');
          this.getSavedPf(this.id);
        }, 100);
        this.apiSvc.reloadSavedPfs(false);
      }
    });
  }

  onTypeChange(event: any) {
    const type = event.value
    console.info('Type:', type);
    this.selectedType = type;
    this.getBreeds(type);
  }

  async processForm() {
    this.isNoData = false;
    this.pf_ids = [];
    console.info('Form:', this.form.value);
    console.info('Ids:', this.pf_ids);
    await this.apiSvc.getFilteredPf(this.form)
      .then((resp) => {
        resp.results.forEach((pf) => {
          pf.currentPhotoIndex = 0;
          this.pf_ids = [...this.pf_ids, pf.id];
        });
        this.pfData = resp.results;
      })
      .catch((err) => {
        console.info(err.message);
        this.pfData = [];
      })
    this.isFiltered = true;
    this.apiStore.loadForm(this.formToLoad(this.form));
    console.info('Loaded ids:', this.pf_ids);
    this.apiStore.loadFormIds(this.pf_ids);
    this.form.reset();
  }

  loadMore() {
    if (this.isFiltered) {
      console.info('Loading more filtered');
      this.apiStore.getLoaded
        .pipe(
          tap(async (load) => {
            console.info('Loaded: ', load);
            await this.apiSvc.loadMorePfFiltered(load)
              .then((resp) => {
                if(resp.results.length > 0) {
                  resp.results.forEach((pf) => {
                    pf.currentPhotoIndex = 0;
                    this.pf_ids.push(pf.id);
                  });
                  this.pfData.push(...resp.results);
                } else {
                  this.isNoData = true;
                }
              })
              .catch((err) => {
                console.info(err.message);
                this.pfData = [];
              });
          })
        ).subscribe()
        console.info('[Filter] After load:', this.pf_ids, this.isNoData);
        this.apiStore.loadFormIds(this.pf_ids);
    } else {
      console.info('[ALL] Loaded:', this.pf_ids);
      this.apiSvc.loadMorePf(this.pf_ids)
        .then((resp) => {
          if(resp.results.length > 0) {
            resp.results.forEach((pf) => {
              pf.currentPhotoIndex = 0;
              this.pf_ids = [...this.pf_ids, pf.id];
            });
            this.pfData.push(...resp.results);
          } else {
            this.isNoData = true;
          }
          
        })
        .catch((err) => {
          console.info(err.message);
          this.pfData = [];
        })
    }
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
    if (this.savedPfs.includes(pfId)) {
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

  get formAge(): FormArray {
    return this.form.get('age') as FormArray;
  }

  getAnimals() {
    console.info('Getting all animals');
    this.pf_ids = [];
    this.isNoData = false;
    this.apiSvc.defaultLoadPf()
      .then((resp) => {
        resp.results.forEach((pf) => {
          pf.currentPhotoIndex = 0;
          this.pf_ids = [...this.pf_ids, pf.id]
        });
        this.pfData = resp.results;
      })
      .catch((err) => {
        console.info(err.message)
        this.pfData = [];
      })
    this.getTypes();
    this.isFiltered = false;
  }

  private formToLoad(form: any): Load {
    return {
      type: form.value['type'] ? form.value['type'] : '',
      breed: form.value['breed'] ? form.value['breed'] : '',
      size: form.value['size'] ? form.value['size'] : '',
      gender: form.value['gender'] ? form.value['gender'] : '',
      age: form.value['age'] ? form.value['age'] : '',
      name: form.value['name'] ? form.value['name'] : '',
      location: form.value['location'] ? form.value['location'] : '',
      pf_ids: [...this.pf_ids]
    } as Load;
  }

  private getTypes() {
    this.apiSvc.getTypes()
      .then((resp) => {
        this.pfTypes = resp.results;
      })
      .catch((err) => {
        console.info(err.message);
        this.pfTypes = []
      })
  }

  private getBreeds(type: string) {
    console.info('Retrieving breeds');
    this.apiSvc.getBreeds(type)
      .then((resp) => {
        this.pfBreeds = resp.results;
      })
      .catch((err) => {
        console.info(err.message);
        this.pfBreeds = [];
      })
  }

  private createForm(): FormGroup {
    return this.fb.group({
      type: this.fb.control<string>(''),
      breed: this.fb.control<string>(''),
      size: this.fb.control<string>(''),
      gender: this.fb.control<string>(''),
      age: this.fb.control<string>(''),
      name: this.fb.control<string>(''),
      location: this.fb.control<string>('')
    }, { validators: this.atLeastOneInput })
  }

  private atLeastOneInput = (ctrl: AbstractControl) => {
    const values = Object.values(ctrl.value);
    const allEmpty = values.every(val => !val)
    if (!allEmpty) {
      return null
    }
    return { atLeastOneInput: true } as ValidationErrors
  }

}
