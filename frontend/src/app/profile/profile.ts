import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';
import { AuthHttpService } from '../auth-http.service';
import { UserService, DemographicsRequest } from '../user.service';
import { EMPTY } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {

  form: FormGroup;

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);
  private originalValues: DemographicsRequest | null = null;

  constructor(
    private fb: FormBuilder,
    private authHttp: AuthHttpService,
    private userService: UserService,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      firstName:       ['', Validators.required],
      lastName:        ['', Validators.required],
      // The regex pattern allows digits, spaces, dashes, and parentheses
      // to support formats like (555) 555-5555 or +1-555-555-5555
      phone:           ['', [Validators.required, Validators.pattern(/^\+?[\d\s\-()\[\]]{7,15}$/)]],
      address1:        ['', Validators.required],
      address2:        [''],  // optional — no validators
      city:            ['', Validators.required],
      state:           ['', Validators.required],
      zip:             ['', Validators.required],
      email:           ['', [Validators.required, Validators.email]],
    });
  }

  ngOnInit() {
    this.authHttp.getStatus().pipe(
      catchError(() => {
        this.errorMessage.set('Could not reach the server. Please try again later.');
        setTimeout(() => this.router.navigate(['/login']), 2000);
        return EMPTY;
      }),
      switchMap(data => {
        if (!data.loggedIn) {
          this.errorMessage.set('You must be logged in to view this page.');
          setTimeout(() => this.router.navigate(['/login']), 2000);
          return EMPTY;
        }
        return this.userService.getUser();
      })
    ).subscribe({
      next: data => {
        this.originalValues = data;
        this.form.patchValue(data);
      },
      error: err => {
        console.error('Failed to load user data:', err);
        this.errorMessage.set('Could not load your profile data. Please try again.');
      }
    });
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const payload = {
      firstName: this.form.value.firstName,
      lastName:  this.form.value.lastName,
      phone:     this.form.value.phone,
      address1:  this.form.value.address1,
      address2:  this.form.value.address2,
      city:      this.form.value.city,
      state:     this.form.value.state,
      zip:       this.form.value.zip,
      email:     this.form.value.email,
    };

    this.form.disable();

    this.userService.updateDemographics(payload).subscribe({
      next: (res) => {
        this.form.enable();
        this.isLoading.set(false);
        this.originalValues = this.form.value;
        this.successMessage.set('Profile updated successfully.');
        this.auth.setUser(res.user);
      },
      error: (err) => {
        console.error('Failed to update profile:', err);

        this.form.enable();
        this.isLoading.set(false);
        this.errorMessage.set(
          err?.error?.message ??
          (typeof err?.error === 'string' ? err.error : null) ??
          'Update failed. Please try again.'
        );
      }
    });
  }

  onCancel(): void {
    if (this.originalValues) {
      this.form.patchValue(this.originalValues);
    }
  }
}
