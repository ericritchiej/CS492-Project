import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

interface SummaryItem {
  name: string;
  quantity: number;
  lineTotal: number;
}

interface CheckoutSummary {
  items: SummaryItem[];
  subtotal: number;
  tax: number;
  total: number;
}

@Component({
  selector: 'app-checkout',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './checkout.html',
  styleUrl: './checkout.css',
})
export class Checkout implements OnInit {
  summary = signal<CheckoutSummary | null>(null);

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<CheckoutSummary>('/api/checkout/summary').subscribe(data => {
      this.summary.set(data);
    });
  }
}
