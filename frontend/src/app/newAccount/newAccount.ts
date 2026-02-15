import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../auth.service';

/**
 * Describes the shape of the response from /api/auth/status.
 * "interface" is similar to "type" — both define the shape of an object.
 * This tells TypeScript what fields to expect so it can catch typos
 * and provide autocomplete when we use the data.
 */
interface AuthStatus {
  loggedIn: boolean;
  message: string;
}

/**
 * Describes the shape of the response from /api/auth/register.
 * When our Spring Boot backend returns JSON after a successful registration,
 * TypeScript will expect it to match this structure exactly.
 * If the backend sends different field names, the data won't map correctly.
 */
type RegisterResponse = {
  message: string;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
  };
};

/**
 * A custom validator function that checks whether the password and
 * confirmPassword fields in the form match each other.
 *
 * WHY A FUNCTION THAT RETURNS A FUNCTION?
 * Angular validators follow this pattern — the outer function is called once
 * to configure the validator, and the inner function is called by Angular
 * every time the form value changes to check if it's valid.
 *
 * This validator is attached to the FORM GROUP (not an individual field)
 * because it needs to read TWO fields at once to compare them.
 * Individual field validators can only see their own field's value.
 *
 * Returns null if passwords match (null = valid in Angular validators).
 * Returns { passwordMismatch: true } if they don't match, which the
 * HTML template uses to show an error message to the user.
 *
 * ValidatorFn     — the type Angular expects for validator functions
 * AbstractControl — the base type for FormGroup, FormControl, etc.
 * ValidationErrors — a key/value object describing what went wrong
 */
function passwordMatchValidator(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  };
}

/**
 * This component handles the new account registration page.
 * It collects the user's personal info, address, and login credentials,
 * validates everything, and sends it to the backend to create the account.
 *
 * selector: 'app-new-account'
 *   The HTML tag for this component. Used if you ever embed it directly
 *   in another template, though typically it's accessed via routing.
 *
 * standalone: true
 *   This component manages its own imports rather than relying on a
 *   shared app.module.ts file.
 *
 * imports:
 *   RouterLink          — enables [routerLink] on the Cancel button
 *   ReactiveFormsModule — required for [formGroup] and formControlName in the HTML
 *   CommonModule        — provides @if, @for, and other template directives
 */
@Component({
  selector: 'app-new-account',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, CommonModule],
  templateUrl: './newAccount.html',
  styleUrl: './newAccount.css',
})
export class NewAccount implements OnInit {

  /**
   * FormGroup holds all the form fields together as one unit.
   * It tracks the value and validation state of every field,
   * and lets us read all values at once when the form is submitted.
   */
  form: FormGroup;

  /**
   * "signal" is Angular's modern way to store reactive state.
   * When a signal's value changes, Angular automatically updates
   * any part of the HTML template that reads that signal.
   *
   * authStatus — stores whether the user is already logged in.
   *              Fetched from the backend when the page loads.
   *
   * isLoading  — tracks whether a registration request is in progress.
   *              Used to disable the submit button and change its text
   *              so the user knows something is happening.
   *
   * errorMessage — stores any error message to display to the user
   *                (e.g. "Email already exists"). null means no error.
   *
   * successMessage — stores a success message if needed.
   *                  Currently we redirect immediately on success,
   *                  but this is here in case we want to show a message first.
   */
  authStatus = signal<AuthStatus | null>(null);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  /**
   * The constructor runs once when Angular creates this component.
   * Angular automatically injects the dependencies we list here:
   *
   *   FormBuilder  — a helper that makes creating FormGroups less verbose
   *   HttpClient   — used to make HTTP requests to our Spring Boot backend
   *   Router       — used to navigate to a different page after registration
   *   AuthService  — the shared service that tracks who is logged in
   *
   * "private" means these are only accessible inside this class.
   * Angular's dependency injection system provides the actual instances.
   */
  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private auth: AuthService
  ) {
    /**
     * fb.group() creates the FormGroup with all our fields.
     * Each field is defined as: fieldName: [initialValue, validators]
     *
     * Validators run automatically whenever the field value changes:
     *   Validators.required     — field cannot be empty
     *   Validators.email        — must be a valid email format
     *   Validators.minLength(8) — must be at least 8 characters
     *   Validators.pattern(...) — must match the regular expression
     *
     * address2 has no validators because it's optional.
     *
     * The second argument { validators: passwordMatchValidator() } attaches
     * our custom validator to the whole group so it can compare
     * the password and confirmPassword fields against each other.
     */
    this.form = this.fb.group({
      firstName:       ['', Validators.required],
      lastName:        ['', Validators.required],
      // The regex pattern allows digits, spaces, dashes, and parentheses
      // to support formats like (555) 555-5555 or +1-555-555-5555
      phone:           ['', [Validators.required, Validators.pattern(/^\+?[\d\s\-\(\)]{7,15}$/)]],
      address1:        ['', Validators.required],
      address2:        [''],  // optional — no validators
      city:            ['', Validators.required],
      state:           ['', Validators.required],
      zip:             ['', Validators.required],
      email:           ['', [Validators.required, Validators.email]],
      password:        ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    }, { validators: passwordMatchValidator() });
  }

  /**
   * ngOnInit is a "lifecycle hook" — Angular calls this automatically
   * after the component has been created and is ready to use.
   * This is the right place to fetch initial data from the backend.
   *
   * Here we check if someone is already logged in when the page loads.
   * .subscribe() is how we handle the response from an HTTP request —
   * the function inside runs when the backend responds with data.
   */
  ngOnInit() {
    this.http.get<AuthStatus>('/api/auth/status').subscribe(data => {
      console.log('in new Account init');
      this.authStatus.set(data);
    });
  }

  /**
   * Called when the user clicks the "Create Account" button.
   * The form's (ngSubmit) event in the HTML triggers this method.
   */
  onSubmit() {
    // If any field is invalid, mark everything as "touched" so the
    // validation error messages appear on all fields, then stop here.
    // Without markAllAsTouched(), errors only show on fields the user
    // has already interacted with.
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // Show the loading state and clear any previous error message
    // before starting the HTTP request
    this.isLoading.set(true);
    this.errorMessage.set(null);

    /**
     * Build the payload — the data we'll send to the backend as JSON.
     * We deliberately exclude confirmPassword because:
     *   1. The backend doesn't need it — password matching is a frontend concern
     *   2. Sending less data is always better for security and performance
     *
     * The field names here must match exactly what the backend's
     * RegisterRequest record expects, otherwise Spring won't map them correctly.
     */
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
      password:  this.form.value.password,
      // confirmPassword intentionally excluded — backend doesn't need it
    };

    /**
     * Send a POST request to the backend registration endpoint.
     * http.post<RegisterResponse> means we expect the response to match
     * the RegisterResponse type we defined at the top of this file.
     *
     * .subscribe() handles two outcomes:
     *   next  — called when the request succeeds (HTTP 2xx response)
     *   error — called when the request fails (HTTP 4xx or 5xx response)
     */
    this.http.post<RegisterResponse>('/api/auth/register', payload).subscribe({
      next: (res) => {
        this.isLoading.set(false);

        // Store the newly registered user in AuthService so the rest
        // of the app (like the menu page) knows who is logged in.
        // This is what makes "Welcome, Jane!" appear immediately after
        // registering without requiring a separate login step.
        this.auth.setUser(res.user);

        // Navigate to the menu page now that the account is created
        this.router.navigate(['/menu']);
      },
      error: (err) => {
        // Log the full error to the browser console for debugging.
        // This can be removed once the app is working reliably.
        console.log('error block hit', err);

        this.isLoading.set(false);

        // Try to extract a meaningful error message from the backend response.
        // The ?? operator means "use the next value if the previous one is null/undefined"
        //
        // Order of attempts:
        //   1. err?.error?.message  — our backend sends { "message": "..." } for errors
        //   2. err?.error as string — sometimes the error body is a plain string
        //   3. fallback message     — if we can't extract anything useful, show a generic message
        //
        // The ?. "optional chaining" operator safely accesses nested properties —
        // if err or err.error is null/undefined, it returns undefined instead of crashing
        this.errorMessage.set(
          err?.error?.message ??
          (typeof err?.error === 'string' ? err.error : null) ??
          'Registration failed. Please try again.'
        );
      }
    });
  }
}
