import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
// TODO: Restore HttpClient import when switching to real API calls
// import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

// ── Interfaces ──────────────────────────────────────────

export interface CartItemDto {
  cartItemId:  number;
  productName: string;
  size:        string;
  crust:       string;
  sauce:       string;
  leftToppings:  string [];
  rightToppings: string [];
  quantity:    number;
  unitPrice:   number;
  lineTotal:   number;
}


export interface CartSummaryDto {
  items:        CartItemDto[];
  subtotal:     number;
  discount:     number;
  promoCode:    string | null;
  tax:          number;
  total:        number;
}

// ── Component ────────────────────────────────────────────

@Component({
  selector:    'app-cart',
  standalone:  true,
  imports:     [CommonModule, FormsModule, RouterLink],
  styleUrls:   ['./cart.css'],
  templateUrl: './cart.html',
})
export class Cart implements OnInit {

  // ── State Signals ─────────────────────────────────────

  cart        = signal<CartSummaryDto | null>(null);
  loading     = signal(true);
  loadError   = signal('');

  // Promo code form
  promoInput  = signal('');
  promoError  = signal('');
  promoLoading = signal(false);

  // Per-item quantity update in-flight tracking
  updatingItem = signal<Set<number>>(new Set());

  // Toast
  toastMessage = signal('');
  toastType    = signal<'success' | 'error'>('success');
  toastVisible = signal(false);
  private toastTimer: any;

  // ── Computed ──────────────────────────────────────────

  items = computed<CartItemDto[]>(() => this.cart()?.items ?? []);
  isEmpty = computed(() => this.items().length === 0);

  // ── Constructor ───────────────────────────────────────

  constructor(private router: Router) {}

  // ── Lifecycle ─────────────────────────────────────────

  ngOnInit(): void {
    this.loadCart();
  }

  // ── Data loading ──────────────────────────────────────

  // TODO: Replace this mock with the real API call below once the backend is ready.
  //
  // loadCart(): void {
  //   this.loading.set(true);
  //   this.loadError.set('');
  //   this.http.get<CartSummaryDto>('/api/cart').subscribe({
  //     next: summary => { this.cart.set(summary); this.loading.set(false); },
  //     error: () => { this.loadError.set('Unable to load your cart. Please try again.'); this.loading.set(false); },
  //   });
  // }

  private readonly MOCK_CART: CartSummaryDto = {
    items: [
      {
        cartItemId:  1,
        productName: 'Pepperoni Feast',
        size:        'LARGE',
        crust:       'Hand Tossed',
        sauce:       'REGULAR',
        leftToppings:    [],
        rightToppings:    [],
        quantity:    2,
        unitPrice:   14.98,
        lineTotal:   29.96,
      },
      {
        cartItemId:  2,
        productName: 'BBQ Chicken Deluxe',
        size:        'MEDIUM',
        crust:       'Thin Crust',
        sauce:       'HEAVY',
        leftToppings:    [],
        rightToppings:    [],
        quantity:    1,
        unitPrice:   13.49,
        lineTotal:   13.49,
      },
      {
        cartItemId:  3,
        productName: 'Custom Pizza',
        size:        'LARGE',
        crust:       'Stuffed Crust',
        sauce:       'LIGHT',
        leftToppings:    ['Pepperoni, Sausage'],
        rightToppings:   ['Spinach, Feta, Roasted Garlic'],
        quantity:    1,
        unitPrice:   17.99,
        lineTotal:   17.99,
      },
      {
        cartItemId:  4,
        productName: 'Custom Pizza',
        size:        'LARGE',
        crust:       'Stuffed Crust',
        sauce:       'LIGHT',
        leftToppings:    ['Sausage, Mushroom'],
        rightToppings:   [],
        quantity:    1,
        unitPrice:   15.99,
        lineTotal:   15.99,
      },
      {
        cartItemId:  5,
        productName: 'Garlic Breadsticks',
        size:        'MEDIUM',
        crust:       'Original',
        sauce:       'REGULAR',
        leftToppings:    [],
        rightToppings:    [],
        quantity:    2,
        unitPrice:   4.99,
        lineTotal:   9.98,
      },
    ],
    subtotal:  71.42,
    discount:  0,
    promoCode: null,
    tax:       5.71,
    total:     77.13,
  };

  loadCart(): void {
    this.loading.set(true);
    this.loadError.set('');
    // Simulate a brief network delay so loading state is visible
    setTimeout(() => {
      this.cart.set(structuredClone(this.MOCK_CART));
      this.loading.set(false);
    }, 400);
  }

  // ── Quantity controls ─────────────────────────────────

  incrementItem(item: CartItemDto): void {
    if (item.quantity >= 99) return;
    this.updateQuantity(item, item.quantity + 1);
  }

  decrementItem(item: CartItemDto): void {
    this.updateQuantity(item, item.quantity - 1);
  }

  // TODO: Replace this mock with the real API call below once the backend is ready.
  //
  // private updateQuantity(item: CartItemDto, newQty: number): void {
  //   const inFlight = new Set(this.updatingItem());
  //   inFlight.add(item.cartItemId);
  //   this.updatingItem.set(inFlight);
  //   this.http.put(`/api/cart/${item.cartItemId}/quantity`, { quantity: newQty }).subscribe({
  //     next: () => { const done = new Set(this.updatingItem()); done.delete(item.cartItemId); this.updatingItem.set(done); this.loadCart(); },
  //     error: (err) => { const done = new Set(this.updatingItem()); done.delete(item.cartItemId); this.updatingItem.set(done); this.showToast(err?.error?.message ?? 'Failed to update quantity.', 'error'); },
  //   });
  // }

  private updateQuantity(item: CartItemDto, newQty: number): void {
    const current = this.cart();
    if (!current) return;

    let updatedItems: CartItemDto[];
    if (newQty <= 0) {
      updatedItems = current.items.filter(i => i.cartItemId !== item.cartItemId);
    } else {
      updatedItems = current.items.map(i =>
        i.cartItemId === item.cartItemId
          ? { ...i, quantity: newQty, lineTotal: parseFloat((i.unitPrice * newQty).toFixed(2)) }
          : i
      );
    }

    const subtotal  = parseFloat(updatedItems.reduce((s, i) => s + i.lineTotal, 0).toFixed(2));
    const discount  = current.discount;
    const taxable   = Math.max(0, subtotal - discount);
    const tax       = parseFloat((taxable * 0.08).toFixed(2));
    const total     = parseFloat((taxable + tax).toFixed(2));

    this.cart.set({ ...current, items: updatedItems, subtotal, tax, total });
  }

  isItemUpdating(cartItemId: number): boolean {
    return this.updatingItem().has(cartItemId);
  }

  // ── Promo code ────────────────────────────────────────

  // TODO: Replace this mock with the real API call below once the backend is ready.
  //
  // applyPromo(): void {
  //   const code = this.promoInput().trim();
  //   if (!code) return;
  //   this.promoError.set('');
  //   this.promoLoading.set(true);
  //   this.http.post<CartSummaryDto>(`/api/cart/promo?code=${encodeURIComponent(code)}`, {}).subscribe({
  //     next: summary => { this.cart.set(summary); this.promoLoading.set(false); this.showToast(`Promo code "${code}" applied!`); },
  //     error: (err) => { this.promoLoading.set(false); this.promoError.set(err?.status === 404 ? 'Promo code not found.' : (err?.error?.message ?? 'Failed to apply promo code.')); },
  //   });
  // }

  private readonly MOCK_PROMOS: Record<string, number> = {
    'COWABUNGA10': 0.10,   // 10% off
    'PIZZA20':     0.20,   // 20% off
    'SAVE5':       5.00,   // $5 flat
  };

  applyPromo(): void {
    const code = this.promoInput().trim().toUpperCase();
    if (!code) return;

    this.promoError.set('');
    this.promoLoading.set(true);

    setTimeout(() => {
      const rate = this.MOCK_PROMOS[code];
      if (rate === undefined) {
        this.promoLoading.set(false);
        this.promoError.set('Promo code not found.');
        return;
      }

      const current = this.cart();
      if (!current) { this.promoLoading.set(false); return; }

      const subtotal = current.subtotal;
      const discount = parseFloat((rate < 1 ? subtotal * rate : rate).toFixed(2));
      const taxable  = Math.max(0, subtotal - discount);
      const tax      = parseFloat((taxable * 0.08).toFixed(2));
      const total    = parseFloat((taxable + tax).toFixed(2));

      this.cart.set({ ...current, discount, promoCode: code, tax, total });
      this.promoLoading.set(false);
      this.showToast(`Promo code "${code}" applied!`);
    }, 400);
  }

  removePromo(): void {
    const current = this.cart();
    if (!current) return;

    this.promoInput.set('');
    this.promoError.set('');

    const taxable = current.subtotal;
    const tax     = parseFloat((taxable * 0.08).toFixed(2));
    const total   = parseFloat((taxable + tax).toFixed(2));
    this.cart.set({ ...current, discount: 0, promoCode: null, tax, total });
  }

  // ── Checkout ──────────────────────────────────────────

  goToCheckout(): void {
    if (this.isEmpty()) return;
    this.router.navigate(['/checkout']);
  }

  // ── Toast ─────────────────────────────────────────────

  showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage.set(message);
    this.toastType.set(type);
    this.toastVisible.set(true);
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toastVisible.set(false), 2500);
  }

  // ── Template helpers ──────────────────────────────────

  formatToppings(toppings: string[]): string {
    return toppings.length ? toppings.join(', ') : 'None';
  }

  sauceLabel(sauce: string): string {
    const map: Record<string, string> = {
      LIGHT: 'Light Sauce', REGULAR: 'Regular Sauce', HEAVY: 'Heavy Sauce',
    };
    return map[sauce?.toUpperCase()] ?? sauce;
  }
}
