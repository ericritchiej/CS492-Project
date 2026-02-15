import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

/**
 * A "type" defines the shape of an object — it tells TypeScript exactly
 * what fields a CurrentUser must have and what types they are.
 * This acts as a contract: anywhere in the app that uses a CurrentUser
 * is guaranteed to have these exact fields available.
 *
 * We export it so other files (like login.ts and newAccount.ts) can
 * import and use this same type definition rather than defining their own.
 */
export type CurrentUser = {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
};

/**
 * A "Service" is a class that holds shared logic and data that multiple
 * components need to access. Think of it as a central hub.
 *
 * In this case, AuthService answers the question "who is currently logged in?"
 * for any component in the app that needs to know.
 *
 * @Injectable({ providedIn: 'root' }) does two things:
 *   1. Marks this class as something Angular can inject into other classes
 *   2. "providedIn: 'root'" means Angular creates ONE single instance of this
 *      service for the entire app — this is called a "singleton."
 *
 * Because it's a singleton, when login.ts calls setUser() to store the logged-in
 * user, that same data is instantly available to every other component that
 * injects AuthService — like the menu page showing "Welcome, Jane!"
 * Without a singleton, each component would have its own separate copy
 * and they couldn't share data with each other.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  /**
   * BehaviorSubject is a special type from the RxJS library that holds a value
   * and automatically notifies anyone who is "subscribed" whenever that value changes.
   *
   * Think of it like a whiteboard in a shared office:
   *   - When someone logs in, we write their name on the whiteboard (setUser)
   *   - Anyone in the office watching the whiteboard instantly sees the update
   *   - New people who walk in can also see the current name on the board
   *
   * The <CurrentUser | null> part means it can hold either a CurrentUser object
   * or null. null means nobody is logged in.
   *
   * We initialize it with null because nobody is logged in when the app first loads.
   *
   * "private" means only code inside THIS class can directly access userSubject.
   * Outside code must go through the public methods below, which keeps the data safe.
   */
  private userSubject = new BehaviorSubject<CurrentUser | null>(null);

  /**
   * user$ is the public "read-only" version of userSubject.
   * The $ suffix is a common convention in Angular to indicate this is an Observable.
   *
   * .asObservable() strips away the ability to call .next() (which changes the value),
   * leaving only the ability to subscribe and watch for changes.
   *
   * This means outside components can WATCH for changes to the logged-in user,
   * but they cannot directly CHANGE who is logged in — they must call setUser() instead.
   * This pattern is called "encapsulation" and helps prevent bugs where something
   * accidentally changes the user state in an unexpected way.
   *
   * Example usage in a component:
   *   this.auth.user$.subscribe(user => console.log(user?.firstName));
   */
  user$ = this.userSubject.asObservable();

  /**
   * Updates who is currently logged in across the entire application.
   * Because userSubject is a BehaviorSubject, calling .next() automatically
   * notifies every component that is subscribed to user$ — they all
   * instantly receive the new value without needing to refresh or re-fetch.
   *
   * Called with a CurrentUser object after successful login or registration.
   * Called with null when the user logs out (not yet implemented).
   *
   * @param user  the logged-in user's data, or null if logging out
   *
   * Example usages:
   *   this.auth.setUser(res.user);  // after login or registration
   *   this.auth.setUser(null);      // on logout
   */
  setUser(user: CurrentUser | null) {
    this.userSubject.next(user);
  }
}
