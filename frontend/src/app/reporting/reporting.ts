import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';

interface Report {
  name: string;
  value: string | number;
}

@Component({
  selector: 'app-reporting',
  imports: [RouterLink],
  templateUrl: './reporting.html',
  styleUrl: './reporting.css',
})
export class Reporting implements OnInit {
  reports = signal<Report[]>([]);

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<Report[]>('/api/reports').subscribe(data => {
      this.reports.set(data);
    });
  }
}
