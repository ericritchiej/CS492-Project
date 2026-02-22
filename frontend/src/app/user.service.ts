import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CurrentUser } from './auth.service';

// Created this so we don't have to update this in every place if it changes.
export interface DemographicsRequest {
  firstName: string;
  lastName:  string;
  phone:     string;
  address1:  string;
  address2:  string;
  city:      string;
  state:     string;
  zip:       string;
  email:     string;
}

export type UpdateResponse = {
  message: string;
  user: CurrentUser;
};

@Injectable({ providedIn: 'root' })
export class UserService {

  constructor(private http: HttpClient) {}

  getUser(): Observable<DemographicsRequest> {
    return this.http.get<DemographicsRequest>('/api/user/getUser');
  }

  updateDemographics(payload: DemographicsRequest): Observable<UpdateResponse> {
    return this.http.post<UpdateResponse>('/api/user/updateDemographics', payload);
  }
}
