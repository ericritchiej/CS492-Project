import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CurrencyPipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';  // 👈 ADD THIS

interface CrustType {
  crustId: number;
  crustName: string;
  price: number;
}

@Component({
  selector: 'app-menu',
  imports: [CurrencyPipe],
  templateUrl: './menu.html',
  styleUrl: './menu.css',
})
export class Menu implements OnInit {

  crustTypes = signal<CrustType[]>([]);
  successMessage = signal<string | null>(null);  // 👈 ADD THIS

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute   // 👈 ADD THIS
  ) {}

  ngOnInit() {

    // Load crust data
    this.http.get<CrustType[]>('/api/crust-types')
      .subscribe(data => {
        this.crustTypes.set(data);
      });

    // 👇 CHECK FOR SUCCESS FLAG
    this.route.queryParams.subscribe(params => {
      if (params['saved']) {
        this.successMessage.set('Profile updated successfully!');
      }
    });
  }
}
