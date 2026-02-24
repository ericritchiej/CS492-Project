import {Component, OnInit, signal} from '@angular/core';
import { CommonModule }  from '@angular/common';
import { FormsModule, ReactiveFormsModule,
  FormBuilder, FormGroup, FormArray,
  Validators }  from '@angular/forms';
import { HttpClient }   from '@angular/common/http';

// ── Interfaces ──────────────────────────────────────────
export interface Product {
  productId: number;
  productName: string;
  categoryId: number;
  basePrice: number;
  customizable: boolean;
}

export interface Category {
  categoryId: number;
  categoryName: string;
}

export interface PizzaSize {
  sizeId: number;
  sizeName: string;
  price: number;
}

export interface CrustType {
  crustId: number;
  crustName: string;
  price: number;
}

export interface Topping {
  toppingId: number;
  toppingName: string;
  extraCost: number;
}

export interface CartPayload {
  menuproductId:      number | null;  // null for custom pizza
  size:            string;
  sauce:           string;
  toppingIds:      number[];
  leftToppingIds:  number[] | null;
  rightToppingIds: number[] | null;
  quantity:        number;
  crustTypeId:     number;
}

// ── Component ────────────────────────────────────────────

@Component({
  selector:  'app-menu',
  standalone: true,
  imports:   [CommonModule, FormsModule, ReactiveFormsModule],
  styleUrls:   ['./menu.css'],
  templateUrl: './menu.html',
})
export class Menu implements OnInit {

  // ── Signals ──────────────────────────────────────────

  /** All menu products returned by the API */
  products    = signal<Product[]>([]);
  /** Available sizes (loaded from API – fallback to defaults) */
  sizes        = signal<PizzaSize[]>([]);
  /** Available crust types */
  crustTypes   = signal<CrustType[]>([]);
  /** Available toppings */
  toppings     = signal<Topping[]>([]);
  /** Whether data is still loading */
  loading      = signal(true);
  /** Top-level error (menu load failure) */
  loadError    = signal('');

  // ── Derived helpers (plain getters – fully resolved by IntelliJ) ──

  /** Unique categories built from menu products - populated after API load */
  categories: Category[] = [];

  /** products keyed by categoryId - populated after API load */
  productsByCategory: Map<number, Product[]> = new Map();

  /** Call this after loading menu products to populate categories and productsByCategory */
  private buildDerivedData(products: Product[]): void {
    const productMap = new Map<number, Product[]>();
    for (const product of products) {
      const arr = productMap.get(product.categoryId) ?? [];
      arr.push(product);
      productMap.set(product.categoryId, arr);
    }
    this.productsByCategory = productMap;
  }

  // ── Modal state ───────────────────────────────────────

  /** Currently open product (null = modal closed) */
  selectedProduct   = signal<Product | null>(null);
  /** True when in "custom pizza" mode (no menu product) */
  isCustom       = signal(false);
  modalError     = signal('');
  addingToCart   = signal(false);

  // ── Per-modal form values ─────────────────────────────

  selectedSizeId  = signal<number>(0);
  selectedCrustId = signal<number>(0);
  sauceLevel      = signal<number>(2);   // 1=Light 2=Regular 3=Heavy
  quantity        = signal<number>(1);

  /** Toppings selected for whole pizza (whole-pizza mode) */
  selectedToppingIds = signal<Set<number>>(new Set());
  /** Half-pizza mode toggle */
  halfPizzaMode      = signal(false);
  leftToppingIds     = signal<Set<number>>(new Set());
  rightToppingIds    = signal<Set<number>>(new Set());

  // ── Computed price ────────────────────────────────────

  /** Computed total price shown in the modal footer */
  get modalPrice(): string {
    const product  = this.selectedProduct();
    const base  = product?.basePrice ?? 11.99;
    const size  = this.sizes().find(s => s.sizeId === this.selectedSizeId());
    const crust = this.crustTypes().find(c => c.crustId === this.selectedCrustId());
    const sizeAdd  = size?.price ?? 0;
    const crustAdd = crust?.price     ?? 0;
    return ((base + sizeAdd + crustAdd) * this.quantity()).toFixed(2);
  }

  sauceLabelMap: Record<number, string> = { 1: 'Light', 2: 'Regular', 3: 'Heavy' };

  // ── Constructor ───────────────────────────────────────

  constructor(private http: HttpClient) {}

  // ── Lifecycle ─────────────────────────────────────────

  ngOnInit(): void {
    this.loadMenuData();
  }

  private loadMenuData(): void {
    this.loading.set(true);
    this.loadError.set('');

    // 1. Load categories first, then products so category names are available
    this.http.get<Category[]>('/api/productCategory/getProductCategories').subscribe({
      next: cats => {
        this.categories = cats;
        // Now load products — category names will resolve correctly
        this.http.get<Product[]>('/api/product/getProducts').subscribe({
          next:  products => { this.products.set(products); this.buildDerivedData(products); this.loading.set(false); },
          error: ()       => { this.loadError.set('Unable to load menu products. Please try again.'); this.loading.set(false); },
        });
      },
      error: () => {
        this.loadError.set('Unable to load menu categories. Please try again.');
        this.loading.set(false);
      },
    });

    // 2. Sizes
    this.http.get<PizzaSize[]>('/api/pizzaSize/getPizzaSizes').subscribe({
      next: sizes => {
        this.sizes.set(sizes);
        if (sizes.length) this.selectedSizeId.set(sizes[1]?.sizeId ?? sizes[0].sizeId); // default Medium
      },
      error: () => {
        // Graceful fallback
        const fallback: PizzaSize[] = [
          { sizeId: 1, sizeName: 'Small',  price: 0  },
          { sizeId: 2, sizeName: 'Medium', price: 2  },
          { sizeId: 3, sizeName: 'Large',  price: 4  },
        ];
        this.sizes.set(fallback);
        this.selectedSizeId.set(2);
      },
    });

    // 3. Crust types  (SCRUM-39 – GET /api/crust-types)
    this.http.get<CrustType[]>('/api/crust/getCrusts').subscribe({
      next: crusts => {
        this.crustTypes.set(crusts);
        if (crusts.length) this.selectedCrustId.set(crusts[0].crustId);
      },
      error: () => {
        const fallback: CrustType[] = [
          { crustId: 1, crustName: 'Hand-Tossed', price: 0  },
          { crustId: 2, crustName: 'Deep Dish',   price: 2  },
          { crustId: 3, crustName: 'Thin Crust',  price: 0  },
        ];
        this.crustTypes.set(fallback);
        this.selectedCrustId.set(1);
      },
    });

    // 4. Toppings (SCRUM-43 – GET /api/toppings)
    this.http.get<Topping[]>('/api/topping/getToppings').subscribe({
      next:  tops  => this.toppings.set(tops),
      error: ()    => {
        // Graceful fallback so custom builder still works
        this.toppings.set([
          {
            toppingId: 1, toppingName: 'Sausage', extraCost: 0 },
          { toppingId: 2, toppingName: 'Pepperoni', extraCost: 0     },
          { toppingId: 3, toppingName: 'Canadian Bacon', extraCost: 0},
        ]);
      },
    });

  }

  // ── Category sidebar helpers ──────────────────────────

  scrollToCategory(categoryId: number): void {
    const el = document.getElementById(`category-${categoryId}`);
    el?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  productsForCategory(categoryId: number): Product[] {
    return this.productsByCategory.get(categoryId) ?? [];
  }

  // ── Modal open/close ──────────────────────────────────

  /** Open modal for a standard (named) menu product */
  openModal(product: Product): void {
    this.selectedProduct.set(product);
    this.isCustom.set(false);
    this.resetModalState();
  }

  /** Open modal for the custom pizza builder */
  openCustomModal(): void {
    this.selectedProduct.set(null);
    this.isCustom.set(true);
    this.resetModalState();
  }

  closeModal(): void {
    this.selectedProduct.set(null);
    this.isCustom.set(false);
    this.modalError.set('');
  }

  private resetModalState(): void {
    this.quantity.set(1);
    this.sauceLevel.set(2);
    this.halfPizzaMode.set(false);
    this.selectedToppingIds.set(new Set());
    this.leftToppingIds.set(new Set());
    this.rightToppingIds.set(new Set());
    this.modalError.set('');
    // Reset to first available defaults
    const sizes  = this.sizes();
    const crusts = this.crustTypes();
    this.selectedSizeId.set(sizes[1]?.sizeId ?? sizes[0]?.sizeId ?? 0);
    this.selectedCrustId.set(crusts[0]?.crustId ?? 0);
  }

  // ── Pill selectors ────────────────────────────────────

  selectSize(id: number): void  { this.selectedSizeId.set(id); }
  selectCrust(id: number): void { this.selectedCrustId.set(id); }

  // ── Topping toggles ───────────────────────────────────

  toggleTopping(id: number): void {
    const s = new Set(this.selectedToppingIds());
    s.has(id) ? s.delete(id) : s.add(id);
    this.selectedToppingIds.set(s);
  }

  toggleLeftTopping(id: number): void {
    const s = new Set(this.leftToppingIds());
    s.has(id) ? s.delete(id) : s.add(id);
    this.leftToppingIds.set(s);
  }

  toggleRightTopping(id: number): void {
    const s = new Set(this.rightToppingIds());
    s.has(id) ? s.delete(id) : s.add(id);
    this.rightToppingIds.set(s);
  }

  isToppingSelected(id: number): boolean      { return this.selectedToppingIds().has(id); }
  isLeftToppingSelected(id: number): boolean  { return this.leftToppingIds().has(id);     }
  isRightToppingSelected(id: number): boolean { return this.rightToppingIds().has(id);    }

  toggleHalfPizza(): void {
    this.halfPizzaMode.set(!this.halfPizzaMode());
    // Reset topping selections when toggling
    this.selectedToppingIds.set(new Set());
    this.leftToppingIds.set(new Set());
    this.rightToppingIds.set(new Set());
  }

  // ── Quantity controls ─────────────────────────────────

  incrementQty(): void { if (this.quantity() < 99) this.quantity.update(q => q + 1); }
  decrementQty(): void { if (this.quantity() > 1)  this.quantity.update(q => q - 1); }

  // ── Add to cart ───────────────────────────────────────

  /**
   * Called from "Add to Cart" button.
   * Validates the form then POSTs to /api/cart/add (SCRUM-53).
   */
  addToCart(): void {
    this.modalError.set('');

    // Validate: custom pizza must have at least one topping (SCRUM-51)
    if (this.isCustom()) {
      const hasTopping = this.halfPizzaMode()
        ? this.leftToppingIds().size > 0 || this.rightToppingIds().size > 0
        : this.selectedToppingIds().size > 0;

      if (!hasTopping) {
        this.modalError.set('Please select at least one topping for your custom pizza.');
        return;
      }
    }

    const size = this.sizes().find(s => s.sizeId === this.selectedSizeId());
    const payload: CartPayload = {
      menuproductId:      this.isCustom() ? null : (this.selectedProduct()?.productId ?? null),
      size:            size?.sizeName.toUpperCase() ?? 'MEDIUM',
      sauce:           this.sauceLabelMap[this.sauceLevel()].toUpperCase(),
      toppingIds:      this.halfPizzaMode() ? [] : Array.from(this.selectedToppingIds()),
      leftToppingIds:  this.halfPizzaMode() ? Array.from(this.leftToppingIds())  : null,
      rightToppingIds: this.halfPizzaMode() ? Array.from(this.rightToppingIds()) : null,
      quantity:        this.quantity(),
      crustTypeId:     this.selectedCrustId(),
    };

    this.addingToCart.set(true);

    this.http.post('/api/cart/add', payload).subscribe({
      next: () => {
        this.addingToCart.set(false);
        this.closeModal();
        // If your app uses a shared cart service, emit an update here, e.g.:
        // this.cartService.refresh();
      },
      error: (err) => {
        this.addingToCart.set(false);
        this.modalError.set(err?.error?.message ?? 'Failed to add product to cart. Please try again.');
      },
    });
  }

  // ── Template helpers ──────────────────────────────────

  /** True when modal should be visible */
  get modalOpen(): boolean {
    return this.selectedProduct() !== null || this.isCustom();
  }

  get modalproductName(): string {
    return this.isCustom() ? 'Custom Pizza' : (this.selectedProduct()?.productName ?? '');
  }
}
