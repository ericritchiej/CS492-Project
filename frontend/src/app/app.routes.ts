import { Routes } from '@angular/router';
import { Menu } from './menu/menu';
import { Orders } from './orders/orders';
import { Admin } from './admin/admin';
import { Login } from './login/login';
import { RestaurantInfo } from './restaurant-info/restaurant-info';
import { Profile } from './profile/profile';
import { Cart } from './cart/cart';
import { Checkout } from './checkout/checkout';
import { Reporting } from './reporting/reporting';

export const routes: Routes = [
  { path: 'menu', component: Menu },
  { path: 'orders', component: Orders },
  { path: 'admin', component: Admin },
  { path: 'login', component: Login },
  { path: 'restaurant-info', component: RestaurantInfo },
  { path: 'profile', component: Profile },
  { path: 'cart', component: Cart },
  { path: 'checkout', component: Checkout },
  { path: 'reporting', component: Reporting },
];
