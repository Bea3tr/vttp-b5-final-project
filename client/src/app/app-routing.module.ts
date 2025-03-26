import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login.component';
import { HomeComponent } from './components/home.component';
import { authGuard, profileGuard } from './guards/auth.guard';
import { RegisterComponent } from './components/register.component';
import { ProfileComponent } from './components/profile.component';
import { ResetComponent } from './components/reset.component';
import { ProfileOthersComponent } from './components/profile-others.component';
import { MarketplaceComponent } from './components/marketplace.component';

const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'home/:userId', component: HomeComponent, canActivate: [ authGuard ] },
  { path: 'profile/:userId', component: ProfileComponent, canActivate: [ profileGuard ] },
  { path: 'user/:id/:userId', component: ProfileOthersComponent, canActivate: [ authGuard ]},
  { path: 'reset-password', component: ResetComponent },
  { path: '**', redirectTo: '/', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { onSameUrlNavigation: 'reload' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
