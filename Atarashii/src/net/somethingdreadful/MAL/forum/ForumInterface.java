package net.somethingdreadful.MAL.forum;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.JavascriptInterface;

import net.somethingdreadful.MAL.ForumActivity;
import net.somethingdreadful.MAL.ProfileActivity;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.dialog.ChooseDialogFragment;
import net.somethingdreadful.MAL.dialog.NumberPickerDialogFragment;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;

public class ForumInterface {
    private final ForumActivity forum;

    public ForumInterface(ForumActivity forum) {
        this.forum = forum;
    }

    /**
     * convert HTML to BBCode
     */
    @JavascriptInterface
    public void convertHTML(final String username, final String messageID, String bbCode) {
        if (bbCode.contains("src=\"http://youtube.com/embed/")) {
            forum.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Theme.Snackbar(forum, R.string.toast_info_disabled_youtube);
                }
            });
        } else {
            bbCode = convertMessageQuote(bbCode);
            bbCode = convertUserQuote(bbCode);
            bbCode = convertQuote(bbCode);
            bbCode = convertQuoteSpoiler(bbCode);
            bbCode = convertSpoiler(bbCode);
            bbCode = bbCode.replace(" target=\"_blank", ""); //image trash
            bbCode = bbCode.replaceAll("<b>((.|\\n)+?)</b>", "[b]$1[/b]"); //Bold text
            bbCode = bbCode.replaceAll("<i>((.|\\n)+?)</i>", "[i]$1[/i]"); //Italics
            bbCode = bbCode.replaceAll("<u>((.|\\n)+?)</u>", "[u]$1[/u]"); //Underlined text
            bbCode = bbCode.replaceAll("<ul>((.|\\n)+?)</ul>", "[list]$1[/list]"); //list
            bbCode = bbCode.replaceAll("<ol>((.|\\n)+?)</ol>", "[list=1]$1[/list]"); //list
            bbCode = bbCode.replaceAll("<li>((.|\\n)+?)</li>", "[*]$1"); //list items
            bbCode = bbCode.replaceAll("<pre>((.|\\n)+?)</pre>", "$1"); //unknown
            bbCode = bbCode.replaceAll("<a (.+?)>\\[b]@(.+?)\\[/b](.+?)</a>", "@$2"); //@ mention
            bbCode = bbCode.replaceAll("<span style=\"text-decoration:line-through;\">((.|\\n)+?)</span>", "[s]$1[/s]"); //Strike-thru text
            bbCode = bbCode.replaceAll("<span style=\"font-size: (\\d+?)%;\">((.|\\n)+?)</span>", "[size=$1]$2[/size]"); //resized text
            bbCode = bbCode.replaceAll("<span style=\"color: (\\w+?)\">((.|\\n)+?)</span>", "[color=$1]$2[/color]"); //colored text
            bbCode = bbCode.replaceAll("<div style=\"text-align: center;\">((.|\\n)+?)</div>", "[center]$1[/center]"); //centered text
            bbCode = bbCode.replaceAll("<div style=\"text-align: right;\">((.|\\n)+?)</div>", "[right]$1[/right]"); //right text
            bbCode = bbCode.replaceAll("<a href=\"(.+?)\" rel=\"nofollow\">((.|\\n)+?)</a>", "[url=$1]$2[/url]"); //Text link
            bbCode = bbCode.replaceAll("<img class=\"userimg\" src=\"(.+?)\">", "[img]$1[/img]"); //image
            bbCode = bbCode.replaceAll("<img class=\"userimg img-a-l\" src=\"(.+?)\">", "[img align=left]$1[/img]"); //image left
            bbCode = bbCode.replaceAll("<img class=\"userimg img-a-r\" src=\"(.+?)\">", "[img align=right]$1[/img]"); //image right
            bbCode = bbCode.replaceAll("<div class=\"codetext\">((.|\\n)+?)<div>", "[code]$[/code]"); //code

            final String finalBbCode = bbCode.replaceAll("<br>", "\n"); //new line
            if (finalBbCode.contains("<div")) {
                Theme.Snackbar(forum, R.string.toast_error_convert);
            } else {
                forum.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        forum.webview.loadUrl("javascript:document.getElementById(\"textarea\").scrollIntoView();");
                        if (username.equalsIgnoreCase(AccountService.getUsername())) {
                            forum.webview.loadUrl("javascript:document.getElementById(\"textarea\").setAttribute(\"name\", \"" + messageID + "\");");
                            forum.webview.loadUrl("javascript:updateTextarea(\"" + finalBbCode + "\");");
                        } else {
                            forum.webview.loadUrl("javascript:document.getElementById(\"textarea\").setAttribute(\"name\", \"0\");");
                            forum.webview.loadUrl("javascript:updateTextarea(\"[quote=" + username + " message=" + messageID + "]" + finalBbCode + "[/quote]\");");
                        }
                    }
                });
            }
        }
    }

    private int convertMessageQuote = 0;
    private String convertMessageQuote(String HTML) {
        convertMessageQuote = convertMessageQuote + 1;
        if (convertMessageQuote <= 8) {
            HTML = HTML.replaceAll("<div class=\"quotetext\"><strong><a href=\"/forum/message/(.+?)\\?goto=topic\">(.+?) said:</a></strong>((.|\\n)+?)</div>", "[quote=$2 message=$1]$3[/quote]"); //real quote
            if (HTML.contains("<div class=\"quotetext\"><strong><a href=\"/forum/message/"))
                return convertMessageQuote(HTML);
            convertMessageQuote = 0;
        }
        return HTML;
    }

    private int convertUserQuote = 0;
    private String convertUserQuote(String HTML) {
        convertUserQuote = convertUserQuote + 1;
        if (convertUserQuote <= 8) {
            HTML = HTML.replaceAll("<div class=\"quotetext\"><strong>(.+?) said:</strong>((.|\\n)+?)</div>", "[quote=$1]$2[/quote]"); //real quote
            if (HTML.contains("<div class=\"quotetext\"><strong>"))
                return convertUserQuote(HTML);
            convertUserQuote = 0;
        }
        return HTML;
    }

    private int convertQuote = 0;
    private String convertQuote(String HTML) {
        convertQuote = convertQuote + 1;
        if (convertQuote <= 8) {
            HTML = HTML.replaceAll("<div class=\"quotetext\">((.|\\n)+?)<div>", "[quote]$1[/quote]"); //quote
            if (HTML.contains("<div class=\"quotetext\">"))
                return convertQuote(HTML);
            convertQuote = 0;
        }
        return HTML;
    }

    private int convertQuoteSpoiler = 0;
    private String convertQuoteSpoiler(String HTML) {
        convertQuoteSpoiler = convertQuoteSpoiler + 1;
        if (convertQuoteSpoiler <= 8) {
            HTML = HTML.replaceAll("<input class=\"spoilerbutton\"(.+?)spoiler quotetext\"><strong>(.+?) said:</strong>((.|\\n)+?)</div>", "[spoiler][quote=$2]$3[/quote][/spoiler]"); //quote
            if (HTML.contains("spoiler quotetext\"><strong>"))
                return convertQuoteSpoiler(HTML);
            convertQuoteSpoiler = 0;
        }
        return HTML;
    }

    private int convertSpoiler = 0;
    private String convertSpoiler(String HTML) {
        convertSpoiler = convertSpoiler + 1;
        if (convertSpoiler <= 8) {
            HTML = HTML.replaceAll("<input class=\"spoilerbutton\"(.+?)spoiler quotetext\">((.|\\n)+?)</div>", "[spoiler]$2[/spoiler]"); //quote
            if (HTML.contains("spoiler quotetext\">"))
                return convertSpoiler(HTML);
            convertSpoiler = 0;
        }
        return HTML;
    }

    /**
     * Get the topics from a certain category.
     */
    @JavascriptInterface
    public void tileClick(final String id) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                forum.getRecords(ForumJob.CATEGORY, Integer.parseInt(id), "1");
            }
        });
    }

    /**
     * Get more pages certain category.
     */
    @JavascriptInterface
    public void topicList(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                forum.getRecords(ForumJob.CATEGORY, Integer.parseInt(details[1]), page);
            }
        });
    }

    /**
     * Get the topics from a certain category.
     */
    @JavascriptInterface
    public void subTileClick(final String id) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                forum.getRecords(ForumJob.SUBCATEGORY, Integer.parseInt(id), "1");
            }
        });
    }

    /**
     * Get the posts from a certain topic.
     */
    @JavascriptInterface
    public void topicClick(final String id) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                forum.getRecords(ForumJob.TOPIC, Integer.parseInt(id), "1");
            }
        });
    }

    /**
     * Get next topic page.
     */
    @JavascriptInterface
    public void nextTopicList(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                forum.getRecords(ForumJob.CATEGORY, Integer.parseInt(details[1]), page);
            }
        });
    }

    /**
     * Get topic comment page.
     */
    @JavascriptInterface
    public void prevTopicList(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                forum.getRecords(ForumJob.CATEGORY, Integer.parseInt(details[1]), page);
            }
        });
    }

    /**
     * Send a comment.
     */
    @JavascriptInterface
    public void sendComment(final String comment, final String messageID) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (comment.length() > 16) {
                    forum.setLoading(true);
                    final String[] details = forum.webview.getTitle().split(" ");


                    ChooseDialogFragment lcdf = new ChooseDialogFragment();
                    Bundle bundle = new Bundle();
                    if (messageID.equals("0")) {
                        bundle.putString("title", forum.getString(R.string.dialog_title_add_comment));
                        bundle.putString("message", forum.getString(R.string.dialog_message_add_comment));
                    } else {
                        bundle.putString("title", forum.getString(R.string.dialog_title_edit_comment));
                        bundle.putString("message", forum.getString(R.string.dialog_message_edit_comment));
                    }
                    bundle.putString("positive", forum.getString(android.R.string.yes));
                    lcdf.setArguments(bundle);
                    lcdf.setCallback(new ChooseDialogFragment.onClickListener() {
                        @Override
                        public void onPositiveButtonClicked() {
                            if (messageID.equals("0"))
                                new ForumNetworkTask(forum, forum, ForumJob.ADDCOMMENT, Integer.parseInt(details[1])).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, comment, details[3]);
                            else
                                new ForumNetworkTask(forum, forum, ForumJob.UPDATECOMMENT, Integer.parseInt(messageID)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, comment, details[3]);
                        }
                    });
                    lcdf.show(forum.getFragmentManager(), "fragment_sendComment");
                } else {
                    Theme.Snackbar(forum, R.string.toast_info_comment);
                }
            }
        });
    }

    /**
     * Get next comment page.
     */
    @JavascriptInterface
    public void nextCommentList(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                forum.getRecords(ForumJob.TOPIC, Integer.parseInt(details[1]), page);
            }
        });
    }

    /**
     * Get comment page.
     */
    @JavascriptInterface
    public void pagePicker(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                Bundle bundle = new Bundle();
                bundle.putInt("id", Integer.parseInt(details[1]));
                bundle.putString("title", forum.getString(R.string.Page_number));
                bundle.putInt("current", Integer.parseInt(page));
                bundle.putInt("max", Integer.parseInt(details[2]));
                bundle.putInt("min", 1);
                FragmentManager fm = forum.getFragmentManager();
                NumberPickerDialogFragment dialogFragment = new NumberPickerDialogFragment().setOnSendClickListener(forum);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(fm, "fragment_page");
            }
        });
    }

    /**
     * Get previous comment page.
     */
    @JavascriptInterface
    public void prevCommentList(final String page) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] details = forum.webview.getTitle().split(" ");
                forum.getRecords(ForumJob.TOPIC, Integer.parseInt(details[1]), page);
            }
        });
    }

    /**
     * Open the userprofile.
     */
    @JavascriptInterface
    public void profileClick(final String username) {
        forum.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent Profile = new Intent(forum, ProfileActivity.class);
                Profile.putExtra("username", username);
                forum.startActivity(Profile);
            }
        });
    }
}