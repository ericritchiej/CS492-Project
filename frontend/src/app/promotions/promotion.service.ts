import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {promotion} from './promotion.model';

@Injectable({ providedIn: 'root' })
export class promotionService {
  private http = inject(HttpClient);

  getpromotions(): Observable<promotion[]> {
    return this.http.get<promotion[]>('/api/promotions');
  }
}
