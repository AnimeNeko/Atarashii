package net.somethingdreadful.MAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class Card extends RelativeLayout implements View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {
    public TextView Header;
    public boolean center;
    public RelativeLayout Card;
    public RelativeLayout Content;
    LayoutInflater inflater;
    boolean click;
    boolean WrapHeight;
    int CardColor;
    int CardColorPressed;
    onCardClickListener listener;

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
        CardColor = a.getResourceId(R.styleable.Card_card_Color, R.color.card_content);
        CardColorPressed = a.getResourceId(R.styleable.Card_card_ColorPressed, R.color.card_content_pressed);
        Boolean TouchEvent = a.getBoolean(R.styleable.Card_card_TouchFeedback, false);
        WrapHeight = a.getBoolean(R.styleable.Card_card_WrapHeight, false);

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
        Header.setText(TitleText);
        Header.setTextColor(getResources().getColor(TitleColor));
        setHeaderColor(HeaderColor);
        setCardColor(CardColor);
        if (TouchEvent) {
            Content.setOnTouchListener(this);
            Header.setOnTouchListener(this);
            Card.setOnTouchListener(this);
        } else {
            (this.findViewById(R.id.actionableIcon)).setVisibility(View.GONE);
        }

        if (WrapHeight)
            Content.getViewTreeObserver().addOnGlobalLayoutListener(this);
        a.recycle();
    }

    /*
     * Wrap height if it is enabled in the layout
     */
    @Override
    public void onGlobalLayout() {
        wrapHeight();
    }

    /*
     * Setup clicklistener
     */
    public void setCardClickListener(onCardClickListener listener) {
        this.listener = listener;
    }

    /*
     * Add content to the card
     */
    public void setContent(int res) {
        inflater.inflate(res, Content);
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

    /*
     * Handle the touch feedback
     */
    @SuppressLint("ResourceAsColor")
    @Override
    public boolean onTouch(View v, MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                click = true;

                setCardColor(CardColorPressed);
                break;
            case MotionEvent.ACTION_UP:
                if (click) {
                    listener.onCardClickListener(this.getId());
                    click = false;
                }
                setCardColor(CardColor);
                break;
        }
        return true;
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
     * Wraps the height of a card
     */
    public void wrapHeight() {
        Card.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        Card.setLayoutParams(new LayoutParams(Card.getWidth(), Card.getMeasuredHeight()));
    }

    /*
     * Interface for clicklistener
     */
    public interface onCardClickListener {
        public void onCardClickListener(int id);
    }
}