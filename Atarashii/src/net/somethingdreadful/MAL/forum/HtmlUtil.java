package net.somethingdreadful.MAL.forum;

import android.content.Context;

import net.somethingdreadful.MAL.DateTools;
import net.somethingdreadful.MAL.R;
import net.somethingdreadful.MAL.Theme;
import net.somethingdreadful.MAL.account.AccountService;
import net.somethingdreadful.MAL.api.BaseModels.AnimeManga.Reviews;
import net.somethingdreadful.MAL.api.BaseModels.History;
import net.somethingdreadful.MAL.api.BaseModels.Profile;
import net.somethingdreadful.MAL.api.MALModels.Forum;
import net.somethingdreadful.MAL.api.MALModels.ForumMain;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class HtmlUtil {
    private Context context;
    String structure;
    String postStructure;
    String spoilerStructure;
    String pageString;

    public HtmlUtil(Context context) {
        structure = getString(context, R.raw.forum_post_structure);
        postStructure = getString(context, R.raw.forum_post_post_structure);
        spoilerStructure = getString(context, R.raw.forum_post_spoiler_structure);
        this.context = context;
    }

    /**
     * Change the spoiler text with resource id's
     *
     * @param hide String resource id
     * @param show String resource id
     */
    private void setSpoilerText(int hide, int show){
        spoilerStructure = spoilerStructure
                .replace("Hide spoiler", context.getString(hide).toUpperCase())
                .replace("Show spoiler", context.getString(show).toUpperCase());
        structure = structure
                .replace("Hide spoiler", context.getString(hide).toUpperCase())
                .replace("Show spoiler", context.getString(show).toUpperCase());
    }

    /**
     * Creates from the given data the list.
     *
     * @param result The post list
     * @param maxpages The maximum amount of pages
     * @param page   The current page number
     * @return String The html source
     *
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
            list = list.replace("pages", maxpages == -1 ? "?" : Integer.toString(maxpages));
            list = list.replace("page", Integer.toString(page));
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
     * convert a AL comment into a HTML comment.
     *
     * @param comment The HTML comment
     * @return String The BBCode comment
     */
    public String convertALComment(String comment) {
        comment = comment.replace("\n", "<br>");                                                                                            // New line

        comment = comment.replace("~~~img(", "<img width=\"100%\" src=\"").replace(")~~~", "\">");                                          // Image
        comment = comment.replaceAll("~~~(.+?)~~~", "<center>$1</center>");                                                                 // Center
        comment = comment.replaceAll("__(.+?)__", "<b>$1</b>");                                                                             // Text bold
        comment = comment.replaceAll("_(.+?)_", "<em>$1</em>");                                                                             // Text Italic
        comment = comment.replaceAll("~~(.+?)~~", "<em>$1</em>");                                                                           // Text strike

        String[] spaces = comment.split("<br><br>");
        int length = 0;
        for (String line : spaces) {
            length = length + line.length() + 8;
            if (length > 300) {
                comment = comment.substring(0, length - 4) + spoilerStructure + comment.substring(length, comment.length());
                break;
            }
        }
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
     * @param page The page number
     * @return String The HTML source
     */
    public String convertList(User record, int page) {
        ArrayList<History> list = record.getActivity();
        String result = "";
        if (list != null) {
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
                        title = post.getUsers().get(0).getDisplayName();
                        break;
                    case "list":
                        if (post.getSeries() != null) {
                            comment = post.getUsers().get(0).getDisplayName() + " " + post.getStatus() + " " + post.getValue() + " of " + post.getSeries().getTitleRomaji();
                            image = post.getSeries().getImageUrlLge();
                            title = post.getSeries().getTitleRomaji();
                        }
                        break;
                }

                comment = comment.replace("data-src=", "width=\"100%\" src=");
                comment = comment.replace("img src=", "img width=\"100%\" src=");
                postreal = postreal.replace("image", image);
                postreal = postreal.replace("Title", title);
                postreal = postreal.replace("itemID", Integer.toString(post.getId()));
                postreal = postreal.replace("position", Integer.toString(i));
                postreal = postreal.replace("Subhead", DateTools.parseDate(post.getCreatedAt(), true));
                postreal = postreal.replace("<!-- place post content here -->", comment);

                result = result + postreal;
            }
        }
        pageString = context.getString(R.string.no_history);
        return buildList(result, 0, page);
    }

    /**
     * Convert a forum array into a HTML list.
     *
     * @param record   The ForumMain object that contains the list which should be converted in a HTML list
     * @param username The username of the user, this is used for special rights
     * @return String The HTML source
     */
    public String convertList(ForumMain record, String username, int page) {
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
            else
                postreal = postreal.replace("<!-- special right methods -->", "<img class=\"edit\" onClick=\"quote('itemID', 'position')\" src=\"http://i.imgur.com/yYtLVTV.png\"/>");
            if (User.isDeveloperRecord(post.getUsername()))
                postreal = postreal.replace("=\"title\">", "=\"developer\">");
            if (!post.getProfile().getDetails().getAccessRank().equals("Member"))
                postreal = postreal.replace("=\"title\">", "=\"staff\">");
            postreal = postreal.replace("image", post.getProfile().getAvatarUrl() != null ? post.getProfile().getAvatarUrl() : "http://cdn.myanimelist.net/images/na.gif");
            postreal = postreal.replace("Title", post.getUsername());
            postreal = postreal.replace("itemID", Integer.toString(post.getId()));
            postreal = postreal.replace("position", Integer.toString(i));
            postreal = postreal.replace("Subhead", DateTools.parseDate(post.getTime(), true));
            postreal = postreal.replace("<!-- place post content here -->", comment);

            result = result + postreal;
        }
        pageString = context.getString(R.string.no_activity);
        return buildList(result, record.getPages(), page);
    }

    /**
     * Convert a forum array into a HTML list.
     *
     * @param record   The ForumMain object that contains the list which should be converted in a HTML list
     * @return String The HTML source
     */
    public String convertList(ArrayList<Reviews> record, int page) {
        setSpoilerText(R.string.show_less, R.string.read_more);
        String result = "";
        for (int i = 0; i < record.size(); i++) {
            Reviews review = record.get(i);
            String reviewreal = postStructure;
            String comment = review.getReview().replace("<span style=\"display: none;\"", spoilerStructure + "<span ") + "</div></input>";
            comment = AccountService.isMAL() ? comment : convertALComment(comment);

            if (User.isDeveloperRecord(review.getUsername()))
                reviewreal = reviewreal.replace("=\"title\">", "=\"developer\">");
            reviewreal = reviewreal.replace("image", review.getAvatarUrl() != null ? review.getAvatarUrl() : "http://cdn.myanimelist.net/images/na.gif");
            reviewreal = reviewreal.replace("Title", review.getUsername());
            reviewreal = reviewreal.replace("itemID", Integer.toString(i));
            reviewreal = reviewreal.replace("position", Integer.toString(i));
            reviewreal = reviewreal.replace("Subhead", DateTools.parseDate(review.getDate(), !AccountService.isMAL()));
            reviewreal = reviewreal.replace("<!-- place post content here -->", comment);

            result = result + reviewreal;
        }
        pageString = context.getString(R.string.no_reviews);

        int maxPage;
        if (record.size() == 0)
            maxPage = 0;
        else if (record.size() < 20 && AccountService.isMAL())
            maxPage = page;
        else
            maxPage = AccountService.isMAL() ? -1 : 0;

        return buildList(result, maxPage, page);
    }
}
