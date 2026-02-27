import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

// ── Interfaces ──────────────────────────────────────────

export interface PizzaSize  { sizeId: number; sizeName: string; }
export interface CrustType  { crustId: number; crustName: string; }
export interface Toppings  { toppingId: number; toppingName: string; extraCost: number }

export interface CartItemDto {
  cartItemId:      number;
  productId:       number | null;  // null for custom pizza
  name:            string;
  sizeId:          number | null;
  crustTypeId:     number | null;
  sauceName:       string | null;
  toppingIdsFull:  number[] | null;
  toppingIdsLeft:  number[] | null;
  toppingIdsRight: number[] | null;
  quantity:        number;
  price:           number;
  lineTotal:       number;
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

  // Size / crust lookup / Topping
  sizes      = signal<PizzaSize[]>([]);
  crustTypes = signal<CrustType[]>([]);
  toppings = signal<Toppings[]>([]);

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

  constructor(private router: Router,private http: HttpClient) {}

  // ── Lifecycle ─────────────────────────────────────────

  ngOnInit(): void {
    this.loadCartSummary();
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

  // ── Data loading ──────────────────────────────────────

  loadCartSummary(): void {
    this.loading.set(true);
    this.loadError.set('');

    this.http.get<CartSummaryDto>('/api/cart').subscribe({
      next: summary => {
        this.cart.set(summary);
        this.loading.set(false);
        },
      error: () => {
        this.loadError.set('Unable to load your cart. Please try again.');
        this.loading.set(false); },
    });
  }

  // ── Quantity controls ─────────────────────────────────

  incrementItem(item: CartItemDto): void {
    if (item.quantity >= 99) return;
    this.updateQuantity(item, item.quantity + 1);
  }

  decrementItem(item: CartItemDto): void {
    this.updateQuantity(item, item.quantity - 1);
  }

  private updateQuantity(item: CartItemDto, newQty: number): void {
    const inFlight = new Set(this.updatingItem());
    inFlight.add(item.cartItemId);
    this.updatingItem.set(inFlight);
    this.http.put(`/api/cart/update`, { cartItemId: item.cartItemId, quantity: newQty }).subscribe({
      next: () => {
        const done = new Set(this.updatingItem());
        done.delete(item.cartItemId);
        this.updatingItem.set(done);
        this.loadCartSummary();
        },
      error: (err) => {
        const done = new Set(this.updatingItem());
        done.delete(item.cartItemId);
        this.updatingItem.set(done);
        this.showToast(err?.error?.message ?? 'Failed to update quantity.', 'error');
        },
    });
  }

  isItemUpdating(cartItemId: number): boolean {
    return this.updatingItem().has(cartItemId);
  }

  // ── Promo code ────────────────────────────────────────
  applyPromo(): void {
    const code = this.promoInput().trim();
    if (!code) return;

    this.promoError.set('');
    this.promoLoading.set(true);

    this.http.post<CartSummaryDto>(`/api/cart/promo?code=${encodeURIComponent(code)}`, {}).subscribe({
      next: summary => {
        this.cart.set(summary);
        this.promoLoading.set(false);
        this.showToast(`Promo code "${code}" applied!`);
      },
      error: err => {
        this.promoLoading.set(false);
        this.promoError.set(err?.error?.message ?? 'Failed to apply promo code.');
      },
    });
  }

  removePromo(): void {
    this.promoInput.set('');
    this.promoError.set('');

    this.http.delete<CartSummaryDto>('/api/cart/promo').subscribe({
      next: summary => {
        this.cart.set(summary)
        this.showToast(`Promo code removed!`);
      },
      error: err => {
        this.promoError.set(err?.error?.message ?? 'Failed to remove promo code.');
      },
    });
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

  toppingLabel(toppingId: number | null): string {
    if (toppingId == null) return '';
    return this.toppings().find(c => c.toppingId === toppingId)?.toppingName ?? String(toppingId);
  }

  formatToppingIds(ids: number[]): string {
    return ids.length ? ids.map(id => this.toppingLabel(id)).join(', ') : 'None';
  }

}
