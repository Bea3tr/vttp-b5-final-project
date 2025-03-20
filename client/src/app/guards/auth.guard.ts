import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = 
  (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const router = inject(Router)
    const authSvc = inject(AuthService)
    
    // Allow navigation if logged in
    if (authSvc.isLoggedIn()) {
      return true; 
    } 
    // Redirect to login if not authenticated
    alert('Please login')
    return router.navigate(['/']); 
}
