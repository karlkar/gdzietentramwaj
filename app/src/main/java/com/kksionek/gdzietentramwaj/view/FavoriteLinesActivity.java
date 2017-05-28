package com.kksionek.gdzietentramwaj.view;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.kksionek.gdzietentramwaj.R;
import com.kksionek.gdzietentramwaj.DataSource.FavoriteTramData;
import com.kksionek.gdzietentramwaj.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public class FavoriteLinesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_lines);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setNavigationOnClickListener(v -> onBackPressed());

        GridView gridView = (GridView) findViewById(R.id.gridView);

        ArrayList<FavoriteTramData> tramDatas = new ArrayList<>();
        for (SortedMap.Entry<String, Boolean> entry : Model.getInstance().getFavoriteTramData().entrySet()) {
            tramDatas.add(new FavoriteTramData(entry.getKey(), entry.getValue()));
        }
        FavoritesAdapter adapter = new FavoritesAdapter(tramDatas);
        gridView.setAdapter(adapter);
    }

    private class FavoritesAdapter extends ArrayAdapter<FavoriteTramData> {

        public FavoritesAdapter(List<FavoriteTramData> objects) {
            super(FavoriteLinesActivity.this, R.layout.grid_favorite_element, objects);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.grid_favorite_element, parent, false);

                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.tramNum);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();

            final FavoriteTramData tramData = getItem(position);
            holder.textView.setText(tramData.getLine());
            if (tramData.isFavorite())
                convertView.setBackgroundResource(R.color.favoriteLineColor);
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    convertView.setBackground(null);
                else
                    convertView.setBackgroundDrawable(null);
            }

            convertView.setOnClickListener(v -> {
                tramData.setFavorite(!tramData.isFavorite());
                Model.getInstance().getFavoriteManager().setFavorite(tramData.getLine(), tramData.isFavorite());
                notifyDataSetChanged();
            });
            return convertView;
        }

        private class ViewHolder {
            TextView textView;
        }
    }
}
