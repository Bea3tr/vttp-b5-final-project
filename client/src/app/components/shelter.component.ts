import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { PfResult } from '../models/models';
import { ApiService } from '../services/api.service';
import { ActivatedRoute } from '@angular/router';
import { AbstractControl, FormArray, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-shelter',
  standalone: false,
  templateUrl: './shelter.component.html',
  styleUrl: './shelter.component.css'
})
export class ShelterComponent implements OnInit {

  constructor(private matIconRegistry: MatIconRegistry, private domSanitizer: DomSanitizer,
    private apiSvc: ApiService, private actRoute: ActivatedRoute, private fb: FormBuilder
  ) {
    this.matIconRegistry.addSvgIcon(
      "refresh",
      this.domSanitizer.bypassSecurityTrustResourceUrl("icons/refresh-icon.svg")
    )
  }
  protected ageList = ['Baby', 'Young', 'Adult', 'Senior'];
  protected sizeList = ['Small', 'Medium', 'Large', 'Extra Large'];
  protected genderList = ['Male', 'Female', 'Unknown'];

  protected pfData: PfResult[] = [];
  protected pfTypes: string[] = [];
  protected pfBreeds: string[] = [];
  protected selectedType = '';
  protected id = '';
  protected savedPfs: number[] = [];
  protected breeds: string[] = [];
  protected form!: FormGroup;
  protected isAtBottom = false;
  protected counter = 0;

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
    this.counter = 0;
  }

  onTypeChange(event: any) {
    const type = event.value
    console.info('Type:', type);
    this.selectedType = type;
    this.getBreeds(type);
  }

  processForm() {
    console.info('Form:', this.form.value);
    this.apiSvc.getFilteredPf(this.form)
      .then((resp) => {
        resp.results.forEach((pf) => {
          pf.currentPhotoIndex = 0;
        });
        this.pfData = resp.results;
      })
      .catch((err) => {
        console.info(err.message);
        this.pfData = [];
      })
    this.form.reset();
  }

  loadMore(count: number) {
    this.counter += count;
    this.apiSvc.loadMorePf(this.counter)
      .then((resp) => {
        resp.results.forEach((pf) => {
          pf.currentPhotoIndex = 0;
        });
        this.pfData.push(...resp.results);
      })
      .catch((err) => {
        console.info(err.message);
        this.pfData = [];
      })
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

  private getAnimals() {
    this.apiSvc.defaultLoadPf()
      .then((resp) => {
        resp.results.forEach((pf) => {
          pf.currentPhotoIndex = 0;
        });
        this.pfData = resp.results;
      })
      .catch((err) => {
        console.info(err.message)
        this.pfData = [];
      })
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
    if (!allEmpty){
      return null
    }
    return { atLeastOneInput: true } as ValidationErrors
  }

}
