import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type CurrentUser = {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private userSubject = new BehaviorSubject<CurrentUser | null>(null);
  user$ = this.userSubject.asObservable();

  setUser(user: CurrentUser | null) {
    this.userSubject.next(user);
  }
}
