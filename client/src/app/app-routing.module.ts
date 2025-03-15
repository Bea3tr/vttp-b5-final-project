import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login.component';
import { HomeComponent } from './components/home.component';
import { authGuard } from './guards/auth.guard';
import { RegisterComponent } from './components/register.component';
import { PostComponent } from './components/post.component';
import { ShelterComponent } from './components/shelter.component';

const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'home/:userId', component: HomeComponent, canActivate: [ authGuard ] },
  { path: 'shelter/:userId', component: ShelterComponent },
  { path: '**', redirectTo: '/', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
