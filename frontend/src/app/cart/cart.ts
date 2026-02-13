import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

interface CartItem {
  name: string;
  quantity: number;
  price: number;
}

interface CartData {
  items: CartItem[];
  total: number;
}

@Component({
  selector: 'app-cart',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './cart.html',
  styleUrl: './cart.css',
})
export class Cart implements OnInit {
  cart = signal<CartData | null>(null);

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<CartData>('/api/cart').subscribe(data => {
      this.cart.set(data);
    });
  }
}
