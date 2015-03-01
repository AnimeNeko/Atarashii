package net.somethingdreadful.MAL.forum;

import android.content.Context;

import net.somethingdreadful.MAL.MALDateTools;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.api.response.Forum;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.api.response.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HtmlUtil {
    String structure;
    String postStructure;
    String spoilerStructure;

    public HtmlUtil(Context context) {
        structure = getString(context, R.raw.forum_post_structure);
        postStructure = getString(context, R.raw.forum_post_post_structure);
        spoilerStructure = getString(context, R.raw.forum_post_spoiler_structure);
    }

    /**
     * Convert a forum array into a HTML list.
     *
     * @param record The ForumMain object that contains the list which should be converted in a HTML list
     * @param context The application context to format the dates
     * @param username The username of the user, this is used for special rights
     * @return String The HTML source
     */
    public String convertList(ForumMain record, Context context, String username, int page) {
        ArrayList<Forum> list = record.getList();
        String result = "";
        for (int i = 0; i < list.size(); i++) {
            Forum post = list.get(i);
            String postreal = postStructure;
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
        return buildList(result, record, page);
    }

    /**
     * Creates from the given data the list.
     *
     * @param result The post list
     * @param record The ForumMain object that contains the pagenumbers
     * @param page The current page number
     * @return String The html source
     */
    private String buildList(String result, ForumMain record, Integer page){
        String list = structure.replace("<!-- insert here the posts -->", rebuildSpoiler(result));
        if (page == 1)
            list = list.replace("class=\"item\" value=\"1\"", "class=\"item hidden\" value=\"1\"");
        if (page == record.getPages())
            list = list.replace("class=\"item\" value=\"2\"", "class=\"item hidden\" value=\"2\"");
        list = list.replace("pages", Integer.toString(record.getPages()));
        list = list.replace("page", Integer.toString(page));
        return list;
    }

    /**
     * convert a HTML comment into a BBCode comment.
     *
     * @param comment The HTML comment
     * @return String The BBCode comment
     */
    public String convertComment(String comment) {
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
        comment = comment.replace("<!--quote--><div class=\"quotetext][b]", "[quote=").replace(" said:[/b]<!--quotesaid-->", "]");          // Quote
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
    private String convertSpoiler(String text) {
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
    private String rebuildSpoiler(String html) {
        html = html.replace("<div class=\"spoiler\">", "");
        html = html.replace("<input type=\"button\" class=\"button\"", "");
        html = html.replace(" onclick=\"this.nextSibling.nextSibling.style.display='block';", "");
        html = html.replace("this.style.display='none';\"", "");
        html = html.replace(" value=\"Show spoiler\">", "");
        html = html.replace("<span class=\"spoiler_content\" style=\"display:none\">", "");
        html = html.replace("<input type=\"button\" class=\"button\" ", "");
        html = html.replace(" onclick=\"this.parentNode.style.display='none';", "");
        html = html.replace("this.parentNode.parentNode.childNodes[0].style.display='block';\" ", "");
        html = html.replace("value=\"Hide spoiler\"><br>", spoilerStructure);
        html = html.replace("<!--spoiler--></span>", "");
        return html;
    }

    /**
     * Get the string of the given resource file.
     *
     * @param context The application context
     * @param resource The resource of which string we need
     * @return String the wanted string
     */
    @SuppressWarnings("StatementWithEmptyBody")
    private String getString(Context context, int resource) {
        try {
            InputStream inputStream = context.getResources().openRawResource(resource);
            byte[] buffer = new byte[inputStream.available()];
            while (inputStream.read(buffer) != -1) ;
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
