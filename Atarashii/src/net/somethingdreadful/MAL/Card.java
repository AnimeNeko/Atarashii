package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class Card extends RelativeLayout {
    public TextView Header;
    public boolean center;
    public RelativeLayout Card;
    public RelativeLayout Content;
    LayoutInflater inflater;
    boolean click;
    int CardColor;
    int CardColorPressed;
    onCardClickListener listener;
    int screenWidth;
    Float density;

    public Card(Context context, AttributeSet attrs) {
        super(context, attrs);

        /*
         * Get attributes
         */
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Card, 0, 0);
        center = a.getBoolean(R.styleable.Card_header_Title_center, false);
        String TitleText = a.getString(R.styleable.Card_header_Title);
        int TitleColor = a.getResourceId(R.styleable.Card_header_Title_Color, android.R.color.black);
        int HeaderColor = a.getResourceId(R.styleable.Card_header_Color, R.color.card_content);
        Integer maxWidth = a.getInteger(R.styleable.Card_card_maxWidth, 0);
        Integer divide = a.getInteger(R.styleable.Card_card_divide, 0);
        CardColor = a.getResourceId(R.styleable.Card_card_Color, R.color.card_content);
        CardColorPressed = a.getResourceId(R.styleable.Card_card_ColorPressed, R.color.card_content_pressed);

        /*
         * Setup layout
         */
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.card_layout_base, this);

        /*
         * Get the views
         */
        Card = (RelativeLayout) findViewById(R.id.BaseCard);
        Content = (RelativeLayout) findViewById(R.id.content);
        Header = (TextView) this.findViewById(R.id.CardTitle);

        /*
         * Apply attributes
         */
        if (divide != 0 || maxWidth!= 0)
            setWidth(divide, maxWidth);
        Header.setText(TitleText);
        Header.setTextColor(getResources().getColor(TitleColor));
        setHeaderColor(HeaderColor);
        setCardColor(CardColor);

        a.recycle();
    }

    /*
     * Add content to the card
     *
     * Also checks if the view contains a listview to apply the right paddings
     */
    public void setContent(int res) {
        inflater.inflate(res, Content);
        if (this.findViewById(R.id.ListView) != null)
            Content.setPadding(0, 0, 0, Content.getPaddingBottom() / 12 * 6);
    }

    /*
     * Change the padding of a card
     */
    public void setPadding(int left, int top, int right, int bottom) {
            Content.setPadding(0, 0, 0, (bottom != -1 ? bottom : (Content.getPaddingBottom() / 12 * 6)));
    }

    /*
     * Recalculate the required height of a listview and apply it
     */
    public void refreshList(Integer total, Integer normalHeight, Integer headers, Integer headerHeight, Integer divider) {
        if (total == 0) {
            this.setVisibility(View.GONE);
        } else {
            float density = (getResources().getDisplayMetrics().densityDpi / 160f);
            Integer normal = total - headers;

            float Height = normal * normalHeight * density;
            Height = Height + (headers * headerHeight * density);
            Height = Height + ((total - 1) * divider * density);

            if (this.findViewById(R.id.ListView) != null)
                this.findViewById(R.id.ListView).getLayoutParams().height = (int) Height;
        }
    }

    /*
     * Change the background color
     */
    public void setCardColor(int color) {
        LayerDrawable layers = (LayerDrawable) Card.getBackground();
        GradientDrawable shape = (GradientDrawable) (layers.findDrawableByLayerId(R.id.card_content_drawable));
        shape.setColor(getResources().getColor(color));
    }

    /*
     * Change the header color
     */
    public void setHeaderColor(int color) {
        GradientDrawable shape = (GradientDrawable) Header.getBackground();
        shape.setColor(getResources().getColor(color));
    }

    public void setOnClickListener(int res, onCardClickListener callback) {
        listener = callback;
        (this.findViewById(R.id.actionableIcon)).setVisibility(View.VISIBLE);
        Content.findViewById(res).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCardClickListener(v.getId());
            }
        });
    }

    /*
     * Wraps the width of a card
     */
    public void wrapWidth(boolean header) {
        int width;
        Header.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        Card.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        Content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

        if (Content.getMeasuredWidth() >= Header.getMeasuredWidth() || !header)
            width = Content.getMeasuredWidth();
        else
            width = Header.getMeasuredWidth();

        Header.setLayoutParams(new LayoutParams(width, Header.getLayoutParams().height));
        Card.setLayoutParams(new LayoutParams(width, Card.getMeasuredHeight()));
        if (center)
            Header.setGravity(Gravity.CENTER);
    }

    /*
     * Set the card width
     *
     * amount is the amount of cards besides each other
     * maxWidth is the maximum width in dp
     */
    public void setWidth(Integer amount, Integer maxWidth) {
        if (amount == 0)
            amount = 1;
        int divider = amount - 1;
        divider = (divider * 4) + 16;
        divider = (int) (divider * getDensity());
        int card = (getScreenWidth() - divider) / amount;
        maxWidth = (int) (maxWidth * getDensity());

        if (card > maxWidth && maxWidth != 0)
            Card.getLayoutParams().width = maxWidth;
        else
            Card.getLayoutParams().width = card;
    }

    @SuppressLint("InlinedApi")
    public Integer getScreenWidth() {
        if (screenWidth == 0) {
            try {
                screenWidth = (int) (getResources().getConfiguration().screenWidthDp * getDensity());
            } catch (NoSuchFieldError e) {
                screenWidth = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
            }
        }
        return screenWidth;
    }

    /*
     * Get the display density
     *
     */
    public Float getDensity() {
        if (density == null)
            density = (getResources().getDisplayMetrics().densityDpi / 160f);
        return density;
    }

    /*
     * Interface for clicklistener
     */
    public interface onCardClickListener {
        public void onCardClickListener(int id);
    }
}