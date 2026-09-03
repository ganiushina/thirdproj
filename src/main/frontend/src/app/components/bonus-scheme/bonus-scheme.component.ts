import { CommonModule } from '@angular/common';
import { Component, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

interface Gap {
  id: number;
  percent: number;
  schemeDate: string;
}

interface Position {
  id: number;
  name: string;
}

interface SchemeLimit {
  id: number;
  limits: number;
  positionId: number;
  gapId: number;
  divisionId: number;
  schemeLimitsDate: string;
}

interface BonusSchemeEntry {
  id: number;
  schemeId: number;
  limit: number;
  positionId: number;
  gapId: number;
  divisionId?: number;
  dateScheme: string;
}

@Component({
  selector: 'app-bonus-scheme',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './bonus-scheme.component.html',
  styleUrl: './bonus-scheme.component.css'
})
export class BonusSchemeComponent {
  private readonly gaps: Gap[] = [
    { id: 1, percent: 3, schemeDate: '2021-01-01' },
    { id: 2, percent: 5, schemeDate: '2021-01-01' },
    { id: 3, percent: 7, schemeDate: '2021-01-01' },
    { id: 16, percent: 17, schemeDate: '2021-01-01' },
    { id: 17, percent: 25, schemeDate: '2024-07-01' },
    { id: 20, percent: 26, schemeDate: '2024-09-12' }
  ];

  private readonly positions: Position[] = [
    { id: 1, name: 'R2' },
    { id: 4, name: 'K3' },
    { id: 7, name: 'BDM' },
    { id: 10, name: 'Manager' },
    { id: 13, name: 'K4 Junior' },
    { id: 27, name: 'K3+R Эксперт' }
  ];

  private readonly limits = signal<SchemeLimit[]>([
    { id: 1, limits: 520000, positionId: 7, gapId: 2, divisionId: 2, schemeLimitsDate: '2021-01-01' },
    { id: 3, limits: 750000, positionId: 7, gapId: 6, divisionId: 2, schemeLimitsDate: '2021-01-01' },
    { id: 14, limits: 830000, positionId: 7, gapId: 3, divisionId: 3, schemeLimitsDate: '2021-01-01' },
    { id: 32, limits: 1150000, positionId: 7, gapId: 16, divisionId: 3, schemeLimitsDate: '2024-07-01' },
    { id: 40, limits: 1450000, positionId: 7, gapId: 19, divisionId: 3, schemeLimitsDate: '2024-07-01' },
    { id: 67, limits: 1120000, positionId: 7, gapId: 19, divisionId: 7, schemeLimitsDate: '2024-07-01' }
  ]);

  private nextSchemeId = 1000;

  protected readonly schemeEntries = signal<BonusSchemeEntry[]>([
    { id: 1, schemeId: 1, limit: 0, positionId: 4, gapId: 4, dateScheme: '2021-01-01' },
    { id: 8, schemeId: 1, limit: 360000, positionId: 3, gapId: 9, dateScheme: '2021-01-01' },
    { id: 54, schemeId: 3, limit: 0, positionId: 20, gapId: 4, dateScheme: '2021-01-01' },
    { id: 70, schemeId: 1, limit: 750000, positionId: 4, gapId: 9, dateScheme: '2022-10-01' },
    { id: 128, schemeId: 4, limit: 0, positionId: 23, gapId: 10, dateScheme: '2021-01-01' },
    { id: 175, schemeId: 3, limit: 850000, positionId: 4, gapId: 9, dateScheme: '2024-07-01' },
    { id: 263, schemeId: 1, limit: 850000, positionId: 27, gapId: 17, dateScheme: '2024-07-01' },
    { id: 627, schemeId: 1, limit: 850000, positionId: 27, gapId: 17, dateScheme: '2024-10-01' }
  ]);

  protected readonly schemeForm = this.fb.group({
    schemeId: [1, [Validators.required, Validators.min(1)]],
    positionId: [this.positions[0].id, Validators.required],
    gapId: [this.gaps[0].id, Validators.required],
    divisionId: [1, [Validators.required, Validators.min(1)]],
    limit: [0, [Validators.required, Validators.min(0)]],
    dateScheme: [this.getToday(), Validators.required]
  });

  readonly orderedGaps = computed(() => this.gaps.sort((a, b) => a.id - b.id));
  readonly orderedPositions = computed(() => this.positions.sort((a, b) => a.name.localeCompare(b.name)));
  readonly orderedLimits = computed(() => this.limits().slice().sort((a, b) => a.id - b.id));
  readonly orderedSchemes = computed(() => this.schemeEntries().slice().sort((a, b) => b.id - a.id));

  constructor(private readonly fb: FormBuilder) {}

  protected addScheme(): void {
    if (this.schemeForm.invalid) {
      this.schemeForm.markAllAsTouched();
      return;
    }

    const formValue = this.schemeForm.value;
    const newEntry: BonusSchemeEntry = {
      id: ++this.nextSchemeId,
      schemeId: formValue.schemeId!,
      positionId: formValue.positionId!,
      gapId: formValue.gapId!,
      divisionId: formValue.divisionId!,
      limit: formValue.limit!,
      dateScheme: formValue.dateScheme!
    };

    this.schemeEntries.update((entries) => [newEntry, ...entries]);
    this.schemeForm.reset({
      schemeId: formValue.schemeId,
      positionId: formValue.positionId,
      gapId: formValue.gapId,
      divisionId: formValue.divisionId,
      limit: 0,
      dateScheme: formValue.dateScheme
    });
  }

  protected formatCurrency(value: number): string {
    return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', maximumFractionDigits: 0 }).format(value);
  }

  protected positionName(positionId: number): string {
    return this.positions.find((p) => p.id === positionId)?.name ?? '—';
  }

  protected gapPercent(gapId: number): number | string {
    return this.gaps.find((g) => g.id === gapId)?.percent ?? '—';
  }

  private getToday(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
