package net.somethingdreadful.MAL.forum;

import android.content.Context;

import net.somethingdreadful.MAL.MALDateTools;
import net.somethingdreadful.MAL.api.response.Forum;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.api.response.User;

import java.util.ArrayList;

public class HtmlList {
    static String begin =
            "<!DOCTYPE HTML>" +
                    "<html>" +
                    "  <head>" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "    <script type=\"text/javascript\">\n" +
                    "      function edit(id, position) {Posts.edit(id, position);}" +
                    "      function viewProfile(position) {Posts.viewProfile(position);}" +
                    "      function first() {Posts.first();}" +
                    "      function previous() {Posts.previous();}" +
                    "      function next() {Posts.next();}" +
                    "      function last() {Posts.last();}" +
                    "    </script>" +
                    "    <style type=\"text/css\">" +
                    "      html, body {" +
                    "        background-color: #FAFAFA;" +
                    "        margin: 0;" +
                    "        padding: 0;" +
                    "        font-family: \"Roboto Medium\", sans-serif;" +
                    "        font-size: 10.5pt;" +
                    "      }" +
                    "      div.post {" +
                    "        border-bottom: 1px solid #D2D2D2;" +
                    "        padding: 0 16px;" +
                    "      }" +
                    "      div.head {" +
                    "        width: 100%;" +
                    "        height: 72px;" +
                    "        padding: 16px 0;" +
                    "        box-sizing: border-box;" +
                    "        font-weight: bolder;" +
                    "      }" +
                    "      div.head img.avatar{" +
                    "        float: left;" +
                    "        height: 40px;" +
                    "        width: 40px;" +
                    "        border-radius: 50%;" +
                    "        background-color: #999999;" +
                    "        margin-right: 12px;" +
                    "      }" +
                    "      div.head div.title {color: #444444; margin-top: 5px;}" +
                    "      div.head div.developer {color: #0096C8; margin-top: 5px;}" +
                    "      div.head div.staff {color: #F44336; margin-top: 5px;}" +
                    "      div.head img.edit {" +
                    "        float: right;" +
                    "        height: 36px;" +
                    "        width: 36px;" +
                    "        margin-top: 2px;" +
                    "      }" +
                    "      div.head div.subheader {color: #999999; margin-top: 1px;}" +
                    "      div.content {padding-bottom: 16px;}" +
                    "      div.content div.quotetext {" +
                    "        background-color: rgba(0,150,200,0.1); " +
                    "        padding:5px; " +
                    "        padding-left:10px; " +
                    "        padding-right:10px;" +
                    "        margin-top: 5px;" +
                    "        margin-bottom: 5px;" +
                    "      }" +
                    "      div.content input.spoilerbutton {display:block;margin:5px 0;}" +
                    "      div.content input.spoilerbutton[value=\"Show spoiler\"] + .spoiler {display: none;}" +
                    "      div.content input.spoilerbutton[value=\"Hide spoiler\"] + .spoiler {display: show;}" +
                    "      div.footer {display: inline-block; margin-top: 4px;}" +
                    "      div.footer img.item:active {background-color: #D9D9D9;}" +
                    "      div.footer img.item {max-height: 48px; padding-left: 4px; padding-right: 4px; float:left;}" +
                    "      div.footer img.hidden {visibility: hidden;}" +
                    "      div.footer div.title {color: #757575; font-size:1.9em; margin-top: 5px; float:left; padding-right: 4px; float:left;}" +
                    "    </style>" +
                    "  </head>" +
                    "  <body>";
    static String postLayout =
            "<!-- begin post html -->" +
                    "    <div class=\"post\" >" +
                    "      <div class=\"head\">" +
                    "        <img class=\"avatar\" onClick=\"viewProfile('position')\" src=\"image\"/>" +
                    "        <!-- special right methods -->" +
                    "        <div class=\"title\">Title</div>" +
                    "        <div class=\"subheader\">Subhead</div>" +
                    "      </div>" +
                    "      <div class=\"content\">" +
                    "        <!-- place post content here -->" +
                    "      </div>" +
                    "    </div>" +
                    "    <!-- end post html -->";
    static String end =
            "<center>" +
                    "      <div class=\"footer\">" +
                    "        <img class=\"item\" value=\"1\" onClick=\"first()\" src=\"http://i.imgur.com/hBB2zQG.png\"/>" +
                    "        <img class=\"item\" value=\"1\" onClick=\"previous()\"  src=\"http://i.imgur.com/YTiJFJz.png\"/>" +
                    "        <div class=\"title\">(page/pages)</div>" +
                    "        <img class=\"item\" value=\"2\" onClick=\"next()\"  src=\"http://i.imgur.com/fPDlVme.png\"/>" +
                    "        <img class=\"item\" value=\"2\" onClick=\"last()\"  src=\"http://i.imgur.com/AqtBsCO.png\"/>" +
                    "      </div>" +
                    "    </center>" +
                    "  </body>" +
                    "</html>";
    static String spoiler = "<input class=\"spoilerbutton\" type=\"button\" value=\"Show spoiler\" onclick=\"this.value=this.value=='Show spoiler'?'Hide spoiler':'Show spoiler';\">" +
            "<div class=\"spoiler quotetext\">";

    /**
     * Convert a forum array into a HTML list.
     *
     * @param record The ForumMain object that contains the list which should be converted in a HTML list
     * @param context The application context to format the dates
     * @param username The username of the user, this is used for special rights
     * @return String The HTML source
     */
    public static String convertList(ForumMain record, Context context, String username, int page) {
        ArrayList<Forum> list = record.getList();
        String result = "";
        for (int i = 0; i < list.size(); i++) {
            Forum post = list.get(i);
            String postreal = postLayout;
            String comment = post.getComment();

            comment = comment.replace("data-src=", "width=\"100%\" src=");
            comment = comment.replace("img src=", "img width=\"100%\" src=");

            if (post.getUsername().equals(username))
                postreal = postreal.replace("<!-- special right methods -->", "<img class=\"edit\" onClick=\"edit('itemID', 'position')\" src=\"http://i.imgur.com/uZ0TbNv.png\"/>");
            if (User.isDeveloperRecord(post.getUsername()))
                postreal = postreal.replace("=\"title\">", "=\"developer\">");
            if (!post.getProfile().getDetails().getAccessRank().equals("Member"))
                postreal = postreal.replace("=\"title\">", "=\"staff\">");
            postreal = postreal.replace("image", post.getProfile().getAvatarUrl() != null ? post.getProfile().getAvatarUrl() : "http://cdn.myanimelist.net/images/na.gif");
            postreal = postreal.replace("Title", post.getUsername());
            postreal = postreal.replace("itemID", Integer.toString(post.getId()));
            postreal = postreal.replace("position", Integer.toString(i));
            postreal = postreal.replace("Subhead", MALDateTools.formatDateString(post.getTime(), context, true));
            postreal = postreal.replace("<!-- place post content here -->", comment);

            result = result + postreal;
        }
        return begin + rebuildSpoiler(result) + buildFooter(record, page);
    }

    /**
     * convert a HTML comment into a BBCode comment.
     *
     * @param comment The HTML comment
     * @return String The BBCode comment
     */
    public static String convertComment(String comment) {
        comment = comment.replace("\">", "]");
        comment = comment.replace("\n", "");

        comment = comment.replace("<br>", "\n");                                                                                            // Empty line
        comment = comment.replace("data-src=", "src=").replace(" class=\"userimg\"", "");                                                   // Image
        comment = comment.replace("<img src=\"", "[img]").replace("\" border=\"0\" />", "[/img]");
        comment = comment.replace(".png]", ".png[/img]").replace(".jpg]", ".jpg[/img]").replace(".gif]", ".gif[/img]");
        comment = comment.replace("<span style=\"color:", "[color=").replace("<!--color-->", "[/color]");                                   // Text color
        comment = comment.replace("<span style=\"font-size: ", "[size=").replace("%;", "").replace("<!--size-->", "[/size]");               // Text size
        comment = comment.replace("<strong>", "[b]").replace("</strong>", "[/b]");                                                          // Text bold
        comment = comment.replace("<u>", "[u]").replace("</u>", "[/u]");                                                                    // Text underline
        comment = comment.replace("<div style=\"text-align: center;]", "[center]").replace("<!--center-->", "[/center]");                   // Center
        comment = convertSpoiler(comment);                                                                                                  // Spoiler
        comment = comment.replace("<!--link--><a href=\"", "[url=").replace("\" rel=\"nofollow]", "]").replace("</a>", "[/url]");           // Hyperlink
        comment = comment.replace("<!--quote--><div class=\"quotetext][b]", "[quote=").replace(" said:[/b]<!--quotesaid-->", "]");        // Quote
        comment = comment.replace("<!--quote-->", "[/quote]");
        comment = comment.replace("<ol>", "[list]").replace("</ol>", "[/list]").replace("<li>", "[*]");                                     // List
        comment = comment.replace("<span style=\"text-decoration:line-through;]", "[s]").replace("<!--strike-->", "[/s]");                  // Text strike
        comment = comment.replace("<em>", "[i]").replace("</em>", "[/i]");                                                                  // Text Italic

        comment = comment.replace("\n&#13;", "");
        comment = comment.replace("&amp;", "&");
        comment = comment.replace("</div>", "");
        comment = comment.replace("</span>", "");
        comment = comment.replace("</li>", "");

        return comment;
    }

    /**
     * Converts the spoiler HTML code into the BBCode.
     *
     * @param text The text to search for a spoiler
     * @return String The text with the BBCode spoiler
     */
    public static String convertSpoiler(String text) {
        text = text.replace("<div class=\"spoiler]", "");
        text = text.replace("<input type=\"button\" class=\"button\"", "");
        text = text.replace(" onclick=\"this.nextSibling.nextSibling.style.display='block';", "");
        text = text.replace("this.style.display='none';\"", "");
        text = text.replace(" value=\"Show spoiler]", "");
        text = text.replace("<span class=\"spoiler_content\" style=\"display:none]", "");
        text = text.replace("<input type=\"button\" class=\"button\" ", "");
        text = text.replace("onclick=\"this.parentNode.style.display='none';", "");
        text = text.replace("this.parentNode.parentNode.childNodes[0].style.display='block';\" ", "");
        text = text.replace("value=\"Hide spoiler]", "[spoiler]");
        text = text.replace("<!--spoiler--></span>", "[/spoiler]");
        return text;
    }

    /**
     * Rebuild the spoiler to work again.
     *
     * @param html The html source where this method should fix the spoilers
     * @return String The source with working spoilers
     */
    private static String rebuildSpoiler(String html) {
        html = html.replace("<div class=\"spoiler\">", "");
        html = html.replace("<input type=\"button\" class=\"button\"", "");
        html = html.replace(" onclick=\"this.nextSibling.nextSibling.style.display='block';", "");
        html = html.replace("this.style.display='none';\"", "");
        html = html.replace(" value=\"Show spoiler\">", "");
        html = html.replace("<span class=\"spoiler_content\" style=\"display:none\">", "");
        html = html.replace("<input type=\"button\" class=\"button\" ", "");
        html = html.replace(" onclick=\"this.parentNode.style.display='none';", "");
        html = html.replace("this.parentNode.parentNode.childNodes[0].style.display='block';\" ", "");
        html = html.replace("value=\"Hide spoiler\"><br>", spoiler);
        html = html.replace("<!--spoiler--></span>", "");
        return html;
    }

    /**
     * Create the footer.
     *
     * @param record The ForumMain object that contains the pagenumbers
     * @param page The current page number
     * @return String The html source
     */
    private static String buildFooter(ForumMain record, Integer page) {
        String html = end;
        if (page == 1)
            html = html.replace("class=\"item\" value=\"1\"", "class=\"item hidden\" value=\"1\"");
        if (page == record.getPages())
            html = html.replace("class=\"item\" value=\"2\"", "class=\"item hidden\" value=\"2\"");
        html = html.replace("pages", Integer.toString(record.getPages()));
        html = html.replace("page", Integer.toString(page));
        return html;
    }
}
