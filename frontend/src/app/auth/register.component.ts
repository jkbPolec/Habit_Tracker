import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { ApiError } from '../core/models';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="auth-shell">
      <section class="auth-panel">
        <h1>Create account</h1>
        <p class="muted">Start tracking habits with your own private dashboard.</p>

        <form [formGroup]="form" (ngSubmit)="submit()">
          <label>
            Username
            <input type="text" formControlName="username" autocomplete="username">
          </label>
          <label>
            Email
            <input type="email" formControlName="email" autocomplete="email">
          </label>
          <label>
            Password
            <input type="password" formControlName="password" autocomplete="new-password">
          </label>

          @if (error) {
            <p class="error">{{ error }}</p>
          }

          <button class="primary" type="submit" [disabled]="form.invalid || loading">
            {{ loading ? 'Creating...' : 'Register' }}
          </button>
        </form>

        <p class="switch">Already registered? <a routerLink="/login">Login</a></p>
      </section>
    </main>
  `
})
export class RegisterComponent {
  loading = false;
  error = '';

  form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
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
    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl('/dashboard'),
      error: err => {
        const apiError = err.error as ApiError;
        this.error = apiError?.message ?? 'Registration failed';
        this.loading = false;
      }
    });
  }
}
