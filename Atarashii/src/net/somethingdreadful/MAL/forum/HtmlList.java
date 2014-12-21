package net.somethingdreadful.MAL.forum;

import android.content.Context;

import net.somethingdreadful.MAL.MALDateTools;
import net.somethingdreadful.MAL.api.response.Forum;

import java.util.ArrayList;

public class HtmlList {
    static String begin =
            "<!DOCTYPE HTML>" +
                    "<html>" +
                    "  <head>" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "    <script type=\"text/javascript\">\n" +
                    "      function clicked(id, position) {" +
                    "        Posts.clicked(id, position);" +
                    "      }" +
                    "    </script>" +
                    "    <style type=\"text/css\">" +
                    "      html, body {" +
                    "        margin: 0;" +
                    "        padding: 0;" +
                    "        font-family: \"Roboto Medium\", sans-serif;" +
                    "        font-size: 10.5pt;" +
                    "      }" +
                    "      div.post {" +
                    "        background-color: #FAFAFA;" +
                    "        border-bottom: 1px solid #D2D2D2;" +
                    "        padding: 0 16px;" +
                    "      }" +
                    "      div.post:active {" +
                    "        background-color: #D9D9D9;" +
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
                    "      div.head div.title {" +
                    "        color: #444444;" +
                    "        margin-top: 5px;" +
                    "      }" +
                    "      div.head div.subheader {" +
                    "        color: #999999;" +
                    "        margin-top: 1px;" +
                    "      }" +
                    "      div.content {" +
                    "        padding-bottom: 16px;" +
                    "      }" +
                    "    </style>" +
                    "  </head>" +
                    "  <body>";
    static String postLayout =
            "<!-- begin post html -->" +
                    "    <div class=\"post\" onClick=\"clicked('itemID', 'position')\" >" +
                    "      <div class=\"head\">" +
                    "        <img class=\"avatar\" src=\"image\"/>" +
                    "        <div class=\"title\">Title</div>" +
                    "        <div class=\"subheader\">Subhead</div>" +
                    "      </div>" +
                    "      <div class=\"content\">" +
                    "        <!-- place post content here -->" +
                    "      </div>" +
                    "    </div>" +
                    "    <!-- end post html -->";
    static String end =
            "  </body>" +
            "</html>";

    /**
     * Convert a forum array into a HTML list.
     *
     * @param list The list that should be converted in a HTML list
     * @param context The application context to format the dates
     * @return String The HTML source
     */
    public static String convertList(ArrayList<Forum> list, Context context) {
        String result = "";
        for (int i = 0; i < list.size(); i++) {
            Forum post = list.get(i);
            String postreal = postLayout;
            String comment = post.getComment();

            comment = comment.replace("data-src=", "width=\"100%\" src=");
            comment = comment.replace("img src=", "img width=\"100%\" src=");

            postreal = postreal.replace("image", post.getProfile().getAvatarUrl() != null ? post.getProfile().getAvatarUrl() : "http://cdn.myanimelist.net/images/na.gif");
            postreal = postreal.replace("Title", post.getUsername());
            postreal = postreal.replace("itemID", Integer.toString(post.getId()));
            postreal = postreal.replace("position", Integer.toString(i));
            postreal = postreal.replace("Subhead", MALDateTools.formatDateString(post.getTime(), context, true));
            postreal = postreal.replace("<!-- place post content here -->", comment);

            result = result + postreal;
        }
        return begin + result + end;
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
        comment = comment.replace("<!--link--><a href=\"", "[url=").replace(" rel=\"nofollow]", "\"]").replace("</a>", "[/url]");           // Hyperlink
        comment = comment.replace("<!--quote--><div class=\"quotetext]\n[b]", "[quote=").replace(" said:[/b]<!--quotesaid-->", "]");        // Quote
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
}
