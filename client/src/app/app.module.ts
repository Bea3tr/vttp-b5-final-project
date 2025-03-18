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
import { Subscription } from 'rxjs';
import { PostComponent } from './components/post.component';
import { ShelterComponent } from './components/shelter.component';
import { ApiService } from './services/api.service';
import { AuthService } from './services/auth.service';
import { PostService } from './services/post.service';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HomeComponent,
    RegisterComponent,
    PostComponent,
    ShelterComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MaterialModule,
    ReactiveFormsModule
  ],
  providers: [ provideHttpClient(), ApiService, AuthService, PostService ],
  bootstrap: [AppComponent]
})
export class AppModule { }
