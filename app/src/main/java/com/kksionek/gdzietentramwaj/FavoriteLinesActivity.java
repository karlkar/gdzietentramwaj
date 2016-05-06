package com.kksionek.gdzietentramwaj;

import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FavoriteLinesActivity extends AppCompatActivity {

    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_lines);

        mGridView = (GridView) findViewById(R.id.gridView);
        FavoritesAdapter adapter = new FavoritesAdapter(Model.getInstance().getFavoriteTramData());
        mGridView.setAdapter(adapter);
    }

    private class FavoritesAdapter extends ArrayAdapter<FavoriteTramData> {

        public FavoritesAdapter(List<FavoriteTramData> objects) {
            super(FavoriteLinesActivity.this, R.layout.grid_favorite_element, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            GridView gridView = (GridView)parent;
            int size = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                size = gridView.getColumnWidth();

            if (convertView == null) {
                convertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.grid_favorite_element, parent, false);
                if (size != 0)
                    convertView.setLayoutParams(new GridView.LayoutParams(size, size));

                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.tramNum);
//                holder.imageView = (ImageView) convertView.findViewById(R.id.tramFav);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();

            final FavoriteTramData tramData = getItem(position);
            holder.textView.setText(tramData.getLine());
            if (tramData.isFavorite())
                convertView.setBackgroundResource(android.R.color.holo_green_light);
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    convertView.setBackground(null);
                else
                    convertView.setBackgroundDrawable(null);
            }

//            holder.imageView.setImageResource(tramData.isFavorite() ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tramData.setFavorite(!tramData.isFavorite());
                    Model.getInstance().getFavoriteManager().setFavorite(tramData.getLine(), tramData.isFavorite());
                    Model.getInstance().getFavoriteManager().markChanged();
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }

        private class ViewHolder {
            TextView textView;
//            ImageView imageView;
        }
    }
}
