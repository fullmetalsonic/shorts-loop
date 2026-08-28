# ShortsLoop project rules

## Structure and scope

- Java/native Android, minSdk26, compile/target35. Keep core Android-independent.
- Separate detection, playback policy, settings, overlay, accessibility service, tile, UI and updates.
- Preserve existing artifacts and settings. Do not refactor unrelated features.
- Record technical decisions, changes, tests, regressions and remaining limitations in the linked project documents.

## Product contract

- 0.5.0 navigation: home has three app-entry buttons and common controls; each detail page owns host selection, repeat/tap mode and supported special rules. Preserve drafts and existing stored settings. Home navigation never starts execution.

- Repeat count 0–99, typed input and arrows. Floating tap modes: 0→1→…→configured count→0, or 0↔configured count.
- Zero stops normal repeat, clockless timer and visual analysis. Instagram/TikTok ads remain independent when execution, host selection and ad option are ON.
- YouTube live previews are a separate opt-in(default OFF); delay0–60(default0), zero means immediate after safe page settlement, not off. Live skipping remains independent of ordinary count0 and stops with overall executionOFF.
- Live detection uses the dedicated preview element, full single-page/window guards and RAM-only node identity, never titles/CTA/viewer counts. A live-to-live transition requires fresh same-pager index evidence plus a different stable page key. Reused/ambiguous nodes fail closed; do not bypass transition confirmation.
- Long-video skipping is separate/default OFF: known total duration >= configured seconds(1–3600, initial60), selected YouTube/Instagram/TikTok, independent of count0. Unknown duration is not guessed. Require latest real forward playback and stable same-window evidence. The generic YouTube/Instagram confirmation path accepts a stable different page identity, or a request-fresh same-pager/window/index change plus stable different valid duration and fresh forward progress. TikTok uses its stricter source/pager/media/index transition contract below. The YouTube supplementary-content-key path requires a stable different content key AND either verified pager movement (including fresh direct-page CollectionItemInfo row exactly requestRow+1) or a different valid duration, together with fresh forward progress. Read the same request pager/window/full-page bounds every sample; known same/backward/skipped rows and unsafe shape/refresh failures override other evidence. Missing position data alone may retain the existing duration path. Never latch a row change across rollback. Pin the identity source at request time; missing metadata is not a new page. Duration alone never confirms. Long-request failures remain hard stops, not ordinary-recovery retries.
- Execution OFF stops all automation. Floating display is optional; moving it preserves its host-relative position. A host-labelled X pauses only that host; the in-app host resume control or overall OFF/ON restores it.
- Multiple-app mode retains the dual_mode key and is opt-in/default OFF. OFF processes only the active host window; ON observes each selected visible non-PiP host (one, two or three) with fully independent counters/settings/failures. Never force playback or blindly alternate focus. Serialize OS gestures and semantic scroll actions through immutable leases and a bounded fair per-host queue; expired or changed-page deferred intentions are discarded and fresh geometry is revalidated before dispatch. Preserve original settings keys and use incremental host schema v2; TikTok starts deselected with count2 and never inherits unsupported special-content options.
- Timer fallback is Instagram/TikTok for eligible clockless single videos: default OFF, initial3 seconds for unset values, range2–60. Preserve existing valid saved values and toggle. The two-second qualification is included in the selected total, not added afterward. It is a timeout, not a precise playback count.
- Instagram/TikTok ad pre-skip delay is separate from each host's ad toggle and ordinary count: integer tenths0–99, default0, displayed0.0–9.9 seconds with0.1-second steps. Zero means no extra delay after safety guards, not disabled. Positive delay requires stable same-ad source identity, expires/cancels on unsafe or changed page/window/settings/execution state, and never occupies the shared input lease while waiting. Unknown identity waits; delay is not a substitute for transition confirmation.
- TikTok (com.ss.android.ugc.trill) supports positively identified SurfaceView/TextureView recommendation videos, normalized progress and optional exact seek-control elapsed/total clocks. No duration inferred from range/rate. Known ad/photo structures retain overlapping evidence; ad skipping ON takes priority, OFF permits only otherwise eligible ordinary/photo rules. Ad delay0.0–9.9s in0.1s steps, clockless opt-in2–60/default3, photo whole/each0–10/default3, known-duration long filter1–3600/default60; each opt-in defaultsOFF and uses independent host.tiktok keys. TikTok LIVE/visual and other variants remain unsupported. Read-only observations are not physical automation PASS.
- TikTok vertical requests revalidate the raw same page/pager/media/index/window immediately before semantic input. Confirmation requires same scope/pager, stable different page AND media, exact next index if known, plus fresh forward progress when available; positively recognized clockless/ad/photo destinations use stable-source confirmation. All unresolved TikTok requests remain hard stops until explicit master OFF/ON. Photo horizontal moves require unchanged post/pager/feed index and exact next photo index. No horizontal input on unclassified advertising carousels.
- Photo automation is Instagram/TikTok, independent of count0 and default OFF: whole-Reel / each-photo modes, separate0–10-second delays(default3). Zero means after safe settlement, not disabled. Optional unreadable-index fallback(default OFF) uses the whole-Reel delay only on a known photo page. Exact next index with unchanged total/same post confirms a horizontal move; a stable different post AND independent different media source-node in the same window confirms a vertical move. Stateless window/node-hash keys must preserve A→B→A rollback;node reuse/collision/missing metadata never proves movement. Missing metadata cannot bypass a pending request, mixed/unsafe screens cannot become timer candidates, and failures stay blocked until explicit execution OFF/ON. Never use ordinary fresh-start recovery for a photo request.
- Second-based ordinary progress uses the existing repeat counter with a start cap min(1.3 seconds,10 percent of duration); TikTok uses a separate normalized counter. Menus, pause, lock, app/window changes and unconfirmed transitions retain their safety guards. Reset-reason diagnostics do not mean every count-zero cause has been fixed.
- Ordinary progress-based confirmation timeouts may enter read-only fresh-start recovery: same request host/window, near-start plus plausible forward progress, then a newly observed full target count. No immediate/retry swipe, no inferred completion, no special-content bypass. Rejected/cancelled gestures and non-ordinary timeouts remain hard stops.
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
- Keep intermittent detection limitations visible. Report endurance results with their exact build, host, options and observed transition types; a mixed20-transition pass is not an all-content or all-window guarantee.
- Compare source/build identity, installed APK, public release asset and hashes. Preserve the existing release signing identity.
- Publish only when explicitly requested; never infer email permission from a GitHub release request.
- Public documentation contains technical requirements, usage, change history and evidence, not conversational approval transcripts.
