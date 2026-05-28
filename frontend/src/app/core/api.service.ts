import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {
  ActivityLogResponse,
  DashboardStatisticsResponse,
  HabitCompletionResponse,
  HabitRequest,
  HabitResponse
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly apiUrl = 'http://localhost:8080/api';

  constructor(private readonly http: HttpClient) {}

  getHabits() {
    return this.http.get<HabitResponse[]>(`${this.apiUrl}/habits`);
  }

  getDashboardStatistics() {
    return this.http.get<DashboardStatisticsResponse>(`${this.apiUrl}/habits/statistics`);
  }

  createHabit(payload: HabitRequest) {
    return this.http.post<HabitResponse>(`${this.apiUrl}/habits`, payload);
  }

  updateHabit(id: number, payload: HabitRequest) {
    return this.http.put<HabitResponse>(`${this.apiUrl}/habits/${id}`, payload);
  }

  deleteHabit(id: number) {
    return this.http.delete<void>(`${this.apiUrl}/habits/${id}`);
  }

  completeToday(id: number) {
    return this.http.post<HabitCompletionResponse>(`${this.apiUrl}/habits/${id}/completions`, {
      completionDate: this.today(),
      note: 'Done from dashboard'
    });
  }

  undoToday(id: number) {
    const today = this.today();
    return this.http.delete<void>(`${this.apiUrl}/habits/${id}/completions/${today}`);
  }

  getCompletions(id: number) {
    return this.http.get<HabitCompletionResponse[]>(`${this.apiUrl}/habits/${id}/completions`);
  }

  getActivity() {
    return this.http.get<ActivityLogResponse[]>(`${this.apiUrl}/activity`);
  }

  private today(): string {
    const date = new Date();
    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
