import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgOptimizedImage } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef } from '@angular/core';

// ── INTERFACES ──
interface CrustType {
  id: number;
  name: string;
  price: number;
}

export interface PizzaSize {
  id: number;
  name: string;
  price: number;
}

export interface ProductCategory {
  id: number;
  name: string;
}

export interface Product {
  id: number;
  name: string;
  catId: number;
  price: number;
  img: string | null;
}

export interface Topping {
  id: number;
  name: string;
  cost: number;
}

export type SectionKey = 'crust' | 'sizes' | 'categories' | 'products' | 'toppings';

export interface EditContext {
  key: SectionKey;
  id: number;
}

@Component({
  selector: 'app-admin-management',
  standalone: true,
  imports: [FormsModule, NgOptimizedImage],
  templateUrl: './admin.html',
  styleUrls: ['./admin.css']
})
export class Admin implements OnInit {

  // ── SECTION TOGGLE STATE ──
  openSections: Record<SectionKey, boolean> = {
    crust: true,
    sizes: true,
    categories: true,
    products: true,
    toppings: true
  };

  // ── DATA ──
  crusts: CrustType[] = [];
  sizes: PizzaSize[] = [];
  categories: ProductCategory[] = [];
  products: Product[] = [];
  toppings: Topping[] = [];

  // ── ID COUNTERS ──
  private nextId: Record<SectionKey, number> = {
    crust: 10, sizes: 10, categories: 10, products: 10, toppings: 10
  };

  // ── ADD FORM MODELS ──
  newCrust:    Partial<CrustType>       = { name: '', price: undefined };
  newSize:     Partial<PizzaSize>       = { name: '', price: undefined };
  newCategory: Partial<ProductCategory> = { name: '' };
  newProduct:  Partial<Product>         = { name: '', catId: undefined, price: undefined, img: null };
  newTopping:  Partial<Topping>         = { name: '', cost: undefined };

  // ── EDIT MODAL ──
  editModalOpen = false;
  editContext: EditContext | null = null;
  editName  = '';
  editPrice = 0;
  editCatId = 0;

  // ── TOAST ──
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';
  toastVisible = false;
  private toastTimer: any;

  // ── PENDING IMAGE ──
  pendingProductImg: string | null = null;
  productImgLabel = '📷 Choose image';

  ngOnInit(): void {
    // In a real app, you would load data from your Spring Boot API here:
    this.loadCrusts();
    this.loadPizzaSize();
    this.loadProductCategories();
    this.loadProducts();
    this.loadTopping();
  }

  // ── SECTION TOGGLE ──
  toggleSection(key: SectionKey): void {
    this.openSections[key] = !this.openSections[key];
  }

  // ── UTILITIES ──
  formatPrice(n: number): string {
    return '$' + Number(n).toFixed(2);
  }

  // look up the category name using the category id
  getCategoryName(catId: number): string {
    return this.categories.find(c => c.id === catId)?.name ?? '—';
  }

  showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage = message;
    this.toastType = type;
    this.toastVisible = true;
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => (this.toastVisible = false), 2500);
  }

  // ── ADD HANDLERS ──
  addCrust(): void {
    const name = (this.newCrust.name ?? '').trim();
    if (!name) { this.showToast('Enter a crust name', 'error'); return; }

    const body = {
      name,
      price: this.newCrust.price ?? 0
    };

    this.http.post<CrustType>('/api/crust/add', body).subscribe({
      next: () => {
        this.newCrust = { name: '', price: undefined };
        this.showToast('Crust type added!');
        this.loadCrusts(); // refresh list from backend
      },
      error: err => {
        console.error('Failed to add crust:', err);
        this.showToast('Could not add crust.', 'error');
      }
    });
  }

  addSize(): void {
    const name = (this.newSize.name ?? '').trim();
    if (!name) { this.showToast('Enter a size name', 'error'); return; }
    this.sizes = [...this.sizes, {
      id: this.nextId.sizes++,
      name,
      price: this.newSize.price ?? 0
    }];
    this.newSize = { name: '', price: undefined };
    this.showToast('Size added!');
  }

  addCategory(): void {
    const name = (this.newCategory.name ?? '').trim();
    if (!name) { this.showToast('Enter a category name', 'error'); return; }
    this.categories = [...this.categories, {
      id: this.nextId.categories++,
      name
    }];
    this.newCategory = { name: '' };
    this.showToast('Category added!');
  }

  addProduct(): void {
    const name = (this.newProduct.name ?? '').trim();
    if (!name) { this.showToast('Enter a product name', 'error'); return; }
    if (!this.newProduct.catId) { this.showToast('Select a category', 'error'); return; }
    this.products = [...this.products, {
      id: this.nextId.products++,
      name,
      catId: this.newProduct.catId,
      price: this.newProduct.price ?? 0,
      img: this.pendingProductImg
    }];
    this.newProduct = { name: '', catId: undefined, price: undefined, img: null };
    this.pendingProductImg = null;
    this.productImgLabel = '📷 Choose image';
    this.showToast('Product added!');
  }

  addTopping(): void {
    const name = (this.newTopping.name ?? '').trim();
    if (!name) { this.showToast('Enter a topping name', 'error'); return; }
    this.toppings = [...this.toppings, {
      id: this.nextId.toppings++,
      name,
      cost: this.newTopping.cost ?? 0
    }];
    this.newTopping = { name: '', cost: undefined };
    this.showToast('Topping added!');
  }

  // ── IMAGE UPLOAD ──
  onProductImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      this.productImgLabel = '✅ ' + file.name.slice(0, 20);
      const reader = new FileReader();
      reader.onload = (e) => {
        this.pendingProductImg = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  // ── DELETE ──
  deleteItem(key: SectionKey, id: number): void {
    if (!confirm('Delete this item?')) return;

    this.http.delete(`/api/${key}/delete/${id}`).subscribe({
      next: () => {
        (this as any)[key] = (this as any)[key].filter((x: any) => x.id !== id);
        this.cdr.detectChanges();
        this.showToast('Item deleted.');
      },
      error: err => {
        console.error('Failed to delete:', err);
        this.showToast('Could not delete item.', 'error');
      }
    });
  }

  // ── EDIT MODAL ──
  openEdit(key: SectionKey, id: number): void {
    const list: any[] = (this as any)[key];
    const item = list.find(x => x.id === id);
    if (!item) return;
    this.editContext = { key, id };
    this.editName  = item.name ?? '';
    this.editPrice = item.price ?? item.cost ?? 0;
    this.editCatId = item.catId ?? 0;
    this.editModalOpen = true;
  }

  closeModal(): void {
    this.editModalOpen = false;
    this.editContext = null;
  }

  saveEdit(): void {
    if (!this.editContext) return;
    const { key, id } = this.editContext;
    const list: any[] = (this as any)[key];
    const index = list.findIndex(x => x.id === id);
    if (index === -1) return;

    const updated = { ...list[index], name: this.editName.trim() };
    if (key === 'toppings') updated.cost  = this.editPrice;
    else                    updated.price = this.editPrice;
    if (key === 'products') updated.catId = this.editCatId;

    // Immutable update so Angular's change detection picks it up
    const newList = [...list];
    newList[index] = updated;
    (this as any)[key] = newList;

    this.closeModal();
    this.showToast('Changes saved!');
  }

  // ── MODAL LABEL HELPER ──
  get editModalTitle(): string {
    const labels: Record<SectionKey, string> = {
      crust: 'Crust', sizes: 'Size', categories: 'Category',
      products: 'Product', toppings: 'Topping'
    };
    return this.editContext ? 'Edit ' + labels[this.editContext.key] : 'Edit';
  }

  get editingProducts(): boolean {
    return this.editContext?.key === 'products';
  }

  get editingToppings(): boolean {
    return this.editContext?.key === 'toppings';
  }

  get editingCategories(): boolean {
    return this.editContext?.key === 'categories';
  }

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef,) { }

  loadCrusts(): void {
    this.http.get<CrustType[]>('/api/crust/getCrusts').subscribe({
      next: data => {
        this.crusts = data;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Failed to fetch crusts:', err);
        this.showToast('Could not load crusts.', 'error');
      }
    });
  }

  // saveCrust(crust: CrustType): void {
  //   this.http.post<CrustType>('/api/crusts', crust).subscribe(() => this.loadCrusts());
  // }
  // updateCrust(crust: CrustType): void {
  //   this.http.put<CrustType>(`/api/crusts/${crust.id}`, crust).subscribe(() => this.loadCrusts());
  // }
  deleteCrust(id: number): void {
    if (!confirm('Delete this item?')) return;

    this.http.delete(`/api/crust/delete/${id}`).subscribe({
      next: () => {
        this.showToast('Crust type Deleted!');
        this.loadCrusts(); // refresh list from backend
      },
      error: err => {
        console.error('Failed to delete crust:', err);
        this.showToast('Could not delete crust.', 'error');
      }
    });
  }

  loadPizzaSize(): void {
    this.http.get<PizzaSize[]>('/api/pizzaSize/getPizzaSizes').subscribe({
      next: data => {
        this.sizes = data;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Failed to fetch pizza sizes:', err);
        this.showToast('Could not load pizza sizes.', 'error');
      }
    });
  }
  loadProductCategories(): void {
    this.http.get<ProductCategory[]>('/api/productCategory/getProductCategories').subscribe({
      next: data => {
        this.categories = data;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Failed to fetch product categories:', err);
        this.showToast('Could not load product categories.', 'error');
      }
    });
  }
  loadProducts(): void {
    this.http.get<Product[]>('/api/product/getProducts').subscribe({
      next: data => {
        this.products = data;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Failed to fetch products:', err);
        this.showToast('Could not load products.', 'error');
      }
    });
  }
  loadTopping(): void {
    this.http.get<Topping[]>('/api/topping/getToppings').subscribe({
      next: data => {
        this.toppings = data;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Failed to fetch toppings:', err);
        this.showToast('Could not load toppings.', 'error');
      }
    });
  }
}
