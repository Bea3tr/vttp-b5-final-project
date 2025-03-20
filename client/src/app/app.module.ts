import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './components/login.component';
import { HomeComponent } from './components/home.component';
import { provideHttpClient } from '@angular/common/http';
import { MaterialModule } from './material.module';
import { ReactiveFormsModule } from '@angular/forms';
import { RegisterComponent } from './components/register.component';
import { PostComponent } from './components/post.component';
import { ShelterComponent } from './components/shelter.component';
import { ApiService } from './services/api.service';
import { AuthService } from './services/auth.service';
import { PostService } from './services/post.service';
import { ApiStore } from './api.store';
import { Subscription } from 'rxjs';
import { ProfileComponent } from './components/profile.component';
import { ProfileService } from './services/profile.service';
import { ResetComponent } from './components/reset.component';

@NgModule({
  declarations: 
    [ AppComponent, LoginComponent, HomeComponent, RegisterComponent, PostComponent, 
      ShelterComponent, ProfileComponent, ResetComponent ],
  imports: [ BrowserModule, AppRoutingModule, MaterialModule, ReactiveFormsModule ],
  providers: [ provideHttpClient(), ApiService, AuthService, PostService, ProfileService, ApiStore ],
  bootstrap: [AppComponent]
})
export class AppModule { }
