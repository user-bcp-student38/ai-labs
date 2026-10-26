import { readFileSync } from 'node:fs';
import '@angular/compiler';
import { Router } from '@angular/router';
import axios from 'axios';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { LoginComponent } from './login.component';

vi.mock('axios', () => ({
  default: {
    post: vi.fn()
  }
}));

const preferenceStorageKey = 'workshop_preferences';
const tokenStorageKey = 'workshop_token';
const userStorageKey = 'workshop_user';
const storage = new Map<string, string>();

function createComponent() {
  const router = {
    navigate: vi.fn().mockResolvedValue(true)
  };

  return {
    component: new LoginComponent(router as unknown as Router),
    router
  };
}

describe('LoginComponent', () => {
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

  it('shows a username validation message as plain text', async () => {
    const { component } = createComponent();

    await component.login();

    expect(component.message).toEqual({ title: 'Ingrese un usuario.', detail: '' });
    expect(axios.post).not.toHaveBeenCalled();
  });

  it('shows a password validation message as plain text', async () => {
    const { component } = createComponent();
    component.username = 'student';

    await component.login();

    expect(component.message).toEqual({ title: 'Ingrese una contraseña.', detail: '' });
    expect(axios.post).not.toHaveBeenCalled();
  });

  it('shows a missing-token message as plain text', async () => {
    const { component } = createComponent();
    component.username = 'student';
    component.password = 'Workshop123!';
    vi.mocked(axios.post).mockResolvedValue({ data: {} } as never);

    await component.login();

    expect(component.message).toEqual({
      title: 'El backend respondió sin token.',
      detail: ''
    });
  });

  it('keeps login and backend error values as plain text', async () => {
    const { component } = createComponent();
    component.username = 'student <test>';
    component.password = 'wrong';
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
    vi.mocked(axios.post).mockRejectedValue({
      response: { data: { message: 'Acceso & denegado' } }
    });

    await component.login();

    expect(component.message).toEqual({
      title: 'No se pudo autenticar a student <test>.',
      detail: 'Acceso & denegado'
    });
    expect(component.busy).toBe(false);
  });

  it('shows an unavailable authentication service error without navigating', async () => {
    const { component, router } = createComponent();
    component.username = 'student';
    component.password = 'Workshop123!';
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
    vi.mocked(axios.post).mockRejectedValue(new Error('Network Error'));

    await component.login();

    expect(component.message).toEqual({
      title: 'No se pudo autenticar a student.',
      detail: 'Network Error'
    });
    expect(component.busy).toBe(false);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('shows the authentication timeout error without navigating', async () => {
    const { component, router } = createComponent();
    component.username = 'student';
    component.password = 'Workshop123!';
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
    vi.mocked(axios.post).mockRejectedValue(
      Object.assign(new Error('timeout of 5000ms exceeded'), { code: 'ECONNABORTED' })
    );

    await component.login();

    expect(component.message).toEqual({
      title: 'No se pudo autenticar a student.',
      detail: 'timeout of 5000ms exceeded'
    });
    expect(component.busy).toBe(false);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('preserves the login request and successful navigation behavior', async () => {
    const { component, router } = createComponent();
    component.username = 'student';
    component.password = 'Workshop123!';
    component.preferences = { theme: 'dark', showTips: false, compact: true };
    vi.mocked(axios.post).mockResolvedValue({
      data: { token: 'test-token', user: { username: 'student' } }
    } as never);

    await component.login();

    expect(axios.post).toHaveBeenCalledWith(
      'http://localhost:8080/auth/login',
      { username: 'student', password: 'Workshop123!', client: 'angular-workshop' },
      { timeout: 5000 }
    );
    expect(storage.get(tokenStorageKey)).toBe('test-token');
    expect(storage.get(userStorageKey)).toBe('{"username":"student"}');
    expect(storage.get(preferenceStorageKey)).toBe(
      '{"theme":"dark","showTips":false,"compact":true}'
    );
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
    expect(component.busy).toBe(false);
  });

  it('renders dynamic message fields with interpolation instead of innerHTML', () => {
    const template = readFileSync(new URL('./login.component.html', import.meta.url), 'utf8');

    expect(template).toContain('<strong>{{ message.title }}</strong>');
    expect(template).toContain('{{ message.detail }}');
    expect(template).not.toContain('[innerHTML]');
  });
});
