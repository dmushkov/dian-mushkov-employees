import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EmployeePairResult } from '../models/employee-pair-result';
import {CommonModule} from '@angular/common';
import {environment} from '../../environments/environment';

@Component({
  selector: 'app-upload-and-table',
  standalone: true,
  imports:[CommonModule],
  templateUrl: './upload-and-table.html',
  styleUrls: ['./upload-and-table.css'],
})
export class UploadAndTableComponent {
  selectedFile: File | null = null;
  rows: EmployeePairResult[] = [];
  loading = false;
  error: string | null = null;

  private baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files && input.files[0] ? input.files[0] : null;
    this.error = null;
  }

  upload() {
    if (!this.selectedFile) {
      return;
    }
    this.loading = true;
    this.error = null;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.http.post<EmployeePairResult[]>(`${this.baseUrl}/api/upload`, formData).subscribe({
      next: (data) => {
        this.rows = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to upload file';
        this.loading = false;
      },
    });
  }
}
