import { Component } from '@angular/core';
import { BonusSchemeComponent } from './components/bonus-scheme/bonus-scheme.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [BonusSchemeComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'frontend';
}
