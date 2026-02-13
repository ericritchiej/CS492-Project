import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface ProfileData {
  name: string;
  email: string;
  address: string;
  phone: string;
}

@Component({
  selector: 'app-profile',
  imports: [],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  profile = signal<ProfileData | null>(null);

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<ProfileData>('/api/profile').subscribe(data => {
      this.profile.set(data);
    });
  }
}
