import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Promotion} from './promotion.model';

@Injectable({ providedIn: 'root' })
export class PromotionService {
  private http = inject(HttpClient);

  getPromotions(): Observable<Promotion[]> {
    return this.http.get<Promotion[]>('/api/promotions');
  }
}
