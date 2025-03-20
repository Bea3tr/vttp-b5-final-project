import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login.component';
import { HomeComponent } from './components/home.component';
import { authGuard } from './guards/auth.guard';
import { RegisterComponent } from './components/register.component';
import { ProfileComponent } from './components/profile.component';
import { ResetComponent } from './components/reset.component';

const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'home/:userId', component: HomeComponent, canActivate: [ authGuard ] },
  { path: 'profile/:userId', component: ProfileComponent, canActivate: [ authGuard ] },
  { path: 'reset-password', component: ResetComponent },
  { path: '**', redirectTo: '/', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { onSameUrlNavigation: 'reload' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
