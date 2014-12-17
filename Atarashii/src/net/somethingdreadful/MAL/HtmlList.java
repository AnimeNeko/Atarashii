package net.somethingdreadful.MAL;

import android.content.Context;

import net.somethingdreadful.MAL.api.response.Forum;

import java.util.ArrayList;

public class HtmlList {

    public static String HtmlList(ArrayList<Forum> list, Context context) {
        String begin = 
                "<!DOCTYPE HTML>" +
                "<html>" +
                "  <head>" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
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

        String postLayout =
                "<!-- begin post html -->" +
                "    <div class=\"post\">" +
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

        String end = "" +
                "  </body>" +
                "</html>";

        String result = "";
        for (Forum post : list){
            String postreal = postLayout;
            String comment = post.getComment();

            comment = comment.replace("data-src=", "width=\"100%\" src=");

            postreal = postreal.replace("image", post.getProfile().getAvatarUrl() != null ? post.getProfile().getAvatarUrl() : "http://cdn.myanimelist.net/images/na.gif");
            postreal = postreal.replace("Title", post.getUsername());
            postreal = postreal.replace("Subhead", MALDateTools.formatDateString(post.getTime(), context, true));
            postreal = postreal.replace("<!-- place post content here -->", comment);

            result = result + postreal;
        }
        return begin + result + end;
    }
}
