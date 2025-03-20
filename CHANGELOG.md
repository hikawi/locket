## v0.6 - Firebase Messaging

We would like our devices to receive the data, for example, new posts, new messages as soon as possible. We can do that in two ways:

1. By establishing a WebSocket with the Spring Server, and the server would send any new information back. This is **difficult** because we need a background task to constantly check and keep this web socket open, if the web socket closes there have to be ways to reopen correctly.
2. By subscribing to a public record breaker (for example, Android's Google Notifications Service or Apple's Push Notifications Service). This is **easier** because we just send a FCM token to the backend (like Authentication), and the backend would use that token to tell Google or Apple to send those notifications to the device.

> **WARNING!**
>
> To run the backend locally, now you need a `.json` file that includes your Google's Firebase Credentials. Get one on your own Firebase System and name it `locket-firebase-key.json`.
>
> Without it, the backend will not boot.

A **notification message** should instantly notify the device, without any code on Android. A **data message** will be called on a handler on the Android side, this should usually be handled silently.

<details>
<summary>Changes</summary>

Changes that add data or notification messages, checkout Backend Documentation.

### Added route `POST /subscribe`

- This route accepts your newly registered FCM token generated from the device. One device has only one FCM token, separate from Authentication token.
- Accepts `{ token: string }`.
- Returns `200` with `{ token: string }` with the token you sent.

### Added route `DELETE /subscribe`

- This route deletes your registered device token from the database, for example, logging out, changing account.
- Accepts `{ token: string }`.
- Returns:
  - 404 if the token can not be found.
  - 403 if the token is not yours.
  - 200 if the token is deleted.

### Modified route `DELETE /friends`

- This route now sends a data message.

### Modified route `POST /requests`

- This route again, now accepts `username` instead of `id` of the user.

</details>

## v0.5 - ID Migration

The main issue with this release is that some routes used to use "username" as a query for users we want to interact with, for example, to send a message to a user, we would send a `username` parameter to the server. This will cause desyncs if the user decides to change their username before the other device can properly cache. Now we use an `id` which can never change.

<details>
<summary>Changes</summary>

Note: **All paginated route has the following format**: `total`, `totalPages`, `page`, `perPage`, `results`.

- Fix `GET /profiles` returning 403 even when authenticated.
- Route `GET /posts` is now a paginated route. Changed params `from` to `since` and `to` to `until`.
- Route `POST /register` doesn't need a `birthdate` field anymore.
- Route `GET /requests` is now a paginated route.
- Route `POST /requests` now accepts `id` instead of `username`.
- Route `DELETE /requests` now accepts `id` instead of `username`.
- Route `DELETE /friends` now accepts `id` instead of `username`.
- Route `POST /posts`'s viewers part now require IDs instead of usernames. Now also returns a proper post response.

</details>
