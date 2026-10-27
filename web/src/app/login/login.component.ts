import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import axios from 'axios';
import merge from 'lodash/merge';

interface LoginMessage {
  title: string;
  detail: string;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  username: any = '';
  password: any = '';
  busy: any = false;
  message: LoginMessage = { title: '', detail: '' };
  preferences: any = { theme: 'light', showTips: true, compact: false };

  constructor(private router: Router) {
    try {
      const saved = JSON.parse(localStorage.getItem('workshop_preferences') || '{}');
      this.preferences = merge(this.preferences, saved);
    } catch (error) {
      console.log('Preferences could not be loaded', error);
    }
  }

  async login(): Promise<void> {
    this.message = { title: '', detail: '' };
    if (this.username === null || this.username === undefined || String(this.username).trim() === '') {
      this.message = { title: 'Ingrese un usuario.', detail: '' };
    } else {
      if (this.password === null || this.password === undefined || String(this.password).trim() === '') {
        this.message = { title: 'Ingrese una contraseña.', detail: '' };
      } else {
        this.busy = true;
        try {
          const response: any = await axios.post(
            'http://localhost:8080/auth/login',
            { username: this.username, password: this.password, client: 'angular-workshop' },
            { timeout: 5000 }
          );
          if (response && response.data && response.data.token) {
            localStorage.setItem('workshop_token', response.data.token);
            localStorage.setItem('workshop_user', JSON.stringify(response.data.user));
            localStorage.setItem('workshop_preferences', JSON.stringify(this.preferences));
            await this.router.navigate(['/dashboard']);
          } else {
            this.message = { title: 'El backend respondió sin token.', detail: '' };
          }
        } catch (error: any) {
          const backendMessage = error?.response?.data?.message || error?.message || 'Error desconocido';
          this.message = {
            title: 'No se pudo autenticar a ' + this.username + '.',
            detail: backendMessage
          };
          console.error(error);
        } finally {
          this.busy = false;
        }
      }
    }
  }
}
