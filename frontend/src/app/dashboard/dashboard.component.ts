import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import {
  ApiError,
  DashboardStatisticsResponse,
  DailyCompletionStatsResponse,
  HabitCategory,
  HabitCompletionResponse,
  HabitFrequency,
  HabitRequest,
  HabitResponse
} from '../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <main class="app-shell">
      <header class="topbar">
        <div>
          <h1>Habit Tracker</h1>
          <span>{{ auth.currentUser()?.username }}</span>
        </div>
        <nav>
          <a routerLink="/activity">Activity</a>
          <button type="button" (click)="auth.logout()">Logout</button>
        </nav>
      </header>

      @if (error()) {
        <p class="banner">{{ error() }}</p>
      }

      <section class="stats-grid">
        <div class="metric">
          <span>Active habits</span>
          <strong>{{ stats()?.activeHabits ?? 0 }}</strong>
        </div>
        <div class="metric">
          <span>This month</span>
          <strong>{{ stats()?.currentMonthCompletions ?? 0 }}</strong>
        </div>
        <div class="metric">
          <span>Total habits</span>
          <strong>{{ habits().length }}</strong>
        </div>
      </section>

      <section class="chart-panel">
        <div class="section-head">
          <h2>Daily completions</h2>
          <span>Last 14 days</span>
        </div>
        <div class="line-chart">
          <svg viewBox="0 0 720 220" role="img" aria-label="Daily completions line chart">
            <line class="chart-grid" x1="30" y1="180" x2="700" y2="180"></line>
            <line class="chart-grid" x1="30" y1="110" x2="700" y2="110"></line>
            <line class="chart-grid" x1="30" y1="40" x2="700" y2="40"></line>
            <polyline class="chart-line" [attr.points]="linePoints()"></polyline>
            @for (point of chartPoints(); track point.date) {
              <circle class="chart-point" [attr.cx]="point.x" [attr.cy]="point.y" r="5"></circle>
              <text class="chart-value" [attr.x]="point.x" [attr.y]="point.y - 12">{{ point.completions }}</text>
            }
          </svg>
          <div class="line-labels">
            @for (day of dailyStats(); track day.date) {
              <span>{{ formatChartDate(day.date) }}</span>
            }
          </div>
        </div>
      </section>

      <section class="workspace">
        <form class="habit-form" [formGroup]="form" (ngSubmit)="saveHabit()">
          <h2>{{ editingHabitId() ? 'Edit habit' : 'Add habit' }}</h2>
          <label>
            Name
            <input type="text" formControlName="name">
          </label>
          <label>
            Description
            <textarea rows="4" formControlName="description"></textarea>
          </label>
          <div class="form-row">
            <label>
              Category
              <select formControlName="category">
                @for (category of categories; track category) {
                  <option [value]="category">{{ category }}</option>
                }
              </select>
            </label>
            <label>
              Frequency
              <select formControlName="frequency">
                @for (frequency of frequencies; track frequency) {
                  <option [value]="frequency">{{ frequency }}</option>
                }
              </select>
            </label>
          </div>
          <div class="form-row">
            <label>
              Target count
              <input type="number" min="1" formControlName="targetCount">
            </label>
            <label class="checkbox">
              <input type="checkbox" formControlName="active">
              Active
            </label>
          </div>
          <div class="actions">
            <button class="primary" type="submit" [disabled]="form.invalid">
              {{ editingHabitId() ? 'Save changes' : 'Add habit' }}
            </button>
            @if (editingHabitId()) {
              <button type="button" (click)="cancelEdit()">Cancel</button>
            }
          </div>
        </form>

        <section class="habit-list">
          <h2>Your habits</h2>
          @if (!habits().length) {
            <p class="empty">Add the first habit to start the demo flow.</p>
          }
          @for (habit of habits(); track habit.id) {
            <article class="habit-card" [class.inactive]="!habit.active">
              <div class="card-head">
                <div>
                  <h3>{{ habit.name }}</h3>
                  <p>{{ habit.description || 'No description' }}</p>
                </div>
                <span class="pill">{{ habit.category }}</span>
              </div>
              <div class="habit-meta">
                <span>{{ habit.frequency }}</span>
                <span>Target {{ habit.targetCount }}</span>
                <span>Current {{ habit.currentStreak }}</span>
                <span>Best {{ habit.bestStreak }}</span>
              </div>
              <div class="actions wrap">
                @if (!habit.active) {
                  <span class="status inactive-status">Inactive</span>
                } @else if (habit.completedToday) {
                  <button type="button" (click)="undoToday(habit)">Undo today</button>
                } @else {
                  <button class="primary" type="button" (click)="completeToday(habit)">Done today</button>
                }
                <button type="button" (click)="selectHabit(habit)">History</button>
                <button type="button" (click)="editHabit(habit)">Edit</button>
                <button class="danger" type="button" (click)="deleteHabit(habit)">Delete</button>
              </div>
            </article>
          }
        </section>

        <aside class="details-panel">
          <h2>Habit details</h2>
          @if (selectedHabit()) {
            <h3>{{ selectedHabit()?.name }}</h3>
            <p class="muted">Recent completions</p>
            @if (!selectedCompletions().length) {
              <p class="empty">No completions yet.</p>
            }
            <ul class="timeline">
              @for (completion of selectedCompletions(); track completion.id) {
                <li>
                  <span>{{ completion.completionDate }}</span>
                  <small>{{ completion.note || 'Completed' }}</small>
                </li>
              }
            </ul>
          } @else {
            <p class="empty">Choose History on a habit card.</p>
          }
        </aside>
      </section>
    </main>
  `
})
export class DashboardComponent implements OnInit {
  readonly habits = signal<HabitResponse[]>([]);
  readonly stats = signal<DashboardStatisticsResponse | null>(null);
  readonly dailyStats = signal<DailyCompletionStatsResponse[]>([]);
  readonly error = signal('');
  readonly editingHabitId = signal<number | null>(null);
  readonly selectedHabit = signal<HabitResponse | null>(null);
  readonly selectedCompletions = signal<HabitCompletionResponse[]>([]);

  readonly categories: HabitCategory[] = ['HEALTH', 'STUDY', 'WORK', 'FITNESS', 'PERSONAL', 'OTHER'];
  readonly frequencies: HabitFrequency[] = ['DAILY', 'WEEKLY'];

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(80)]],
    description: ['', Validators.maxLength(500)],
    category: ['HEALTH' as HabitCategory, Validators.required],
    frequency: ['DAILY' as HabitFrequency, Validators.required],
    targetCount: [1, [Validators.required, Validators.min(1)]],
    active: [true, Validators.required]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: ApiService,
    readonly auth: AuthService
  ) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.api.getHabits().subscribe({
      next: habits => this.habits.set(habits),
      error: err => this.showError(err)
    });
    this.api.getDashboardStatistics().subscribe({
      next: stats => this.stats.set(stats),
      error: err => this.showError(err)
    });
    this.api.getDailyCompletionStatistics().subscribe({
      next: stats => this.dailyStats.set(stats),
      error: err => this.showError(err)
    });
  }

  saveHabit(): void {
    if (this.form.invalid) {
      return;
    }
    const payload = this.form.getRawValue() as HabitRequest;
    const id = this.editingHabitId();
    const request = id ? this.api.updateHabit(id, payload) : this.api.createHabit(payload);
    request.subscribe({
      next: () => {
        this.cancelEdit();
        this.refresh();
      },
      error: err => this.showError(err)
    });
  }

  editHabit(habit: HabitResponse): void {
    this.editingHabitId.set(habit.id);
    this.form.setValue({
      name: habit.name,
      description: habit.description ?? '',
      category: habit.category,
      frequency: habit.frequency,
      targetCount: habit.targetCount,
      active: habit.active
    });
  }

  cancelEdit(): void {
    this.editingHabitId.set(null);
    this.form.reset({
      name: '',
      description: '',
      category: 'HEALTH',
      frequency: 'DAILY',
      targetCount: 1,
      active: true
    });
  }

  completeToday(habit: HabitResponse): void {
    this.api.completeToday(habit.id).subscribe({
      next: () => this.refresh(),
      error: err => this.showError(err)
    });
  }

  undoToday(habit: HabitResponse): void {
    this.api.undoToday(habit.id).subscribe({
      next: () => this.refresh(),
      error: err => this.showError(err)
    });
  }

  deleteHabit(habit: HabitResponse): void {
    this.api.deleteHabit(habit.id).subscribe({
      next: () => {
        if (this.selectedHabit()?.id === habit.id) {
          this.selectedHabit.set(null);
          this.selectedCompletions.set([]);
        }
        this.refresh();
      },
      error: err => this.showError(err)
    });
  }

  selectHabit(habit: HabitResponse): void {
    this.selectedHabit.set(habit);
    this.api.getCompletions(habit.id).subscribe({
      next: completions => this.selectedCompletions.set(completions),
      error: err => this.showError(err)
    });
  }

  chartPoints(): Array<{ date: string; completions: number; x: number; y: number }> {
    const stats = this.dailyStats();
    if (!stats.length) {
      return [];
    }
    const max = Math.max(...stats.map(day => day.completions), 1);
    const step = stats.length === 1 ? 0 : 670 / (stats.length - 1);
    return stats.map((day, index) => ({
      date: day.date,
      completions: day.completions,
      x: 30 + step * index,
      y: 180 - (day.completions / max) * 140
    }));
  }

  linePoints(): string {
    return this.chartPoints()
      .map(point => `${point.x},${point.y}`)
      .join(' ');
  }

  formatChartDate(date: string): string {
    return new Intl.DateTimeFormat('en', { day: '2-digit', month: '2-digit' }).format(new Date(`${date}T00:00:00`));
  }

  private showError(err: { error?: ApiError }): void {
    const apiError = err.error;
    const details = apiError?.details ? Object.values(apiError.details).join(' ') : '';
    this.error.set(details || apiError?.message || 'Request failed');
  }
}
