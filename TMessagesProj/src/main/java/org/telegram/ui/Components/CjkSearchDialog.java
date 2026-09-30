package org.telegram.ui.Components;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CjkCacheSearch;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/** Prototype surface: explicit cached-only scope; never claims complete history. */
public final class CjkSearchDialog {
    public interface OpenMessage { void open(int id); }

    public static AlertDialog show(Context context, int account, long dialogId, OpenMessage open) {
        LinearLayout panel = new LinearLayout(context);
        panel.setOrientation(LinearLayout.VERTICAL);
        int padding = AndroidUtilities.dp(16);
        panel.setPadding(padding, padding, padding, padding);
        TextView scope = new TextView(context);
        scope.setText("搜尋此聊天在這台裝置已快取的文字與圖片說明。未下載的歷史不在範圍內；不包含遷移前的舊群組、秘密聊天或限時訊息。支援中日韓子字串、全半形與英文大小寫。\nCached messages only · not complete history.");
        panel.addView(scope);
        EditText input = new EditText(context);
        input.setSingleLine(true);
        input.setHint("山本 / 豆拉 / キーワード / 검색");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        panel.addView(input);
        Button search = new Button(context);
        search.setText("搜尋本機 / Search locally");
        panel.addView(search);
        TextView status = new TextView(context);
        panel.addView(status);
        ListView list = new ListView(context);
        panel.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                AndroidUtilities.dp(260)));
        ArrayList<CjkCacheSearch.Hit> results = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        list.setAdapter(adapter);
        CjkCacheSearch[] active = new CjkCacheSearch[1];
        AlertDialog dialog = new AlertDialog.Builder(context).setTitle("CJKGram · 本機搜尋 Preview")
                .setView(panel).setNegativeButton("關閉 / Close", null).create();
        search.setOnClickListener(view -> {
            if (active[0] != null) active[0].cancel();
            results.clear();
            adapter.clear();
            String query = input.getText().toString().trim();
            if (query.isEmpty()) {
                status.setText("請輸入關鍵字 / Enter a query");
                return;
            }
            AndroidUtilities.hideKeyboard(input);
            status.setText("正在搜尋快取… / Searching cache…");
            active[0] = new CjkCacheSearch(account, dialogId, query, (hits, scanned, limited, failed) -> {
                results.addAll(hits);
                DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                for (CjkCacheSearch.Hit hit : hits) {
                    String excerpt = hit.text.length() > 320 ? hit.text.substring(0, 320) + "…" : hit.text;
                    adapter.add(format.format(new Date(hit.date * 1000L)) + "\n" + excerpt);
                }
                status.setText(failed ? "讀取快取失敗，結果可能不完整 / Cache read failed"
                        : "已檢查 " + scanned + " 則；找到 " + hits.size() + " 則"
                        + (limited ? "（已達 100 則上限，請縮小關鍵字）" : "（僅本機快取）"));
            });
            active[0].start();
        });
        list.setOnItemClickListener((parent, view, position, id) -> {
            int messageId = results.get(position).id;
            dialog.dismiss();
            open.open(messageId);
        });
        dialog.setOnDismissListener(ignored -> {
            if (active[0] != null) active[0].cancel();
        });
        dialog.show();
        return dialog;
    }
}
