import { Component, OnInit, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

type PromotionRow = Record<string, any>;
type ApiResult = { success: boolean; message: string };

@Component({
  selector: 'app-promotions',
  imports: [],
  templateUrl: './promotions.html',
  styleUrl: './promotions.css',
})
export class Promotions implements OnInit {
  promotions = signal<PromotionRow[]>([]);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  message = signal<{ type: 'success' | 'error'; text: string } | null>(null);
  savingId = signal<number | null>(null);
  deletingId = signal<number | null>(null);

  columns = computed(() => {
    const list = this.promotions();
    const cols: string[] = [];
    for (const row of list) {
      for (const key of Object.keys(row)) {
        if (key === '__isNew' || key === '__rowKey') continue;
        if (!cols.includes(key)) cols.push(key);
      }
    }
    return cols;
  });

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadPromotions();
  }

  trackPromoRow(promo: PromotionRow, index: number): string | number {
    const id = promo?.['promotion_id'];
    if (id !== null && id !== undefined && id !== '') return `id-${id}`;
    if (promo?.['__rowKey']) return promo['__rowKey'];
    return `idx-${index}`;
  }

  loadPromotions(): void {
    this.loading.set(true);
    this.error.set(null);

    this.http.get<PromotionRow[]>('/api/promotions').subscribe({
      next: (data) => {
        const rows = (data ?? []).map((r) => ({ ...r, __rowKey: `id-${r['promotion_id'] ?? Math.random()}` }));
        this.promotions.set(rows);
        this.loading.set(false);
      },
      error: (err) => {
        console.error(err);
        this.error.set('Failed to load promotions.');
        this.loading.set(false);
      }
    });
  }

  addNewPromotionRow(): void {
    this.message.set(null);
    const rows = [...this.promotions()];
    rows.unshift({
      promotion_id: '',
      code: '',
      discount_value: '',
      __isNew: true,
      __rowKey: `new-${Date.now()}-${Math.random()}`
    });
    this.promotions.set(rows);
  }

  cancelNew(rowIndex: number): void {
    const row = this.promotions()[rowIndex];
    if (!row || !row['__isNew']) return;
    const rows = [...this.promotions()];
    rows.splice(rowIndex, 1);
    this.promotions.set(rows);
    this.message.set(null);
  }

  updateField(rowIndex: number, col: string, value: any): void {
    const list = [...this.promotions()];
    const row = { ...list[rowIndex] };
    row[col] = value;
    list[rowIndex] = row;
    this.promotions.set(list);
  }

  save(rowIndex: number): void {
    this.message.set(null);

    const row = this.promotions()[rowIndex];
    if (!row) return;

    const isNew = !!row['__isNew'];
    const code = (row['code'] ?? '').toString().trim();
    const discountRaw = (row['discount_value'] ?? '').toString().trim();
    const promotionDesc = (row['promotion_desc'] ?? '').toString().trim();
    const promotionSummary = (row['promotion_summary'] ?? '').toString().trim();
    const expDt = (row['exp_dt'] ?? '').toString().trim();
    const minOrderAmtRaw = (row['min_order_amt'] ?? '').toString().trim();

    if (!code || !discountRaw || !promotionDesc || !promotionSummary || !expDt || !minOrderAmtRaw) {
      this.message.set({ type: 'error', text: 'All fields are required.' });
      return;
    }

    const discountNum = Number(discountRaw);
    if (!Number.isFinite(discountNum)) {
      this.message.set({ type: 'error', text: 'Discount must be numeric.' });
      return;
    }

    const minOrderAmtNum = Number(minOrderAmtRaw);
    if (!Number.isFinite(minOrderAmtNum)) {
      this.message.set({ type: 'error', text: 'Minimum order amount must be numeric.' });
      return;
    }

    const payload = {
      code,
      discount_value: discountNum,
      promotion_desc: promotionDesc,
      promotion_summary: promotionSummary,
      exp_dt: expDt,
      min_order_amt: minOrderAmtNum
    };

    if (isNew) {
      this.http.post<ApiResult>('/api/promotions', payload).subscribe({
        next: (res) => {
          if (res?.success) {
            this.message.set({ type: 'success', text: res.message || 'Promotion added successfully.' });
            this.loadPromotions();
          } else {
            this.message.set({ type: 'error', text: res?.message || 'Failed to add promotion.' });
          }
        },
        error: (err) => {
          console.error(err);
          this.message.set({ type: 'error', text: 'Failed to add promotion.' });
        }
      });
      return;
    }

    const promotionId = Number(row['promotion_id']);
    if (!Number.isFinite(promotionId)) {
      this.message.set({ type: 'error', text: 'Invalid promotion id.' });
      return;
    }

    this.savingId.set(promotionId);

    this.http.put<ApiResult>(`/api/promotions/${promotionId}`, payload).subscribe({
      next: (res) => {
        if (res?.success) {
          this.message.set({ type: 'success', text: res.message || 'Saved successfully.' });
          this.loadPromotions();
        } else {
          this.message.set({ type: 'error', text: res?.message || 'Save failed.' });
        }
        this.savingId.set(null);
      },
      error: (err) => {
        console.error(err);
        this.message.set({ type: 'error', text: 'Save failed (server error).' });
        this.savingId.set(null);
      }
    });
  }

  delete(rowIndex: number): void {
    this.message.set(null);

    const row = this.promotions()[rowIndex];
    if (!row) return;

    if (row['__isNew']) {
      this.cancelNew(rowIndex);
      return;
    }

    const promotionId = Number(row['promotion_id']);
    if (!Number.isFinite(promotionId)) {
      this.message.set({ type: 'error', text: 'Invalid promotion id.' });
      return;
    }

    this.deletingId.set(promotionId);

    this.http.delete<ApiResult>(`/api/promotions/${promotionId}`).subscribe({
      next: (res) => {
        if (res?.success) {
          this.message.set({ type: 'success', text: res.message || 'Deleted successfully.' });
          this.loadPromotions();
        } else {
          this.message.set({ type: 'error', text: res?.message || 'Delete failed.' });
        }
        this.deletingId.set(null);
      },
      error: (err) => {
        console.error(err);
        this.message.set({ type: 'error', text: 'Delete failed (server error).' });
        this.deletingId.set(null);
      }
    });
  }

  isEditable(col: string): boolean {
    return col !== 'promotion_id';
  }
}
