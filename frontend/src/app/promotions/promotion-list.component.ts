import {Component, inject, OnInit, ChangeDetectorRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import { promotionService } from './promotion.service';
import { promotion } from './promotion.model';

@Component({
  selector: 'app-promotion-list',
  imports: [CommonModule],
  templateUrl: './promotion-list.component.html',
  styleUrl: './promotion-list.component.css'
})
export class PromotionListComponent implements OnInit {
  private promotionService = inject(promotionService);
  private cdr = inject(ChangeDetectorRef);

  promotions: promotion[] = [];
  isLoading = true;
  errorMessage = '';

  ngOnInit() {
    this.promotionService.getpromotions().subscribe({
      next: (data) => {
        console.log(data);
        this.promotions = data;
        this.isLoading = false;
        this.cdr.detectChanges();  // tell Angular to re-render
      },
      error: () => {
        this.errorMessage = 'Could not load promotions.';
        this.isLoading = false;
        this.cdr.detectChanges();  // tell Angular to re-render
      }
    });
  }
}
