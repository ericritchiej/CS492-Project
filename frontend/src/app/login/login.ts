import { Component, OnInit, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef } from '@angular/core';
import { AuthService } from '../auth.service';

/**
 * Describes the shape of the response from /api/auth/status.
 * Used to check if someone is already logged in when the page loads.
 */
interface AuthStatus {
  loggedIn: boolean;
  message: string;
}

/**
 * Describes the shape of the response from /api/auth/identify.
 * The backend resolves the email domain and tells us whether this
 * is a WORKER or CUSTOMER before authenticating.
 */
interface IdentifyResponse {
  loginType: 'WORKER' | 'CUSTOMER';
}

/**
 * Describes the shape of the response from /api/auth/signin.
 * These field names must match what AuthController sends back.
 */
type SignInResponse = {
  message: string;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
  };
};

@Component({
  selector: 'app-login',
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {

  authStatus = signal<AuthStatus | null>(null);

  loginForm!: FormGroup;

  /**
   * Stores any error message to display when login fails.
   */
  signinError: any;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private auth: AuthService
  ) {
    /**
     * Single form — email and password shown together from the start.
     * On submit, the backend's /identify endpoint is called first to
     * determine the user type, then the appropriate flow is triggered.
     * The user never has to click "Continue" between steps.
     */
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
    });
  }

  ngOnInit() {
    this.http.get<AuthStatus>('/api/auth/status').subscribe(data => {
      this.authStatus.set(data);
    });
  }

  /**
   * Called when the user clicks Sign In.
   *
   * First calls /api/auth/identify with the email to resolve the user type.
   * Then routes to the worker login page (WORKER) or authenticates directly (CUSTOMER).
   * The user fills out one form and clicks one button — no intermediate step.
   */
  onSignin() {
    this.signinError = '';

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { username, password } = this.loginForm.getRawValue();

    // Step 1 — identify the user type from the email domain
    this.http.post<IdentifyResponse>('/api/auth/identify', { email: username }).subscribe({
      next: (res) => {
        if (res.loginType === 'WORKER') {
          this.signinEmployee(username, password);
        } else {
          this.signinCustomer(username, password);
        }
      },
      error: (err) => {
        this.signinError =
          err?.error?.error ??
          err?.error?.message ??
          'Could not verify email address.';
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * Authenticates a customer against /api/auth/signin.
   * Only called after /identify has confirmed the user is a CUSTOMER.
   */
  private signinCustomer(username: string, password: string) {
    const body = new HttpParams()
      .set('username', username)
      .set('password', password)
      .toString();

    this.http.post<SignInResponse>('/api/auth/signin/customer', body, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
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
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * Authenticates a customer against /api/auth/signin.
   * Only called after /identify has confirmed the user is a CUSTOMER.
   */
  private signinEmployee(username: string, password: string) {
    const body = new HttpParams()
      .set('username', username)
      .set('password', password)
      .toString();

    this.http.post<SignInResponse>('/api/auth/signin/employee', body, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
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
        this.cdr.detectChanges();
      }
    });
  }
}
