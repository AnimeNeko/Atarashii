package net.somethingdreadful.MAL.forum;

import android.content.Context;

import net.somethingdreadful.MAL.DateTools;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.api.BaseModels.History;
import net.somethingdreadful.MAL.api.BaseModels.Profile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HtmlUtil {
    private final Context context;
    private String structure;
    private final String postStructure;
    private String spoilerStructure;
    private String pageString;

    public HtmlUtil(Context context) {
        structure = getString(context, R.raw.forum_post_structure);
        postStructure = getString(context, R.raw.forum_post_post_structure);
        spoilerStructure = getString(context, R.raw.forum_comment_spoiler_structure);
        this.context = context;
    }

    /**
     * Creates from the given data the list.
     *
     * @param result   The post list
     * @param maxpages The maximum amount of pages
     * @param page     The current page number
     * @return String The html source
     * <p/>
     * note: if the maxpages equals -1 it will show a questionmark instead a pageString message.
     */
    private String buildList(String result, int maxpages, Integer page) {
        String list = structure.replace("<!-- insert here the posts -->", rebuildSpoiler(result));
        if (page == 1)
            list = list.replace("class=\"item\" value=\"1\"", "class=\"item hidden\" value=\"1\"");
        if (maxpages == 0 || page == maxpages)
            list = list.replace("class=\"item\" value=\"2\"", "class=\"item hidden\" value=\"2\"");
        if (maxpages == 0) {
            list = list.replace("(page/pages)", pageString);
        } else {
            list = list.replace("pages", maxpages == -1 ? "?" : String.valueOf(maxpages));
            list = list.replace("page", String.valueOf(page));
        }

        if (Theme.darkTheme) {
            list = list.replace("#D2D2D2", "#212121"); // divider
            list = list.replace("#FAFAFA", "#313131"); // post
            list = list.replace("#444444", "#E3E3E3"); // title text
            list = list.replace("class=\"content\"", "class=\"content\" style=\"color:#E3E3E3\""); // post text
        }

        return list;
    }

    /**
     * convert a HTML comment into a BBCode comment.
     *
     * @param comment The HTML comment
     * @return String The BBCode comment
     */
    public String convertMALComment(String comment) {
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
        comment = comment.replaceAll("<div class=\"spoiler](.+?)value=\"Hide spoiler]", "[spoiler]");                                       // Spoiler
        comment = comment.replace("<!--spoiler--></span>", "[/spoiler]");
        comment = comment.replaceAll("<iframe class=\"movie youtube\"(.+?)/embed/", "[yt]");                                                // Youtube
        comment = comment.replace("?rel=1]</iframe>", "[/yt]");
        comment = comment.replace("<a href=\"", "[url=").replace("target=\"_blank ", "").replace("</a>", "[/url]");                         // Hyperlink
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
        comment = comment.replace("<!--link-->", "");

        return comment;
    }

    /**
     * Rebuild the spoiler to work again.
     *
     * @param html The html source where this method should fix the spoilers
     * @return String The source with working spoilers
     */
    private String rebuildSpoiler(String html) {
        html = html.replaceAll("<div class=\"spoiler\">(\\s.+?)value=\"Hide spoiler\">", spoilerStructure);
        html = html.replace("<!--spoiler--></span>", "");
        return html;
    }

    /**
     * Get the string of the given resource file.
     *
     * @param context  The application context
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

    /**
     * Convert a user activity array into a HTML list.
     *
     * @param record The UserActivity object that contains the list which should be converted in a HTML list
     * @param page   The page number
     * @return String The HTML source
     */
    public String convertList(Profile record, int page) {
        ArrayList<History> list = record.getActivity();
        String result = "";
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                History post = list.get(i);
                String postreal = postStructure;

                String comment = "";
                String image = "";
                String title = "";
                switch (post.getActivityType()) {
                    case "message":
                    case "text":
                        comment = post.getValue();
                        image = post.getUsers().get(0).getImageUrl();
                        title = post.getUsers().get(0).getUsername();
                        break;
                    case "list":
                        if (post.getSeries() != null) {
                            comment = post.getUsers().get(0).getUsername() + " " + post.getStatus() + " " + post.getValue() + " of " + post.getSeries().getTitle();
                            image = post.getSeries().getImageUrl();
                            title = post.getSeries().getTitle();
                        }
                        break;
                }

                comment = comment.replace("data-src=", "width=\"100%\" src=");
                comment = comment.replace("img src=", "img width=\"100%\" src=");
                postreal = postreal.replace("image", image);
                postreal = postreal.replace("Title", title);
                postreal = postreal.replace("itemID", String.valueOf(post.getId()));
                postreal = postreal.replace("position", String.valueOf(i));
                postreal = postreal.replace("Subhead", DateTools.parseDate(post.getCreatedAt(), true));
                postreal = postreal.replace("<!-- place post content here -->", comment);

                result = result + postreal;
            }
        }
        pageString = context.getString(R.string.no_history);
        return buildList(result, 0, page);
    }
}
