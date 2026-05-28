import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, RouterOutlet, Routes } from '@angular/router';
import { Component } from '@angular/core';
import { authInterceptor } from './app/core/auth.interceptor';
import { authGuard } from './app/core/auth.guard';
import { LoginComponent } from './app/auth/login.component';
import { RegisterComponent } from './app/auth/register.component';
import { DashboardComponent } from './app/dashboard/dashboard.component';
import { ActivityComponent } from './app/activity/activity.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'activity', component: ActivityComponent, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'dashboard' }
];

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet />'
})
class AppComponent {}

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
}).catch(err => console.error(err));
