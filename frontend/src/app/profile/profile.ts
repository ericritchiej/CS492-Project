import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

interface UserProfileResponse {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  address: {
    address1?: string;
    address2?: string;
    city?: string;
    state?: string;
    zip?: string;
  };
}

interface UpdateRequest {
  firstName: string;
  lastName: string;
  phone: string;
  address1: string;
  address2: string;
  city: string;
  state: string;
  zip: string;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {

  profile = signal<UserProfileResponse | null>(null);

  form: UpdateRequest = {
    firstName: '',
    lastName: '',
    phone: '',
    address1: '',
    address2: '',
    city: '',
    state: '',
    zip: '',
  };

  saving = signal(false);
  errorMsg = signal<string | null>(null);

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.http.get<UserProfileResponse>('/api/user', { withCredentials: true })
      .subscribe({
        next: (data) => {
          this.profile.set(data);

          this.form.firstName = data.firstName ?? '';
          this.form.lastName = data.lastName ?? '';
          this.form.phone = data.phone ?? '';
          this.form.address1 = data.address?.address1 ?? '';
          this.form.address2 = data.address?.address2 ?? '';
          this.form.city = data.address?.city ?? '';
          this.form.state = data.address?.state ?? '';
          this.form.zip = data.address?.zip ?? '';
        },
        error: () => {
          this.errorMsg.set('Unable to load profile. Please log in.');
        }
      });
  }

  saveProfile() {
    this.saving.set(true);
    this.errorMsg.set(null);

    this.http.put('/api/user', this.form, { withCredentials: true })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/menu'], { queryParams: { saved: '1' } });
        },
        error: () => {
          this.saving.set(false);
          this.errorMsg.set('Failed to update profile.');
        }
      });
  }
}
