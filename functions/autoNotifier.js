const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://console.firebase.google.com/project/ecanteenapp/database/ecanteenapp-default-rtdb/data/~2F" // ✅ Replace with your DB URL
});

const db = admin.database();

async function sendSpecialItemNotification() {
  try {
    const menuRef = db.ref("menuItems/Special");
    const snapshot = await menuRef.orderByKey().limitToLast(1).once("value");

    if (!snapshot.exists()) {
      console.log("❌ No special items found.");
      return;
    }

    let latestItem;
    snapshot.forEach(child => {
      latestItem = child.val();
    });

    const { name: itemName, price: itemPrice } = latestItem || {};

    if (!itemName || !itemPrice) {
      console.log("❌ Missing item details.");
      return;
    }

    const now = Date.now();
    const notificationBody = `Try ${itemName} today at our canteen for ₹${itemPrice}!`;

    // Check for duplicates
    const notifRef = db.ref("notifications");
    const notifSnap = await notifRef.once("value");

    let isDuplicate = false;
    notifSnap.forEach(child => {
      const n = child.val();
      if (n.title === "Today's Special!" && n.body === notificationBody) {
        isDuplicate = true;
      }
    });

    if (isDuplicate) {
      console.log("⚠️ Notification already sent for this item.");
      return;
    }

    // Fetch all user tokens
    const tokensSnap = await db.ref("userTokens").once("value");
    const tokens = Object.values(tokensSnap.val() || {});

    if (tokens.length === 0) {
      console.log("⚠️ No user tokens found.");
      return;
    }

    const message = {
      notification: {
        title: "Today's Special!",
        body: notificationBody
      },
      tokens
    };

    const response = await admin.messaging().sendMulticast(message);
    console.log(`✅ Notification sent to ${response.successCount} users`);

    // Save in Firebase
    await notifRef.push().set({
      title: "Today's Special!",
      body: notificationBody,
      timestamp: now
    });

    console.log("📝 Notification saved in Firebase.");

  } catch (error) {
    console.error("❌ Error sending notification:", error);
  }
}

sendSpecialItemNotification();
