import { Component, OnInit, signal, computed, ViewEncapsulation  } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { CartSummaryDto, CartItemDto } from '../cart/cart';
import { CartService } from '../cart.service';
import { FormsModule } from '@angular/forms';

// ── Interfaces ──────────────────────────────────────────

export interface UserProfileDto {
  id:        number;
  firstName: string;
  lastName:  string;
  email:     string;
  phone:     string | null;
  address:   AddressDto | null;
}

export interface AddressDto {
  addressId: number;
  address1: string | null;
  address2: string | null;
  city:     string | null;
  state:    string | null;
  zip:      string | null;
}

export interface PaymentDto {
  cardNumber: string;
  expirationDate: string;
  cvv: string;
}

export interface PizzaSize  { sizeId: number; sizeName: string; }
export interface CrustType  { crustId: number; crustName: string; }
export interface Toppings   { toppingId: number; toppingName: string; extraCost: number; }

// ── Component ────────────────────────────────────────────

@Component({
  selector:    'app-checkout',
  standalone:  true,
  imports: [CommonModule, RouterLink, FormsModule],
  encapsulation: ViewEncapsulation.None,
  styleUrls:   ['./checkout.css'],
  templateUrl: './checkout.html',
})
export class Checkout implements OnInit {

  // ── State Signals ─────────────────────────────────────

  cart        = signal<CartSummaryDto | null>(null);
  profile     = signal<UserProfileDto | null>(null);
  loading     = signal(true);
  loadError   = signal('');
  placing     = signal(false);
  deliveryMethod = signal<'DELIVERY' | 'PICKUP'>('DELIVERY');

  // Credit card info
  cardNumber     = '';
  expirationDate = '';
  cvv            = '';

  // Lookup data
  sizes       = signal<PizzaSize[]>([]);
  crustTypes  = signal<CrustType[]>([]);
  toppings    = signal<Toppings[]>([]);

  // Confirmation modal — snapshot of the cart at time of order
  orderConfirmed    = signal(false);
  confirmedOrderId  = signal<number | null>(null);
  confirmedCart     = signal<CartSummaryDto | null>(null);


  // Toast
  toastMessage = signal('');
  toastType    = signal<'success' | 'error'>('success');
  toastVisible = signal(false);
  private toastTimer: any;

  // ── Computed ──────────────────────────────────────────

  items   = computed<CartItemDto[]>(() => this.cart()?.items ?? []);
  isEmpty = computed(() => this.items().length === 0);

  // Items shown inside the confirmation modal (snapshot)
  confirmedItems = computed<CartItemDto[]>(() => this.confirmedCart()?.items ?? []);

  fullName = computed(() => {
    const p = this.profile();
    if (!p) return '';
    return `${p.firstName} ${p.lastName}`.trim();
  });

  fullAddress = computed(() => {
    const userProfile = this.profile()?.address;
    if (!userProfile) return null;

    if (userProfile.address2) {
      const line1 = userProfile.address1;
      const line2 = userProfile.address2;
      const line3 = [userProfile.city, userProfile.state, userProfile.zip].filter(Boolean).join(', ');
      return [line1, line2, line3].filter(Boolean).join('\n');
    } else {
      const line1 = userProfile.address1;
      const line2 = [userProfile.city, userProfile.state, userProfile.zip].filter(Boolean).join(', ');
      return [line1, line2].filter(Boolean).join('\n');
    }

  });

  // ── Constructor ───────────────────────────────────────

  constructor(private http: HttpClient, private cartService: CartService) {}

  // ── Lifecycle ─────────────────────────────────────────

  ngOnInit(): void {
    this.loadAll();
  }

  private loadAll(): void {
    this.loading.set(true);
    this.loadError.set('');

    let pending = 2;
    const done = () => { if (--pending === 0) this.loading.set(false); };

    this.http.get<CartSummaryDto>('/api/cart').subscribe({
      next: summary => { this.cart.set(summary); done(); },
      error: () => {
        this.loadError.set('Unable to load cart.');
        this.loading.set(false);
      },
    });

    this.http.get<UserProfileDto>('/api/user').subscribe({
      next: profile => {
        this.profile.set(profile);
        done(); },
      error: () => {
        // Non-fatal — show empty profile state
        done();
      },
    });

    this.http.get<PizzaSize[]>('/api/pizzaSize/getPizzaSizes').subscribe({
      next: sizes => this.sizes.set(sizes),
      error: () => this.showToast('Unable to load pizza sizes.', 'error'),
    });
    this.http.get<CrustType[]>('/api/crust/getCrusts').subscribe({
      next: crusts => this.crustTypes.set(crusts),
      error: () => this.showToast('Unable to load crust types.', 'error'),
    });
    this.http.get<Toppings[]>('/api/topping/getToppings').subscribe({
      next: toppings => this.toppings.set(toppings),
      error: () => this.showToast('Unable to load toppings.', 'error'),
    });
  }

  // ── Place Order ───────────────────────────────────────

  placeOrder(): void {
    if (this.isEmpty() || this.placing()) return;
    this.placing.set(true);

    // Snapshot the cart now so the modal has data even after cart clears
    const cartSnapshot = this.cart();
    const addressDto = this.profile()?.address;

    const body = {
      deliveryMethod:  this.deliveryMethod(),
      deliveryAddress: this.fullAddress(),
      addressId:       addressDto?.addressId ?? null,
      cardNumber:      this.cardNumber,
      expirationDate:  this.expirationDate,
      cvv:             this.cvv,
    };

    if (!this.isValidExpDate(this.expirationDate)) {
      this.showToast('Please enter a valid expiration date (MM/YY).', 'error');
      this.placing.set(false);
      return;
    }

    const stripped = this.cardNumber.replace(/\s/g, '');
    if (stripped.length !== 16) {
      this.showToast('Please enter a complete 16-digit card number.', 'error');
      this.placing.set(false);
      return;
    }

    if (!/^\d{13,19}$/.test(this.cardNumber.replace(/\s/g, '')) || this.cardNumber.length < 16) {
      this.showToast('Please enter a valid card number.', 'error');
      this.placing.set(false);
      return;
    }

    if (!/^\d{3,4}$/.test(this.cvv)) {
      this.showToast('Please enter a valid CVV.', 'error');
      this.placing.set(false);
      return;
    }

    this.http.post<{ orderId: number }>('/api/payment/process', body).subscribe({
      next: res => {
        this.http.post<{ orderId: number }>('/api/checkout/process', body).subscribe({
          next: res => {
            this.placing.set(false);
            // Store snapshot for the modal
            this.confirmedOrderId.set(res.orderId);
            this.confirmedCart.set(cartSnapshot);
            // Show modal
            this.orderConfirmed.set(true);
            // Clear the live cart so it reflects "empty" behind the modal
            this.cart.set(null);
            this.cartService.setCount(0);
            this.triggerPizzaRain();
          },
          error: err => {
            this.placing.set(false);
            this.showToast(err?.error?.message ?? 'Failed to place order. Please try again.', 'error');
          },
        });
      },
      error: err => {
        this.placing.set(false);
        this.showToast(err?.error?.message ?? 'Failed to verifying payment information. Please try again.', 'error');
      },
    });

  }

  // ── Modal ─────────────────────────────────────────────

  closeModal(): void {
    this.orderConfirmed.set(false);
  }

  /** Close modal when clicking the dark backdrop (not the card itself) */
  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('backdrop')) {
      this.closeModal();
    }
  }

  // ── Toast ─────────────────────────────────────────────

  showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage.set(message);
    this.toastType.set(type);
    this.toastVisible.set(true);
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toastVisible.set(false), 3000);
  }

  // ── Template helpers ──────────────────────────────────

  sizeLabel(sizeId: number | null): string {
    if (sizeId == null) return '';
    return this.sizes().find(s => s.sizeId === sizeId)?.sizeName ?? String(sizeId);
  }

  crustLabel(crustId: number | null): string {
    if (crustId == null) return '';
    return this.crustTypes().find(c => c.crustId === crustId)?.crustName ?? String(crustId);
  }

  sauceLabel(sauce: string): string {
    const map: Record<string, string> = {
      LIGHT: 'Light Sauce', REGULAR: 'Regular Sauce', HEAVY: 'Heavy Sauce',
    };
    return map[sauce?.toUpperCase()] ?? sauce;
  }

  toppingLabel(toppingId: number): string {
    return this.toppings().find(t => t.toppingId === toppingId)?.toppingName ?? String(toppingId);
  }

  formatToppingIds(ids: number[]): string {
    return ids.length ? ids.map(id => this.toppingLabel(id)).join(', ') : 'None';
  }

  isValidExpDate(value: string): boolean {
    const match = value.match(/^(0[1-9]|1[0-2])\/(\d{2})$/);
    if (!match) return false;

    const month = parseInt(match[1], 10);
    const year = parseInt('20' + match[2], 10);
    const now = new Date();
    const expDate = new Date(year, month); // first day of month after expiry

    return expDate > now;
  }

  formatCardNumber(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/\D/g, '').substring(0, 16);
    this.cardNumber = digits.replace(/(.{4})/g, '$1 ').trim();
  }

  formatExpDate(event: Event): void {
    const input = event.target as HTMLInputElement;
    const digits = input.value.replace(/\D/g, '').substring(0, 4);
    this.expirationDate = digits.length > 2 ? digits.substring(0, 2) + '/' + digits.substring(2) : digits;
  }

  triggerPizzaRain(): void {
    const style = document.createElement('style');
    style.textContent = `
      @keyframes pizzaFall {
        0%   { transform: translateY(0) rotate(0deg); opacity: 1; }
        100% { transform: translateY(110vh) rotate(360deg); opacity: 0; }
      }
    `;
    document.head.appendChild(style);

    for (let i = 0; i < 30; i++) {
      setTimeout(() => {
        const pizza = document.createElement('div');
        pizza.style.position = 'fixed';
        pizza.style.top = '-50px';
        pizza.style.left = Math.random() * 100 + 'vw';
        pizza.style.fontSize = (Math.random() * 1.5 + 1) + 'rem';
        pizza.style.zIndex = '9999';
        pizza.style.pointerEvents = 'none';
        pizza.style.animation = `pizzaFall ${Math.random() * 2 + 1.5}s linear forwards`;
        pizza.textContent = '🍕';
        document.body.appendChild(pizza);
        setTimeout(() => pizza.remove(), 4000);
      }, Math.random() * 1500);
    }
  }
}
