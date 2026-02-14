import {Component, OnInit, signal} from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {RouterLink} from '@angular/router';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef } from '@angular/core';

import { AuthService } from '../auth.service'; // adjust path if needed


interface AuthStatus {
  loggedIn: boolean;
  message: string;
}

type SignInResponse = {
  message: string;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
  };
};

@Component({
  selector: 'app-login',
  imports: [CommonModule,RouterLink, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  authStatus = signal<AuthStatus | null>(null);

  loginForm!: FormGroup;
  signinError: any;

  constructor(private fb: FormBuilder, private http: HttpClient, private router: Router, private cdr: ChangeDetectorRef, private auth: AuthService) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required]],
    });
  }

  ngOnInit() {
    this.http.get<AuthStatus>('/api/auth/status').subscribe(data => {
      this.authStatus.set(data);
    });
  }

  onSignin() {
    this.signinError = '';

    const {username, password} = this.loginForm.getRawValue();

    const body = new HttpParams()
      .set('username', username)
      .set('password', password)
      .toString();

    if (this.loginForm.invalid) {
      // This marks all fields as "touched," triggering the @if blocks in your HTML
      this.loginForm.markAllAsTouched();
      return;
    }

    this.http.post<SignInResponse>('/api/auth/signin', body, {
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    }).subscribe({
      next: (res) => {
        this.auth.setUser(res.user);

        this.router.navigate(['/menu']);
      },
      error: (err) => {
        this.signinError =
          err?.error?.message ??
          (typeof err?.error === 'string' ? err.error : null) ??
          'Sign-in failed';
        this.cdr.detectChanges(); // 👈 render immediately
      }
    });
  }
}
