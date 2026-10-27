import '@angular/compiler';
import { ChangeDetectorRef, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import axios from 'axios';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { DashboardComponent } from './dashboard.component';

vi.mock('axios', () => ({
  default: {
    get: vi.fn()
  }
}));

const storage = new Map<string, string>();

function createComponent() {
  const router = {
    navigate: vi.fn().mockResolvedValue(true)
  };
  const changeDetector = {
    detectChanges: vi.fn()
  };
  const zone = {
    run: vi.fn((callback: () => void) => callback())
  };

  return {
    component: new DashboardComponent(
      router as unknown as Router,
      changeDetector as unknown as ChangeDetectorRef,
      zone as unknown as NgZone
    ),
    router,
    changeDetector,
    zone
  };
}

describe('DashboardComponent', () => {
  beforeEach(() => {
    storage.clear();
    vi.clearAllMocks();
    vi.stubGlobal('localStorage', {
      getItem: vi.fn((key: string) => storage.get(key) ?? null),
      setItem: vi.fn((key: string, value: string) => storage.set(key, value)),
      removeItem: vi.fn((key: string) => storage.delete(key))
    });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it('loads, filters, labels, and sorts tasks for an authenticated user', async () => {
    storage.set('workshop_token', 'test-token');
    storage.set('workshop_user', JSON.stringify({ username: 'student' }));
    const { component, router, changeDetector, zone } = createComponent();
    const tasks = [
      { status: 'TODO', priority: 'HIGH', owner: 'student', title: 'Review', description: 'High' },
      { status: 'ARCHIVED', priority: 'LOW', owner: 'student', title: 'Old', description: 'Archived' },
      { status: 'IN_PROGRESS', priority: 'URGENT', owner: 'student', title: 'Resolve', description: 'Urgent' }
    ];
    vi.mocked(axios.get).mockResolvedValue({ data: tasks } as never);

    await component.ngOnInit();

    expect(axios.get).toHaveBeenCalledWith(
      'http://localhost:8000/api/tasks?owner=student',
      { headers: { Authorization: 'Bearer test-token' } }
    );
    expect(component.tasks).toBe(tasks);
    expect(component.visibleTasks.map((task) => task.title)).toEqual(['Resolve', 'Review']);
    expect(component.visibleTasks[0].label).toBe('URGENT · STUDENT');
    expect(component.loading).toBe(false);
    expect(component.error).toBe('');
    expect(router.navigate).not.toHaveBeenCalled();
    expect(zone.run).toHaveBeenCalledOnce();
    expect(changeDetector.detectChanges).toHaveBeenCalledOnce();
  });

  it('redirects to login and skips the task request when no token exists', async () => {
    const { component, router } = createComponent();

    await component.ngOnInit();

    expect(axios.get).not.toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
    expect(component.loading).toBe(true);
  });

  it('shows an invalid-token response and leaves the session in place', async () => {
    storage.set('workshop_token', 'expired-token');
    storage.set('workshop_user', JSON.stringify({ username: 'student' }));
    const { component, router, changeDetector } = createComponent();
    vi.mocked(axios.get).mockRejectedValue({
      response: { data: { detail: 'Token inválido' } }
    });

    await component.ngOnInit();

    expect(component.error).toBe('Token inválido');
    expect(component.loading).toBe(false);
    expect(storage.get('workshop_token')).toBe('expired-token');
    expect(router.navigate).not.toHaveBeenCalled();
    expect(changeDetector.detectChanges).toHaveBeenCalledOnce();
  });

  it('shows a task-request timeout without redirecting', async () => {
    storage.set('workshop_token', 'test-token');
    storage.set('workshop_user', JSON.stringify({ username: 'student' }));
    const { component, router } = createComponent();
    vi.mocked(axios.get).mockRejectedValue(new Error('timeout of 10000ms exceeded'));

    await component.ngOnInit();

    expect(component.error).toBe('timeout of 10000ms exceeded');
    expect(component.loading).toBe(false);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('shows a task-service network error without redirecting', async () => {
    storage.set('workshop_token', 'test-token');
    storage.set('workshop_user', JSON.stringify({ username: 'student' }));
    const { component, router } = createComponent();
    vi.mocked(axios.get).mockRejectedValue(new Error('Network Error'));

    await component.ngOnInit();

    expect(component.error).toBe('Network Error');
    expect(component.loading).toBe(false);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('handles an unexpected task response as a UI error', async () => {
    storage.set('workshop_token', 'test-token');
    storage.set('workshop_user', JSON.stringify({ username: 'student' }));
    const { component } = createComponent();
    vi.mocked(axios.get).mockResolvedValue({ data: null } as never);

    await component.ngOnInit();

    expect(component.error).toBeTruthy();
    expect(component.loading).toBe(false);
  });

  it('uses the documented fallback when task loading fails without a message', async () => {
    storage.set('workshop_token', 'test-token');
    storage.set('workshop_user', JSON.stringify({ username: 'student' }));
    const { component } = createComponent();
    vi.mocked(axios.get).mockRejectedValue({});

    await component.ngOnInit();

    expect(component.error).toBe('No se pudo cargar el dashboard');
    expect(component.loading).toBe(false);
  });
});
