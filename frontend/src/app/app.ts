import {Component, OnInit, signal} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink } from '@angular/router';
import { AuthService } from './auth.service';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';


/**
 * @Component is a decorator that tells Angular "this class is a component."
 * A component is a building block of an Angular app — it combines a TypeScript
 * class (logic), an HTML template (structure), and a CSS file (styling).
 *
 * This is the ROOT component — the very first component Angular loads when
 * the app starts. Every other component (Login, Menu, NewAccount, etc.) is
 * displayed INSIDE this one. Think of it as the outer shell of the entire app.
 *
 * selector: 'app-root'
 *   This is the HTML tag that represents this component.
 *   In index.html you'll find <app-root></app-root> — that's where Angular
 *   mounts the entire application.
 *
 * standalone: true
 *   Modern Angular apps use "standalone" components, which means each component
 *   declares its own dependencies in its imports array rather than relying on
 *   a shared app.module.ts file. This makes components more self-contained
 *   and easier to understand.
 *
 * imports: [...]
 *   Since this is the root component, it imports the modules that need to be
 *   available app-wide:
 *
 *   CommonModule        — provides common Angular directives like @if and @for
 *                         for showing/hiding elements and looping through lists
 *   RouterOutlet        — this is the placeholder where Angular swaps in different
 *                         page components based on the current URL. Without this,
 *                         navigation between pages wouldn't work.
 *   RouterLink          — allows HTML elements to act as navigation links using
 *                         [routerLink]="/menu" instead of regular href links.
 *                         RouterLink works with Angular's router so the page
 *                         doesn't fully reload when navigating.
 *   ReactiveFormsModule — enables Angular's reactive forms system used in the
 *                         login and registration forms throughout the app
 *
 * templateUrl / styleUrl
 *   Rather than writing HTML and CSS directly in this file, we point to
 *   separate files. This keeps the code organized as files grow larger.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  title = signal<string>('');
  error = signal<string | null>(null);

  /**
   * The constructor runs once when this component is first created.
   *
   * "public auth: AuthService" does two things at once:
   *   1. Asks Angular to inject the AuthService singleton (the one shared
   *      instance that tracks who is logged in across the whole app)
   *   2. Makes it publicly accessible as "this.auth" in both this class
   *      AND directly in app.html
   *
   * We inject it here at the root level because app.html is always visible
   * (it contains the navigation bar) and needs to know if a user is logged
   * in to show/hide things like a username display or logout button.
   *
   * "public" vs "private":
   *   If we used "private auth", the template (app.html) could not access it.
   *   "public" makes it available to the template, which is why we can write
   *   things like {{ auth.user$ | async }} directly in app.html.
   */
  constructor(public auth: AuthService, private http: HttpClient) {}


  ngOnInit() {
    this.http.get<{ name: string }>('/api/restaurant-info').subscribe({
      next: data => {
        this.title.set(data.name);
      },
      error: err => {
        this.title.set('Pizza Store');  // fallback name
        this.error.set('Could not load restaurant info.');
        console.error('Failed to fetch restaurant info:', err);
      }
    });
  }
}
