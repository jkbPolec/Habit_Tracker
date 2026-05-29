import { Component, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { ActivityLogResponse, ApiError } from '../core/models';

@Component({
  selector: 'app-activity',
  standalone: true,
  imports: [RouterLink, DatePipe],
  template: `
    <main class="app-shell">
      <header class="topbar">
        <div>
          <h1>Activity log</h1>
          <span>{{ auth.currentUser()?.username }}</span>
        </div>
        <nav>
          <a routerLink="/dashboard">Dashboard</a>
          <button type="button" (click)="auth.logout()">Logout</button>
        </nav>
      </header>

      @if (error()) {
        <p class="banner">{{ error() }}</p>
      }

      <section class="activity-list">
        @if (!logs().length) {
          <p class="empty">No activity recorded yet.</p>
        }
        @for (log of logs(); track log.id) {
          <article class="activity-item" [class]="activityClass(log.eventType)">
            <div class="activity-main">
              <time>{{ log.createdAt | date:'short' }}</time>
              <p>{{ log.message }}</p>
            </div>
            <strong class="activity-badge">{{ activityLabel(log.eventType) }}</strong>
          </article>
        }
      </section>
    </main>
  `
})
export class ActivityComponent implements OnInit {
  readonly logs = signal<ActivityLogResponse[]>([]);
  readonly error = signal('');

  constructor(private readonly api: ApiService, readonly auth: AuthService) {}

  ngOnInit(): void {
    this.api.getActivity().subscribe({
      next: logs => this.logs.set(logs),
      error: err => {
        const apiError = err.error as ApiError;
        this.error.set(apiError?.message ?? 'Could not load activity');
      }
    });
  }

  activityClass(eventType: string): string {
    return `activity-item ${eventType.toLowerCase().replaceAll('_', '-')}`;
  }

  activityLabel(eventType: string): string {
    return eventType.toLowerCase().replaceAll('_', ' ');
  }
}
