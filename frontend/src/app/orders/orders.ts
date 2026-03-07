import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';

// ── Interfaces ──────────────────────────────────────────

export interface OrderItemDto {
  cartItemId:      number;
  name:            string;
  quantity:        number;
  lineTotal:       number;
  sizeId?:         number;
  crustTypeId?:    number;
  sauceName?:      string;
  toppingIdsFull?:  number[];
  toppingIdsLeft?:  number[];
  toppingIdsRight?: number[];
}

export interface OrderDto {
  orderId:        number;
  placedAt:       string;           // ISO date string
  status:         OrderStatus;
  deliveryMethod: 'DELIVERY' | 'PICKUP';
  items:          OrderItemDto[];
  subtotal:       number;
  discount:       number;
  promoCode:      string | null;
  tax:            number;
  total:          number;
}

export type OrderStatus = 'PLACED' | 'PREPARING' | 'READY' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'CANCELLED';

// ── Component ────────────────────────────────────────────

@Component({
  selector:    'orders',
  standalone:  true,
  imports:     [CommonModule, FormsModule, RouterLink],
  styleUrls:   ['./orders.css'],
  templateUrl: './orders.html',
})
export class Orders implements OnInit {

  // ── State Signals ─────────────────────────────────────

  allOrders   = signal<OrderDto[]>([]);
  loading     = signal(true);
  loadError   = signal('');

  // ── Filters ───────────────────────────────────────────

  filterDeliveryMethod = signal<'ALL' | 'DELIVERY' | 'PICKUP'>('ALL');
  searchQuery          = signal('');

  // ── Expanded order detail ─────────────────────────────

  expandedOrderId = signal<number | null>(null);

  // ── Toast ─────────────────────────────────────────────

  toastMessage = signal('');
  toastType    = signal<'success' | 'error'>('success');
  toastVisible = signal(false);
  private toastTimer: any;

  // ── Computed ──────────────────────────────────────────

  filteredOrders = computed<OrderDto[]>(() => {
    const method   = this.filterDeliveryMethod();
    const query    = this.searchQuery().trim().toLowerCase();

    return this.allOrders().filter(order => {
      if (status !== 'ALL' && order.status !== status) return false;
      if (method !== 'ALL' && order.deliveryMethod !== method) return false;
      if (query) {
        const haystack = [
          `#${order.orderId}`,
          order.status,
          order.deliveryMethod,
          ...order.items.map(i => i.name ?? 'custom'),
          order.promoCode ?? '',
        ].join(' ').toLowerCase();
        if (!haystack.includes(query)) return false;
      }
      return true;
    });
  });

  isEmpty = computed(() => this.filteredOrders().length === 0);

  // ── Constructor ───────────────────────────────────────

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);
    this.http.get<OrderDto[]>('/api/orders/history').subscribe({
      next: orders => {
        this.allOrders.set(orders);
        this.loading.set(false);
        },
      error: () => {
        this.showToast("Unable to load order history.", 'error');
        this.loadError.set('Unable to load order history.');
        this.loading.set(false);
        },
    });
  }

  // ── Interactions ──────────────────────────────────────

  toggleExpand(orderId: number): void {
    this.expandedOrderId.set(
      this.expandedOrderId() === orderId ? null : orderId
    );
  }

  isExpanded(orderId: number): boolean {
    return this.expandedOrderId() === orderId;
  }

  clearFilters(): void {
    this.filterDeliveryMethod.set('ALL');
    this.searchQuery.set('');
  }

  // ── Display Helpers ───────────────────────────────────

  readonly STATUS_LABELS: Record<OrderStatus, string> = {
    PLACED:           'Order Placed',
    PREPARING:        'Preparing',
    READY:            'Ready for Pickup',
    OUT_FOR_DELIVERY: 'Out for Delivery',
    DELIVERED:        'Delivered',
    CANCELLED:        'Cancelled',
  };

  readonly STATUS_ICONS: Record<OrderStatus, string> = {
    PLACED:           '📋',
    PREPARING:        '👨‍🍳',
    READY:            '✅',
    OUT_FOR_DELIVERY: '🚗',
    DELIVERED:        '🏠',
    CANCELLED:        '✕',
  };

  statusLabel(status: OrderStatus): string { return this.STATUS_LABELS[status] ?? status; }
  statusIcon(status: OrderStatus): string  { return this.STATUS_ICONS[status]  ?? '📋'; }

  sauceLabel(sauce: string): string {
    const map: Record<string, string> = { LIGHT: 'Light Sauce', REGULAR: 'Regular Sauce', HEAVY: 'Heavy Sauce' };
    return map[sauce?.toUpperCase()] ?? sauce;
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  formatTime(iso: string): string {
    return new Date(iso).toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
  }

  itemSummary(order: OrderDto): string {
    return order.items
      .map(i => `${i.name ?? 'Custom'} ×${i.quantity}`)
      .join(', ');
  }

  isActiveOrder(status: OrderStatus): boolean {
    return ['PLACED', 'PREPARING', 'READY', 'OUT_FOR_DELIVERY'].includes(status);
  }

  // ── Toast ─────────────────────────────────────────────

  showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage.set(message);
    this.toastType.set(type);
    this.toastVisible.set(true);
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toastVisible.set(false), 2500);
  }
}
