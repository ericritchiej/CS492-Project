import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CartService {
  private countSubject = new BehaviorSubject<number>(0);
  cartCount$ = this.countSubject.asObservable();

  constructor(private http: HttpClient) {}

  refresh(): void {
    this.http.get<{ items: { quantity: number }[] }>('/api/cart').subscribe({
      next: summary => {
        const total = summary.items.reduce((sum, i) => sum + i.quantity, 0);
        this.countSubject.next(total);
      },
      error: () => this.countSubject.next(0),
    });
  }

  setCount(n: number): void {
    this.countSubject.next(n);
  }
}