# Privacy

KhataGo is built as a local-first finance application.

## Current privacy posture
- no ads
- no tracking SDKs
- no analytics SDKs
- no remote financial backend
- no account sign-in
- no cloud dependency for reports, backup, restore, CSV, or PDF export

## Permissions
Current manifest requests:
- `POST_NOTIFICATIONS`

This permission is used only for local reminder notifications on supported Android versions.

KhataGo does not request Internet, contacts, location, camera, microphone, SMS, or phone permissions.

## Data residency
- financial data is stored locally in Room
- settings are stored locally in DataStore
- backup, CSV, and PDF exports are user-directed through Android document pickers
- exported files may contain sensitive financial data and should be stored carefully by the user
