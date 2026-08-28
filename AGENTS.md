# ShortsLoop project rules

## Structure and scope

- Java/native Android, minSdk26, compile/target35. Keep core Android-independent.
- Separate detection, playback policy, settings, overlay, accessibility service, tile, UI and updates.
- Preserve existing artifacts and settings. Do not refactor unrelated features.
- Record technical decisions, changes, tests, regressions and remaining limitations in the linked project documents.

## Product contract

- Repeat count 0–99, typed input and arrows. Floating tap modes: 0→1→…→configured count→0, or 0↔configured count.
- Zero stops normal repeat, clockless timer and visual analysis. Instagram ads remain independent when execution, host selection and ad option are ON.
- YouTube live previews are a separate opt-in(default OFF); delay0–60(default0), zero means immediate after safe page settlement, not off. Live skipping remains independent of ordinary count0 and stops with overall executionOFF.
- Live detection uses the dedicated preview element, full single-page/window guards and RAM-only node identity, never titles/CTA/viewer counts. A live-to-live transition requires fresh same-pager index evidence plus a different stable page key. Reused/ambiguous nodes fail closed; do not bypass transition confirmation.
- Execution OFF stops all automation. Floating display is optional; moving it preserves its position, and X stops execution.
- Timer fallback is Instagram-only for eligible clockless single videos: default OFF, initial10 seconds, range5–60. It is a timeout, not a precise playback count.
- Normal progress uses the existing repeat counter. Menus, pause, lock, app/window changes and unconfirmed transitions retain their safety guards.
- Visual assistance is experimental, opt-in, API34+ only, with RAM-only window analysis. Unsupported OS/host choices remain saved but inactive.
- API33+ can request tile addition; older OS shows manual instructions. API29+ tile subtitle, older OS state label.
- The unconnected VisualSequence and separate audio-probe experiments are not product features. Their failures cannot be counted as product PASS.

## Permissions, privacy and installation

- Accessibility and overlay permission changes require explicit user action; never secretly grant permissions through ADB.
- INTERNET is limited to fixed Public GitHub update lookup/download; no video, account or viewing-history upload.
- REQUEST_INSTALL_PACKAGES connects to the Android installer. Source permission and final install confirmation are manual.
- No background updater, notification permission, silent install, token, arbitrary endpoint or product QA bypass.
- Before installation stop execution only and preserve other settings.
- Validate update metadata, size, SHA256, package, version, OS and the complete current signer set. Android performs final installation validation.
- Do not publish private keys, personal screenshots, raw device logs, credentials, conversations or unnecessary local paths.

## Verification and release

- Follow [verification](docs/VERIFICATION.md), [compatibility](docs/COMPATIBILITY.md) and [update delivery](docs/UPDATE_DELIVERY_PLAN.md).
- Do not describe a UI smoke test or requested swipe as confirmed social-app auto-advance.
- Existing intermittent detection limitations and uncompleted20-transition endurance testing remain visible.
- Compare source/build identity, installed APK, public release asset and hashes. Preserve the existing release signing identity.
- Publish only when explicitly requested; never infer email permission from a GitHub release request.
- Public documentation contains technical requirements, usage, change history and evidence, not conversational approval transcripts.
