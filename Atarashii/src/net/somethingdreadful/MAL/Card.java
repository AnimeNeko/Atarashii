package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.somethingdreadful.MAL.adapters.DetailViewRelationsAdapter;


public class Card extends RelativeLayout {
    public boolean center;
    public TextView Header;
    public ImageView Image;
    public CardView Card;
    public RelativeLayout Content;

    onCardClickListener listener;
    private int screenWidth;
    private int minHeight;
    private Float density;
    private LayoutInflater inflater;

    public Card(Context context) {
        this(context, null);
    }

    public Card(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Card(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Get attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Card, 0, 0);
        center = a.getBoolean(R.styleable.Card_header_Title_center, false);
        String TitleText = a.getString(R.styleable.Card_header_Title);
        int TitleColor = a.getResourceId(R.styleable.Card_header_Title_Color, android.R.color.black);
        int HeaderColor = a.getResourceId(R.styleable.Card_header_Color, R.color.bg_light);
        Integer maxWidth = a.getInteger(R.styleable.Card_card_maxWidth, 0);
        minHeight = a.getInteger(R.styleable.Card_card_minHeight, 0);
        Integer divide = a.getInteger(R.styleable.Card_card_divide, 0);
        int content = a.getResourceId(R.styleable.Card_card_content, 0);

        // Setup layout
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.card_layout_base, this);

        // Get the views
        Card = (CardView) findViewById(R.id.BaseCard);
        Content = (RelativeLayout) findViewById(R.id.content);
        Header = (TextView) findViewById(R.id.CardTitle);

        // Apply attributes
        if (divide != 0 || maxWidth != 0)
            setWidth(divide, maxWidth);
        Header.setText(TitleText);
        Header.setTextColor(getResources().getColor(TitleColor));
        setHeaderColor(HeaderColor);
        if (content != 0)
            setContent(content);

        a.recycle();
    }

    /**
     * Add content to the card.
     *
     * @param resource The resource id that you want to appear in the card
     */
    public void setContent(int resource) {
        inflater.inflate(resource, Content);
        if (this.findViewById(R.id.ListView) != null)
            setPadding(0);
    }

    /**
     * Change the content padding of a card.
     *
     * @param left   The padding of the left side in dp
     * @param top    The padding of the top in dp
     * @param right  The padding of the right side in dp
     * @param bottom The padding of the bottom in dp
     */
    public void setPadding(int left, int top, int right, int bottom) {
        if (Content != null)
            Content.setPadding(convert(left), convert(top), convert(right), convert(bottom));
    }

    /**
     * Change the content padding of a card.
     *
     * @param all The padding of all the sides
     */
    public void setPadding(int all) {
        all = convert(all);
        Content.setPadding(all, all, all, all);
    }

    /**
     * Recalculate the required height of a listview and apply it.
     *
     * @param adapter The listadapter
     */
    public void refreshList(DetailViewRelationsAdapter adapter) {
        if (adapter.visable == 0) {
            this.setVisibility(View.GONE);
        } else {
            int Height = ((adapter.visable - adapter.headers.size()) * 56);
            Height = Height + (adapter.headers.size() * 48);
            Height = Height + (adapter.visable - 1);

            if (this.findViewById(R.id.ListView) != null)
                this.findViewById(R.id.ListView).getLayoutParams().height = convert(Height);
        }
    }

    /**
     * Set the card at the right side of another card.
     *
     * @param res    The card at the left side of your desired point
     * @param amount The amount of cards that will be at the left & right sides of your desired point
     *               Note: This also includes this card it self
     * @param screen The minimum amount of dp when the card will be placed at the right side
     *               Note: Use 0 if you don't want any
     */
    public void setRightof(Card res, int amount, int screen) {
        if (convert(screen) <= getScreenWidth()) {
            RelativeLayout.LayoutParams card = new LayoutParams(getWidth(amount, 0), convert(minHeight));
            card.addRule(RelativeLayout.RIGHT_OF, res.getId());
            card.setMargins(convert(4), 0, 0, 0);
            this.setLayoutParams(card);
        }
    }

    /**
     * Change the header color.
     *
     * @param color The resource id of the color
     */
    public void setHeaderColor(int color) {
        GradientDrawable shape = (GradientDrawable) Header.getBackground();
        shape.setColor(getResources().getColor(color));
    }

    /**
     * Create an onClick event for listening for clicks.
     *
     * @param view     The view id that will trigger the interface method
     * @param callback The activity that contains the interface method
     */
    public void setOnClickListener(int view, onCardClickListener callback) {
        listener = callback;
        (this.findViewById(R.id.actionableIcon)).setVisibility(View.VISIBLE);
        Content.findViewById(view).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCardClickListener(v.getId());
            }
        });
    }

    /**
     * Wraps the width of a card
     *
     * @param width  The width of the image in dp
     * @param height The height of the image in dp
     */
    public void wrapImage(int width, int height) {
        setPadding(16);

        Header.getLayoutParams().width = convert(width + 34);
        Card.getLayoutParams().width = convert(width + 34);
        Card.getLayoutParams().height = convert(height + 96);

        if (Image == null)
            Image = (ImageView) findViewById(R.id.Image);

        Image.getLayoutParams().height = convert(height);
        Image.getLayoutParams().width = convert(width);

        if (center)
            Header.setGravity(Gravity.CENTER);
    }

    /**
     * Set the card width.
     *
     * @param amount   The amount of cards besides each other
     * @param maxWidth The maximum width in dp
     */
    public void setWidth(Integer amount, Integer maxWidth) {
        Card.getLayoutParams().width = getWidth(amount, maxWidth);
    }

    /**
     * Get the card width.
     *
     * @param amount   The amount of cards besides each other
     * @param maxWidth The maximum width in dp
     * @return int The width that the card should be
     */
    public int getWidth(Integer amount, Integer maxWidth) {
        if (amount == 0)
            amount = 1;
        int divider = amount - 1;
        divider = convert((divider * 4) + 16);
        int card = (getScreenWidth() - divider) / amount;
        maxWidth = convert(maxWidth);

        if (card > maxWidth && maxWidth != 0)
            return maxWidth;
        else
            return card;
    }

    /**
     * Get the display density.
     *
     * @return Float The display density
     */
    private Float getDensity() {
        if (density == null)
            density = (getResources().getDisplayMetrics().densityDpi / 160f);
        return density;
    }

    /**
     * Convert dp to pixels.
     *
     * @param number The number in dp to convert in pixels
     * @return int The converted dp in pixels
     */
    private int convert(int number) {
        return Math.round(getDensity() * number);
    }

    /**
     * Get the screen width.
     *
     * @return int The screen width in pixels
     */
    @SuppressLint("InlinedApi")
    private int getScreenWidth() {
        if (screenWidth == 0) {
            try {
                screenWidth = convert(getResources().getConfiguration().screenWidthDp);
            } catch (NoSuchFieldError e) {
                screenWidth = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
            }
        }
        return screenWidth;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (Content != null) {
            Content.addView(child, index, params);
        } else {
            super.addView(child, index, params);
        }
    }

    /**
     * The Interface that will get triggered by the OnClick method.
     */
    public interface onCardClickListener {
        public void onCardClickListener(int id);
    }
}