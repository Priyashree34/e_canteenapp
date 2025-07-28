const fetch = require('node-fetch');
const { GoogleAuth } = require('google-auth-library');
const path = require('path');

const keyPath = path.join(__dirname, 'serviceAccountKey.json');
const fcmUrl = 'https://fcm.googleapis.com/v1/projects/ecanteenapp/messages:send';

// 🔑 Paste your actual token here
const targetToken = "f1olw1N4RZ6LF7zfdwbLb5:APA91bHerPkMzjSm9uNi9A3JzhfN-E7jIAuscDyXG7yOdN3cDaWfztn9P_lJkTe0jxvzW894tbxUKrYpDPaLXe2s6mR7cWl4VjYWRDL8eNNxWiMTuT70Yfc";

async function getAccessToken() {
  const auth = new GoogleAuth({
    keyFile: keyPath,
    scopes: ['https://www.googleapis.com/auth/firebase.messaging']
  });

  const client = await auth.getClient();
  const token = await client.getAccessToken();
  return token.token;
}

async function sendNotification() {
  try {
    const accessToken = await getAccessToken();

    const message = {
      message: {
        token: targetToken,
        notification: {
          title: "🍽️ Today's Special!",
          body: "Try our new dish now in the canteen!"
        }
      }
    };

    const response = await fetch(fcmUrl, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(message)
    });

    const result = await response.json();
    console.log("✅ Notification sent:", result);
  } catch (error) {
    console.error("❌ Error sending notification:", error);
  }
}

sendNotification();
