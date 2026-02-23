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
        error: (err: any) => {
          console.error('Failed to fetch user:', err);
          this.saving.set(false);
          if (err.status === 401) setTimeout(() => this.router.navigate(['/login']), 2000);
          else this.errorMsg.set('Unable to load profile. Please try again.');
        }
      });
  }

  private validateForm(): string | null {
    if (!this.form.firstName.trim()) return 'First name is required.';
    if (!this.form.lastName.trim()) return 'Last name is required.';

    const phoneDigits = this.form.phone.replace(/\D/g, '');
    if (!this.form.phone.trim()) return 'Phone number is required.';
    if (phoneDigits.length !== 10) return 'Phone number must be 10 digits.';

    if (!this.form.address1.trim()) return 'Address Line 1 is required.';
    if (!this.form.city.trim()) return 'City is required.';
    if (!this.form.state.trim()) return 'State is required.';

    const zipPattern = /^\d{5}$/;
    if (!this.form.zip.trim()) return 'Zip code is required.';
    if (!zipPattern.test(this.form.zip.trim())) return 'Zip code must be exactly 5 digits.';

    return null;
  }

  saveProfile() {
    this.errorMsg.set(null);

    const validationError = this.validateForm();
    if (validationError) {
      this.errorMsg.set(validationError);
      return;
    }

    this.saving.set(true);

    this.http.put<{ message: string }>('/api/user', this.form, { withCredentials: true })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.router.navigate(['/menu'], { queryParams: { saved: '1' } });
        },
        error: (err) => {
          console.error('Failed to update profile:', err);
          this.saving.set(false);
          this.errorMsg.set('Failed to update profile.');
        }
      });
  }
}
