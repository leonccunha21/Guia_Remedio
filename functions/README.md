This folder contains Firebase Cloud Functions to verify Google Play purchases server-side.

Setup steps (summary):
1. Install Firebase CLI and initialize functions in this folder (firebase init functions).
2. Create a Google Play Developer service account with permission to "View financial data, orders, and subscription","View app information and download reports" and enable Android Publisher API. Download the JSON key.
3. Store the service account JSON securely in the Functions environment or use Secret Manager.
4. Replace placeholders in index.js with your packageName and set proper auth.
5. Deploy with: firebase deploy --only functions

The provided index.js implements endpoints:
- POST /verifyPurchase
  body: { purchaseToken, sku, packageName }
  response: { valid: true/false, message: "..." }

Note: You must configure service account credentials and grant the service account access to the Google Play Developer API. See detailed README in this file for more info.
