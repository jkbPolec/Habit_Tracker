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
          <h1>Witaj {{ auth.currentUser()?.username }}!</h1>
          <span>Habit Tracker</span>
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
        <div class="metric active-metric">
          <span>Active habits</span>
          <strong>{{ stats()?.activeHabits ?? 0 }}</strong>
        </div>
        <div class="metric done-metric">
          <span>Done today</span>
          <strong>{{ doneTodayCount() }}</strong>
        </div>
        <div class="metric pending-metric">
          <span>Not done yet</span>
          <strong>{{ pendingTodayCount() }}</strong>
        </div>
        <div class="metric failed-metric">
          <span>Failed today</span>
          <strong>{{ failedTodayCount() }}</strong>
        </div>
        <div class="metric month-metric">
          <span>This month</span>
          <strong>{{ stats()?.currentMonthCompletions ?? 0 }}</strong>
        </div>
      </section>

      <section class="chart-panel">
        <div class="section-head">
          <div>
            <h2>Daily completions</h2>
            <span>Last 14 days</span>
          </div>
          <button type="button" (click)="toggleChart()">
            {{ chartExpanded() ? 'Hide chart' : 'Show chart' }}
          </button>
        </div>
        @if (chartExpanded()) {
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
        }
      </section>

      <section class="workspace">
        <section class="habit-list">
          <div class="section-head">
            <div>
              <h2>Your habits</h2>
              <span>{{ habits().length }} total</span>
            </div>
            <button class="primary" type="button" (click)="openCreateDialog()">Add habit</button>
          </div>
          @if (!habits().length) {
            <p class="empty">Add the first habit to start the demo flow.</p>
          }
          @for (habit of habits(); track habit.id) {
            <article class="habit-card"
                     [class.completed]="isDoneToday(habit)"
                     [class.pending]="isPendingToday(habit)"
                     [class.failed]="isFailedToday(habit)"
                     [class.target-reached]="targetReached(habit)"
                     [class.inactive]="!habit.active">
              <div class="card-head">
                <div>
                  <h3>{{ habit.name }}</h3>
                  <p>{{ habit.description || 'No description' }}</p>
                </div>
                <div class="card-badges">
                  <span class="status"
                        [class.done-status]="isDoneToday(habit)"
                        [class.pending-status]="isPendingToday(habit)"
                        [class.failed-status]="isFailedToday(habit)"
                        [class.inactive-status]="!habit.active">
                    {{ habitStatusLabel(habit) }}
                  </span>
                  @if (targetReached(habit)) {
                    <span class="target-badge">Target reached</span>
                  }
                  <span class="pill">{{ habit.category }}</span>
                </div>
              </div>
              <div class="habit-meta">
                <span>{{ habit.frequency }}</span>
                <span>Target {{ habit.targetCount }}</span>
                <span>Current {{ habit.currentStreak }}</span>
                <span>Best {{ habit.bestStreak }}</span>
              </div>
              <div class="target-progress" [class.reached]="targetReached(habit)">
                <div>
                  <span>Target progress</span>
                  <strong>{{ targetProgressLabel(habit) }}</strong>
                </div>
                <div class="progress-track" aria-hidden="true">
                  <span class="progress-fill" [style.width.%]="targetProgress(habit)"></span>
                </div>
              </div>
              <div class="actions wrap">
                @if (!habit.active) {
                  <span class="card-note">Paused, history is kept</span>
                } @else if (habit.completedToday) {
                  <button type="button" (click)="undoToday(habit)">
                    <span class="button-icon" aria-hidden="true">&#8634;</span>
                    Undo today
                  </button>
                } @else {
                  <button class="primary" type="button" (click)="openCompletionDialog(habit)">
                    <span class="button-icon" aria-hidden="true">&check;</span>
                    Done today
                  </button>
                }
                <button type="button" (click)="selectHabit(habit)">
                  <span class="button-icon" aria-hidden="true">i</span>
                  History
                </button>
                <button type="button" (click)="editHabit(habit)">
                  <span class="button-icon" aria-hidden="true">&#9998;</span>
                  Edit
                </button>
                <button class="danger" type="button" (click)="deleteHabit(habit)">
                  <span class="button-icon" aria-hidden="true">&times;</span>
                  Delete
                </button>
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

      @if (habitDialogOpen()) {
        <div class="modal-backdrop" (click)="closeHabitDialog()">
          <form class="modal-panel" [formGroup]="form" (ngSubmit)="saveHabit()" (click)="$event.stopPropagation()">
            <div class="modal-head">
              <div>
                <h2>{{ editingHabitId() ? 'Edit habit' : 'Add habit' }}</h2>
                <p>{{ editingHabitId() ? 'Update habit details and status.' : 'Create a new habit for your dashboard.' }}</p>
              </div>
              <button type="button" class="icon-button" (click)="closeHabitDialog()" aria-label="Close dialog">&times;</button>
            </div>

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
              @if (editingHabitId()) {
                <label class="checkbox">
                  <input type="checkbox" formControlName="active">
                  Active
                </label>
              } @else {
                <div class="form-note">
                  New habits start active. You can pause them later from Edit.
                </div>
              }
            </div>
            <div class="modal-actions">
              <button type="button" (click)="closeHabitDialog()">Cancel</button>
              <button class="primary" type="submit" [disabled]="form.invalid">
                {{ editingHabitId() ? 'Save changes' : 'Add habit' }}
              </button>
            </div>
          </form>
        </div>
      }

      @if (completingHabit()) {
        <div class="modal-backdrop" (click)="closeCompletionDialog()">
          <form class="modal-panel" [formGroup]="completionForm" (ngSubmit)="completeToday()" (click)="$event.stopPropagation()">
            <div class="modal-head">
              <div>
                <h2>Complete habit</h2>
                <p>{{ completingHabit()?.name }}</p>
              </div>
              <button type="button" class="icon-button" (click)="closeCompletionDialog()" aria-label="Close dialog">&times;</button>
            </div>

            <label>
              Note
              <textarea rows="4" formControlName="note" placeholder="Optional note for today's completion"></textarea>
            </label>

            <div class="modal-actions">
              <button type="button" (click)="closeCompletionDialog()">Cancel</button>
              <button class="primary" type="submit" [disabled]="completionForm.invalid">Save completion</button>
            </div>
          </form>
        </div>
      }
    </main>
  `
})
export class DashboardComponent implements OnInit {
  readonly habits = signal<HabitResponse[]>([]);
  readonly stats = signal<DashboardStatisticsResponse | null>(null);
  readonly dailyStats = signal<DailyCompletionStatsResponse[]>([]);
  readonly error = signal('');
  readonly editingHabitId = signal<number | null>(null);
  readonly habitDialogOpen = signal(false);
  readonly chartExpanded = signal(false);
  readonly completingHabit = signal<HabitResponse | null>(null);
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

  completionForm = this.fb.nonNullable.group({
    note: ['', Validators.maxLength(300)]
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
        this.closeHabitDialog();
        this.refresh();
      },
      error: err => this.showError(err)
    });
  }

  openCreateDialog(): void {
    this.editingHabitId.set(null);
    this.resetHabitForm();
    this.habitDialogOpen.set(true);
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
    this.habitDialogOpen.set(true);
  }

  closeHabitDialog(): void {
    this.editingHabitId.set(null);
    this.habitDialogOpen.set(false);
    this.resetHabitForm();
  }

  openCompletionDialog(habit: HabitResponse): void {
    this.completingHabit.set(habit);
    this.completionForm.reset({ note: '' });
  }

  closeCompletionDialog(): void {
    this.completingHabit.set(null);
    this.completionForm.reset({ note: '' });
  }

  completeToday(): void {
    if (this.completionForm.invalid) {
      return;
    }
    const habit = this.completingHabit();
    if (!habit) {
      return;
    }
    this.api.completeToday(habit.id, this.completionForm.getRawValue().note).subscribe({
      next: () => {
        this.closeCompletionDialog();
        this.refresh();
        if (this.selectedHabit()?.id === habit.id) {
          this.selectHabit(habit);
        }
      },
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

  toggleChart(): void {
    this.chartExpanded.update(expanded => !expanded);
  }

  doneTodayCount(): number {
    return this.habits().filter(habit => habit.active && habit.completedToday).length;
  }

  failedTodayCount(): number {
    return this.habits().filter(habit => this.isFailedToday(habit)).length;
  }

  pendingTodayCount(): number {
    return this.habits().filter(habit => this.isPendingToday(habit)).length;
  }

  isDoneToday(habit: HabitResponse): boolean {
    return habit.active && habit.completedToday;
  }

  isPendingToday(habit: HabitResponse): boolean {
    return habit.active && !habit.completedToday && !this.isFailedToday(habit);
  }

  isFailedToday(habit: HabitResponse): boolean {
    return habit.active && !habit.completedToday && habit.currentStreak === 0 && habit.bestStreak > 0;
  }

  habitStatusLabel(habit: HabitResponse): string {
    if (!habit.active) {
      return 'Inactive';
    }
    if (this.isDoneToday(habit)) {
      return 'Done today';
    }
    return this.isFailedToday(habit) ? 'Failed today' : 'Not done yet';
  }

  targetReached(habit: HabitResponse): boolean {
    return habit.active && habit.completedToday && habit.currentStreak >= habit.targetCount;
  }

  targetProgress(habit: HabitResponse): number {
    if (habit.targetCount <= 0) {
      return 0;
    }
    return Math.min(100, Math.round((habit.currentStreak / habit.targetCount) * 100));
  }

  targetProgressLabel(habit: HabitResponse): string {
    return `${Math.min(habit.currentStreak, habit.targetCount)}/${habit.targetCount}`;
  }

  formatChartDate(date: string): string {
    return new Intl.DateTimeFormat('en', { day: '2-digit', month: '2-digit' }).format(new Date(`${date}T00:00:00`));
  }

  private resetHabitForm(): void {
    this.form.reset({
      name: '',
      description: '',
      category: 'HEALTH',
      frequency: 'DAILY',
      targetCount: 1,
      active: true
    });
  }

  private showError(err: { error?: ApiError }): void {
    const apiError = err.error;
    const details = apiError?.details ? Object.values(apiError.details).join(' ') : '';
    this.error.set(details || apiError?.message || 'Request failed');
  }
}
