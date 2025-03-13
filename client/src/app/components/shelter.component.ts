import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';

@Component({
  selector: 'app-shelter',
  standalone: false,
  templateUrl: './shelter.component.html',
  styleUrl: './shelter.component.css'
})
export class ShelterComponent {
  private http = inject(HttpClient)

  // getAnimals() {
  //   return lastValueFrom(this.http.post<UploadResponse>('/api/shelter'))
  // }
}
