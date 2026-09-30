# CJKGram Android preview

Fork of the official DrKLO/Telegram Android client. Upstream baseline: dc780e8.
Name: CJKGram. Application ID: space.hjs.cjkgram (preview: .beta).

## First preview

Open an ordinary cloud chat → overflow menu → **CJK 本機搜尋 / Local search**.
Search is literal, offline, Unicode NFKC-normalized and case-insensitive.
Examples: 山本 finds 絕境山本; Japanese half-width text matches full-width text;
Korean decomposed syllables match composed syllables. Simplified/Traditional
conversion is not yet implemented.

This preview scans only that account/chat's existing `messages_v2` cache in
256-row batches, yielding the storage queue between batches. It does not call
server search, upload query text, make another copy of messages on disk, or
claim to have indexed all history. It returns up to 100 matches in descending
message-ID order and jumps to the selected message in the chat. Query changes
and closing/destroying the dialog cancel outstanding work.

Not available inside topic views, secret chats, or special chat modes.
Migrated predecessor groups and messages stored only in topic-specific tables
are not included. Expiring messages are excluded. Results are a point-in-time
view: concurrent edits/deletes can change the chat after a search completes.
Re-run search to refresh. Clearing Telegram's cache changes searchable coverage.

## Build

Manual GitHub Actions workflow: `CJKGram preview APK`. Requires secrets:
TELEGRAM_API_ID, TELEGRAM_API_HASH, SIGNING_KEY_BASE64, KEY_STORE_PASSWORD,
KEY_PASSWORD, KEY_ALIAS. The workflow builds only ARM64, with a dedicated stable
preview key; no Play upload or production release occurs. Credentials are
injected only in the ephemeral runner and never included as source artifacts.
Telegram API client credentials necessarily reside in the resulting client.

Preview disables Firebase configuration processing: FCM push and Google Maps
are not configured for CJKGram. The existing beta app SnapForward is separate.
Do not use the upstream dummy keystore for distributed builds.

Run Unicode matching tests with JDK 8+: `bash tools/cjkgram/test.sh`.
Full Android compilation uses JDK 17, SDK/build-tools 36, NDK 27.2.12479018,
CMake 3.22.1, and the pinned recursive git submodules.

## Next milestones before a wider beta

- On-device login/search/result-navigation and lifecycle verification.
- Persistent per-account n-gram index with edit/delete/logout synchronization.
- Explicit selected-chat history backfill, rate-limit handling and resumable
  cursor; coverage dates and progress. Never claim server history completeness
  from cached message counts.
- Single-character, 2-character and long-query benchmarks on 100k+ messages;
  optional Traditional/Simplified matching and topic/migrated-chat support.
- Own Firebase setup, full branding, signed release AAB, privacy/store listing
  and Play testing setup for this new package.

The original GPL license and upstream notices remain in force. This is an
unofficial client; the upstream README follows unchanged for reference.
