import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';

interface AuthStatus {
  loggedIn: boolean;
  message: string;
}

@Component({
  selector: 'app-login',
  imports: [RouterLink],
  templateUrl: './newAccount.html',
  styleUrl: './newAccount.css',
})
export class NewAccount implements OnInit {
  authStatus = signal<AuthStatus | null>(null);

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<AuthStatus>('/api/auth/status').subscribe(data => {
      console.log('in new Account init');
      this.authStatus.set(data);

    });
  }
}
