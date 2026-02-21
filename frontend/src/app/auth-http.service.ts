import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CurrentUser } from './auth.service';

// Created this so we don't have to update this in every place if it changes.
export interface AuthStatus {
  loggedIn: boolean;
  message: string;
}

export interface IdentifyResponse {
  loginType: 'WORKER' | 'CUSTOMER';
}

export type AuthUserResponse = {
  message: string;
  user: CurrentUser;
};

export type RegisterPayload = {
  firstName: string;
  lastName:  string;
  phone:     string;
  address1:  string;
  address2:  string;
  city:      string;
  state:     string;
  zip:       string;
  email:     string;
  password:  string;
};

@Injectable({ providedIn: 'root' })
export class AuthHttpService {

  constructor(private http: HttpClient) {}

  getStatus(): Observable<AuthStatus> {
    return this.http.get<AuthStatus>('/api/auth/status');
  }

  identify(email: string): Observable<IdentifyResponse> {
    return this.http.post<IdentifyResponse>('/api/auth/identify', { email });
  }

  signInCustomer(username: string, password: string): Observable<AuthUserResponse> {
    return this.http.post<AuthUserResponse>('/api/auth/signIn/customer', { username, password });
  }

  signInEmployee(username: string, password: string): Observable<AuthUserResponse> {
    return this.http.post<AuthUserResponse>('/api/auth/signIn/employee', { username, password });
  }

  register(payload: RegisterPayload): Observable<AuthUserResponse> {
    return this.http.post<AuthUserResponse>('/api/auth/register/new/customer', payload);
  }
}
