const functions = require('firebase-functions');
const {google} = require('googleapis');

// Reads Google Play service account key from functions config (preferred) or environment variable.
// To set via firebase CLI: firebase functions:config:set googleplay.key="$(cat key.json)"

const rawKeyFromConfig = (functions.config && functions.config().googleplay && functions.config().googleplay.key) ? functions.config().googleplay.key : null
let SERVICE_ACCOUNT_KEY = null

if (rawKeyFromConfig) {
  try {
    SERVICE_ACCOUNT_KEY = JSON.parse(rawKeyFromConfig)
  } catch (e) {
    console.warn('Invalid JSON in functions config googleplay.key')
  }
} else if (process.env.GOOGLE_PLAY_SERVICE_ACCOUNT_JSON) {
  try {
    SERVICE_ACCOUNT_KEY = JSON.parse(process.env.GOOGLE_PLAY_SERVICE_ACCOUNT_JSON)
  } catch (e) {
    console.warn('Invalid JSON in env GOOGLE_PLAY_SERVICE_ACCOUNT_JSON')
  }
} else {
  console.warn('Google Play service account key not configured. Use `firebase functions:config:set googleplay.key="$(cat key.json)"` or set env var.')
}

exports.verifyPurchase = functions.https.onRequest(async (req, res) => {
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Methods', 'POST');
  res.set('Access-Control-Allow-Headers', 'Content-Type');
  if (req.method === 'OPTIONS') return res.status(204).send('');

  try {
    const { purchaseToken, sku, packageName } = req.body;
    if (!purchaseToken || !sku || !packageName) return res.status(400).json({ valid: false, message: 'Missing fields' });

    if (!SERVICE_ACCOUNT_KEY) return res.status(500).json({ valid: false, message: 'Service account not configured' });

    const authClient = new google.auth.JWT(
      SERVICE_ACCOUNT_KEY.client_email,
      null,
      SERVICE_ACCOUNT_KEY.private_key,
      ['https://www.googleapis.com/auth/androidpublisher']
    );

    const androidpublisher = google.androidpublisher({ version: 'v3', auth: authClient });

    // Try subscription verification first
    try {
      const subRes = await androidpublisher.purchases.subscriptions.get({
        packageName,
        subscriptionId: sku,
        token: purchaseToken
      });
      const data = subRes.data;
      // Basic validation: check if expiresTimeMillis is in the future
      const now = Date.now();
      const expiry = parseInt(data.expiryTimeMillis || '0');
      if (expiry && expiry > now) return res.json({ valid: true, message: 'Subscription active' });
    } catch (e) {
      // not a subscription or failed; continue to try products
    }

    // Try product (one-time purchase)
    try {
      const prodRes = await androidpublisher.purchases.products.get({
        packageName,
        productId: sku,
        token: purchaseToken
      });
      const pdata = prodRes.data;
      if (pdata && pdata.purchaseState === 0) { // PURCHASED
        return res.json({ valid: true, message: 'One-time purchase valid' });
      }
    } catch (e) {
      // fallback
    }

    return res.json({ valid: false, message: 'Purchase not valid or expired' });
  } catch (err) {
    console.error(err);
    return res.status(500).json({ valid: false, message: 'Server error: ' + err.message });
  }
});
