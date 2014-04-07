package net.somethingdreadful.MAL;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockDialogFragment;

import net.somethingdreadful.MAL.api.MALApi;

public class RatingPickerDialogFragment extends SherlockDialogFragment implements SlidingRatingBar.IUpdateRatingText {

    View view;

    SlidingRatingBar RatingBar;
    TextView FlavourText;

    int rating;
    MALApi.ListType type;
    String[] ratingsText;


    public RatingPickerDialogFragment() {

    }

    public interface RatingDialogDismissedListener {
        void onRatingDialogDismissed(int rating);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        view = View.inflate(new ContextThemeWrapper(getActivity(), R.style.AlertDialog), R.layout.dialog_rating_picker, null);

        Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialog));

        builder.setPositiveButton(R.string.dialog_label_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                ((DetailView) getActivity()).onRatingDialogDismissed(rating);
                dismiss();
            }
        }
        ).setNegativeButton(R.string.dialog_label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dismiss();
            }
        }
        ).setView(view).setTitle(getResources().getString(R.string.dialog_title_rating));

        return builder.create();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        ratingsText = getResources().getStringArray(R.array.array_ratings_text);

        if (state == null) {
            type = ((DetailView) getActivity()).recordType;

            if (type == MALApi.ListType.ANIME) {
                rating = ((DetailView) getActivity()).animeRecord.getScore();
            } else {
                rating = ((DetailView) getActivity()).mangaRecord.getScore();
            }
        } else {
            type = (MALApi.ListType) state.getSerializable("type");
            rating = state.getInt("rating");

        }

        RatingBar = (SlidingRatingBar) view.findViewById(R.id.dialogMyRatingBar);
        FlavourText = (TextView) view.findViewById(R.id.RatingBarFlavourText);

        RatingBar.setRating(rating / 2);
        FlavourText.setText(ratingsText[rating]);

        RatingBar.setPasser(this);

        RatingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float mRating,
                                        boolean fromUser) {
                rating = (int) (mRating * 2);
                Log.v("MALX", "Rating set to: " + rating);

            }

        });

        return null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {


    }

    @Override
    public void onCancel(DialogInterface dialog) {

        this.dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putInt("rating", rating);
        state.putSerializable("type", type);

        super.onSaveInstanceState(state);
    }

    @Override
    public void updateRatingText(int rating) {
        FlavourText.setText(ratingsText[rating]);
    }


}
