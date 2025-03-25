import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ProfileService } from '../services/profile.service';

export const authGuard: CanActivateFn = 
  (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const router = inject(Router)
    const authSvc = inject(AuthService)
    
    // Allow navigation if logged in
    if (authSvc.isLoggedIn()) {
      return true 
    } 
    // Redirect to login if not authenticated
    alert('Please login')
    return router.navigate(['/']) 
}

export const profileGuard: CanActivateFn = 
  (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const router = inject(Router)
    const profileSvc = inject(ProfileService)
    
    const userId = route.params['userId']
    // Allow navigation if user if self
    if (profileSvc.isUser(userId)) {
      return true 
    } 
    // Redirect to login if not authenticated
    return router.navigate(['/home', sessionStorage.getItem('user')]) 
}
