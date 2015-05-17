package net.somethingdreadful.MAL.detailView;

import android.content.Intent;
import android.webkit.JavascriptInterface;

public class ReviewsInterface {
    DetailViewReviews reviews;

    ReviewsInterface(DetailViewReviews reviews) {
        this.reviews = reviews;
    }

    /**
     * This method will be triggered when the user clicks on a profile image.
     *
     * @param position The array position of the post
     */
    @JavascriptInterface
    public void viewProfile(String position) {
        String username = reviews.record.get(Integer.parseInt(position)).getUsername();
        Intent profile = new Intent(reviews.activity, net.somethingdreadful.MAL.ProfileActivity.class);
        profile.putExtra("username", username);
        reviews.startActivity(profile);
    }

    /**
     * Go the the previous page.
     */
    @JavascriptInterface
    public void previous() {
        reviews.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reviews.getRecords(reviews.page - 1);
            }
        });
    }

    /**
     * Go to the next page.
     */
    @JavascriptInterface
    public void next() {
        reviews.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reviews.getRecords(reviews.page + 1);
            }
        });
    }

    /**
     * Go to the first page.
     */
    @JavascriptInterface
    public void first() {
        reviews.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reviews.getRecords(1);
            }
        });
    }

    /**
     * Go to the last page.
     */
    @JavascriptInterface
    public void last() {
        reviews.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reviews.getRecords(reviews.page + 1);
            }
        });
    }
}