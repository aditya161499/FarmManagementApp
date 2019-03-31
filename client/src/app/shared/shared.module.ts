import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FooterComponent } from './footer/footer.component';
import { HeaderComponent } from './header/header.component';
import { NavComponent } from './nav/nav.component';
import { MatButtonModule, MatInputModule } from '@angular/material';
import { MatSelectModule } from '@angular/material/select';
import { MatRadioModule } from '@angular/material/radio';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material';

import { FlashMessagesModule } from 'angular2-flash-messages';
import { NgxPaginationModule } from 'ngx-pagination';
import { TableFilterPipe } from './../pipes/search.pipe';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    FlashMessagesModule.forRoot(),
    NgxPaginationModule,
    MatButtonModule,
    MatInputModule,
    MatSelectModule,
    MatRadioModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatNativeDateModule
  ],
  declarations: [FooterComponent, HeaderComponent, NavComponent, TableFilterPipe ],
  exports: [
    FormsModule,
    ReactiveFormsModule,
    FooterComponent,
    NavComponent,
    HeaderComponent,
    FlashMessagesModule,
    NgxPaginationModule,
    MatButtonModule,
    MatInputModule,
    MatSelectModule,
    MatRadioModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatNativeDateModule,
    TableFilterPipe
  ]
})
export class SharedModule { }
