import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BonusKpiComponent } from './bonus-kpi.component';

describe('BonusKpiComponent', () => {
  let component: BonusKpiComponent;
  let fixture: ComponentFixture<BonusKpiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BonusKpiComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BonusKpiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
