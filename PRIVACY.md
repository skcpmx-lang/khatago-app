# Privacy

KhataGo is built as a local-first finance application.

## Current privacy posture

- no ads
- no tracking SDKs
- no analytics SDKs
- no remote financial backend
- no account sign-in
- no unnecessary network calls in the current implementation

## Permissions

Current manifest does not request Internet, contacts, location, camera, microphone, SMS, phone, or notification permissions.

Notification permissions can be added later only when reminder delivery is fully implemented.

## Data residency

Financial data is stored locally in Room.

Backup export logic exists in the repository layer, but user-directed file export and restore flows are still incomplete.
