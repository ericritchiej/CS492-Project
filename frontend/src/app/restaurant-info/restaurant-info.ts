import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {PromotionListComponent} from '../promotions/promotion-list.component';

interface RestaurantInfoData {
  id: number;
  name: string;
  streetAddr1: string;
  streetAddr2: string;
  city: string;
  state: string;
  zipCode: string;
  phoneNumber: string;
  description: string;
}

interface RestaurantHours {
  id: number;
  restaurantId: number;
  displayText: string;
  sortOrder: number;
}

@Component({
  selector: 'app-restaurant-info',
  imports: [
    PromotionListComponent
  ],
  templateUrl: './restaurant-info.html',
  styleUrl: './restaurant-info.css',
})
export class RestaurantInfo implements OnInit {
  info = signal<RestaurantInfoData | null>(null);
  hours = signal<RestaurantHours[]>([]);
  error = signal<string | null>(null);

  constructor(private http: HttpClient) {
  }

  ngOnInit() {
    this.http.get<RestaurantInfoData>('/api/restaurant-info').subscribe({
      next: data => {
        this.info.set(data);
      },
      error: err => {
        this.error.set('Could not load restaurant information.');
        console.error('Failed to fetch restaurant info:', err);
      }
    });

    this.http.get<RestaurantHours[]>('/api/restaurant-hours').subscribe({
      next: data => {
        this.hours.set(data);
      },
      error: err => {
        this.error.set('Could not load restaurant hours.');
        console.error('Failed to fetch restaurant hours:', err);
      }
    });
  }

  formatPhone(phoneNumber: string | undefined): string {
    if (!phoneNumber) return '';
    const digits = phoneNumber.replace(/\D/g, '');
    if (digits.length === 10) {
      return `(${digits.slice(0, 3)}) ${digits.slice(3, 6)}-${digits.slice(6)}`;
    }
    return phoneNumber;
  }

}
