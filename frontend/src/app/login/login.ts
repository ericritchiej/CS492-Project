import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface AuthStatus {
  loggedIn: boolean;
  message: string;
}

@Component({
  selector: 'app-login',
  imports: [],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  authStatus = signal<AuthStatus | null>(null);

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<AuthStatus>('/api/auth/status').subscribe(data => {
      this.authStatus.set(data);
    });
  }
}
