import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UploadAndTable } from './upload-and-table';

describe('UploadAndTable', () => {
  let component: UploadAndTable;
  let fixture: ComponentFixture<UploadAndTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UploadAndTable]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UploadAndTable);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
