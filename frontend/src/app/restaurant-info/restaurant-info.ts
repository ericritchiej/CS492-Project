import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {PromotionListComponent} from '../promotions/promotion-list.component';

interface RestaurantInfoData {
  name: string;
  address: string;
  phone: string;
  hours: string[];
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

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<RestaurantInfoData>('/api/restaurant-info').subscribe(data => {
      this.info.set(data);
    });
  }
}
