import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

interface CartItem {
  cartItemId: number;
  name: string;
  quantity: number;
  lineTotal: number;
}

interface CartSummary {
  items: CartItem[];
  subtotal: number;
  discount: number;
  tax: number;
  total: number;
}

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css'],
})
export class Payment implements OnInit {
  cart = signal<CartSummary | null>(null);
  loading = signal(true);
  submitting = signal(false);
  confirmed = signal(false);
  confirmationNumber = signal('');
  errorMessage = signal('');

  cardNumber = '';
  expirationDate = '';
  cvv = '';
  deliveryMethod = 'DELIVERY';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.http.get<CartSummary>('/api/cart').subscribe({
      next: cart => {
        this.cart.set(cart);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Unable to load cart.');
        this.loading.set(false);
      },
    });
  }

  submitPayment(): void {
    this.errorMessage.set('');
    this.submitting.set(true);

    const payload = {
      cardNumber: this.cardNumber,
      expirationDate: this.expirationDate,
      cvv: this.cvv,
      deliveryMethod: this.deliveryMethod,
    };

    this.http.post<any>('/api/payment/process', payload).subscribe({
      next: res => {
        this.submitting.set(false);
        this.confirmationNumber.set(res.confirmationNumber);
        this.confirmed.set(true);
      },
      error: err => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Payment failed. Please try again.');
      },
    });
  }
}
