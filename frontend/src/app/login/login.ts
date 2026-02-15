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
 * Describes the shape of the response from /api/auth/signin.
 * When Spring Boot returns a successful login response, TypeScript
 * will expect it to match this structure exactly.
 * These field names must match what AuthController sends back.
 */
type SignInResponse = {
  message: string;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
  };
};

/**
 * This component handles the login page where existing customers
 * sign into their account.
 *
 * Note: standalone: true is missing from this component's @Component decorator.
 * Since newAccount.ts uses standalone: true, this should too for consistency.
 * Without it, Angular may look for this component to be declared in a module,
 * which could cause unexpected behavior in a standalone app.
 */
@Component({
  selector: 'app-login',
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {

  /**
   * Stores the auth status fetched from the backend on page load.
   * signal<AuthStatus | null>(null) means it starts as null (unknown)
   * and gets updated once the backend responds.
   * Currently fetched but could be used to redirect already-logged-in
   * users away from the login page automatically.
   */
  authStatus = signal<AuthStatus | null>(null);

  /**
   * loginForm holds the username and password fields.
   * The "!" is a TypeScript "non-null assertion" — it tells TypeScript
   * "trust me, this will be assigned before it's used" even though it's
   * not assigned on this line. It gets assigned in the constructor below.
   */
  loginForm!: FormGroup;

  /**
   * Stores any error message to display when login fails.
   * Typed as "any" for flexibility, but could be typed as "string | null"
   * to be more precise — this would match how newAccount.ts handles errors
   * using signals, which is the more modern Angular approach.
   */
  signinError: any;

  /**
   * The constructor runs once when Angular creates this component.
   * Angular automatically injects all listed dependencies:
   *
   *   FormBuilder      — helper for creating FormGroups cleanly
   *   HttpClient       — makes HTTP requests to the backend
   *   Router           — navigates to other pages programmatically
   *   ChangeDetectorRef — manually triggers Angular's change detection
   *                       (explained below where it's used)
   *   AuthService      — the shared singleton that tracks who is logged in
   */
  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private auth: AuthService
  ) {
    /**
     * Create the login form with two fields.
     * Each field is defined as: fieldName: [initialValue, validators]
     *
     * username — requires a value and must be at least 3 characters.
     *            Note: we use "username" here but it actually holds
     *            the user's email address — this matches what the
     *            backend's @RequestParam("username") expects.
     *
     * password — only requires a value, no length check here since
     *            we're verifying against an existing stored password.
     */
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required]],
    });
  }

  /**
   * ngOnInit is called by Angular after the component is ready.
   * We use it to check the current auth status from the backend.
   * A future improvement would be to redirect already-logged-in
   * users directly to /menu instead of showing them the login page.
   */
  ngOnInit() {
    this.http.get<AuthStatus>('/api/auth/status').subscribe(data => {
      this.authStatus.set(data);
    });
  }

  /**
   * Called when the user clicks the Sign In button.
   * Validates the form, builds the request, and sends it to the backend.
   */
  onSignin() {
    // Clear any previous error message from a failed login attempt
    this.signinError = '';

    /**
     * Extract the username and password from the form using destructuring.
     * getRawValue() returns all field values regardless of whether they
     * are disabled — safer than this.loginForm.value for most cases.
     */
    const { username, password } = this.loginForm.getRawValue();

    /**
     * Build the request body as URL-encoded form data rather than JSON.
     * This is because our Spring Boot backend's signin endpoint uses
     * @RequestParam which expects data in this format:
     *   username=jane%40example.com&password=mypassword
     *
     * This is DIFFERENT from the register endpoint which uses @RequestBody
     * and expects JSON. The two formats are not interchangeable.
     *
     * HttpParams builds the encoded string safely, handling special
     * characters like @ in email addresses automatically.
     * .toString() converts it to the final string format for sending.
     */
    const body = new HttpParams()
      .set('username', username)
      .set('password', password)
      .toString();

    // Validate the form before sending anything to the backend.
    // markAllAsTouched() forces all validation error messages to appear
    // so the user can see exactly what they need to fix.
    // Note: ideally this check should happen BEFORE extracting values
    // and building the body — currently we build the request body even
    // if we're about to cancel the submission.
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    /**
     * Send the login request to the backend.
     *
     * The Content-Type header tells the backend to expect URL-encoded
     * form data (not JSON). Without this header, Spring Boot would not
     * be able to read the @RequestParam values correctly.
     *
     * .subscribe() handles two outcomes:
     *   next  — called on successful login (HTTP 200)
     *   error — called on failure (HTTP 401 for wrong credentials, etc.)
     */
    this.http.post<SignInResponse>('/api/auth/signin', body, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    }).subscribe({
      next: (res) => {
        // Store the logged-in user in AuthService so every other component
        // in the app instantly knows who is logged in.
        // This is what makes "Welcome, Jane!" appear on the menu page.
        this.auth.setUser(res.user);

        // Navigate to the menu page after successful login
        this.router.navigate(['/menu']);
      },
      error: (err) => {
        // Try to extract a meaningful error message from the backend response.
        // The ?? operator means "use the next value if the previous is null/undefined":
        //   1. err?.error?.message  — our backend sends { "message": "..." } for errors
        //   2. err?.error as string — in case the error body is a plain string
        //   3. fallback message     — generic message if nothing else works
        this.signinError =
          err?.error?.message ??
          (typeof err?.error === 'string' ? err.error : null) ??
          'Sign-in failed';

        /**
         * cdr.detectChanges() manually tells Angular to update the template
         * immediately after setting signinError.
         *
         * Normally Angular detects changes automatically, but since signinError
         * is a plain variable (not a signal or Observable), Angular sometimes
         * misses the update. detectChanges() forces it to re-render right away
         * so the error message appears instantly.
         *
         * This wouldn't be needed if signinError were converted to a signal
         * like errorMessage in newAccount.ts — signals trigger automatic
         * re-renders without needing detectChanges().
         */
        this.cdr.detectChanges();
      }
    });
  }
}
