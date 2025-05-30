import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {UploadAndTableComponent} from './upload-and-table/upload-and-table';

const routes: Routes = [
  { path: '', component: UploadAndTableComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
