const { GoogleAuth } = require('google-auth-library');
const path = require('path');

const keyPath = path.join(__dirname, 'serviceAccountKey.json');

async function getAccessToken() {
  const auth = new GoogleAuth({
    keyFile: keyPath,
    scopes: ['https://www.googleapis.com/auth/firebase.messaging']
  });

  const client = await auth.getClient();
  const token = await client.getAccessToken();
  console.log("Your Access Token:\n", token.token);
}

getAccessToken();
