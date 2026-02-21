import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef } from '@angular/core';
import { RouterLink } from '@angular/router';

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
}

export interface Topping {
  id: number;
  name: string;
  cost: number;
}

export type SectionKey = 'crust' | 'sizes' | 'categories' | 'products' | 'toppings';

@Component({
  selector: 'app-admin-management',
  standalone: true,
  imports: [FormsModule, RouterLink],
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

  // ── ADD FORM MODELS ──
  newCrust:    Partial<CrustType>       = { name: '', price: undefined };
  newSize:     Partial<PizzaSize>       = { name: '', price: undefined };
  newCategory: Partial<ProductCategory> = { name: '' };
  newProduct:  Partial<Product>         = { name: '', catId: undefined, price: undefined };
  newTopping:  Partial<Topping>         = { name: '', cost: undefined };

  // ── EDIT MODAL ──
  editModalOpen = false;
  editName  = '';
  editPrice = 0;
  editCatId = 0;
  saveEditCallback: (() => void) | null = null;
  editingSection: SectionKey | null = null;  // ← add here

  // ── TOAST ──
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';
  toastVisible = false;
  private toastTimer: any;

  ngOnInit(): void {
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

  // Show a message on the screen
  showToast(message: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage = message;
    this.toastType = type;
    this.toastVisible = true;
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => (this.toastVisible = false), 2500);
  }

  // ── EDIT MODAL ──
  closeModal(): void {
    this.editModalOpen = false;
    this.editingSection = null;
    this.saveEditCallback = null;
  }

  // This method is called from the "Save Changes Button" on the model and uses the variable saveEditCallback to determine which update function to call
  saveEdit(): void {
    this.saveEditCallback?.();
    this.closeModal();
  }

  // Set a property for the modal to show the correct fields
  get editingProducts(): boolean {
    return this.editingSection === 'products';
  }

  // Set a property for the modal to show the correct fields
  get editingToppings(): boolean {
    return this.editingSection === 'toppings';
  }

  // Set a property for the modal to show the correct fields
  get editingCategories(): boolean {
    return this.editingSection === 'categories';
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

  updateCrust(crust: CrustType): void {
    this.editName  = crust.name;
    this.editPrice = crust.price;
    this.editModalOpen = true;

    this.saveEditCallback = () => {
      const updated = { ...crust, name: this.editName.trim(), price: this.editPrice };
      this.http.put<CrustType>(`/api/crust/update/${updated.id}`, updated).subscribe({
        next: () => {
          this.showToast('Crust Updated!');
          this.loadCrusts();
        },
        error: err => {
          console.error('Failed to update crust:', err);
          this.showToast('Could not update crust.', 'error');
        }
      });
    };
  }
  deleteCrust(id: number): void {
    if (!confirm('Delete this item?')) return;

    this.http.delete(`/api/crust/delete/${id}`).subscribe({
      next: () => {
        this.showToast('Crust Deleted!');
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

  addPizzaSize(): void {
    const name = (this.newSize.name ?? '').trim();
    if (!name) { this.showToast('Enter a pizza size name', 'error'); return; }

    const body = {
      name,
      price: this.newSize.price ?? 0
    };

    this.http.post<PizzaSize>('/api/pizzaSize/add', body).subscribe({
      next: () => {
        this.newSize = { name: '', price: undefined };
        this.showToast('Pizza size added!');
        this.loadPizzaSize(); // refresh list from backend
      },
      error: err => {
        console.error('Failed to add pizza size:', err);
        this.showToast('Could not add pizza size.', 'error');
      }
    });
  }

  updatePizzaSize(size: PizzaSize): void {
    this.editName  = size.name;
    this.editPrice = size.price;
    this.editModalOpen = true;

    this.saveEditCallback = () => {
      const updated = { ...size, name: this.editName.trim(), price: this.editPrice };
      this.http.put<PizzaSize>(`/api/pizzaSize/update/${updated.id}`, updated).subscribe({
        next: () => {
          this.showToast('Pizza Size Updated!');
          this.loadPizzaSize(); // refresh list from backend
        },
        error: err => {
          console.error('Failed to update pizza size:', err);
          this.showToast('Could not update pizza size.', 'error');
        }
      });
    };
  }

  deletePizzaSize(id: number): void {
    if (!confirm('Delete this item?')) return;

    this.http.delete(`/api/pizzaSize/delete/${id}`).subscribe({
      next: () => {
        this.showToast('Pizza size Deleted!');
        this.loadPizzaSize(); // refresh list from backend
      },
      error: err => {
        console.error('Failed to delete pizza size:', err);
        this.showToast('Could not delete pizza size.', 'error');
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

  addProductCategory(): void {
    const name = (this.newCategory.name ?? '').trim();
    if (!name) { this.showToast('Enter a category name', 'error'); return; }

    const body = {
      name
    };

    this.http.post<ProductCategory>('/api/productCategory/add', body).subscribe({
      next: () => {
        this.newCategory = { name: '' };
        this.showToast('Category type added!');
        this.loadProductCategories(); // refresh list from backend
      },
      error: err => {
        console.error('Failed to add category:', err);
        this.showToast('Could not add category.', 'error');
      }
    });
  }

  updateProductCategory(productCategory: ProductCategory): void {
    this.editingSection = 'categories';
    this.editName  = productCategory.name;
    this.editModalOpen = true;

    this.saveEditCallback = () => {
      const updated = { ...productCategory, name: this.editName.trim() };
      this.http.put<ProductCategory>(`/api/productCategory/update/${updated.id}`, updated).subscribe({
        next: () => {
          this.showToast('Category Updated!');
          this.loadProductCategories(); // refresh list from backend
        },
        error: err => {
          console.error('Failed to update category:', err);
          this.showToast('Could not update category.', 'error');
        }
      });
    };
  }
  deleteProductCategory(id: number): void {
    if (!confirm('Delete this item?')) return;

    this.http.delete(`/api/productCategory/delete/${id}`).subscribe({
      next: () => {
        this.showToast('Category Deleted!');
        this.loadProductCategories(); // refresh list from backend
      },
      error: err => {
        console.error('Failed to delete category:', err);
        this.showToast('Could not delete category.', 'error');
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

  addProduct(): void {
    const name = (this.newProduct.name ?? '').trim();
    if (!name) { this.showToast('Enter a product name', 'error'); return; }

    const body = {
      name,
      catId: this.newProduct.catId,
      price: this.newProduct.price ?? 0
    };

    this.http.post<Product>('/api/product/add', body).subscribe({
      next: () => {
        this.newProduct = { id: 0, name: '', catId: undefined, price: undefined };
        this.showToast('Product added!');
        this.loadProducts(); // refresh list from backend
      },
      error: err => {
        console.error('Failed to add product:', err);
        this.showToast('Could not add product.', 'error');
      }
    });
  }

  updateProduct(product: Product): void {
    this.editingSection = 'products';
    this.editName  = product.name;
    this.editPrice = product.price;
    this.editCatId = product.catId;
    this.editModalOpen = true;

    this.saveEditCallback = () => {
      const updated = { ...product, name: this.editName.trim(), catId: this.editCatId, price: this.editPrice };
      this.http.put<Product>(`/api/product/update/${updated.id}`, updated).subscribe({
        next: () => {
          this.showToast('Product Updated!');
          this.loadProducts(); // refresh list from backend
        },
        error: err => {
          console.error('Failed to update product:', err);
          this.showToast('Could not update product.', 'error');
        }
      });
    };
  }
  deleteProduct(id: number): void {
    if (!confirm('Delete this item?')) return;

    this.http.delete(`/api/product/delete/${id}`).subscribe({
      next: () => {
        this.showToast('Product Deleted!');
        this.loadProducts(); // refresh list from backend
      },
      error: err => {
        console.error('Failed to delete product:', err);
        this.showToast('Could not delete product.', 'error');
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

  addTopping(): void {
    const name = (this.newTopping.name ?? '').trim();
    if (!name) { this.showToast('Enter a topping name', 'error'); return; }

    const body = {
      name,
      cost: this.newTopping.cost ?? 0
    };

    this.http.post<Topping>('/api/topping/add', body).subscribe({
      next: () => {
        this.newTopping = { name: '', cost: undefined };
        this.showToast('Topping added!');
        this.loadTopping(); // refresh list from backend
      },
      error: err => {
        console.error('Failed to add topping:', err);
        this.showToast('Could not add topping.', 'error');
      }
    });
  }

  updateTopping(topping: Topping): void {
    this.editingSection = 'toppings';
    this.editName  = topping.name;
    this.editPrice = topping.cost;
    this.editModalOpen = true;

    this.saveEditCallback = () => {
      const updated = { ...topping, name: this.editName.trim(), cost: this.editPrice };
      this.http.put<Topping>(`/api/topping/update/${updated.id}`, updated).subscribe({
        next: () => {
          this.showToast('Topping Updated!');
          this.loadTopping();
        },
        error: err => {
          console.error('Failed to update topping:', err);
          this.showToast('Could not update topping.', 'error');
        }
      });
    };
  }
  deleteTopping(id: number): void {
    if (!confirm('Delete this item?')) return;

    this.http.delete(`/api/topping/delete/${id}`).subscribe({
      next: () => {
        this.showToast('Topping Deleted!');
        this.loadTopping(); // refresh list from backend
      },
      error: err => {
        console.error('Failed to delete topping:', err);
        this.showToast('Could not delete topping.', 'error');
      }
    });
  }
}
