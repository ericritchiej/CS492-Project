import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators, FormGroup } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AuthService } from '../auth.service';
import { AuthHttpService } from '../auth-http.service';


@Component({
  selector: 'app-manager-login',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './manager-login.html',
  styleUrl: './manager-login.css',
})
export class ManagerLogin {
  form: FormGroup;
  loginError: string = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private auth: AuthService,
    private authHttp: AuthHttpService,
  ) {
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
    });
  }

  onLogin() {
    this.loginError = '';

    // T13: validation that both fields are entered and valid
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.loginError = 'Please enter both email and password.';
      return;
    }

    const { username, password } = this.form.getRawValue();

    // T12: validate with backend, route on success, error on failure
    this.authHttp.signInEmployee(username, password).subscribe({
      next: (res) => {
        this.auth.setUser(res.user);
        this.router.navigate(['/admin']);
      },
      error: (err) => {
        this.loginError =
          err?.error?.message ??
          (typeof err?.error === 'string' ? err.error : null) ??
          'Manager login failed';
        this.cdr.detectChanges();
      }
    });
  }
}
