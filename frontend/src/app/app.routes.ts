import { Routes } from '@angular/router';
import { Menu } from './menu/menu';
import { Orders } from './orders/orders';
import { Admin } from './admin/admin';
import { Login } from './login/login';
import { RestaurantInfo } from './restaurant-info/restaurant-info';
import { Profile } from './profile/profile';
import { Cart } from './cart/cart';
import { Checkout } from './checkout/checkout';
import { Reporting } from './reporting/reporting';
import { NewAccount } from './newAccount/newAccount';

/**
 * This file defines the "routes" for the application — the rules that determine
 * which component (page) Angular should display based on the URL in the browser.
 *
 * For example:
 *   http://localhost:4200/menu     → displays the Menu component
 *   http://localhost:4200/login    → displays the Login component
 *   http://localhost:4200/cart     → displays the Cart component
 *
 * This is called "client-side routing" — the browser doesn't actually load a new
 * page from the server when you navigate. Instead, Angular intercepts the URL change
 * and swaps out the component displayed inside <RouterOutlet> in app.html.
 * This makes navigation feel instant compared to traditional multi-page websites.
 *
 * Each import at the top brings in a component class from its file so we can
 * reference it in the routes array below. The path after './' matches the
 * folder and file structure in your project — for example:
 *   './menu/menu' refers to src/app/menu/menu.ts
 */
export const routes: Routes = [

  /**
   * Each route is an object with at minimum two properties:
   *
   *   path      — the URL segment after the domain (e.g. 'menu' matches /menu)
   *   component — the Angular component to display when that URL is visited
   *
   * When Angular sees a URL change, it scans this array top to bottom and
   * displays the first component whose path matches.
   */
  // when a user comes directly to the root, redirect them to the menu page.
  { path: '', redirectTo: '/menu', pathMatch: 'full' },

  // The main menu page showing food items available to order
  { path: 'menu', component: Menu },

  // Order history or active orders for the logged-in customer
  { path: 'orders', component: Orders },

  // Admin dashboard — in a production app this route should be
  // protected so only admin users can access it (using a Route Guard)
  { path: 'admin', component: Admin },

  // The sign-in page where existing customers enter their credentials
  { path: 'login', component: Login },

  // Static information about the restaurant (hours, location, contact, etc.)
  { path: 'restaurant-info', component: RestaurantInfo },

  // The logged-in customer's profile page (personal info, saved addresses, etc.)
  { path: 'profile', component: Profile },

  // The shopping cart showing items the customer has added before checkout
  { path: 'cart', component: Cart },

  // The checkout flow where the customer confirms their order and pays
  { path: 'checkout', component: Checkout },

  // Sales and order reporting — likely admin-only in a production app
  { path: 'reporting', component: Reporting },

  // The new account registration form for first-time customers
  { path: 'new-account', component: NewAccount },
];
