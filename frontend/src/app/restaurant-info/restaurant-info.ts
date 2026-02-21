import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface RestaurantInfoData {
  name: string;
  address: string;
  phone: string;
  hours: string[];
}

@Component({
  selector: 'app-restaurant-info',
  imports: [],
  templateUrl: './restaurant-info.html',
  styleUrl: './restaurant-info.css',
})
export class RestaurantInfo implements OnInit {
  info = signal<RestaurantInfoData | null>(null);

  // Hard-coded values requested for location and hours of operation
  private readonly hardCodedAddress = '123 Main St, Colorado Springs, CO 80903';
  private readonly hardCodedHours = [
    'Monday - Thursday: 11:00 AM - 9:00 PM',
    'Friday: 11:00 AM - 10:00 PM',
    'Saturday: 12:00 PM - 10:00 PM',
    'Sunday: 12:00 PM - 8:00 PM'
  ];

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<RestaurantInfoData>('/api/restaurant-info').subscribe({
      next: (data) => {
        this.info.set({
          ...data,
          // Override API values with hard-coded values per requirement
          address: this.hardCodedAddress,
          hours: this.hardCodedHours,
        });
      },
      error: () => {
        // Fallback so page still works even if API is unavailable
        this.info.set({
          name: 'Pizza Store',
          phone: '(555) 123-4567',
          address: this.hardCodedAddress,
          hours: this.hardCodedHours,
        });
      }
    });
  }
}
