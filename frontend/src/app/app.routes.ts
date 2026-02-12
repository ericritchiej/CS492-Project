import { Routes } from '@angular/router';
import { Menu } from './menu/menu';
import { Orders } from './orders/orders';
import { Admin } from './admin/admin';

export const routes: Routes = [
  { path: 'menu', component: Menu },
  { path: 'orders', component: Orders },
  { path: 'admin', component: Admin },
];
