// Minimal, privacy-respecting pageview logger.
//
// No analytics provider (GA, Plausible, etc.) is wired up in this build phase.
// Rather than faking network calls to a provider that doesn't exist, pageviews
// are only recorded when the user has accepted cookies, and are logged locally.
// Swap trackPageview()'s body for a real provider call when one is configured.

const CONSENT_KEY = 'vs_cookie_consent';

export function getConsent() {
  return localStorage.getItem(CONSENT_KEY); // 'accepted' | 'rejected' | null
}

export function setConsent(value) {
  localStorage.setItem(CONSENT_KEY, value);
}

export function trackPageview(path) {
  if (getConsent() !== 'accepted') return;
  // eslint-disable-next-line no-console
  console.info('[analytics] pageview', path, new Date().toISOString());
}
