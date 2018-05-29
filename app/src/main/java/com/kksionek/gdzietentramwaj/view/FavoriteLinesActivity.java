package com.kksionek.gdzietentramwaj.view;

import android.arch.lifecycle.ViewModelProviders;
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

import com.kksionek.gdzietentramwaj.DataSource.Room.FavoriteTram;
import com.kksionek.gdzietentramwaj.R;
import com.kksionek.gdzietentramwaj.ViewModel.FavoriteLinesActivityViewModel;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class FavoriteLinesActivity extends AppCompatActivity {

    private FavoriteLinesActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_lines);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        myToolbar.setNavigationOnClickListener(v -> onBackPressed());

        GridView gridView = findViewById(R.id.gridView);

        mViewModel = ViewModelProviders.of(this)
                .get(FavoriteLinesActivityViewModel.class);

        FavoritesAdapter adapter = new FavoritesAdapter();
        gridView.setAdapter(adapter);
        mViewModel.getFavoriteTrams().observe(this, adapter::setData);
    }

    private class FavoritesAdapter extends ArrayAdapter<FavoriteTram> {

        FavoritesAdapter() {
            super(FavoriteLinesActivity.this, R.layout.grid_favorite_element);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(FavoriteLinesActivity.this).inflate(
                        R.layout.grid_favorite_element,
                        parent,
                        false);

                holder = new ViewHolder();
                holder.textView = convertView.findViewById(R.id.tramNum);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final FavoriteTram tramData = getItem(position);
            holder.textView.setText(tramData.getLineId());
            if (tramData.isFavorite()) {
                convertView.setBackgroundResource(R.color.favoriteLineColor);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    convertView.setBackground(null);
                } else {
                    convertView.setBackgroundDrawable(null);
                }
            }

            convertView.setOnClickListener(
                    v -> Observable.fromCallable(() -> {
                        mViewModel.setTramFavorite(tramData.getLineId(), !tramData.isFavorite());
                        return 1;
                    }).subscribeOn(Schedulers.io())
                    .subscribe());
            return convertView;
        }

        public void setData(List<FavoriteTram> favoriteTrams) {
            clear();
            Collections.sort(favoriteTrams, new NaturalOrderComparator<>());
            addAll(favoriteTrams);
        }

        private class ViewHolder {
            TextView textView;
        }
    }
}
