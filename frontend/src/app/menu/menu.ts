import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CurrencyPipe } from '@angular/common';

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
  crustTypes: CrustType[] = [];

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<CrustType[]>('/api/crust-types').subscribe(data => {
      this.crustTypes = data;
    });
  }
}
