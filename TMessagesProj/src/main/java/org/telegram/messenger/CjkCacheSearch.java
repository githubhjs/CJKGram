package org.telegram.messenger;

import org.telegram.SQLite.SQLiteCursor;
import org.telegram.tgnet.TLRPC;
import java.util.ArrayList;

/** Read-only search of one account's cached cloud-chat messages. No network calls. */
public final class CjkCacheSearch {
    public static final class Hit {
        public final int id, date;
        public final String text;
        Hit(int id, int date, String text) {
            this.id = id;
            this.date = date;
            this.text = text;
        }
    }

    public interface Callback {
        void done(ArrayList<Hit> hits, int scanned, boolean limited, boolean failed);
    }

    private final MessagesStorage storage;
    private final long dialogId;
    private final String query;
    private final Callback callback;
    private final ArrayList<Hit> hits = new ArrayList<>();
    private volatile boolean cancelled;
    private long beforeId = Long.MAX_VALUE;
    private int scanned;

    public CjkCacheSearch(int account, long dialogId, String query, Callback callback) {
        storage = MessagesStorage.getInstance(account);
        this.dialogId = dialogId;
        this.query = CjkText.normalize(query.trim());
        this.callback = callback;
    }

    public void start() {
        if (query.isEmpty() || DialogObject.isEncryptedDialog(dialogId)) {
            finish(false, true);
            return;
        }
        storage.getStorageQueue().postRunnable(this::batch);
    }

    public void cancel() { cancelled = true; }

    private void batch() {
        if (cancelled) return;
        SQLiteCursor cursor = null;
        int rows = 0;
        boolean failed = false;
        try {
            // Keyset pagination yields the storage queue every 256 rows. Bind all values.
            cursor = storage.getDatabase().queryFinalized(
                    "SELECT mid, date, data FROM messages_v2 WHERE uid = ? AND mid > 0 AND mid < ? ORDER BY mid DESC LIMIT 256",
                    dialogId, beforeId);
            while (!cancelled && cursor.next()) {
                beforeId = cursor.longValue(0);
                rows++;
                scanned++;
                TLRPC.Message message = cursor.tlObjectValue(2, TLRPC.Message::TLdeserialize, false);
                if (message != null && !(message instanceof TLRPC.TL_messageService)
                        && message.ttl == 0 && message.ttl_period == 0
                        && CjkText.matches(message.message, query)) {
                    hits.add(new Hit((int) beforeId, cursor.intValue(1), message.message));
                    if (hits.size() == 100) break;
                }
            }
        } catch (Exception e) {
            // Do not log chat text, queries, or serialized messages.
            failed = true;
        } finally {
            if (cursor != null) cursor.dispose();
        }
        if (cancelled) return;
        if (failed || rows < 256 || hits.size() >= 100) {
            finish(hits.size() >= 100, failed);
        } else {
            storage.getStorageQueue().postRunnable(this::batch);
        }
    }

    private void finish(boolean limited, boolean failed) {
        AndroidUtilities.runOnUIThread(() -> {
            if (!cancelled) callback.done(hits, scanned, limited, failed);
        });
    }
}
