import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { ApiError } from '../core/models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="auth-shell">
      <section class="auth-panel">
        <h1>Habit Tracker</h1>
        <p class="muted">Sign in to manage your habits.</p>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <label>
            Email
            <input type="email" formControlName="email" autocomplete="email">
          </label>
          <label>
            Password
            <input type="password" formControlName="password" autocomplete="current-password">
          </label>

          @if (error) {
            <p class="error">{{ error }}</p>
          }

          <button class="primary" type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Signing in...' : 'Login' }}
          </button>
        </form>

        <p class="switch">No account yet? <a routerLink="/register">Create one</a></p>
      </section>
    </main>
  `
})
export class LoginComponent {
  loading = false;
  error = '';

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly router: Router
  ) {}

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.loading = true;
    this.error = '';
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: err => {
        const apiError = err.error as ApiError;
        this.error = apiError?.message ?? 'Login failed';
        this.loading = false;
      }
    });
  }
}
