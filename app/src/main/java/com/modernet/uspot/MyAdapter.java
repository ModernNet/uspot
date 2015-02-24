package com.modernet.uspot;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements View.OnClickListener {
    private static final String TAG = "MyAdapter";
    public static Activity activity;
    private ArrayList<InterestPoint> mDataset;
    private int expandedPosition = -1;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<InterestPoint> myDataset, Activity myActivity) {
        mDataset = myDataset;
        activity = myActivity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        ViewHolder holder = new ViewHolder(v);
        holder.itemView.setOnClickListener(MyAdapter.this);
        holder.itemView.setTag(holder);
        return holder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.title.setText(mDataset.get(position).name);
        double dist = mDataset.get(position).distance;
        if(dist>=0)
            holder.distance.setText(dispDist(dist));
        else
            holder.distance.setVisibility(View.INVISIBLE);

        holder.description.setText(mDataset.get(position).description);
        if(!mDataset.get(position).adapted)
            holder.mobility.setVisibility(View.INVISIBLE);

        if (position == expandedPosition) {
            if (holder.isViewExpanded)
                holder.collapse(holder.itemView);
            else
                holder.expand(holder.itemView);
        }
        else if (holder.isViewExpanded) {
            holder.collapse(holder.itemView);
        }

        holder.map_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, MapsActivity.class);
                double lat = mDataset.get(position).lat;
                double lon = mDataset.get(position).lon;
                String name = mDataset.get(position).name;
                intent.putExtra("lat",lat);
                intent.putExtra("lon",lon);
                intent.putExtra("name",name);
                activity.startActivity(intent);
            }
        });
    }

    private String dispDist(double dist) {
        if (dist>=600) {
            dist = dist / 100;
            dist = Math.round(dist);
            dist = dist/10;
            return String.valueOf(dist) + " km";
        } else  {
            dist = Math.round(dist);
            return String.valueOf( (int) dist)+" m";
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onClick(View v) {
        ViewHolder holder = (ViewHolder) v.getTag();

        // Check for an expanded view, collapse if you find one
        if (expandedPosition >= 0) {
            int prev = expandedPosition;
            notifyItemChanged(prev);
        }
        // Set the current position to "expanded"
        expandedPosition = holder.getPosition();
        notifyItemChanged(expandedPosition);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView distance;
        public TextView time;
        public LinearLayout expand_area;
        public TextView description;
        // each data item is just a string in this case
        private int originalHeight = 0;
        private boolean isViewExpanded = false;
        private ImageView map_icon;
        private ImageView mobility;
        private RelativeLayout.LayoutParams layoutParams;


        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            distance = (TextView) v.findViewById(R.id.distance);
            description = (TextView) v.findViewById(R.id.description);
            expand_area = (LinearLayout) v.findViewById(R.id.expand_area);
            map_icon = (ImageView) v.findViewById(R.id.map_icon);
            mobility = (ImageView) v.findViewById(R.id.mobility);
            layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            if (!isViewExpanded) {
                // Set Views to View.GONE and .setEnabled(false)
                expand_area.setVisibility(View.GONE);
                expand_area.setEnabled(false);
            }
        }

        public void expand(View v) {
            if (originalHeight == 0) originalHeight = v.getHeight();
            ValueAnimator valueAnimator;

            layoutParams.setMargins(0,0,0,0);
            title.setLayoutParams(layoutParams);

            expand_area.setVisibility(View.VISIBLE);
            expand_area.setEnabled(true);
            isViewExpanded = true;

            final int widthSpec = View.MeasureSpec
                    .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            final int heightSpec = View.MeasureSpec
                    .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            expand_area.measure(widthSpec, heightSpec);

            valueAnimator = ValueAnimator.ofInt(
                    originalHeight,
                    originalHeight + expand_area.getMeasuredHeight()
            ); // These values in this method can be changed to expand however much you like
            startAnimation(v,valueAnimator);
        }

        public void collapse(View v) {
            ValueAnimator valueAnimator;

            layoutParams.setMargins(0,0,0,23);
            title.setLayoutParams(layoutParams);

            valueAnimator = ValueAnimator.ofInt(originalHeight + expand_area.getMeasuredHeight(), originalHeight);

            Animation a = new AlphaAnimation(1.00f, 0.00f); // Fade out
            a.setDuration(100);
            // Set a listener to the animation and configure onAnimationEnd
            a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    expand_area.setVisibility(View.GONE);
                    expand_area.setEnabled(false);
                    isViewExpanded = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            // Set the animation on the custom view
            expand_area.startAnimation(a);

            startAnimation(v,valueAnimator);
        }

        public void startAnimation(final View v, ValueAnimator valueAnimator) {
            valueAnimator.setDuration(200);
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer value = (Integer) animation.getAnimatedValue();
                    v.getLayoutParams().height = value;
                    v.requestLayout();
                }
            });

            valueAnimator.start();
        }
    }
}