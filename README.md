<p align = "center">
	<img alt = "Google Play Logo" src = "img/google_play_logo.png">
</p>

<p align = "center">
	<a href = "https://play.google.com/store/apps/details?id=com.opskins.trade.waxexpresstrade">Available on Google Play</a>
</p>

## Setup

### Request an API Key

* [Existing OPSkins user](https://github.com/Kevin-Reinke/WAX_ExpressTrade_Integration#request-an-api-key)
* [New OPSkins user](https://github.com/Kevin-Reinke/WAX_ExpressTrade_Integration#set-up-an-opskins-account)

## Configuration

### *app/src/main/res/values/keys.xml*

* `OPSKINS_OAUTH_CLIENT_ID`: Enter your client's ID
* `OPSKINS_OAUTH_CLIENT_SECRET`: Enter a random string | [RANDOM.ORG String Generator](https://www.random.org/strings/)

## Key Features

### Trade with a specific user (redirect)

*https://trade.opskins.com/t/{USER_ID}/{TOKEN}*

### View a specific trade offer (redirect)

*https://trade.opskins.com/trade-offers/{OFFER_ID}*

### Automatically accepts case-opening trade offers

(Only when the app is open/minimized to avoid user conflicts - e.g. site development)