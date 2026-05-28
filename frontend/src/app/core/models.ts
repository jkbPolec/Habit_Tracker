export type HabitCategory = 'HEALTH' | 'STUDY' | 'WORK' | 'FITNESS' | 'PERSONAL' | 'OTHER';
export type HabitFrequency = 'DAILY' | 'WEEKLY';

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  username: string;
  email: string;
}

export interface HabitRequest {
  name: string;
  description: string;
  category: HabitCategory;
  frequency: HabitFrequency;
  targetCount: number;
  active?: boolean;
}

export interface HabitResponse {
  id: number;
  name: string;
  description: string;
  category: HabitCategory;
  frequency: HabitFrequency;
  targetCount: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  completedToday: boolean;
  currentStreak: number;
  bestStreak: number;
}

export interface DashboardStatisticsResponse {
  activeHabits: number;
  currentMonthCompletions: number;
}

export interface HabitCompletionResponse {
  id: number;
  habitId: number;
  completionDate: string;
  completedAt: string;
  note: string;
}

export interface ActivityLogResponse {
  id: number;
  habitId: number;
  eventType: string;
  message: string;
  createdAt: string;
}

export interface ApiError {
  message?: string;
  details?: Record<string, string>;
}
