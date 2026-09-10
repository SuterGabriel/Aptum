import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('zeigt die Wortmarke und eine benannte Navigation', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const html: HTMLElement = fixture.nativeElement;
    expect(html.querySelector('.wortmarke')?.textContent).toContain('Aptum');
    expect(html.querySelector('nav')?.getAttribute('aria-label')).toBe('Hauptnavigation');
  });
});
