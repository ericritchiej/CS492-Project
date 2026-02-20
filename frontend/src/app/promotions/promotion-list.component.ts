import {Component, inject, OnInit, ChangeDetectorRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import { PromotionService } from './promotion.service';
import { Promotion } from './promotion.model';

@Component({
  selector: 'app-promotion-list',
  imports: [CommonModule],
  templateUrl: './promotion-list.component.html',
  styleUrl: './promotion-list.component.css'
})
export class PromotionListComponent implements OnInit {
  private promotionService = inject(PromotionService);
  private cdr = inject(ChangeDetectorRef);

  promotions: Promotion[] = [];
  isLoading = true;
  errorMessage = '';

  ngOnInit() {
    this.promotionService.getPromotions().subscribe({
      next: (data) => {
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
